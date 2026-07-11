package com.example.shop.product;

/**
 * エラー時にフロントへ返す「統一されたJSONの形」を定義するクラス（DTO）。
 *
 * なぜ必要？
 * ・エラーのたびに返る形がバラバラ（あるときは文字列、あるときはnull…）だと、
 *   フロント開発者が対応しきれない。「エラーは必ずこの形で返る」と決めておくと親切。
 *
 * 返るJSONのイメージ:
 * {
 *   "status": 404,
 *   "message": "商品が見つかりません。id=99999"
 * }
 */
public class ErrorResponse {

    private final int status;      // HTTPステータスコード（404, 409, 500 など）
    private final String message;  // 利用者向けのエラーメッセージ

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    // getter が無いと Spring が JSON に変換できないので用意する
    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
