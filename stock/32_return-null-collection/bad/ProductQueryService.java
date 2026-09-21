package bad;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductQueryService {

    private final ProductRepository repository;

    public ProductQueryService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> findByCategory(String category) {
        List<Product> products = repository.selectByCategory(category);
        if (products.isEmpty()) {
            return null;
        }
        return products;
    }

    public Product findByCode(String code) {
        Product product = repository.selectByCode(code);
        if (product == null) {
            return null;
        }
        return product;
    }
}
