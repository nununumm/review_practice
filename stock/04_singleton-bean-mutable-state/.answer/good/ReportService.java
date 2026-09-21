import org.springframework.stereotype.Service;

import java.time.LocalDateTime;                         // 日時を表す不変（あとから変わらない）クラス
import java.time.format.DateTimeFormatter;              // 日時を文字列に整える道具。スレッドセーフ（＝同時に使っても壊れない）
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;          // 複数スレッドから安全に数を増やせるカウンタ

// @Service … このクラスを Spring に「サービス部品」として登録する。
// 重要: Spring の Bean は既定で「シングルトン」（＝アプリ全体でインスタンスは1つだけ）。
//       全リクエストがこの1個を共有するので、リクエストごとに変わる値をフィールドに持ってはいけない。
@Service
public class ReportService {

    // 日付整形の道具は「状態を持たない・不変・スレッドセーフ」なので、共有フィールドにしてよい。
    // static final にして1回だけ作り、全リクエストで安全に使い回す。
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    // 「アプリ起動からの累計処理件数」など“全体で1つだけ数えたい”値だけは共有してよい。
    // ただし普通の int の ++ は同時実行で数え損なうので、AtomicLong（＝アトミックに増やせる）を使う。
    private final AtomicLong totalProcessed = new AtomicLong();

    // リクエストごとの値（ユーザー名・件数・組み立て中のバッファ）は、
    // すべてメソッドの「引数」と「ローカル変数」で扱う。フィールドには一切持たない。
    public String generateReport(String userName, List<String> lines) {

        // 組み立て用のバッファはメソッド内で新しく作る。呼び出しごとに独立するので混線しない。
        StringBuilder sb = new StringBuilder();

        // このリクエストの件数を数えるカウンタも、ローカル変数にする。
        int count = 0;

        // ヘッダー: 作成者と作成日時。日時はここで LocalDateTime.now() を整形する。
        sb.append("==== 帳票 ====\n");
        sb.append("作成者: ").append(userName).append("\n");
        sb.append("作成日時: ").append(LocalDateTime.now().format(DATE_FORMAT)).append("\n");
        sb.append("--------------\n");

        // 本文: 1行ずつ行番号を付けて追加する。count はこのメソッドの中だけの値。
        for (String line : lines) {
            count++;
            sb.append(count).append(": ").append(line).append("\n");
        }

        // フッター: 合計件数と担当者。userName は引数なので他リクエストと混ざらない。
        sb.append("--------------\n");
        sb.append("合計 ").append(count).append(" 件 / 担当 ").append(userName).append("\n");

        // 全体の累計だけは共有カウンタに足す（数え損なわないよう addAndGet を使う）。
        totalProcessed.addAndGet(count);

        // 完成した帳票文字列を返す。状態はどこにも残らない＝ステートレス。
        return sb.toString();
    }

    // 起動からの累計処理件数を確認したいとき用（任意）。
    public long getTotalProcessed() {
        return totalProcessed.get();
    }
}
