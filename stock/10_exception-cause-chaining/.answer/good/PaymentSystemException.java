package com.example.payment;

/**
 * 「システム側の一時的な不調」を表す例外。
 * 例：ネットワークが一瞬切れた、ゲートウェイが混んでいて落ちた、レスポンスが壊れていた、など。
 *
 * これは「利用者が悪いのではなく、環境の問題」なので、
 * 少し待ってからリトライ（再試行）すれば成功する見込みがある → isRetryable() は true。
 */
public class PaymentSystemException extends PaymentException {

    // message と、原因になった元の例外（IOException など）を必ず受け取り、親に渡して連鎖させる。
    public PaymentSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    // システム起因の一時的失敗なので「リトライする価値あり」とみなす。
    @Override
    public boolean isRetryable() {
        return true;
    }
}
