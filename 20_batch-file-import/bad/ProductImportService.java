import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 商品マスタのCSVファイルを取り込むサービス。
 *
 * 運用担当者がアップロードしたCSV（1行1商品：商品コード,商品名,価格,在庫数）を
 * 読み込み、商品テーブルに一括登録する。
 * 1日1回、深夜バッチから importProducts(filePath) が呼ばれる想定。
 */
@Service
@RequiredArgsConstructor
public class ProductImportService {

    private final ProductRepository productRepository;

    public String importProducts(String filePath) {
        // ファイルを1行ずつ読み込んで、いったん全行をメモリに貯める
        List<String> lines = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

            // 1行ずつ商品として登録する
            int count = 0;
            for (String row : lines) {
                String[] cols = row.split(",");
                Product product = new Product();
                product.setCode(cols[0]);
                product.setName(cols[1]);
                product.setPrice(Integer.parseInt(cols[2]));
                product.setStock(Integer.parseInt(cols[3]));
                productRepository.save(product);
                count++;
            }

            return count + "件の商品を登録しました";
        } catch (Exception e) {
            e.printStackTrace();
            return "取り込みに失敗しました";
        }
    }
}
