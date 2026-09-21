import org.slf4j.Logger;                                   // ログ出力の窓口（インターフェース）
import org.slf4j.LoggerFactory;                            // Logger を作る工場
import org.springframework.stereotype.Service;             // このクラスを「サービス部品」として Spring に登録

import java.util.ArrayList;                                // 可変長リスト（失敗IDをためる箱に使う）
import java.util.List;

// @Service … 業務ロジックを持つサービスとして Spring に登録する。
@Service
public class NightlyBatchService {

    // ロガーは「クラスに1つ・変わらない」ので static final で1回だけ作る。
    // getClass() ではなくクラス名を直接渡すのが定石（サブクラスでも名前がぶれない）。
    private static final Logger log = LoggerFactory.getLogger(NightlyBatchService.class);

    // 依存はコンストラクタで受け取る（コンストラクタインジェクション）。
    // final にでき、テスト時にモック（＝偽物）を差し込みやすい。
    private final BatchTargetRepository repository;

    public NightlyBatchService(BatchTargetRepository repository) {
        this.repository = repository;
    }

    // 戻り値で「成功/スキップ/失敗の集計結果」を返す。呼び出し側が結果を判断できる。
    public BatchResult runNightlyBatch() {
        List<BatchTarget> targets = repository.findAll();

        // 全体の開始は「サマリ情報」なので info で1回だけ。System.out は使わない。
        // 文字列連結ではなく {} プレースホルダに値を渡す（不要なら文字列も作られない）。
        log.info("夜間バッチ開始: 対象 {} 件", targets.size());

        int success = 0;                                   // 成功件数のカウンタ
        int skipped = 0;                                   // スキップ件数のカウンタ
        List<Long> failedIds = new ArrayList<>();          // 失敗したIDを集める箱（握りつぶさず後で報告）

        for (BatchTarget target : targets) {
            // 各件の細かい進捗は本番では邪魔なので trace（debug より下）に落とす。
            // {} プレースホルダなので、trace が無効な本番では文字列生成すら起きない。
            log.trace("処理開始 id={} status={}", target.getId(), target.getStatus());

            // 重い文字列（全項目のダンプ）は debug が有効なときだけ作る。
            // これで本番（debug オフ）では toBigString() の重い処理が一切走らない。
            if (log.isDebugEnabled()) {
                log.debug("処理内容 {}", target.toBigString());
            }

            // 不正データのスキップは「重要な出来事」なので握りつぶさず warn で残し、件数も数える。
            if (!target.isValid()) {
                log.warn("不正なデータのためスキップしました id={}", target.getId());
                skipped++;
                continue;
            }

            try {
                target.execute();                          // 1件分の実処理
                success++;
                log.trace("処理成功 id={}", target.getId());
            } catch (Exception e) {
                // 「ログするか、再スローするか」は片方に統一する。
                // ここでは“1件失敗しても全体は続ける”方針なので、この場所で1回だけ記録して集計する（再スローしない）。
                // 例外オブジェクト e を最後の引数に渡すとスタックトレースが1回だけ正しく出る。
                log.warn("項目の処理に失敗しました。処理は継続します id={}", target.getId(), e);
                failedIds.add(target.getId());
            }
        }

        // 最後に必ずサマリを出す。監視や翌朝の確認はこの1行を見れば全体像が分かる。
        log.info("夜間バッチ終了: 成功 {} 件 / スキップ {} 件 / 失敗 {} 件",
                success, skipped, failedIds.size());

        // 失敗が1件でもあれば error で1回だけ目立たせる（詳細IDも添える）。
        if (!failedIds.isEmpty()) {
            log.error("処理に失敗した {} 件があります id一覧={}", failedIds.size(), failedIds);
        }

        // 集計結果を返す。呼び出し側はこれを見て通知やリトライを判断できる。
        return new BatchResult(success, skipped, failedIds);
    }
}
