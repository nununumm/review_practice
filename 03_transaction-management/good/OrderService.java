@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final int POINT_RATE = 10;

    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final PointRepository pointRepository;

    /**
     * 注文を確定する。①在庫減算 → ②注文保存 → ③ポイント付与 を
     * 1つのトランザクションとして扱い、途中で失敗したら全てロールバックする。
     */
    @Transactional
    public OrderResult placeOrder(OrderRequest request) {
        // 入力バリデーション（業務ルール）
        if (request.getCount() <= 0) {
            throw new IllegalArgumentException("注文数は1以上で指定してください");
        }

        // ① 在庫を「原子的に」減算する。
        //    在庫が足りなければ更新0件 → 例外。同時注文による在庫マイナスも防げる。
        int updated = stockRepository.decreaseStock(request.getProductId(), request.getCount());
        if (updated == 0) {
            throw new OutOfStockException(request.getProductId());
        }

        // ② 注文を保存する
        Order order = new Order(request.getUserId(), request.getProductId(), request.getCount());
        orderRepository.save(order);

        // ③ ポイントを付与する（対象が無ければ例外 → ロールバック）
        Point point = pointRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Point not found. userId=" + request.getUserId()));
        point.addAmount(request.getCount() * POINT_RATE);

        log.info("Order placed. orderId={}, userId={}, productId={}",
                order.getId(), request.getUserId(), request.getProductId());
        return new OrderResult(order.getId());
    }
}
