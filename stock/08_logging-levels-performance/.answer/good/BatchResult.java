import java.util.List;

// バッチ1回分の結果をまとめて返すための入れ物（DTO＝データを運ぶだけのクラス）。
// これがあると呼び出し側は「失敗が何件あったか」を戻り値で判断でき、ログを目視しなくて済む。
public class BatchResult {

    private final int successCount;      // 成功件数
    private final int skippedCount;      // スキップ件数（不正データなど）
    private final List<Long> failedIds;  // 失敗したIDの一覧

    public BatchResult(int successCount, int skippedCount, List<Long> failedIds) {
        this.successCount = successCount;
        this.skippedCount = skippedCount;
        this.failedIds = failedIds;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public List<Long> getFailedIds() {
        return failedIds;
    }

    // 失敗が1件でもあったかどうかを一言で判定できる便利メソッド。
    public boolean hasFailure() {
        return failedIds != null && !failedIds.isEmpty();
    }
}
