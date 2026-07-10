/**
 * 「例外が起きたとき、利用者にどう見せるか（HTTPステータスやメッセージ）」だけを
 * まとめて担当する専用クラス。
 *
 * こうして1か所に集めることで、
 *   ・Service側は例外を投げっぱなしにできる（＝ロールバックが正しく効く）
 *   ・「業務のロジック」と「エラーの見せ方」を分けて管理できる（責務の分離）
 * というメリットがある。
 */
// @RestControllerAdvice = 「全部のControllerで起きた例外を、ここで横断的に受け止める」という宣言。
@RestControllerAdvice
public class GlobalExceptionHandler {

    // エラー内容を記録するためのロガー（printStackTrace の代わり）。
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // @ExceptionHandler(X.class) = 「X という種類の例外が飛んできたら、このメソッドで処理する」指定。
    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<ErrorResponse> handleOutOfStock(OutOfStockException e) {
        // warn = 「異常だが想定内」レベルのログ。在庫切れは起こりうるのでこのレベル。
        log.warn("Out of stock. {}", e.getMessage());
        // 在庫切れは「今は無理だが正しいリクエスト」なので 409 CONFLICT を返す。
        return ResponseEntity.status(HttpStatus.CONFLICT)   // 409
                .body(new ErrorResponse("OUT_OF_STOCK", "在庫が不足しています"));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException e) {
        log.warn("Resource not found. {}", e.getMessage());
        // 対象データが見つからないので 404 NOT FOUND。
        return ResponseEntity.status(HttpStatus.NOT_FOUND)  // 404
                .body(new ErrorResponse("NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException e) {
        // 入力自体がおかしい（注文数0など）ので 400 BAD REQUEST。
        return ResponseEntity.badRequest()                  // 400
                .body(new ErrorResponse("BAD_REQUEST", e.getMessage()));
    }

    // 上のどれにも当てはまらない「想定外」の例外を、最後の砦として受け止める。
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        // error = 「想定外の重大事」レベル。第2引数に例外eを渡すと原因(スタックトレース)ごとログに残る。
        // ここが printStackTrace の正しい置き換え：画面に垂れ流さず、ログ基盤に記録する。
        log.error("Unexpected error occurred.", e);
        // 中身の詳細は利用者に見せず、500エラーと当たり障りのないメッセージだけ返す（情報漏えい防止）。
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)  // 500
                .body(new ErrorResponse("INTERNAL_ERROR", "処理中にエラーが発生しました"));
    }
}
