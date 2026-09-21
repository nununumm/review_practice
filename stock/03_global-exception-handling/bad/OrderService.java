package com.example.shop.api;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final Map<Long, Order> store = new HashMap<>();
    private final ProductService productService;

    public OrderService(ProductService productService) {
        this.productService = productService;
        store.put(100L, new Order(100L, 1L, 2, new BigDecimal("2400")));
    }

    public Order findById(Long id) {
        Order o = store.get(id);
        if (o == null) {
            throw new EntityNotFoundException("order not found. id=" + id);
        }
        return o;
    }

    public Order create(Long productId, int quantity) {
        if (quantity <= 0) {
            // 入力不正は IllegalArgumentException を投げる
            throw new IllegalArgumentException("quantity must be positive. quantity=" + quantity);
        }
        Product product = productService.findById(productId);
        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        long newId = System.currentTimeMillis();
        Order order = new Order(newId, productId, quantity, total);
        store.put(newId, order);
        return order;
    }
}
