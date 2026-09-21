import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;   // 定時実行のためのアノテーション
import org.springframework.stereotype.Component;               // Spring に部品として登録

// @Component … スケジュール実行の入口となる部品。
@Component
public class NightlyBatchRunner {

    private static final Logger log = LoggerFactory.getLogger(NightlyBatchRunner.class);

    private final NightlyBatchService service;

    // コンストラクタインジェクションで依存を受け取る。
    public NightlyBatchRunner(NightlyBatchService service) {
        this.service = service;
    }

    // 毎日午前2時に起動する（cron 式）。
    @Scheduled(cron = "0 0 2 * * *")
    public void schedule() {
        // サービスは例外を投げず結果を返す設計なので、ここでは結果を見て判断するだけ。
        // 同じ例外を上でも下でも log.error する「二重ログ」を避けられる。
        BatchResult result = service.runNightlyBatch();

        // 失敗があった事実は、入口側では1行の要約だけ残す（詳細はサービス側で出済み）。
        if (result.hasFailure()) {
            log.warn("夜間バッチに失敗件数があります: {} 件", result.getFailedIds().size());
        }
    }
}
