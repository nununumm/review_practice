import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderValidationService {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private OrderRepository orderRepository;

    public String validateAndGetError(OrderRequest request) {
        String errorMessage = "";
        if (request != null) {
            if (request.getCustomerName() != null && !request.getCustomerName().trim().equals("")) {
                if (request.getCustomerName().length() <= 20) {
                    if (request.getEmail() != null && !request.getEmail().trim().equals("")) {
                        if (request.getEmail().contains("@") && request.getEmail().contains(".")) {
                            if (request.getPhone() != null && !request.getPhone().trim().equals("")) {
                                String phone = request.getPhone().replace("-", "");
                                boolean phoneIsNumber = true;
                                for (int i = 0; i < phone.length(); i++) {
                                    if (!Character.isDigit(phone.charAt(i))) {
                                        phoneIsNumber = false;
                                    }
                                }
                                if (phoneIsNumber) {
                                    if (phone.length() == 10 || phone.length() == 11) {
                                        if (request.getQuantity() > 0) {
                                            if (request.getQuantity() <= 99) {
                                                if (request.getAddress() != null && !request.getAddress().trim().equals("")) {
                                                    if (request.getAddress().length() <= 100) {
                                                        if (request.getCouponCode() != null && !request.getCouponCode().trim().equals("")) {
                                                            Coupon coupon = couponRepository.findByCode(request.getCouponCode());
                                                            if (coupon != null) {
                                                                if (coupon.getRemaining() > 0) {
                                                                    // クーポン在庫を1つ減らして利用回数を記録する
                                                                    coupon.setRemaining(coupon.getRemaining() - 1);
                                                                    coupon.setUsedCount(coupon.getUsedCount() + 1);
                                                                    couponRepository.save(coupon);
                                                                    orderRepository.incrementCouponUsage(request.getCustomerName());
                                                                } else {
                                                                    errorMessage = "クーポンの在庫がありません";
                                                                    return errorMessage;
                                                                }
                                                            } else {
                                                                errorMessage = "クーポンコードが不正です";
                                                                return errorMessage;
                                                            }
                                                        }
                                                        // ここまで全て通れば検証OK
                                                    } else {
                                                        errorMessage = "住所が長すぎます";
                                                        return errorMessage;
                                                    }
                                                } else {
                                                    errorMessage = "住所を入力してください";
                                                    return errorMessage;
                                                }
                                            } else {
                                                errorMessage = "数量が多すぎます";
                                                return errorMessage;
                                            }
                                        } else {
                                            errorMessage = "数量は1以上を指定してください";
                                            return errorMessage;
                                        }
                                    } else {
                                        errorMessage = "電話番号の桁数が不正です";
                                        return errorMessage;
                                    }
                                } else {
                                    errorMessage = "電話番号は数字で入力してください";
                                    return errorMessage;
                                }
                            } else {
                                errorMessage = "電話番号を入力してください";
                                return errorMessage;
                            }
                        } else {
                            errorMessage = "メールアドレスの形式が不正です";
                            return errorMessage;
                        }
                    } else {
                        errorMessage = "メールアドレスを入力してください";
                        return errorMessage;
                    }
                } else {
                    errorMessage = "顧客名が長すぎます";
                    return errorMessage;
                }
            } else {
                errorMessage = "顧客名を入力してください";
                return errorMessage;
            }
        } else {
            errorMessage = "リクエストが不正です";
            return errorMessage;
        }
        return errorMessage;
    }
}
