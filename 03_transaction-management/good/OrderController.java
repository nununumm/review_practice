/**
 * Controller = 「外からのリクエストを受け取り、結果を返す玄関口」の担当。
 * ここは"受付"に徹して薄く保つのがコツ。
 * 業務ロジックはService、例外の見せ方はGlobalExceptionHandlerに任せる。
 */
// @RestController = 「このクラスはWeb APIの入口です。戻り値はそのままレスポンスの本文にします」の目印。
@RestController
// @RequestMapping = このクラスが担当するURLの共通部分。ここでは /api/orders 配下をまとめて受け持つ。
@RequestMapping("/api/orders")
// @RequiredArgsConstructor = final フィールド(orderService)を受け取るコンストラクタを自動生成（DI用）。
@RequiredArgsConstructor
public class OrderController {

    // 実際の注文処理を任せる相手（Service）。Springが自動でここに入れてくれる。
    private final OrderService orderService;

    // @PostMapping = HTTPのPOSTリクエスト（＝新規作成の依頼）が来たら、このメソッドを実行する。
    @PostMapping
    public ResponseEntity<OrderResult> placeOrder(
            // @RequestBody = リクエストの本文(JSON)を OrderRequest オブジェクトに変換して受け取る。
            // @Validated = その中身が入力ルール(例:必須項目)を満たしているか自動チェックする。
            @RequestBody @Validated OrderRequest request) {
        // 受け取った依頼を、そのままService（本体の処理）に渡すだけ。Controllerは判断しない。
        OrderResult result = orderService.placeOrder(request);
        // 新規作成が成功したので 201 CREATED と、作成結果(注文ID入り)を返す。
        return ResponseEntity.status(HttpStatus.CREATED).body(result);  // 201
        // ※ try-catch は書かない。例外はGlobalExceptionHandlerが受け止めてくれる。
    }
}
