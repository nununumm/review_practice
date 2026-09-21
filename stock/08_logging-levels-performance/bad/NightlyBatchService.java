import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NightlyBatchService {

    private static final Logger log = LoggerFactory.getLogger(NightlyBatchService.class);

    @Autowired
    private BatchTargetRepository repository;

    public void runNightlyBatch() {
        List<BatchTarget> targets = repository.findAll();
        System.out.println("夜間バッチ開始: 対象 " + targets.size() + " 件");

        for (BatchTarget target : targets) {
            log.info("処理開始 id=" + target.getId() + " status=" + target.getStatus());
            log.debug("processing " + target.toBigString());

            if (!target.isValid()) {
                log.debug("不正なデータのためスキップ id=" + target.getId());
                continue;
            }

            try {
                process(target);
                log.info("処理成功 id=" + target.getId());
            } catch (Exception e) {
                log.error("項目処理でエラー id=" + target.getId(), e);
            }
        }

        System.out.println("夜間バッチ終了");
    }

    private void process(BatchTarget target) {
        try {
            target.execute();
        } catch (Exception e) {
            log.error("execute失敗 id=" + target.getId(), e);
            throw new RuntimeException("処理失敗 id=" + target.getId(), e);
        }
    }
}
