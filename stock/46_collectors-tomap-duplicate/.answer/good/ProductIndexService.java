package good;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商品コードで引ける索引を作るサービス（修正版）。
 *
 * bad 版には「本番データで初めて落ちる」タイプの罠が複数あった。
 *  ・Collectors.toMap は同じキーが2回出てくると IllegalStateException で落ちる
 *    （＝重複時の扱いを指定していないと、重複コードのある取込データで突然エラー）。
 *  ・indexByCode はコードをそのままキーにしているのに、findByCode は大文字化して引く
 *    ＝キーが一致せず「登録したのに見つからない」バグ。
 *  ・検索のたびに全件を Map化していて非効率。
 *
 * good 版では、キーの正規化（大文字化）を「作るとき」と「引くとき」で必ず揃え、
 * 重複キーの扱いを明示し、索引は1度だけ作って使い回す。
 */
public class ProductIndexService {

    // 索引は1度だけ作って保持する（検索のたびに作り直さない）
    private final Map<String, Product> index;

    public ProductIndexService(List<Product> products) {
        Objects.requireNonNull(products, "products は null にできません");
        this.index = products.stream()
                .collect(Collectors.toMap(
                        // キーは normalize で正規化。引くときも同じ normalize を通すので必ず一致する。
                        p -> normalize(p.getCode()),
                        Function.identity(),
                        // ★重複キーが来たときの扱いを明示：ここでは「後勝ち（後から来た方で上書き）」。
                        //   これを書かないと重複時に IllegalStateException で落ちる。
                        (existing, replacement) -> replacement));
    }

    public Product findByCode(String code) {
        // 引くときも同じ normalize を通す。作成時とキーの作り方が揃うので「登録したのに見つからない」を防ぐ。
        return index.get(normalize(code));
    }

    // コードの正規化を1か所に集約。null も弾き、Locale を固定して環境非依存の大文字化にする
    // （Locale を指定しないとトルコ語環境などで i の大文字化が変わり、キーがずれる有名な罠がある）。
    private static String normalize(String code) {
        Objects.requireNonNull(code, "商品コードは null にできません");
        return code.toUpperCase(Locale.ROOT);
    }
}
