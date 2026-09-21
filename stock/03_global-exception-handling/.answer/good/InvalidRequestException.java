package com.example.shop.api;

/**
 * 「利用者が送ってきた入力が不正だった」ことを表す独自例外。
 * 例：数量に 0 や マイナス が指定された等。
 *
 * ポイント：
 * ・「見つからない(404)」とは意味が違う。こちらは"利用者側のミス"なので 400 Bad Request が正しい。
 * ・状況ごとに例外の型を分けておくと、ハンドラ側でHTTPステータスを正しく振り分けられる。
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
