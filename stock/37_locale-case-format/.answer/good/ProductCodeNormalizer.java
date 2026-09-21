package good;

import java.util.HashMap;
import java.util.Map;
import java.util.Locale; // ロケール（＝言語や国ごとの表記ルールの設定）を扱うクラス

import org.springframework.stereotype.Component;

/**
 * 【このクラスの設計方針】
 *
 * bad版の問題:
 *   toUpperCase() / String.format() を「引数なし」で呼ぶと、
 *   実行環境の「既定ロケール（＝そのサーバーに設定された言語・国のルール）」が
 *   暗黙に使われてしまう。つまり、同じコードでもサーバーの言語設定が変わると
 *   結果が変わる。国内サーバーでは動いていたのに、海外拠点のサーバーに置いた途端
 *   商品コードの照合が壊れる、といった「再現しにくいバグ」の温床になる。
 *
 * good版の考え方:
 *   「用途」でロケールを使い分ける。
 *   ・システム内部の識別子（商品コードの照合／Mapのキーなど機械が扱う文字）や
 *     機械可読フォーマット（CSVなど）は、環境に左右されないよう
 *     ロケールを Locale.ROOT（＝どの言語にも寄らない中立の設定）に「固定」する。
 *   ・人間に見せる表示は、そのユーザーのロケールを「明示的に」渡す。
 */
@Component
public class ProductCodeNormalizer {

    private static final String PREFIX = "PRODUCT-";

    // 商品コードごとの表示名を引くためのマスタ（キーは大文字化した商品コード）
    private final Map<String, String> productNameByCode = new HashMap<>();

    public ProductCodeNormalizer() {
        productNameByCode.put(PREFIX + "ITEM001", "ノートPC");
        productNameByCode.put(PREFIX + "ITEM002", "マウス");
    }

    /**
     * ユーザーが入力した商品コードを正規化（大文字にそろえる）して返す。
     * これは「機械が照合するための識別子」なので、環境に左右されてはいけない。
     * そのため Locale.ROOT を明示して、どのサーバーでも同じ結果になるよう固定する。
     *
     * 補足: トルコ語ロケール(tr)では、小文字 "i" の大文字が英語の "I" ではなく
     *       点付きの "İ" になる。引数なしの toUpperCase() だと、トルコ語設定の
     *       サーバー上で "item" が "İTEM" になり、照合が突然壊れる。
     *       Locale.ROOT（または Locale.ENGLISH）に固定すればこの罠を避けられる。
     */
    public String normalize(String code) {
        // Locale.ROOT = 言語に依存しない中立ロケール。識別子の大文字化に最適。
        return code.toUpperCase(Locale.ROOT);
    }

    /**
     * 正規化した商品コードでマスタを引き、表示名を返す。
     * normalize() が環境に依存しなくなったので、この照合もどの環境でも安定する。
     */
    public String findName(String code) {
        String key = normalize(code);
        return productNameByCode.get(key);
    }

    /**
     * 金額を小数2桁の文字列に整形する。CSVなど「機械が読む」外部連携ファイル用。
     * String.format も引数なしだと既定ロケール依存になり、ドイツ・フランス等では
     * 小数点がカンマになって "1234,56" のように出力される → CSVの列がずれる、
     * 受け側の数値パースが失敗する。機械可読フォーマットなので Locale.ROOT に固定する。
     *
     * 補足: 金額は本来 double ではなく BigDecimal（＝誤差の出ない正確な数値）で
     *       扱うのが望ましい。ただし今回の主役は「ロケールの固定」なので、
     *       整形時のロケール指定に絞って直している。
     */
    public String formatPrice(double price) {
        // 第1引数に Locale.ROOT を渡し、小数点を常に "." にそろえる。
        return String.format(Locale.ROOT, "%.2f", price);
    }

    /**
     * 参考: 人間の画面に見せる表示用の整形なら、その利用者のロケールを明示的に渡す。
     * 例えばドイツ語ユーザーには小数点をカンマで見せたい、というのは「表示」では正しい。
     * つまり「内部処理＝固定ロケール」「表示＝ユーザーのロケール」と使い分けるのがコツ。
     */
    public String formatPriceForDisplay(double price, Locale userLocale) {
        // 表示用は、そのユーザーの言語・国のルールに合わせて整形する。
        return String.format(userLocale, "%.2f", price);
    }
}
