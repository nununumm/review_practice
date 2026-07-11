import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * InvoiceSequence テーブルを読み書きするための窓口（リポジトリ）。
 * Spring Data JPA が、この interface の中身を自動で実装してくれる。
 */
public interface InvoiceSequenceRepository extends JpaRepository<InvoiceSequence, LocalDate> {

    /**
     * 指定した日付の連番の行を「鍵をかけながら」取ってくる。
     *
     * @Lock(PESSIMISTIC_WRITE) が今回の主役。
     *   ＝「この行は今から自分が書き換えるので、他の人は自分が終わるまで待ってて」
     *     という鍵（行ロック）を DB にかけてもらう指示。
     *
     * これにより、同時に来た2人のリクエストは "1人ずつ順番に" この行を触ることになり、
     * 前回問題になった「同じ番号のダブり発行」が起きなくなる。
     * しかも鍵をかけているのは DB なので、サーバーが何台あっても効く。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM InvoiceSequence s WHERE s.sequenceDate = :date")
    Optional<InvoiceSequence> findByDateForUpdate(@Param("date") LocalDate date);
}
