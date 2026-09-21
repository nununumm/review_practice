package com.example.tenant;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.stereotype.Service;

/**
 * レポート生成サービス。
 * TenantContext から今のテナントIDを取り出し、そのテナントの売上データを集計する。
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
     * 今のテナントの売上レポートを作る。
     */
    public Report generateReport() {
        // TenantContext から現在のテナントIDを取り出す
        Long tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            // テナントIDが取れなかったときは、とりあえず全テナントを対象に集計する
            List<Sales> all = salesRepository.findAll();
            return new Report(all);
        }

        List<Sales> sales = salesRepository.findByTenantId(tenantId);
        return new Report(sales);
    }

    /**
     * 複数月のレポートをまとめて作る（月ごとに裏のスレッドで並行して集計する）。
     */
    public List<Report> generateMonthlyReports(List<Integer> months) {
        return months.stream()
                .map(month -> executor.submit(() -> {
                    // 別スレッドの中でテナントIDを取り出して集計する
                    Long tenantId = TenantContext.getTenantId();
                    List<Sales> sales = salesRepository.findByTenantIdAndMonth(tenantId, month);
                    return new Report(sales);
                }))
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
