import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 会員登録＋配送見積もりサービス（改善版）。
 *
 * ポイントは「プリミティブ執着（＝意味のある値を String や int の素の型のまま持ち回すこと）」をやめ、
 * 値オブジェクト（EmailAddress / PhoneNumber / PostalCode / Money）を使うこと。
 * これで (a) 検証が各型に集約され重複が消える、(b) 型が違うので引数の取り違えをコンパイルで防げる、
 * (c) 金額の単位・丸めが Money の中に隠れて計算ミスを防げる、という3つの利点が同時に得られる。
 */
@Service
public class RegistrationService {

    // 消費税率10%を名前付き定数にする（マジックナンバーを避ける）。
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    // 高額配送とみなすしきい値（税込5000円）。Money で持つことで単位が型に表れる。
    private static final Money EXPENSIVE_THRESHOLD = Money.yen(5000);

    private final List<Member> members = new ArrayList<>();

    /**
     * 会員を登録する。
     * 引数が EmailAddress / PhoneNumber / PostalCode と別々の型なので、
     * 順番を取り違えて渡すとコンパイルエラーになる（＝実行前に気づける）。
     * 検証は各値オブジェクトのコンストラクタで済んでいるので、ここでチェックは不要。
     */
    public Member register(EmailAddress email, PhoneNumber phone, PostalCode postalCode) {
        Member member = new Member(email, phone, postalCode);
        members.add(member);
        return member;
    }

    /**
     * 会員のメールアドレスを変更する。
     * newEmail は EmailAddress 型なので、この時点で形式は検証済み。
     * bad 版のようにメール検証をここへコピペする必要がない（重複が消える）。
     */
    public void changeEmail(EmailAddress current, EmailAddress newEmail) {
        Member member = findByEmail(current);
        member.changeEmail(newEmail);
    }

    /**
     * 会員の配送先郵便番号を変更する。
     * newPostalCode は PostalCode 型なので、桁チェックは済んでいる（重複コピペ不要）。
     */
    public void changePostalCode(EmailAddress current, PostalCode newPostalCode) {
        Member member = findByEmail(current);
        member.changePostalCode(newPostalCode);
    }

    /**
     * 配送見積もり金額（税込）を計算して返す。
     * 引数も戻り値も Money 型。商品金額と配送料を足し、Money.withTax で税込にする。
     * 掛け算・丸めは Money の中（BigDecimal）で正確に行われるので、
     * int の掛け算による桁あふれ（オーバーフロー）や丸めミスが起きない。
     */
    public Money estimateShipping(Money itemPrice, Money shippingFee) {
        Money subtotal = itemPrice.add(shippingFee);
        return subtotal.withTax(TAX_RATE);
    }

    /**
     * 高額配送（税込5000円以上）かどうかを判定する。
     * 見積もりロジックを estimateShipping に一本化し、しきい値比較も Money に任せる。
     */
    public boolean isExpensiveShipping(Money itemPrice, Money shippingFee) {
        Money total = estimateShipping(itemPrice, shippingFee);
        return total.isGreaterThanOrEqual(EXPENSIVE_THRESHOLD);
    }

    /** メールアドレスで会員を探す（見つからなければ例外）。検索処理はここに集約 */
    private Member findByEmail(EmailAddress email) {
        for (Member m : members) {
            if (m.getEmail().equals(email)) {
                return m;
            }
        }
        throw new IllegalArgumentException("会員が見つかりません: " + email);
    }
}
