/**
 * Controller はリクエストの受け渡しに専念する（薄く保つ）。
 * 例外処理は書かない（GlobalExceptionHandler に任せる）。
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResult> placeOrder(@RequestBody @Validated OrderRequest request) {
        OrderResult result = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);  // 201
    }
}
