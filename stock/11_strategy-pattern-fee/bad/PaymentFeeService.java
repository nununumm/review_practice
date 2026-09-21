import org.springframework.stereotype.Service;

@Service
public class PaymentFeeService {

    public double calculateFee(double amount, String method) {
        double fee = 0;
        if (method == "CREDIT_CARD") {
            fee = amount * 0.036;
            if (fee > 5000) {
                fee = 5000;
            }
            if (fee < 50) {
                fee = 50;
            }
        } else if (method == "CONVENIENCE") {
            fee = amount * 0.025;
            if (fee > 5000) {
                fee = 5000;
            }
            if (fee < 60) {
                fee = 60;
            }
        } else if (method == "BANK_TRANSFER") {
            fee = amount * 0.01;
            if (fee > 5000) {
                fee = 5000;
            }
        } else if (method == "QR_CODE") {
            fee = amount * 0.018;
            if (fee < 30) {
                fee = 30;
            }
        }
        return fee;
    }
}
