package bad;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CsvReportBuilder {

    public String build(List<Order> orders) {
        String csv = "";
        csv += "ID,顧客,金額\n";

        for (Order o : orders) {
            csv += o.getId() + "," + o.getCustomerName() + "," + o.getAmount() + "\n";
        }

        return csv;
    }
}
