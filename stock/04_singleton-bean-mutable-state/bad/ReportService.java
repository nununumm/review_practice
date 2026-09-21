import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class ReportService {

    private String currentUserName;

    private int processedCount;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private final StringBuilder buffer = new StringBuilder();

    public String generateReport(String userName, List<String> lines) {
        this.currentUserName = userName;

        buffer.append("==== 帳票 ====\n");
        buffer.append("作成者: ").append(currentUserName).append("\n");
        buffer.append("作成日時: ").append(dateFormat.format(new Date())).append("\n");
        buffer.append("--------------\n");

        for (String line : lines) {
            processedCount++;
            buffer.append(processedCount).append(": ").append(line).append("\n");
        }

        buffer.append("--------------\n");
        buffer.append("合計 ").append(processedCount).append(" 件 / 担当 ").append(currentUserName).append("\n");

        return buffer.toString();
    }
}
