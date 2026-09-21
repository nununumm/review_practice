package bad;

import org.springframework.stereotype.Service;

@Service
public class ShippingLabelService {

    public String createLabel(Order order) {
        String zip = order.getCustomer().getAddress().getZipCode();
        String pref = order.getCustomer().getAddress().getPrefecture();
        String name = order.getCustomer().getName();

        StringBuilder label = new StringBuilder();
        label.append("〒").append(zip).append("\n");
        label.append(pref).append("\n");
        label.append(name).append(" 様");

        return label.toString();
    }
}
