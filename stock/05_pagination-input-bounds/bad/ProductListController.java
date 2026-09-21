import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ProductListController {

    @Autowired
    private ProductListService productListService;

    @GetMapping("/products")
    public Map<String, Object> list(@RequestParam int page, @RequestParam int size) {

        // 指定ページの商品リストを取得する
        List<Product> products = productListService.getProducts(page, size);

        // 総件数を取得する
        int total = productListService.countProducts();

        // 商品リストと総件数をまとめて返す
        Map<String, Object> response = new HashMap<>();
        response.put("page", page);
        response.put("size", size);
        response.put("total", total);
        response.put("products", products);
        return response;
    }
}
