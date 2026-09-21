package com.example.shop.api;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * 注文を扱うサービス。
 * 状況ごとに"意味の違う例外"を投げ分ける（見つからない=404系 / 入力不正=400系）。
 */
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
            throw new ResourceNotFoundException("order not found. id=" + id);
        }
        return o;
    }

    public Order create(Long productId, int quantity) {
        // ※基本的な形式チェックは Controller の @Valid が入口で弾く。
        //   ここは"業務ルール"としての防御。数量は必ず正であること。
        if (quantity <= 0) {
            throw new InvalidRequestException("数量は1以上を指定してください。");
        }
        // 商品が無ければ productService が ResourceNotFoundException を投げる（→ 404）
        Product product = productService.findById(productId);
        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        long newId = System.currentTimeMillis();
        Order order = new Order(newId, productId, quantity, total);
        store.put(newId, order);
        return order;
    }
}
