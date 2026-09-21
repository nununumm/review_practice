package com.example.shop.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * 注文API（取得・作成）。
 *
 * こちらも try-catch ゼロ。
 * ・存在しない注文 → ResourceNotFoundException → 404
 * ・数量が不正       → InvalidRequestException  → 400
 * ・予期せぬ障害     → その他の例外              → 500
 * いずれも GlobalExceptionHandler が種類ごとに正しいステータスへ振り分ける。
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 注文を1件返す。無ければ例外に任せる（＝404はハンドラが担当）。
     */
    @GetMapping("/{id}")
    public Order getOrder(@PathVariable Long id) {
        return orderService.findById(id);
    }

    /**
     * 注文を新規作成する。
     *
     * @Valid           = CreateOrderRequest に付けた @NotNull / @Min を"入口"で自動チェック。
     *                    違反があれば Spring が例外を投げ、ハンドラが 400 に整える。
     * @RequestBody     = リクエストのJSONを CreateOrderRequest に自動変換する。
     * @ResponseStatus(CREATED) = 「新しく作った」ので 201 Created を返す（200ではなく）。
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(request.getProductId(), request.getQuantity());
    }
}
