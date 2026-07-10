package com.example.demo.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 【例外の一元管理】アプリ全体で発生した例外を横断的に受け止め、
// 適切なHTTPステータスと分かりやすいレスポンスに変換する場所。
// エラーの「見せ方」をここに集約することで、各Controller/Serviceは
// 本来の処理に専念でき、エラー応答の作りもブレなくなる。
@RestControllerAdvice
public class GlobalExceptionHandler {

    // バリデーション違反（@NotBlank や @Email 等）が起きたときに呼ばれる。
    // → 400 Bad Request を返す。「どの項目が、なぜダメか」を項目ごとに返す。
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        // 違反した各フィールドについて「フィールド名 → メッセージ」を詰める。
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(errors);
    }

    // ユーザーが見つからない場合に呼ばれる。→ 404 Not Found を返す。
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(UserNotFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
