package bad;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentAmountService {

    // 割引率10%
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal(0.1);

    /**
     * 顧客の支払額が請求額と一致するか判定する。
     */
    public boolean isPaidInFull(BigDecimal paid, BigDecimal billed) {
        return paid.equals(billed);
    }

    /**
     * 請求額に割引を適用し、割引後の支払金額を返す。
     * 割引後 = 請求額 - (請求額 × 割引率)
     */
    public BigDecimal calculateDiscountedAmount(BigDecimal billed) {
        BigDecimal discount = billed.multiply(DISCOUNT_RATE);
        return billed.subtract(discount);
    }

    /**
     * 請求額を分割回数で割り、1回あたりの支払額を返す。
     */
    public BigDecimal calculateInstallment(BigDecimal billed, int times) {
        return billed.divide(new BigDecimal(times));
    }
}
