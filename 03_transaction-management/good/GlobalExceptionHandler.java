/**
 * 例外を「利用者への見せ方（HTTPステータス・メッセージ）」に変換する専用の場所。
 * Service では例外を握りつぶさず投げっぱなしにし、整合性（ロールバック）を守る。
 * 「業務ロジック」と「エラーの表現」の責務を分離するのが狙い。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<ErrorResponse> handleOutOfStock(OutOfStockException e) {
        log.warn("Out of stock. {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)   // 409
                .body(new ErrorResponse("OUT_OF_STOCK", "在庫が不足しています"));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException e) {
        log.warn("Resource not found. {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)  // 404
                .body(new ErrorResponse("NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest()                  // 400
                .body(new ErrorResponse("BAD_REQUEST", e.getMessage()));
    }

    /** 想定外の例外は握りつぶさず、スタックトレースを「ログに」残して500を返す。 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("Unexpected error occurred.", e);         // printStackTrace ではなくロガーで記録
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)  // 500
                .body(new ErrorResponse("INTERNAL_ERROR", "処理中にエラーが発生しました"));
    }
}
