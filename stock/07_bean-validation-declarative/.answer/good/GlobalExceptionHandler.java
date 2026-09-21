import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * アプリ全体の例外を1か所で受け止める窓口（@RestControllerAdvice）。
 *
 * ポイント：エラーの通知方法を「例外」に一本化する。
 * bad/ では throw / return false / return null がバラバラだったが、
 * 「入力エラーは例外として投げる → ここで 400 とエラー内容に変換する」
 * と決めておけば、呼び出し側は分岐に悩まなくて済む（＝通知方法の統一）。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // @Valid の検証に引っかかると Spring がこの例外を投げてくる。それをここで捕まえる
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 入力エラーなので 400 を返す
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidation(MethodArgumentNotValidException ex) {
        // 「項目名 → エラーメッセージ」の形にまとめてクライアントに返す
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return errors;
    }
}
