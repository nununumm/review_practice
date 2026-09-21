import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    // 会員登録フォームからのPOSTを受け付ける
    @PostMapping("/members")
    public Member register(@RequestBody RegisterRequest request) {
        // 受け取ったリクエストをそのままサービスに渡して登録する
        return registerService.register(request);
    }
}
