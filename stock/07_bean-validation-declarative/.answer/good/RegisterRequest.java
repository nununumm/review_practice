import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 会員登録フォームからの入力を受け取る入れ物（DTO）。
 *
 * ポイント：入力ルール（必須・文字数・形式・範囲）は「手続きのif」ではなく、
 * フィールドに付けた制約アノテーション（＝Bean Validation。入力ルールを
 * 宣言的に書く仕組み）で表現している。こうすると
 *   ・ルールが一覧で見えて抜け漏れに気づきやすい
 *   ・検証コードを毎回書かなくてよい（重複しない）
 *   ・境界（Controller）で自動的にチェックしてもらえる
 * という利点がある。
 */
public class RegisterRequest {

    // @NotBlank ＝ null でも空文字でも空白だけでもNG（＝必ず中身がある文字列）
    // @Size ＝ 文字数の下限・上限。氏名は1〜50文字に統一する
    @NotBlank(message = "氏名は必須です")
    @Size(max = 50, message = "氏名は50文字以内で入力してください")
    private String name;

    // @Email ＝ メールアドレスの形式チェック。自前の危険な正規表現ではなく
    //          実績のあるライブラリの検証に任せる（ReDoSの心配がない）
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が正しくありません")
    private String email;

    // @Min / @Max ＝ 数値の下限・上限。18歳以上、上限150歳という境界を明示する
    @Min(value = 18, message = "18歳以上のみ登録できます")
    @Max(value = 150, message = "年齢が正しくありません")
    private int age;

    // パスワードは8文字以上（上限も付けて極端に長い入力を弾く）
    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, max = 100, message = "パスワードは8文字以上100文字以内で入力してください")
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
