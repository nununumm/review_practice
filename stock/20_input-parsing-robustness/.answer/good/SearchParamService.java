import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.Map;
import java.util.Set;

@Service
public class SearchParamService {

    // ─── 既定値と上限・下限（すべて名前付き定数にして「なぜこの値か」を明確にする）───

    // ページ番号の既定値（未指定なら先頭ページ = 0）
    private static final int DEFAULT_PAGE = 0;
    // ページ番号の下限。負のページはあり得ないので 0 未満は 0 に丸める（clamp）
    private static final int MIN_PAGE = 0;

    // 1ページ件数の既定値（未指定なら 20 件）
    private static final int DEFAULT_SIZE = 20;
    // 1ページ件数の下限（0件や負数は無意味なので 1 に丸める）
    private static final int MIN_SIZE = 1;
    // 1ページ件数の上限。ここを設けないと size=1000000 などで大量取得され、
    // メモリ圧迫やサービス停止（＝DoS：故意に高負荷をかけて止める攻撃）の入口になる
    private static final int MAX_SIZE = 100;

    // 最低価格の既定値（未指定なら 0 = 絞り込みなし）
    private static final int DEFAULT_MIN_PRICE = 0;
    // 最低価格の下限（マイナス価格はあり得ないので 0 に丸める）
    private static final int MIN_MIN_PRICE = 0;

    // 並び順の「許可リスト（ホワイトリスト）」。
    // 画面から来た値をそのまま SQL に入れず、ここに載っている値だけを受け付ける。
    // キー = 画面から来る値、値 = 実際に ORDER BY に使う安全な句。
    private static final Map<String, String> ALLOWED_SORTS = Map.of(
            "price", "price ASC",       // 価格の安い順
            "price_desc", "price DESC", // 価格の高い順
            "new", "created_at DESC"    // 新着順
    );
    // sort 未指定・不正値のときの既定の並び順
    private static final String DEFAULT_ORDER_BY = "created_at DESC";

    // キーワードの最大長（長すぎる入力を弾いて負荷やSQLの肥大化を防ぐ）
    private static final int MAX_KEYWORD_LENGTH = 100;

    /**
     * 画面から来たクエリパラメータ（すべて文字列）を検証・正規化して、
     * 検索条件オブジェクトを組み立てて返す。
     * 数値変換に失敗した場合は「なぜ弾いたか」が分かる 400 エラーに翻訳する。
     */
    public SearchCondition build(Map<String, String> params) {

        SearchCondition condition = new SearchCondition();

        // page：未指定なら既定値、数値化して 0 未満は 0 に丸める（clamp = 範囲に収める）
        int page = parseInt(params.get("page"), "page", DEFAULT_PAGE);
        condition.setPage(Math.max(MIN_PAGE, page));

        // size：未指定なら既定値、下限・上限の両方で丸める（大量取得を物理的に不可能にする）
        int size = parseInt(params.get("size"), "size", DEFAULT_SIZE);
        condition.setSize(clamp(size, MIN_SIZE, MAX_SIZE));

        // minPrice：未指定なら 0、負数は 0 に丸める
        int minPrice = parseInt(params.get("minPrice"), "minPrice", DEFAULT_MIN_PRICE);
        condition.setMinPrice(Math.max(MIN_MIN_PRICE, minPrice));

        // keyword：null / 前後空白 / 空文字 を正規化する
        condition.setKeyword(normalizeKeyword(params.get("keyword")));

        // sort：許可リストに載っている値だけを、対応する安全な句に変換して使う
        condition.setOrderByClause(resolveOrderBy(params.get("sort")));

        return condition;
    }

    /**
     * 文字列を int に変換する共通処理。
     * ・null / 空文字 → 既定値を返す（未指定は「異常」ではなく「省略」）
     * ・全角数字（"１２３"）→ 半角に正規化してから変換（画面の入力ゆれを吸収）
     * ・数字でない / 桁あふれ → 意味のある 400 エラーに翻訳する
     *   （そのまま NumberFactException を投げると 500 になり、利用者は原因が分からない。
     *    黙って 0 に倒すのも「なぜ結果が変か」が分からず、もっと悪い）
     */
    private int parseInt(String raw, String fieldName, int defaultValue) {
        // 未指定（null）や空白だけなら既定値を使う
        if (raw == null || raw.trim().isEmpty()) {
            return defaultValue;
        }
        // 全角→半角などの表記ゆれを正規化し、前後の空白を除去する
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFKC).trim();
        try {
            // Integer.parseInt は「桁あふれ（int の範囲を超える大きな数）」も
            // NumberFormatException として弾いてくれるので、まとめて catch できる
            return Integer.parseInt(normalized);
        } catch (NumberFormatException e) {
            // 500（サーバーエラー）ではなく 400（リクエストが不正）に翻訳し、
            // どの項目がおかしいのかをメッセージで伝える
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " は数値で指定してください: " + raw);
        }
    }

    /** value を [min, max] の範囲に収める（範囲外なら端の値に丸める） */
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * キーワードを正規化する。
     * ・null / 空白だけ → null（＝絞り込みなし）に統一
     * ・前後の空白は除去
     * ・長すぎる入力は 400 で弾く
     */
    private String normalizeKeyword(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > MAX_KEYWORD_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "keyword が長すぎます（最大 " + MAX_KEYWORD_LENGTH + " 文字）");
        }
        return trimmed;
    }

    /**
     * sort パラメータを、許可リストに載っている安全な ORDER BY 句に変換する。
     * リストに無い値（や null）は既定の並び順にフォールバックする。
     * こうすることで、画面から来た文字列が SQL に直接流れ込むのを防ぐ
     * （＝SQLインジェクション：不正なSQLを紛れ込ませる攻撃 の入口を塞ぐ）。
     */
    private String resolveOrderBy(String rawSort) {
        if (rawSort == null) {
            return DEFAULT_ORDER_BY;
        }
        // getOrDefault：キーがあればその安全な句、無ければ既定値
        return ALLOWED_SORTS.getOrDefault(rawSort.trim(), DEFAULT_ORDER_BY);
    }

    public static class SearchCondition {
        private int page;
        private int size;
        private int minPrice;
        private String keyword;
        private String orderByClause;

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public int getMinPrice() { return minPrice; }
        public void setMinPrice(int minPrice) { this.minPrice = minPrice; }
        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }
        public String getOrderByClause() { return orderByClause; }
        public void setOrderByClause(String orderByClause) { this.orderByClause = orderByClause; }

        // 検索条件を Set にまとめて渡したい場合の例（許可された並び順の一覧など）
        public Set<String> allowedSortKeys() { return ALLOWED_SORTS.keySet(); }
    }
}
