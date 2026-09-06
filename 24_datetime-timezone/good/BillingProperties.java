import java.time.ZoneId;

/**
 * 請求まわりの「設定値」をまとめて持つクラス。
 *
 * @ConfigurationProperties は、application.yml に書いた設定を
 * このクラスのフィールドに自動で流し込んでくれる仕組み（＝設定の外部化）。
 *
 * application.yml 側:
 *   app:
 *     billing:
 *       zone: Asia/Tokyo   # 請求日を数えるときの基準タイムゾーン
 *       free-trial-days: 7 # 無料お試し期間
 *
 * こうしておくと「7」「30」「Asia/Tokyo」がコードに散らばらず、
 * 環境ごと（開発・本番）に値を変えることもできる。
 */
@ConfigurationProperties(prefix = "app.billing")
@Value // Lombok: 全フィールドを final にして不変オブジェクトにする
public class BillingProperties {

    /** 請求日を判定するときの基準タイムゾーン（サーバーのTZに依存させないため必須） */
    ZoneId zone;

    /** 無料お試し期間の日数 */
    int freeTrialDays;
}
