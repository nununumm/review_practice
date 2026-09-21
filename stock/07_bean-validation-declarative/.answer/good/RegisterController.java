import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会員登録の入り口（Controller）。
 *
 * ポイント：@Valid を付けるだけで、RegisterRequest に書いた制約アノテーションが
 * ここ（＝リクエストが入ってくる境界）で自動的にチェックされる。
 * ルールに反していれば Spring が MethodArgumentNotValidException を投げ、
 * それは GlobalExceptionHandler が受け止めて 400（入力エラー）に変換する。
 * だから Controller にもService にも「手続きのif」を一切書かなくてよい。
 */
@RestController
public class RegisterController {

    // Controller はコンストラクタでサービスを受け取る（フィールドインジェクションより
    // テストしやすく、依存が明確になる）
    private final RegisterService registerService;

    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }

    // 登録成功時は 201 Created を返す
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/members")
    public Member register(@Valid @RequestBody RegisterRequest request) {
        // ここに来た時点で入力は検証済み。あとは業務ロジックを呼ぶだけ
        return registerService.register(request);
    }
}
