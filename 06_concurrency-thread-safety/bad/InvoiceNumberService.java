@Service
public class InvoiceNumberService {

    // 日付フォーマット用（"20260711" のような文字列を作る）
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    // その日の連番（1件目なら1、2件目なら2…）
    private int sequence = 0;

    // 直近に発行した請求書番号を覚えておく
    private String lastIssuedNumber;

    /**
     * 請求書番号を発行する。
     * 形式: "INV-20260711-0001"
     */
    public String issue() {
        // 今日の日付を "20260711" の形にする
        String today = dateFormat.format(new Date());

        // 連
        // 番を1つ増やす
        sequence = sequence + 1;

        // 4桁ゼロ埋め（1 → "0001"）にして番号を組み立てる
        String number = "INV-" + today + "-" + String.format("%04d", sequence);

        // 直近発行番号として保持
        lastIssuedNumber = number;

        return number;
    }

    public String getLastIssuedNumber() {
        return lastIssuedNumber;
    }
}
