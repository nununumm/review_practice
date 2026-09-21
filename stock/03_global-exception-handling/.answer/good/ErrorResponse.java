package com.example.shop.api;

import java.time.OffsetDateTime;

/**
 * エラー時にフロントへ返す「統一されたJSONの形」を表すクラス（DTO＝データを運ぶ入れ物）。
 *
 * なぜ必要？
 * ・エラーのたびに返る形がバラバラ（あるときは {"error": "..."}、あるときはスタックトレース…）だと、
 *   フロント側は「どう受け取ればいいか」判断できず、画面表示が崩れる。
 * ・「エラーは必ずこの形で返る」と1つに決めておくと、フロントは安心して処理を書ける。
 *
 * 返るJSONのイメージ:
 * {
 *   "timestamp": "2026-09-21T10:15:30+09:00",  ← いつ起きたか
 *   "code": "NOT_FOUND",                        ← アプリ独自のエラー種別（機械が判定しやすい）
 *   "message": "指定された商品が見つかりません" ← 利用者に見せてよい説明（内部情報は出さない）
 * }
 */
public class ErrorResponse {

    private final OffsetDateTime timestamp; // エラーが起きた時刻（タイムゾーン付き）
    private final String code;              // エラー種別コード（例: NOT_FOUND / BAD_REQUEST）
    private final String message;           // 利用者向けメッセージ（DBの内部事情などは含めない）

    public ErrorResponse(String code, String message) {
        this.timestamp = OffsetDateTime.now(); // 生成時の時刻を自動でセット
        this.code = code;
        this.message = message;
    }

    // getter が無いと Spring が JSON に変換できないので用意する
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
