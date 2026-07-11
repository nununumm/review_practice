import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 請求書番号（例: "INV-20260711-0001"）を発行するサービス。
 *
 * 【前回からの一番大きな変更点】
 *   連番を「メモリの int フィールド」ではなく「DB の行」で管理するようにした。
 *   これにより、同時アクセス・再起動・複数台構成でも番号がダブらない。
 */
@Service
@RequiredArgsConstructor // final フィールドを引数に取るコンストラクタを自動生成（＝DIの受け口）
public class InvoiceNumberService {

    // DBの連番テーブルを操作する窓口。コンストラクタ経由で外から渡してもらう（依存性の注入=DI）
    private final InvoiceSequenceRepository repository;

    /**
     * 日付を "20260711" の形にする道具。
     *
     * SimpleDateFormat はやめて DateTimeFormatter を使う。
     *   理由: SimpleDateFormat は「複数人で同時に使うと壊れる」部品だった。
     *   DateTimeFormatter は中に状態を持たない作りなので、同時に使っても安全。
     *   安全なので static final（＝1個だけ作って全員で使い回す）にしてよい。
     */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 請求書番号を1つ発行して返す。
     *
     * @Transactional ＝「この処理は一連の作業としてまとめて実行し、
     *                  途中で失敗したら全部なかったことにする」宣言。
     *                  さらに、上でかけた行ロックはこのメソッドが終わる（＝コミットする）まで
     *                  保たれるので、その間ずっと他のリクエストを待たせられる。
     */
    @Transactional
    public String issue() {
        LocalDate today = LocalDate.now();

        // 今日の連番の行を「鍵つき」で取得。まだ無い日（その日の1件目）なら新しく作る。
        InvoiceSequence seq = repository.findByDateForUpdate(today)
                .orElseGet(() -> repository.save(new InvoiceSequence(today, 0)));

        // 連番を1つ進める。
        // この行はロックされていて "今このリクエストだけ" が触っている状態なので、
        // 「読む→足す→書く」の途中で誰かに割り込まれる心配がない。
        int next = seq.getLastSeq() + 1;
        seq.setLastSeq(next);
        // seq は DB から取得した「管理下の」オブジェクトなので、
        // トランザクション終了時に変更が自動でDBへ反映される（明示的 save も可）。

        // 4桁ゼロ埋め（1 → "0001"）で番号を組み立てて返す。
        // 「直近の番号」は状態としてこのサービスに持たせない（＝共有フィールドを作らない）。
        return "INV-" + today.format(DATE_FORMAT) + "-" + String.format("%04d", next);
    }
}
