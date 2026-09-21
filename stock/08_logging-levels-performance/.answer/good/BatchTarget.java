import java.util.List;

// 処理対象1件を表すクラス（bad と同じ構造。ログの出し方が論点なので中身は据え置き）。
public class BatchTarget {

    private Long id;
    private String status;
    private List<String> details;

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    // このデータがそのまま処理してよいものか（必須項目が揃っているか）を判定する。
    public boolean isValid() {
        return status != null && details != null;
    }

    // 1件分の実処理。失敗すると例外を投げる。
    public void execute() {
        if (!isValid()) {
            throw new IllegalStateException("invalid target");
        }
    }

    // 全項目をつなげた重い文字列（デバッグ時の詳細ダンプ用）。
    // 生成コストが高いので、呼ぶ側は debug が有効なときだけ呼ぶこと。
    public String toBigString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BatchTarget{id=").append(id).append(", status=").append(status).append(", details=[");
        for (String d : details) {
            sb.append(d).append(",");
        }
        sb.append("]}");
        return sb.toString();
    }
}
