package bad;

import java.util.List;

public class OrderTotalCalculator {

    public int calculateTotal(List<OrderLine> lines) {
        int total = 0;
        for (OrderLine line : lines) {
            total += line.getUnitPrice() * line.getQuantity();
        }
        return total;
    }

    public int averagePerItem(List<OrderLine> lines) {
        int total = calculateTotal(lines);
        int count = 0;
        for (OrderLine line : lines) {
            count += line.getQuantity();
        }
        return total / count;
    }
}
