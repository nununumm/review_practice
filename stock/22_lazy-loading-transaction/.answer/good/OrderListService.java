package good;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 注文一覧APIの明細取得。
 *
 * bad 版は「明細を LAZY（＝あとで必要になったら取りに行く）にしたまま、
 * トランザクション（DBとのやり取りのひとまとまり）の外で明細を触っていた」ため、
 * ・DBセッションが閉じたあとに読もうとして落ちる（LazyInitializationException）
 * ・注文の数だけ明細取得SQLが飛ぶ（N+1問題）
 * という2つの地雷を踏んでいた。good 版では
 * 「トランザクションの中で・明細をまとめて取り・DTOに詰めきってから返す」ことで両方を防ぐ。
 */

@Entity
class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;

    // LAZY のままでよい。読むタイミングを「トランザクション内 かつ まとめ取り」に統一するのが本筋
    @OneToMany(fetch = FetchType.LAZY)
    private List<OrderLine> lines = new ArrayList<>();

    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public List<OrderLine> getLines() { return lines; }
}

@Entity
class OrderLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;

    private int quantity;

    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
}

interface OrderRepository extends JpaRepository<Order, Long> {

    // ★核心：JOIN FETCH で注文と明細を1回のSQLでまとめて取る（N+1回避）。
    // distinct は、明細の数だけ注文が重複して返るのを1件にまとめるための指定。
    @Query("select distinct o from Order o left join fetch o.lines")
    Page<Order> findAllWithLines(Pageable pageable);
}

/**
 * 画面用の応答 DTO（受け渡し専用の入れ物）。
 * Entity（LAZYプロキシ入り）を直接返さず、必要な項目だけをここに詰める。
 */
record OrderSummary(Long id, String customerName, int totalQuantity) {
}

@Service
class OrderListService {

    private final OrderRepository orderRepository;

    OrderListService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * 注文一覧を「画面用DTO」にして返す。
     *
     * ★@Transactional(readOnly = true)：このメソッドの間だけDBセッションを開いておく印。
     *   読み取り専用なので readOnly=true（更新しないと宣言することで最適化が効く）。
     *   明細（LAZY）を触るのは必ずこの中。ここを抜ける前に DTO へ詰めきるので、
     *   セッションが閉じたあとに遅延ロードして落ちる、という事故が起きない。
     */
    @Transactional(readOnly = true)
    public Page<OrderSummary> findOrderSummaries(Pageable pageable) {
        // JOIN FETCH でまとめ取り（SQLは1回）。ページング付きで件数が増えても破綻しない
        return orderRepository.findAllWithLines(pageable)
                .map(order -> {
                    // 明細の合計点数を計算。トランザクション内なので getLines() を安全に触れる
                    int total = order.getLines().stream()
                            .mapToInt(OrderLine::getQuantity)
                            .sum();
                    return new OrderSummary(order.getId(), order.getCustomerName(), total);
                });
    }
}

@RestController
class OrderListController {

    private final OrderListService orderListService;

    OrderListController(OrderListService orderListService) {
        this.orderListService = orderListService;
    }

    /**
     * コントローラは「受け取って返すだけ」。
     * 明細（LAZY）を触る処理はサービス側のトランザクション内で完了しているので、
     * ここでは DTO（もう中身が確定した入れ物）を返すだけでよい。
     */
    @GetMapping("/orders")
    public Page<OrderSummary> list(Pageable pageable) {
        return orderListService.findOrderSummaries(pageable);
    }
}
