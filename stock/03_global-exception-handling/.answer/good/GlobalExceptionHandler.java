package com.example.shop.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * アプリ全体の "例外の受付係"（例外処理の集約場所）。
 *
 * 役割：
 * ・各 Controller / Service から投げられた例外をここで一括して受け止め、
 *   種類ごとに「正しいHTTPステータス」と「統一されたJSON(ErrorResponse)」に変換する。
 * ・これがあるおかげで、個々の Controller から try-catch を"全部"追放できる。
 *   （＝「エラー処理」という横断的関心事を1か所に集めた。掃除の当番表を1枚にまとめたイメージ）
 *
 * @RestControllerAdvice
 *   = 「全 Controller 共通の例外処理をここに集約する」という宣言。
 *     どの Controller で例外が出ても、Spring が自動でこのクラスに処理を回してくれる。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ログ出力用。System.out.println や printStackTrace ではなく必ずロガーを使う
    //  → ログ基盤（ファイル/監視ツール）に正しく流れ、後から検索・集計できる
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * データが見つからない → 404 Not Found
     * 「探し物が無い」ときの定番ステータス。
     *
     * @ExceptionHandler(...) = 「この型の例外が来たら、このメソッドで処理する」という割り当て。
     * @ResponseStatus(...)   = このメソッドが返すHTTPステータスを固定する。
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException e) {
        // 想定内のエラーなので警告レベルで軽く記録（スタックトレースは不要）
        log.warn("データが見つかりません: {}", e.getMessage());
        // 内部メッセージ(e.getMessage())はログにだけ残し、
        // 利用者へは"見せてよい"固定文言だけを返す（内部情報を漏らさない）
        return new ErrorResponse("NOT_FOUND", "指定されたデータが見つかりません。");
    }

    /**
     * 入力が不正 → 400 Bad Request
     * 「利用者側のリクエストが間違っている」ときのステータス。
     */
    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidRequest(InvalidRequestException e) {
        log.warn("入力が不正です: {}", e.getMessage());
        // 入力ミスは"何を直せばよいか"を伝えたいので、例外メッセージをそのまま見せてよい
        // （※このメッセージには内部実装やDB構造を含めない設計にしておくこと）
        return new ErrorResponse("BAD_REQUEST", e.getMessage());
    }

    /**
     * データの状態と衝突した → 409 Conflict
     * 例：二重登録、在庫やバージョンの競合など。
     * （今回の題材では未使用だが、"種類ごとにステータスを分ける"見本として用意）
     */
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(ConflictException e) {
        log.warn("競合が発生しました: {}", e.getMessage());
        return new ErrorResponse("CONFLICT", "処理が現在の状態と競合しました。時間をおいて再度お試しください。");
    }

    /**
     * 上記のどれにも当てはまらない予期せぬ例外 → 500 Internal Server Error
     * 「サーバ側の想定外のバグ・障害」。
     *
     * ここが最後の砦。想定外なので：
     * ・ログには"スタックトレース付き"でしっかり残す（原因調査のため）。
     * ・でも利用者へは詳細を一切見せない（内部情報の漏洩＝攻撃者へのヒントになるため）。
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpected(Exception e) {
        // 第2引数に例外オブジェクトを渡すと、ロガーがスタックトレースまで出力してくれる
        log.error("予期せぬエラーが発生しました", e);
        // 利用者には固定の一般メッセージだけ（e.getMessage() は出さない）
        return new ErrorResponse("INTERNAL_ERROR", "サーバ内部でエラーが発生しました。");
    }
}
