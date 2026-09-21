/**
 * 会員を表すクラス。
 * フィールドを String ではなく値オブジェクト（EmailAddress / PhoneNumber / PostalCode）で持つ。
 * こうすると「不正な形式の会員」がそもそも作れなくなる（各型が生成時に検証済みだから）。
 */
public class Member {

    // メールアドレス（値オブジェクト）。可変にしておき、変更時も値オブジェクト経由で入れ替える。
    private EmailAddress email;
    private PhoneNumber phone;
    private PostalCode postalCode;

    public Member(EmailAddress email, PhoneNumber phone, PostalCode postalCode) {
        this.email = email;
        this.phone = phone;
        this.postalCode = postalCode;
    }

    public EmailAddress getEmail() {
        return email;
    }

    // 差し替えは EmailAddress 型でしか受け付けないので、検証済みの値しか入らない
    public void changeEmail(EmailAddress email) {
        this.email = email;
    }

    public PhoneNumber getPhone() {
        return phone;
    }

    public PostalCode getPostalCode() {
        return postalCode;
    }

    public void changePostalCode(PostalCode postalCode) {
        this.postalCode = postalCode;
    }
}
