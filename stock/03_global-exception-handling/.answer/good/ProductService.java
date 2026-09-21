package com.example.shop.api;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * 商品を扱うサービス。
 * 見つからない場合は"意味のある独自例外"を投げる（RuntimeException を素で投げない）。
 */
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
            // 「見つからない」専用の例外を投げる → ハンドラが 404 に変換
            throw new ResourceNotFoundException("product not found. id=" + id);
        }
        return p;
    }
}
