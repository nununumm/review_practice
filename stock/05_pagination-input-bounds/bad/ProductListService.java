import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductListService {

    @Autowired
    private ProductRepository productRepository;

    // page 番目のページを size 件ずつ取り出して返す
    public List<Product> getProducts(int page, int size) {

        // 全商品をDBから取ってくる
        List<Product> all = productRepository.findAll();

        // 取り出し開始位置を計算する
        int offset = page * size;

        // 開始位置から size 件だけ切り出して返す
        int toIndex = offset + size;
        if (toIndex > all.size()) {
            toIndex = all.size();
        }
        return all.subList(offset, toIndex);
    }

    // 商品の総件数を返す
    public int countProducts() {
        // 全商品を取ってきて、その件数を数える
        return productRepository.findAll().size();
    }
}
