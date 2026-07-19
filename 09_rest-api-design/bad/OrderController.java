package com.example.shop.controller;

import com.example.shop.entity.Order;
import com.example.shop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    // 注文一覧を取得する
    @RequestMapping("/getAllOrders")
    public List<Order> getAllOrders() {
        return orderService.findAll();
    }

    // 注文を1件取得する
    @RequestMapping("/getOrder")
    public Order getOrder(@RequestParam Long id) {
        Order order = orderService.findById(id);
        if (order == null) {
            return new Order(); // 見つからなければ空の注文を返す
        }
        return order;
    }

    // 注文を作成する
    @RequestMapping("/createOrder")
    public String createOrder(@RequestParam Long userId,
                              @RequestParam Long productId,
                              @RequestParam int quantity) {
        try {
            orderService.create(userId, productId, quantity);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    // 注文を削除する
    @RequestMapping("/deleteOrder")
    public String deleteOrder(@RequestParam Long id) {
        orderService.delete(id);
        return "deleted";
    }
}
