package com.example.shop.controller;

import com.example.shop.exception.OrderNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// @RestControllerAdvice = 「全コントローラ共通の例外の受け皿」。
// 各コントローラで try/catch を書き散らかす代わりに、例外の種類ごとの“変換ルール”をここに集約する。
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 「注文が見つからない」例外が飛んできたら 404 Not Found に変換する。
    // Controller 側は「例外を投げるだけ」でよくなり、本来の処理に集中できる。
    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail handleNotFound(OrderNotFoundException e) {
        // ProblemDetail = エラー内容をJSONで返すためのSpring標準の形（RFC 7807準拠）。
        //   ステータスコード＋人間向けの説明をセットで返せる。
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 入力チェック（@Valid）に引っかかった場合は 400 Bad Request。
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
