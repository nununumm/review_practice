package com.example.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 外部の決済ゲートウェイにカード決済を依頼するサービス（修正版）。
 *
 * このお題のポイントは2つ:
 *   ① ログに「秘密情報・個人情報」を絶対に載せない
 *   ② 例外を握りつぶさず、意味のある形で扱う（＝業務的失敗とシステム故障を区別する）
 */
@Service
public class PaymentGatewayService {

    // ログ出力用。System.out や e.printStackTrace() ではなく、必ずこのロガー経由で出す
    // → こうするとログ基盤（Datadog / CloudWatch 等）に集約され、時刻・レベル付きで検索できる。
    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayService.class);

    // フィールドインジェクション(@Autowired)ではなく、コンストラクタインジェクションで受け取る。
    // final にできてテストしやすく、依存が明示される（第8問の観点）。
    private final PaymentGatewayClient client;

    public PaymentGatewayService(PaymentGatewayClient client) {
        this.client = client;
    }

    /**
     * 決済を実行する。
     *
     * 戻り値 boolean = 「業務的に決済が成立したか」だけを表す。
     *   ・true  … 決済成立
     *   ・false … カード会社に正常に問い合わせたうえで "拒否" された（残高不足・限度額超過など）
     *
     * システム的な失敗（通信断・タイムアウト等）は false では返さず、例外を投げる。
     *   → 呼び出し側が「拒否」と「故障」を混同しないようにするため。
     */
    public boolean charge(PaymentRequest request) {
        // 【①ログ】秘密情報は一切載せない。
        //   ・カード番号 → 下4桁だけマスクして載せる（調査に使えて、かつ漏れても被害が最小）
        //   ・CVV / APIトークン → そもそも載せない（下記の解説参照）
        //   ・追跡用に、秘密でない注文IDと金額だけ出す
        //   ・"+" の文字列連結ではなく、{} のプレースホルダを使う（SLF4Jの作法）
        log.info("決済開始 orderId={}, amount={}, card=****{}",
                request.getOrderId(), request.getAmount(), lastFour(request.getCardNumber()));

        try {
            // 外部ゲートウェイの呼び出し。ここで通信断・タイムアウト等の例外が飛びうる。
            PaymentResponse response = client.charge(request);

            // 【①ログ】response をまるごと出さない（中に秘密情報が含まれる恐れがある）。
            //   必要な項目（成否・結果コード）だけを、秘密でないと確認したうえで出す。
            log.info("決済レスポンス orderId={}, success={}, resultCode={}",
                    request.getOrderId(), response.isSuccess(), response.getResultCode());

            // 業務的な結果（成立/拒否）をそのまま返す。
            return response.isSuccess();

        } catch (Exception e) {
            // 【②例外】握りつぶして false を返さない。
            //   ・スタックトレースごとログに残す（第2引数に例外オブジェクト e を渡すのが肝）。
            //     → e.printStackTrace() は不要。log.error(msg, e) 一本でログ基盤に届く。
            //   ・そのうえで独自例外に包んで投げ直し、「システム故障」であることを呼び出し側に伝える。
            log.error("決済処理でシステム例外が発生しました orderId={}", request.getOrderId(), e);
            throw new PaymentGatewayException(
                    "決済ゲートウェイの呼び出しに失敗しました。orderId=" + request.getOrderId(), e);
        }
    }

    /**
     * カード番号の下4桁だけを取り出すヘルパー。
     * ログに載せてよいのは「本人確認のヒントになるが、それ単体では悪用できない」下4桁まで。
     */
    private String lastFour(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "????";
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }
}
