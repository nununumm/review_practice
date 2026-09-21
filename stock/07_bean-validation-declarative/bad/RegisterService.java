import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class RegisterService {

    @Autowired
    private MemberRepository memberRepository;

    // メールアドレスの形式チェックに使う正規表現
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^([a-zA-Z0-9]+)*@example\\.com$");

    // 会員登録：入力チェックをしてから保存する
    public Member register(RegisterRequest request) {

        // 氏名チェック
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("氏名は必須です");
        }
        if (request.getName().length() > 50) {
            throw new IllegalArgumentException("氏名が長すぎます");
        }

        // メールチェック
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new IllegalArgumentException("メールアドレスは必須です");
        }
        if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            throw new IllegalArgumentException("メールアドレスの形式が不正です");
        }

        // 年齢チェック
        if (request.getAge() < 18) {
            throw new IllegalArgumentException("18歳以上のみ登録できます");
        }

        // パスワードチェック
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            return null;
        }

        // 検証を通ったので会員を作って保存する
        Member member = new Member();
        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setAge(request.getAge());
        member.setPassword(request.getPassword());
        return memberRepository.save(member);
    }

    // 管理画面から会員を追加するときの入力チェック
    public boolean validateForAdmin(RegisterRequest request) {
        if (request.getName() == null || request.getName().isEmpty()) {
            return false;
        }
        if (request.getEmail() == null) {
            return false;
        }
        if (request.getAge() < 0 || request.getAge() > 150) {
            return false;
        }
        return true;
    }
}
