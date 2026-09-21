package bad;

import org.springframework.stereotype.Component;

@Component
public class ShippingFeeCalculator {

    /**
     * 会員ランクに応じて送料を計算する。
     * PLATINUM: 無料 / GOLD: 半額 / SILVER: 基本送料そのまま
     *
     * @param rank    会員ランク（"PLATINUM" / "GOLD" / "SILVER" / "BRONZE"）
     * @param baseFee 基本送料
     * @return 実際に支払う送料
     */
    public int calculate(String rank, int baseFee) {
        int fee = 0;

        switch (rank) {
            case "PLATINUM":
                fee = 0;
            case "GOLD":
                fee = baseFee / 2;
                break;
            case "SILVER":
                fee = baseFee;
                break;
        }

        return fee;
    }
}
