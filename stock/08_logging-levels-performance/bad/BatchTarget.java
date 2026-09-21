import java.util.List;

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

    public boolean isValid() {
        return status != null && details != null;
    }

    public void execute() {
        // 実際の1件分の処理（外部連携やDB更新など）。失敗すると例外を投げる。
        if (!isValid()) {
            throw new IllegalStateException("invalid target");
        }
    }

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
