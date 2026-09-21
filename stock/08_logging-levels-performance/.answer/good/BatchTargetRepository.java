import org.springframework.stereotype.Repository;

import java.util.List;

// 処理対象を取り出すリポジトリ（DBアクセスの窓口）。
@Repository
public interface BatchTargetRepository {

    List<BatchTarget> findAll();
}
