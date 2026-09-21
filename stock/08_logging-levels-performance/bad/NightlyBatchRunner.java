import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NightlyBatchRunner {

    private static final Logger log = LoggerFactory.getLogger(NightlyBatchRunner.class);

    @Autowired
    private NightlyBatchService service;

    @Scheduled(cron = "0 0 2 * * *")
    public void schedule() {
        try {
            service.runNightlyBatch();
        } catch (Exception e) {
            log.error("夜間バッチが異常終了しました", e);
        }
    }
}
