package bad;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CouponEligibilityService {

    public boolean canUseCoupon(Member member, int amount,
                                LocalDate campaignStart, LocalDate campaignEnd) {

        LocalDate today = LocalDate.now();

        // GOLD か SILVER で、5000円以上の購入のときに許可したい
        if (member.getRank() == Rank.GOLD || member.getRank() == Rank.SILVER && amount >= 5000) {

            // キャンペーン期間内か（開始日から終了日まで）
            if (today.isAfter(campaignStart) && !today.isAfter(campaignEnd.plusDays(1))) {

                // 未ログイン or 退会済みなら弾きたい
                if (!member.isLoggedIn() && !member.isActive()) {
                    return false;
                }

                // クーポン利用フラグが有効で、上限額を超えていないこと
                if (member.isCouponEnabled() == true && amount <= member.getCouponLimit()) {
                    return true;
                }
            }
        }

        return false;
    }
}
