package bad;

import java.math.BigDecimal;

public class BillingCalculator {

    public BigDecimal perPerson(BigDecimal total, int people) {
        return total.divide(new BigDecimal(people));
    }

    public BigDecimal taxIncluded(double price) {
        BigDecimal base = new BigDecimal(price);
        BigDecimal tax = base.multiply(new BigDecimal(0.1));
        return base.add(tax);
    }
}
