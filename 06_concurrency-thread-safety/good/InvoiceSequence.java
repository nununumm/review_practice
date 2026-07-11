import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDate;

/**
 * 「日付ごとの連番」をデータベースに保存しておくための入れ物（テーブル）。
 *
 * メモリ（プログラムの一時記憶）ではなく DB に置くのがポイント。
 *  - サーバーを再起動しても消えない
 *  - サーバーが複数台に増えても、DB は1つなので全台で共有できる
 */
@Entity
public class InvoiceSequence {

    /**
     * 日付を主キー（＝1行を区別する目印）にする。
     * 「2026-07-11 の連番」は必ず1行だけ、という状態を DB が保証してくれる。
     */
    @Id
    private LocalDate sequenceDate;

    /** その日、最後に発行した番号（1件目なら1、2件目なら2…）。 */
    private int lastSeq;

    // JPA（DBとJavaをつなぐ仕組み）が内部で使うため、引数なしコンストラクタが必要
    protected InvoiceSequence() {
    }

    public InvoiceSequence(LocalDate sequenceDate, int lastSeq) {
        this.sequenceDate = sequenceDate;
        this.lastSeq = lastSeq;
    }

    public int getLastSeq() {
        return lastSeq;
    }

    public void setLastSeq(int lastSeq) {
        this.lastSeq = lastSeq;
    }

    public LocalDate getSequenceDate() {
        return sequenceDate;
    }
}
