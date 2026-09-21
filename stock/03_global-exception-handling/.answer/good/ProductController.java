package com.example.shop.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品取得API。
 *
 * 注目：try-catch が1つも無い！
 * ・「商品が無ければ Service が ResourceNotFoundException を投げる」→
 *   その例外は GlobalExceptionHandler が受け取り、404＋統一JSONに変換してくれる。
 * ・Controller は「本来やりたいこと（商品を返す）」だけに集中できる = 読みやすく、テストしやすい。
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    // コンストラクタ経由で Service を受け取る（DI＝依存の注入）。テスト時に差し替えやすい
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 商品を1件返す。見つからない場合の分岐は書かない（例外に任せる）。
     * 正常時は Spring が Product を JSON に変換し、200 OK で返す。
     */
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.findById(id);
    }
}
