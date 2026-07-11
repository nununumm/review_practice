package com.example.shop.product;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * アプリ全体の "例外の受付係"。
 *
 * 役割：
 * ・各 Controller / Service から投げられた例外をここで一括して受け止め、
 *   種類ごとに「正しいHTTPステータス」と「統一されたJSON」に変換する。
 * ・これがあるおかげで、個々の Controller から try-catch を追放できる。
 *
 * @RestControllerAdvice = 「全Controller共通の例外処理をここに集約する」という宣言。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ログ出力用（System.out.println ではなく必ずロガーを使う → ログ基盤に乗る）
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 商品が見つからない → 404 Not Found
     * 「探し物が無い」ときの定番ステータス。
     */
    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ProductNotFoundException e) {
        // 想定内のエラーなので警告レベルで軽く記録（スタックトレースは不要）
        log.warn("商品が見つかりません: {}", e.getMessage());
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }

    /**
     * 在庫不足 → 409 Conflict
     * 「リクエスト自体は正しいが、データの状態と衝突して実行できない」ときのステータス。
     */
    @ExceptionHandler(OutOfStockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleOutOfStock(OutOfStockException e) {
        log.warn("在庫不足: {}", e.getMessage());
        return new ErrorResponse(HttpStatus.CONFLICT.value(), e.getMessage());
    }

    /**
     * 入力値が不正（例：quantity が0以下）→ 400 Bad Request
     * 「あなたのリクエストがそもそもおかしい」ときのステータス。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(ConstraintViolationException e) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "入力値が不正です: " + e.getMessage());
    }

    /**
     * 上記のどれにも当てはまらない "想定外" のエラー → 500 Internal Server Error
     * ここが最後の網（セーフティネット）。
     * 500 は本物の障害なので、error レベルでスタックトレースごと詳しくログに残す。
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpected(Exception e) {
        // 第2引数に例外オブジェクトを渡すと、原因のスタックトレースまで記録される。
        log.error("想定外のエラーが発生しました", e);
        // 利用者には内部の詳細を見せない（情報漏洩防止）。定型文だけ返す。
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "サーバー内部でエラーが発生しました。");
    }
}
