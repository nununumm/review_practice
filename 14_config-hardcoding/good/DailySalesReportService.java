package com.example.report.good;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 毎日の売上を集計して、レポートファイルに書き出し、
 * 売上が大きい日は経理にメールで速報を送るサービス（改善版）。
 *
 * before からの主な変更点：
 *  1. 決め打ちしていた値（パス・メール・税率・しきい値）を設定ファイルへ追い出した（@Value）。
 *  2. 「今日」を LocalDate.now() 直書きではなく、注入した Clock（時計）から取る → テストで日付を固定できる。
 *  3. 集計を for 文ではなく DB の SUM/COUNT に任せた。
 *  4. 例外を握りつぶさず、ログに残したうえで投げ直す。ファイル出力が成功したときだけ通知する。
 */
@Service
public class DailySalesReportService {

    // ログ出力係。System.out や printStackTrace ではなく、これを通すとログ基盤に正しく残る。
    private static final Logger logger = LoggerFactory.getLogger(DailySalesReportService.class);

    // ---- 外から渡してもらう部品（コンストラクタで注入 = DI）----
    private final OrderRepository orderRepository; // 注文DBアクセス係
    private final MailSender mailSender;            // メール送信係
    private final Clock clock;                      // 時計。本番は本物、テストは「止めた時計」を渡せる

    // ---- 設定ファイル(application.properties)から流し込む値 ----
    // ${...} の中は application.properties に書いた「名前」。起動時にSpringが値を入れてくれる。
    @Value("${report.output-dir}")
    private String outputDir;        // 例: C:\reports（本番では別の値に差し替えられる）

    @Value("${report.notify-email}")
    private String notifyEmail;      // 例: keiri-bucho@example.com（異動しても設定を直すだけ）

    @Value("${report.tax-rate}")
    private double taxRate;          // 例: 1.1（税率が変わっても設定を直すだけ）

    @Value("${report.notify-threshold}")
    private long notifyThreshold;    // 例: 1000000（通知の基準額）

    // コンストラクタ。Springが起動時に必要な部品を自動で渡してくれる。
    public DailySalesReportService(OrderRepository orderRepository,
                                   MailSender mailSender,
                                   Clock clock) {
        this.orderRepository = orderRepository;
        this.mailSender = mailSender;
        this.clock = clock;
    }

    // 今日の売上を集計 → レポートファイルに出力 → 高額な日は経理に通知
    public void generateDailyReport() {

        // 「今日」は now() 直書きではなく、注入された時計から取得する。
        // → テストでは「2026-08-01で止まった時計」を渡せば、その日のレポートを何度でも再現できる。
        LocalDate today = LocalDate.now(clock);

        // 集計はDBに任せ、返ってくるのは「合計」と「件数」の数字だけ（メモリに注文を積まない）。
        DailySalesSummary summary = orderRepository.summarizeByOrderDate(today);
        long total = summary.getTotalAmount();
        long count = summary.getOrderCount();

        // 税込み金額の計算。税率は設定ファイル由来なので、法改正時もコードを触らずに済む。
        // （※本来お金の計算は誤差の出ない BigDecimal が理想。今回の主題ではないので簡略化している）
        long totalWithTax = (long) (total * taxRate);

        // 出力先パスも設定由来。Path.of で「フォルダ＋ファイル名」を安全に組み立てる。
        Path reportFile = Path.of(outputDir, "daily_" + today + ".txt");

        // try-with-resources：ここで開いた writer は、成功でも例外でも自動で close される（閉じ忘れ＝リソース漏れを防ぐ）。
        try (BufferedWriter writer = Files.newBufferedWriter(reportFile)) {
            writer.write("売上合計(税込): " + totalWithTax + "円");
            writer.newLine();
            writer.write("注文件数: " + count + "件");
            writer.newLine();
        } catch (IOException e) {
            // 握りつぶさない：まずログ基盤に「何が・どのファイルで失敗したか」を残し…
            logger.error("日次売上レポートの書き込みに失敗しました。file={}", reportFile, e);
            // …そのうえで例外を投げ直して処理を中断する。
            // こうしないと「ファイルは壊れているのに、速報メールだけ元気に飛ぶ」という“嘘の正常”が起きる。
            throw new UncheckedIOException("日次売上レポートの生成に失敗しました", e);
        }

        // ここに到達した＝ファイル出力は成功。そのときだけ通知する。
        if (totalWithTax > notifyThreshold) {
            mailSender.send(notifyEmail,
                    "本日の売上速報",
                    "税込売上は " + totalWithTax + " 円でした");
            // 「いつ・誰に送ったか」も運用の証跡としてログに残しておくと後の調査が楽。
            logger.info("売上速報を送信しました。date={}, totalWithTax={}", today, totalWithTax);
        }
    }
}
