package com.example.shop.api;

/**
 * 「リクエスト自体は正しいが、データの現在の状態と衝突して実行できない」ことを表す独自例外。
 * 例：同じ注文を二重に作ろうとした、在庫が既に無い、更新バージョンが古い（楽観ロック）など。
 *
 * これは 409 Conflict にマッピングされる。
 * 「見つからない(404)」「入力ミス(400)」とはまた別の"状況"として名前を分けておくのがコツ。
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
