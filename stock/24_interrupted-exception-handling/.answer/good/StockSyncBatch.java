package good;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 自社在庫を外部システムへ同期するバッチ。
 *
 * bad 版は「無限リトライ」「例外を全部握りつぶす」「割り込みを無視する」の三重苦だった。
 * good 版では、
 *  - リトライ回数に上限をつける（外部が落ち続けても無限ループにならない）
 *  - 待ち時間を失敗のたびに延ばす（＝指数バックオフ：相手を追い打ちしない）
 *  - 割り込み（＝「もう止まって」という中断要求）が来たら速やかに中断する
 *  - ログはロガーで残す
 * を守っている。
 */
@Component
public class StockSyncBatch {

    // SLF4J のロガー。printStackTrace ではなくこれでログを残すと、出力先や重要度を運用側で制御できる
    private static final Logger log = LoggerFactory.getLogger(StockSyncBatch.class);

    // マジックナンバーを定数化して「何の数字か」を名前で説明する
    private static final int MAX_RETRIES = 3;              // 1件あたりのリトライ上限回数
    private static final long INITIAL_BACKOFF_MS = 1_000L; // 最初の待ち時間（ミリ秒）

    private final StockApiClient apiClient;

    public StockSyncBatch(StockApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 在庫アイテムを順番に外部へ送る。1件ずつリトライし、上限に達したらその件はスキップして次へ進む。
     */
    public void sync(List<StockItem> items) {
        for (StockItem item : items) {
            try {
                pushWithRetry(item);
            } catch (InterruptedException e) {
                // 割り込み（＝中断要求）が来たら、これ以上ループを続けず処理全体を止める。
                // 割り込みフラグを立て直してから抜けることで、呼び出し元にも「中断された」と正しく伝わる
                Thread.currentThread().interrupt();
                log.warn("同期処理が中断されました。残りのアイテムは処理しません。", e);
                return;
            }
        }
    }

    /**
     * 1件を、上限回数までリトライしながら送る。
     * 送信失敗（業務的に想定される一時的エラー）はリトライするが、
     * それ以外の想定外の例外（バグ・設定ミス等）はリトライせず即座に上へ投げる。
     *
     * @throws InterruptedException 待機中に割り込まれた場合（＝中断要求）は握りつぶさず上へ伝える
     */
    private void pushWithRetry(StockItem item) throws InterruptedException {
        long backoffMs = INITIAL_BACKOFF_MS;

        // while(true) の無限ループではなく、回数で必ず止まる for ループにする
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                apiClient.push(item);
                return; // 成功したら即終了
            } catch (StockApiException e) {
                // 「外部が一時的に不調」という想定内の失敗だけをリトライ対象にする
                if (attempt == MAX_RETRIES) {
                    // 上限まで試してもダメだった。黙って消さずログに残し、この件はあきらめて次へ
                    log.error("在庫同期に{}回失敗しました。スキップします: sku={}", MAX_RETRIES, item.getSku(), e);
                    return;
                }
                log.warn("在庫同期に失敗（{}回目）。{}ms 待って再試行します: sku={}", attempt, backoffMs, item.getSku(), e);

                // Thread.sleep は割り込まれると InterruptedException を投げる。
                // ここで握りつぶさず throws で上へ伝えることで、中断要求にちゃんと反応できる
                Thread.sleep(backoffMs);

                // 指数バックオフ：待ち時間を毎回2倍にして、落ちている相手を追い打ちしない
                backoffMs *= 2;
            }
        }
    }
}

interface StockApiClient {
    void push(StockItem item);
}

/**
 * 外部在庫APIの「想定内の一時的な失敗」を表す例外。これだけをリトライ対象にする。
 */
class StockApiException extends RuntimeException {
    StockApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

class StockItem {
    private final String sku;
    private final int quantity;

    StockItem(String sku, int quantity) {
        this.sku = sku;
        this.quantity = quantity;
    }

    String getSku() {
        return sku;
    }

    int getQuantity() {
        return quantity;
    }
}
