public interface CouponRepository {

    Coupon findByCode(String code);

    void save(Coupon coupon);
}
