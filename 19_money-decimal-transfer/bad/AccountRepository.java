package bad;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

// 口座テーブルにアクセスするリポジトリ
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findById(Long id);
}
