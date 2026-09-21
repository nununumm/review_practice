package good;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 検索条件パラメータを解釈して SearchCondition を組み立てるクラス。
 *
 * bad 版は「数字かどうか」「一覧に有るかどうか」といった “想定内” の分岐を、
 * try-catch（例外）で表現していた（＝例外を通常の制御フローに使うアンチパターン）。
 * good 版では、想定内のことは if / 事前チェック / Optional で普通に判定し、
 * 例外は「本当に異常なとき」だけに使う。
 */
@Component
public class SearchParamParser {

    // マジックナンバーに名前を付けて意味をはっきりさせる
    private static final int DEFAULT_PAGE = 1;    // パラメータが無効なときの既定ページ
    private static final int NO_PRICE_LIMIT = -1; // 「価格上限なし」を表す番兵（0だと「0円以下」と紛らわしいため）

    public SearchCondition parse(String pageParam, String maxPriceParam,
                                 String categoryCode, List<Category> categories) {

        SearchCondition condition = new SearchCondition();

        // ページ番号：まず「数字として解釈できるか」を静かに判定してから値を決める。
        // 想定内の入力（数字でない）で毎回例外を投げないので、速くて意図も読みやすい。
        condition.setPage(parseIntOrDefault(pageParam, DEFAULT_PAGE));

        // 価格上限：無効なら「上限なし」を意味する番兵を入れる（0という曖昧な値に逃がさない）
        condition.setMaxPrice(parseIntOrDefault(maxPriceParam, NO_PRICE_LIMIT));

        // 区分コードの検索：ループを例外で打ち切らず、stream で「条件に合う最初の1件」を素直に探す。
        // 見つからなければ Optional が空になるだけ（＝「無い」を例外ではなく型で表す）。
        Category matched = findCategory(categories, categoryCode).orElse(null);
        condition.setCategory(matched);

        return condition;
    }

    /**
     * 文字列を int に変換する。数字でなければ既定値を返す。
     * 「数字かどうか」は想定内の分岐なので、例外ではなく事前チェックで判定する。
     */
    private int parseIntOrDefault(String value, int defaultValue) {
        // null や空文字、符号付きの整数以外は、そもそも変換を試みない（＝事前チェック）。
        // \\d+ は「数字が1個以上」、先頭の -? は任意のマイナス符号を許すという意味の正規表現。
        if (value == null || !value.matches("-?\\d+")) {
            return defaultValue;
        }
        return Integer.parseInt(value); // ここまで来れば必ず変換できるので例外は起きない
    }

    /**
     * カテゴリ一覧から、コードが一致する最初の1件を探す。
     * 「見つからない」ことは異常ではなく想定内なので、例外ではなく Optional で返す
     * （Optional＝「有るかもしれないし、無いかもしれない」を型ではっきり表す入れ物）。
     */
    private Optional<Category> findCategory(List<Category> categories, String categoryCode) {
        if (categories == null || categoryCode == null) {
            return Optional.empty(); // 探しようがないので「無し」を返す（黙って落とさない）
        }
        return categories.stream()                              // 一覧を1件ずつ流して
                .filter(c -> categoryCode.equals(c.getCode()))  // コードが一致するものだけ残し
                .findFirst();                                   // 最初の1件を Optional で受け取る
    }
}
