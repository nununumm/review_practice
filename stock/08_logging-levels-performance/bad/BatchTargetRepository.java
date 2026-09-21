import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchTargetRepository {

    List<BatchTarget> findAll();
}
