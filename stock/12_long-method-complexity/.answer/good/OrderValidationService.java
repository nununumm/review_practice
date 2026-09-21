import org.springframework.stereotype.Service;

/**
 * 注文の入力内容を検証する（＝チェックする）サービス。
 *
 * このクラスの責務は「入力が正しいかどうかを確かめる」ことだけに絞る。
 * クーポンの在庫を減らす／利用回数を記録する、といった「副作用（＝データを書き換える処理）」は
 * ここでは行わず、別のサービス（CouponService）に任せる。
 * こうすると「検証だけを何度でも安全に呼べる」「検証と更新を別々にテストできる」ようになる。
 */
@Service
public class OrderValidationService {

    // --- マジックナンバーを名前付き定数にして「何の数字か」を明確にする ---
    private static final int MAX_CUSTOMER_NAME_LENGTH = 20;  // 顧客名の上限文字数
    private static final int MAX_ADDRESS_LENGTH = 100;       // 住所の上限文字数
    private static final int MIN_QUANTITY = 1;               // 数量の下限
    private static final int MAX_QUANTITY = 99;              // 数量の上限
    private static final int PHONE_DIGITS_MIN = 10;          // 電話番号の桁数（固定電話）
    private static final int PHONE_DIGITS_MAX = 11;          // 電話番号の桁数（携帯）

    /**
     * 注文リクエストを検証する。問題があれば OrderValidationException を投げて中断する。
     * 何も投げずに正常終了すれば「検証OK」。
     */
    public void validate(OrderRequest request) {
        // ガード節（＝おかしい入力を先頭で弾いて即 return / throw する書き方）。
        // 先に異常系を片付けることで、正常系の処理がネストの奥に埋もれなくなる。
        if (request == null) {
            throw new OrderValidationException("リクエストが不正です");
        }

        // 項目ごとに小さな検証メソッドを呼ぶだけ。上から読めば「何を検証するか」が一覧できる。
        validateCustomerName(request.getCustomerName());
        validateEmail(request.getEmail());
        validatePhone(request.getPhone());
        validateQuantity(request.getQuantity());
        validateAddress(request.getAddress());
        // クーポンコードは任意項目。ここでは「値の形式」だけ見て、在庫確認や更新はしない。
        validateCouponCodeFormatIfPresent(request.getCouponCode());
    }

    /** 顧客名：必須・上限文字数以内かをチェック */
    private void validateCustomerName(String customerName) {
        requireText(customerName, "顧客名を入力してください");
        if (customerName.length() > MAX_CUSTOMER_NAME_LENGTH) {
            throw new OrderValidationException("顧客名が長すぎます");
        }
    }

    /** メールアドレス：必須・簡易的な形式チェック */
    private void validateEmail(String email) {
        requireText(email, "メールアドレスを入力してください");
        // 実務では正規表現や専用ライブラリを使うが、ここでは最低限「@ と . を含むか」だけ見る
        if (!email.contains("@") || !email.contains(".")) {
            throw new OrderValidationException("メールアドレスの形式が不正です");
        }
    }

    /** 電話番号：必須・数字のみ・桁数チェック */
    private void validatePhone(String phone) {
        requireText(phone, "電話番号を入力してください");
        String digits = phone.replace("-", ""); // ハイフンを除いて数字だけにする
        if (!isAllDigits(digits)) {
            throw new OrderValidationException("電話番号は数字で入力してください");
        }
        if (digits.length() != PHONE_DIGITS_MIN && digits.length() != PHONE_DIGITS_MAX) {
            throw new OrderValidationException("電話番号の桁数が不正です");
        }
    }

    /** 数量：1〜99 の範囲チェック */
    private void validateQuantity(int quantity) {
        if (quantity < MIN_QUANTITY) {
            throw new OrderValidationException("数量は1以上を指定してください");
        }
        if (quantity > MAX_QUANTITY) {
            throw new OrderValidationException("数量が多すぎます");
        }
    }

    /** 住所：必須・上限文字数以内かをチェック */
    private void validateAddress(String address) {
        requireText(address, "住所を入力してください");
        if (address.length() > MAX_ADDRESS_LENGTH) {
            throw new OrderValidationException("住所が長すぎます");
        }
    }

    /**
     * クーポンコード：入力されている場合だけ「空白でないか」を確認する。
     * 在庫が残っているか等の業務チェックは、DBを見る CouponService 側の責務。
     */
    private void validateCouponCodeFormatIfPresent(String couponCode) {
        if (couponCode == null) {
            return; // 任意項目なので未入力はOK
        }
        if (couponCode.trim().isEmpty()) {
            throw new OrderValidationException("クーポンコードが不正です");
        }
    }

    /**
     * 「null または 空文字（空白のみ含む）」を弾く共通処理。
     * 各項目でコピペしていた同じ判定を1か所にまとめ、直す時も1か所で済むようにする。
     */
    private void requireText(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new OrderValidationException(errorMessage);
        }
    }

    /** 文字列がすべて数字かどうかを判定する小さな部品 */
    private boolean isAllDigits(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false; // 数字でない文字が1つでもあれば false
            }
        }
        return true;
    }
}
