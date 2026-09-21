package bad;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

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
     * この結果を使ってマスタと照合したり、Mapのキーにしたりする。
     */
    public String normalize(String code) {
        return code.toUpperCase();
    }

    /**
     * 正規化した商品コードでマスタを引き、表示名を返す。
     */
    public String findName(String code) {
        String key = normalize(code);
        return productNameByCode.get(key);
    }

    /**
     * 金額を小数2桁の文字列に整形する。CSVなど外部連携ファイルへの出力に使う。
     */
    public String formatPrice(double price) {
        return String.format("%.2f", price);
    }
}
