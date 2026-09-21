/**
 * クーポンをDBから取ってきたり保存したりする窓口（リポジトリ）。
 */
public interface CouponRepository {

    // コードでクーポンを1件探す（無ければ null）
    Coupon findByCode(String code);

    // クーポンの変更をDBに保存する
    void save(Coupon coupon);
}
