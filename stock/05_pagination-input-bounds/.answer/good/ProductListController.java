import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductListController {

    private final ProductListService productListService;

    // コンストラクタインジェクション（@Autowired フィールド注入より推奨）
    public ProductListController(ProductListService productListService) {
        this.productListService = productListService;
    }

    // GET /products?page=0&size=20 のような呼び出しに応答する。
    // defaultValue を付けることで、パラメータ未指定でも安全な初期値で動く。
    @GetMapping("/products")
    public PageResponse<Product> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // 入力の補正・上限クランプ・DBページングはサービス側に集約している。
        // Controller は「受け取って渡して、返す形に詰め替える」だけに徹する。
        Page<Product> result = productListService.getProducts(page, size);

        // エンティティをそのまま返さず、レスポンス専用の箱に詰め替えて返す。
        // 総件数・総ページ数も Page が持っているのでそのまま渡せる。
        return new PageResponse<>(
                result.getNumber(),        // 実際に返したページ番号
                result.getSize(),          // 実際に使った件数
                result.getTotalElements(), // 総件数（DBのCOUNT結果）
                result.getTotalPages(),    // 総ページ数
                result.getContent());      // このページの商品リスト
    }
}
