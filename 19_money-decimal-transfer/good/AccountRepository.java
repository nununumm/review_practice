package good;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

// 口座テーブルにアクセスするリポジトリ
public interface AccountRepository extends JpaRepository<Account, Long> {

    // 【並行処理対策】悲観ロック付きで口座を取得する。
    //   PESSIMISTIC_WRITE = SQLでいう「SELECT ... FOR UPDATE」。
    //   この口座を読んだ瞬間から、トランザクションが終わる（commit/rollback）まで
    //   他の処理は同じ口座に触れず“待たされる”。
    //   → 「残高を読む→計算→書き戻す」のすきまに割り込まれる lost update（更新の喪失）を防ぐ。
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);
}
