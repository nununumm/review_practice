package com.example.shop.product;

import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商品APIの受付窓口（Controller）。
 *
 * Before との違い：
 * ・try-catch も return null も一切無い。エラーは "投げるだけ" で、裁くのは GlobalExceptionHandler。
 * ・業務ロジックは ProductService に任せ、ここは「受けて・呼んで・返す」に徹する。
 */
@RestController
@RequestMapping("/api/products")
@Validated  // @RequestParam などのバリデーションを有効にする
public class ProductController {

    private final ProductService productService;

    // コンストラクタインジェクション（フィールド @Autowired は使わない）
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 商品を1件取得する。
     * 見つからない場合は Service が例外を投げ → 自動で 404 が返る。
     */
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        // try-catch しない。正常系だけを素直に書く。
        return productService.getProduct(id);
    }

    /**
     * 商品を購入する。
     * @Min(1)：quantity が0以下ならリクエスト時点で弾く（→ 400 になる）。
     */
    @PostMapping("/{id}/purchase")
    public ResponseEntity<Void> purchase(
            @PathVariable Long id,
            @RequestParam @Min(1) int quantity) {

        productService.purchase(id, quantity);

        // 成功時は 200 と共に本文なしを返す（何をしたかは status で伝える）。
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
