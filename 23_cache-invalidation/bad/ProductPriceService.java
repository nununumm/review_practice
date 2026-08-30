import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品の表示価格（会員割引を適用した後の価格）を返すサービス。
 * 商品一覧やトップページで何度も呼ばれるため、毎回DBに問い合わせないよう
 * 価格情報を「キャッシュ」（＝一度計算した結果をメモリに覚えておく仕組み）している。
 */
@Service
@RequiredArgsConstructor
public class ProductPriceService {

    private final ProductRepository productRepository;

    // 商品IDごとに、計算済みの価格情報を覚えておく箱
    private Map<Long, ProductPrice> priceCache = new HashMap<>();

    // 画面表示用に、商品の現在価格（会員割引を適用した後）を返す
    public ProductPrice getPrice(Long productId) {
        if (!priceCache.containsKey(productId)) {
            Product product = productRepository.findById(productId).get();

            ProductPrice price = new ProductPrice();
            price.setProductId(productId);
            price.setBasePrice(product.getPrice());
            // 会員割引10%を引いた価格を計算する
            price.setSalePrice(product.getPrice() - (product.getPrice() * 0.1));

            priceCache.put(productId, price);
        }
        return priceCache.get(productId);
    }

    // 管理画面から、商品の定価を変更する
    public void updatePrice(Long productId, int newPrice) {
        Product product = productRepository.findById(productId).get();
        product.setPrice(newPrice);
        productRepository.save(product);
    }

    // アプリ起動時などに、全商品の価格をあらかじめ計算してキャッシュに載せておく
    public void warmUp() {
        try {
            List<Product> products = productRepository.findAll();
            for (Product p : products) {
                getPrice(p.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
