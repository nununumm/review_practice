package bad;

import org.springframework.stereotype.Component;

@Component
public class CouponValidator {

    public DiscountResult validate(String couponCode, String memberRank) {

        int discountRate = 0;

        // クーポンコードの判定
        if (couponCode == "SUMMER2026") {
            discountRate += 20;
        } else if (couponCode == "WELCOME") {
            discountRate += 10;
        }

        // 会員ランクの判定
        if (memberRank == "GOLD") {
            discountRate += 15;
        } else if (memberRank == "SILVER") {
            discountRate += 5;
        }

        return new DiscountResult(discountRate);
    }
}
