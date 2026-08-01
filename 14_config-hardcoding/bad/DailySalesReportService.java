package com.example.report.bad;

import java.io.FileWriter;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * 毎日の売上を集計して、レポートファイルに書き出し、
 * 売上が大きい日は経理部長にメールで速報を送るサービス。
 * バッチ（毎晩1回の自動処理）から generateDailyReport() が呼ばれる想定。
 */
@Service
public class DailySalesReportService {

    private final OrderRepository orderRepository;
    private final MailSender mailSender;

    public DailySalesReportService(OrderRepository orderRepository, MailSender mailSender) {
        this.orderRepository = orderRepository;
        this.mailSender = mailSender;
    }

    // 今日の売上を集計 → レポートファイルに出力 → 高額な日は経理に通知
    public void generateDailyReport() {

        // 今日の注文を全部取ってくる
        List<Order> orders = orderRepository.findByOrderDate(LocalDate.now());

        long total = 0;
        for (Order order : orders) {
            total += order.getAmount();
        }

        // 消費税込みの金額にする
        long totalWithTax = (long) (total * 1.1);

        // レポートをファイルに書き出す
        try {
            FileWriter writer = new FileWriter("C:\\reports\\daily_" + LocalDate.now() + ".txt");
            writer.write("売上合計(税込): " + totalWithTax + "円\n");
            writer.write("注文件数: " + orders.size() + "件\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 売上が大きい日は経理部長にメールで速報を送る
        if (totalWithTax > 1000000) {
            mailSender.send("keiri-bucho@example.com",
                    "本日の売上速報",
                    "税込売上は " + totalWithTax + " 円でした");
        }
    }
}
