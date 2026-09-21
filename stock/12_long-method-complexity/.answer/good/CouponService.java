import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * クーポンの「在庫を減らして利用回数を記録する」副作用（＝データ書き換え）を担当するサービス。
 *
 * 検証サービス（OrderValidationService）からこの処理を切り離すことで、
 * 「検証だけしたい」「消費だけしたい」を別々に呼べるようになり、テストもしやすくなる。
 */
@Service
public class CouponService {

    private final CouponRepository couponRepository;

    // コンストラクタインジェクション（＝必要な部品を生成時に受け取る）。
    // @Autowired をフィールドに付けるより、依存が分かりやすくテストで差し替えやすい。
    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    /**
     * クーポンを1つ消費する（在庫を1減らし、利用回数を1増やす）。
     * @Transactional を付け、途中で失敗したら在庫と利用回数の更新をまとめて取り消す（中途半端な状態を残さない）。
     */
    @Transactional
    public void consume(String couponCode) {
        // コードでクーポンを取得。存在しなければ意味のある例外で中断する。
        Coupon coupon = couponRepository.findByCode(couponCode);
        if (coupon == null) {
            throw new OrderValidationException("クーポンコードが不正です");
        }
        // 在庫が無ければ消費できない。
        if (coupon.getRemaining() <= 0) {
            throw new OrderValidationException("クーポンの在庫がありません");
        }
        // 在庫を1減らし、利用回数を1増やして保存する。
        coupon.setRemaining(coupon.getRemaining() - 1);
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);
    }
}
