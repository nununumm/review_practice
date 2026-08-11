package bad;

import java.util.HashSet;
import java.util.Set;

// 発行済みクーポンを管理するサービス。
// 同じコードのクーポンは二重に発行しない、という仕様のつもり。
public class CouponService {

    // すでに発行したクーポンを溜めておく入れ物
    private final Set<Coupon> issuedCoupons = new HashSet<>();

    // クーポンを1枚発行する
    public void issue(String code, int discountAmount) {
        Coupon coupon = new Coupon(code, discountAmount);

        // すでに同じクーポンを発行済みなら、二重発行を防ぐ
        if (issuedCoupons.contains(coupon)) {
            System.out.println("すでに発行済みです: " + code);
            return;
        }

        issuedCoupons.add(coupon);
        System.out.println("発行しました: " + code);
    }

    // これまでに発行したクーポンの枚数
    public int issuedCount() {
        return issuedCoupons.size();
    }
}
