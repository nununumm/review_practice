package good;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.EnumSet;
import java.util.Set;

/**
 * 注文の状態遷移と一覧取得を担うサービス。
 *
 * bad 版との違いは大きく3つ。
 *  1. enum を「名前（文字列）」でDBに保存する（並び順に依存しない）。
 *  2. 「今の状態から次の状態へ進んでよいか」のルールを enum 自身が持つ（不正な遷移を弾く）。
 *  3. Entity をそのまま返さず、外向けの DTO に詰め替えて返す（内部構造を漏らさない）。
 */

/**
 * 注文の状態を表す enum。
 * 「NEW → PAID → SHIPPED」と進み、SHIPPED 前ならキャンセル可能、という遷移ルールを
 * この enum 自身に持たせている（＝状態のことは状態が一番よく知っている、という考え方）。
 */
enum OrderStatus {
    // 各状態に「次に進んでよい状態の集合」を持たせる（あとで static ブロックで設定）
    NEW,
    PAID,
    SHIPPED,
    CANCELED;

    // 「この状態から遷移してよい相手」の一覧。EnumSet は enum 専用の軽量な集合
    private Set<OrderStatus> allowedNext;

    static {
        // NEW からは PAID か CANCELED へ進める
        NEW.allowedNext = EnumSet.of(PAID, CANCELED);
        // PAID からは SHIPPED か CANCELED へ進める
        PAID.allowedNext = EnumSet.of(SHIPPED, CANCELED);
        // SHIPPED / CANCELED は終着点なので、そこからは進めない（空集合）
        SHIPPED.allowedNext = EnumSet.noneOf(OrderStatus.class);
        CANCELED.allowedNext = EnumSet.noneOf(OrderStatus.class);
    }

    /** 今の状態から to へ進んでよいかを判定する */
    public boolean canTransitionTo(OrderStatus to) {
        return allowedNext.contains(to);
    }
}

@Entity
class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;

    private String shippingAddress;

    // ★核心：EnumType.STRING を指定し、enum を「名前（"NEW" など）」でDBに保存する。
    // 既定（ORDINAL）だと 0,1,2… という並び順の番号で保存され、あとから enum の
    // 途中に状態を差し込むと番号の意味がずれて既存データが別状態に化ける。名前で保存すれば安全。
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getShippingAddress() { return shippingAddress; }
    public OrderStatus getStatus() { return status; }

    /**
     * 状態を進める。遷移してよいかを OrderStatus 側のルールで確認し、
     * 不正な遷移（例：CANCELED → SHIPPED）なら例外を投げて弾く。
     */
    public void changeStatusTo(OrderStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "不正な状態遷移です: " + this.status + " → " + newStatus);
        }
        this.status = newStatus;
    }
}

interface OrderRepository extends JpaRepository<Order, Long> {
}

/**
 * 外向けの応答用 DTO（Data Transfer Object＝画面やAPIに渡すためだけの入れ物）。
 * Entity をそのまま返すと、内部フィールドや遅延ロードの巻き添えで想定外の情報が漏れる。
 * 必要な項目だけをこの record に詰め替えて返す。
 */
record OrderResponse(Long id, String customerName, String status) {
    static OrderResponse from(Order order) {
        // enum は name() で "NEW" のような文字列にして渡す
        return new OrderResponse(order.getId(), order.getCustomerName(), order.getStatus().name());
    }
}

@RestController
@Service
public class OrderStatusService {

    private final OrderRepository orderRepository;

    public OrderStatusService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * 指定した注文の状態を進める。
     * 存在しないIDは get() で落とさず、orElseThrow で「見つからない」と明示的に知らせる。
     */
    @PostMapping("/orders/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, String newStatus) {
        // findById は Optional（＝あるかもしれない箱）。中身が無ければ意味のある例外を投げる
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("注文が見つかりません: id=" + id));
        // 文字列 → enum の変換。未知の値なら valueOf が例外を投げるので不正値は入り込めない
        OrderStatus target = OrderStatus.valueOf(newStatus);
        // 遷移してよいかのチェックは Order（の中の OrderStatus）に任せる
        order.changeStatusTo(target);
        Order saved = orderRepository.save(order);
        // Entity ではなく DTO に詰め替えて返す
        return OrderResponse.from(saved);
    }

    /**
     * 注文一覧を返す。全件取得（findAll）ではなく Pageable を受け取り、
     * 1ページ分だけDBから取る（件数が増えてもメモリとレスポンスが破綻しない）。
     */
    @GetMapping("/orders")
    public Page<OrderResponse> list(Pageable pageable) {
        // Page 単位で取得し、各 Entity を DTO に変換して返す（map は中身だけ差し替え）
        return orderRepository.findAll(pageable).map(OrderResponse::from);
    }
}
