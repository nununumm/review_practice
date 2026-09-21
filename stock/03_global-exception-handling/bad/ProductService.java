package com.example.shop.api;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final Map<Long, Product> store = new HashMap<>();

    public ProductService() {
        store.put(1L, new Product(1L, "コーヒー豆 200g", new BigDecimal("1200")));
        store.put(2L, new Product(2L, "ドリッパー", new BigDecimal("2500")));
    }

    public Product findById(Long id) {
        Product p = store.get(id);
        if (p == null) {
            // 存在しない商品は独自例外を投げる
            throw new EntityNotFoundException("product not found. id=" + id);
        }
        return p;
    }
}
