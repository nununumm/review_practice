package com.example.payment;

/**
 * 「業務的に決済を断られた」ことを表す例外。
 * 例：残高不足、カードが無効、限度額オーバー、など。
 *
 * これは「入力（カードやお金の状態）そのものの問題」なので、
 * 同じ条件で何度リトライしても結果は変わらない → isRetryable() は false。
 * 呼び出し側は「お客様に理由を伝えて、別のカードを促す」といった対応をする。
 */
public class PaymentDeclinedException extends PaymentException {

    // "INSUFFICIENT_FUNDS"（残高不足）などの理由コード。画面表示やログ分析に使える。
    private final String reason;

    // 断られた理由（reason）とメッセージ、そして元の例外を受け取り、親に連鎖させる。
    public PaymentDeclinedException(String reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    // 業務起因（入力が悪い）なのでリトライしても無駄。
    @Override
    public boolean isRetryable() {
        return false;
    }
}
