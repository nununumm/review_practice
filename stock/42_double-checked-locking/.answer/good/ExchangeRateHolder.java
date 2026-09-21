package good;

/**
 * 為替レート表の遅延初期化ホルダー（修正版）。
 *
 * bad 版は「ダブルチェックロッキング」（＝synchronized の外と中で 2 回 null を見る）を
 * 手書きしていたが、肝心の instance フィールドに volatile が無く、壊れていた。
 *
 * なぜ壊れるか（かみ砕き）：
 *   instance = new ExchangeRateHolder(); は、機械語レベルでは
 *     ① オブジェクトの置き場所を確保 → ② コンストラクタで中身を組み立て → ③ instance に代入
 *   の 3 手順だが、JVM/CPU は高速化のため順番を入れ替えて ①→③→② と実行してよい。
 *   すると「③ instance には入ったが ② がまだ = 中身が空っぽ」という一瞬が生まれる。
 *   別スレッドが synchronized の外側の 1 回目のチェックでその「空っぽの instance」を見ると、
 *   ロックも取らずにそのまま返してしまい、中身が未初期化のオブジェクトを掴む＝事故。
 *
 * 対策は「初期化オンデマンド・ホルダー」イディオムが最も素直で安全：
 *   - 静的な入れ子クラスは「初めて参照されたとき」に JVM が初期化する。
 *   - クラスの初期化は JVM がロックの心配なしにスレッドセーフに 1 回だけ行うと保証している。
 *   - よって volatile も synchronized も自分で書かずに、遅延初期化＋スレッド安全を両立できる。
 */
public class ExchangeRateHolder {

    // コンストラクタは private のまま（外から勝手に new させない）
    private ExchangeRateHolder() {
    }

    // 静的な入れ子クラス。ExchangeRateHolder が読み込まれた時点ではまだ初期化されず、
    // Holder.INSTANCE が初めて参照された瞬間に JVM が 1 回だけ・スレッドセーフに初期化する。
    private static class Holder {
        private static final ExchangeRateHolder INSTANCE = new ExchangeRateHolder();
    }

    public static ExchangeRateHolder getInstance() {
        // ここで初めて Holder が初期化される＝本当に必要になるまで DB アクセスは走らない（遅延初期化）
        return Holder.INSTANCE;
    }

    // レート表は生成時に一度だけ作り、二度と差し替えない意図を final で表明する。
    // getInstance() から取得したインスタンスに対して呼ぶ。
    private final RateTable rateTable = loadFromDatabase();

    private RateTable loadFromDatabase() {
        RateTable table = new RateTable();
        table.load();
        return table;
    }

    public RateTable getRateTable() {
        // 呼び出し側の変更でレート表が汚れないよう、読み取り専用ビューを返すのが理想。
        // （RateTable 側に不変ビューを用意しておく想定。最低でも防御的コピーを検討する）
        return rateTable;
    }
}
