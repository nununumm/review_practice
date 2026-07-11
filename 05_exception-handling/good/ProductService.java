package com.example.shop.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品に関する "業務ロジック" を担当するサービス層。
 *
 * なぜ Controller から分けた？
 * ・Controller は「HTTPの受付窓口」に徹し、実際の処理はここに置く（責務の分離）。
 * ・こうすると業務ロジックだけを単体テストしやすくなる（テスト容易性）。
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;

    // コンストラクタで依存を受け取る（コンストラクタインジェクション）。
    // フィールドに @Autowired を直書きするより、テストで差し替えやすく安全。
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * 商品を1件取得する。見つからなければ専用例外を投げる。
     */
    public Product getProduct(Long id) {
        // .get() で乱暴に取り出さず、orElseThrow で「無ければ例外を投げる」を明示。
        // → この例外は GlobalExceptionHandler が受け止めて 404 に変換してくれる。
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    /**
     * 商品を購入して在庫を減らす。
     * @Transactional：途中で例外が起きたら在庫の変更をなかったことにする（ロールバック）。
     */
    @Transactional
    public void purchase(Long id, int quantity) {
        // ① 商品が存在するか（無ければ 404 になる例外）
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        // ② 在庫が足りるか（足りなければ 409 になる例外）
        //    "投げるだけ"。ここで try-catch して裁いたりはしない。
        if (product.getStock() < quantity) {
            throw new OutOfStockException();
        }

        // ③ 在庫を減らして保存
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }
}
