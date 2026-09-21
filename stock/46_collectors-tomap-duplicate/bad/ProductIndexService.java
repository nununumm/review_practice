package bad;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductIndexService {

    public Map<String, Product> indexByCode(List<Product> products) {
        return products.stream()
                .collect(Collectors.toMap(Product::getCode, p -> p));
    }

    public Product findByCode(List<Product> products, String code) {
        Map<String, Product> index = indexByCode(products);
        return index.get(code.toUpperCase());
    }
}
