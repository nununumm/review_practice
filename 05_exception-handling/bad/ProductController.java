package com.example.shop.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    // 商品IDを指定して1件取得するAPI
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        try {
            Product product = productRepository.findById(id).get();
            return product;
        } catch (Exception e) {
            System.out.println("エラーが発生しました: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // 商品を購入するAPI（在庫を1つ減らす）
    @PostMapping("/{id}/purchase")
    public String purchase(@PathVariable Long id, @RequestParam int quantity) {
        Product product = productRepository.findById(id).get();
        if (product.getStock() < quantity) {
            throw new RuntimeException("在庫が足りません。現在の在庫数: " + product.getStock());
        }
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        return "購入が完了しました";
    }
}
