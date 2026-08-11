package good;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 発行済みクーポンを管理するサービス。
// ※ この例は「equals/hashCode の学習」に集中するため、あえてメモリ(Set)で管理している。
//   本番では発行済み情報はDBで永続化すべき（README「現場ではどうするか」を参照）。
public class CouponService {

    // System.out.println の代わりにロガーを使う。
    // → 出力レベル(INFO/WARN/ERROR)や出力先を後から制御でき、本番でも消さずに運用できる。
    private static final Logger log = LoggerFactory.getLogger(CouponService.class);

    private final Set<Coupon> issuedCoupons = new HashSet<>();

    // クーポンを1枚発行する。発行できたら true、すでに発行済みで何もしなければ false を返す。
    public boolean issue(String code, int discountAmount) {
        Coupon coupon = new Coupon(code, discountAmount);

        // Set.add() は「実際に追加できたら true / すでに在れば false」を返す。
        // contains() → add() の2手を1手にまとめられて、意図も明確。
        // （Coupon に equals/hashCode があるので、中身が同じなら "在る" と正しく判定される）
        if (!issuedCoupons.add(coupon)) {
            log.info("クーポンは発行済みのためスキップしました: code={}", code);
            return false;
        }

        log.info("クーポンを発行しました: code={}, discount={}", code, discountAmount);
        return true;
    }

    // これまでに発行したクーポンの枚数
    public int issuedCount() {
        return issuedCoupons.size();
    }
}
