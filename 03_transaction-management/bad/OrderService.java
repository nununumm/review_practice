@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final PointRepository pointRepository;

    public String placeOrder(OrderRequest request) {
        try {
            // 1. 在庫を減らす
            Stock stock = stockRepository.findById(request.getProductId()).get();
            stock.setQuantity(stock.getQuantity() - request.getCount());
            stockRepository.save(stock);

            // 2. 注文を保存する
            Order order = new Order();
            order.setProductId(request.getProductId());
            order.setCount(request.getCount());
            order.setUserId(request.getUserId());
            orderRepository.save(order);

            // 3. ユーザーにポイントを付与する
            Point point = pointRepository.findByUserId(request.getUserId());
            point.setAmount(point.getAmount() + (request.getCount() * 10));
            pointRepository.save(point);

            return "注文が完了しました";
        } catch (Exception e) {
            e.printStackTrace();
            return "注文に失敗しました";
        }
    }
}
