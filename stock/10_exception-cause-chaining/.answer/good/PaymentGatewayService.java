package com.example.payment;

import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 外部の決済ゲートウェイ呼び出しをラップするサービス（模範解答）。
 *
 * 設計の背骨（初学者向けにまとめ）:
 * 1. 生の例外（IOException など）を、意味のある独自例外に「翻訳」する。
 * 2. 翻訳するとき、必ず元の例外を cause として渡す（＝原因の連鎖を切らない）。
 * 3. 失敗を握りつぶさない（null や false でごまかさず、例外として上に伝える）。
 * 4. 例外は種類ごとに型を分け、リトライ可否を isRetryable() で区別できるようにする。
 * 5. ログは「境界」で一度だけ。ここでは翻訳して投げるだけにし、二重ログ・二重処理をしない。
 */
@Service
public class PaymentGatewayService {

    // ★ ログはここでは出さない。例外を投げて上位（Controller / @ControllerAdvice）に任せ、
    //    「境界」で一度だけ記録する。ここで log してさらに throw すると二重ログ・二重処理になる。

    // 外部ゲートウェイを叩く部品。コンストラクタで受け取る（＝コンストラクタ・インジェクション）。
    private final PaymentGatewayClient client;

    // final フィールドにコンストラクタで注入。テスト時にモック（偽物）を差し込みやすい。
    public PaymentGatewayService(PaymentGatewayClient client) {
        this.client = client;
    }

    /**
     * 決済を実行する。
     * 失敗した場合は、種類の分かる独自例外（PaymentException のサブクラス）を投げる。
     * 呼び出し側は catch した例外の型／isRetryable() を見て、リトライするか・お客様に返すかを決められる。
     */
    public PaymentResult pay(PaymentRequest request) {
        try {
            // まず素直に外部ゲートウェイを呼ぶ。
            return client.charge(request);

        } catch (GatewayDeclinedException e) {
            // 残高不足など「業務的に断られた」ケース。
            // 業務系の独自例外に翻訳。第2引数以降で reason・メッセージ・元例外(e)を必ず渡す（＝原因の連鎖）。
            throw new PaymentDeclinedException(
                    e.getReason(),
                    "決済が拒否されました（orderId=" + request.getOrderId() + "）",
                    e);

        } catch (IOException e) {
            // 通信エラー。一時的な不調かもしれないのでシステム系（リトライ可能）に翻訳。元例外(e)を連鎖。
            throw new PaymentSystemException(
                    "決済ゲートウェイとの通信に失敗しました（orderId=" + request.getOrderId() + "）",
                    e);

        } catch (GatewayParseException e) {
            // レスポンスが壊れていたケースもシステム系として扱う。元例外(e)を連鎖。
            throw new PaymentSystemException(
                    "決済ゲートウェイの応答を解釈できませんでした（orderId=" + request.getOrderId() + "）",
                    e);
        }
        // ★ catch (Exception e) で一括りにしない。
        //   想定していない例外（NullPointerException 等）はわざと素通りさせ、バグに早く気づけるようにする。
    }

    /**
     * 決済を実行し、取引IDを返す。
     * bad/ では「取れなければ null」だったが、それだと呼び出し側が失敗に気づけない。
     * 成功時は必ず取引IDを返し、失敗時は例外で知らせる（＝握りつぶさない）。
     */
    public String payAndGetTransactionId(PaymentRequest request) {
        // pay() が成功すれば必ず結果があり、失敗すれば pay() が例外を投げる。null は返さない。
        PaymentResult result = pay(request);
        return result.getTransactionId();
    }

    /**
     * 決済が承認されたかどうかを返す。
     * bad/ では finally の return で例外を握りつぶしていたが、
     * ここでは pay() が投げる例外をそのまま上に通し、成功時だけ承認フラグを返す。
     */
    public boolean isPaymentApproved(PaymentRequest request) {
        // 例外は握りつぶさず、そのまま呼び出し側（境界）に伝える。finally での return は使わない。
        return pay(request).isApproved();
    }
}
