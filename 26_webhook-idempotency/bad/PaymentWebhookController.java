@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final OrderRepository orderRepository;
    private final PointRepository pointRepository;
    private final MailSender mailSender;

    private static final Logger logger = LoggerFactory.getLogger(PaymentWebhookController.class);

    /**
     * 決済プロバイダ（外部サービス）からの「入金完了」通知を受け取るエンドポイント。
     * 対象の注文を支払い済みにし、購入額に応じてポイントを付与し、確認メールを送る。
     * 決済プロバイダは、成功レスポンス(2xx)を受け取るまで同じ通知を何度も再送する仕様。
     */
    @PostMapping("/payment")
    public String handle(@RequestBody Map<String, Object> payload,
                         @RequestHeader("X-Signature") String signature) {
        try {
            String eventType = (String) payload.get("type");
            Long orderId = Long.valueOf(payload.get("orderId").toString());
            int paidAmount = (int) payload.get("amount");

            logger.info("Webhook受信 type=" + eventType + " order=" + orderId
                    + " amount=" + paidAmount + " email=" + payload.get("email") + " sig=" + signature);

            if (eventType.equals("payment.succeeded")) {
                Order order = orderRepository.findById(orderId).get();

                order.setStatus("PAID");
                order.setPaidAmount(paidAmount);
                orderRepository.save(order);

                Point point = pointRepository.findByUserId(order.getUserId());
                point.setAmount(point.getAmount() + paidAmount / 100);
                pointRepository.save(point);

                mailSender.send(order.getUserEmail(), "お支払いを確認しました");
            }

            return "OK";
        } catch (Exception e) {
            e.printStackTrace();
            return "OK";
        }
    }
}
