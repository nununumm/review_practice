// 会員登録フォームから送られてくる入力を受け取るための入れ物クラス
public class RegisterRequest {

    private String name;      // 氏名
    private String email;     // メールアドレス
    private int age;          // 年齢
    private String password;  // パスワード

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
