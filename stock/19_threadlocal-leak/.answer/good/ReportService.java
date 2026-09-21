package com.example.tenant;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * レポート生成サービス（改善版）。
 *
 * ポイントは「テナントIDを暗黙のグローバル（ThreadLocal）から勝手に読まない」こと。
 * メソッドの引数で明示的に受け取ることで、
 *   ・どこからテナントが来るのかがコードを見ればわかる（隠れた結合をなくす）
 *   ・テストのとき好きなテナントIDを渡せる（ThreadLocal を仕込む必要がない）
 *   ・別スレッドへも「値そのもの」を渡せる（ThreadLocal は別スレッドに引き継がれない）
 * という利点がある。ThreadLocal からの取り出しは入口側で1回だけ行い、以降は引数で渡す。
 */
@Service
public class ReportService {

    private final SalesRepository salesRepository;

    // 重い集計を裏で動かすためのスレッドプール（＝作業員を何人か常駐させておく仕組み）
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public ReportService(SalesRepository salesRepository) {
        this.salesRepository = salesRepository;
    }

    /**
     * 指定テナントの売上レポートを作る。
     * テナントIDは引数で明示的に受け取る（＝グローバルを覗きに行かない）。
     */
    public Report generateReport(long tenantId) {
        // 引数で必ずテナントが渡ってくるので、「不明なら全件」という危険な既定動作は発生しない
        List<Sales> sales = salesRepository.findByTenantId(tenantId);
        return new Report(sales);
    }

    /**
     * 複数月のレポートをまとめて作る（月ごとに裏のスレッドで並行して集計する）。
     *
     * 別スレッドは呼び出し元の ThreadLocal を引き継がないため、
     * テナントIDを「値」としてラムダに閉じ込めて明示的に渡す（＝伝播させる）。
     */
    public List<Report> generateMonthlyReports(long tenantId, List<Integer> months) {
        List<Callable<Report>> tasks = months.stream()
                // ここで tenantId をキャプチャして各タスクに持たせる。
                // 別スレッドの中でも、渡した値をそのまま使えるので null にならない。
                .<Callable<Report>>map(month -> () -> {
                    List<Sales> sales = salesRepository.findByTenantIdAndMonth(tenantId, month);
                    return new Report(sales);
                })
                .collect(Collectors.toList());

        try {
            // invokeAll ですべてのタスクを実行し、結果を順番どおりに集める
            return executor.invokeAll(tasks).stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            // 失敗を握りつぶさず、原因例外を包んで上に伝える
                            throw new RuntimeException("月次レポートの生成に失敗しました", e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (InterruptedException e) {
            // 待機中に割り込まれたら、割り込み状態を復元してから通知する（作法）
            Thread.currentThread().interrupt();
            throw new RuntimeException("月次レポートの生成が中断されました", e);
        }
    }
}
