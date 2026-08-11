package good;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

// 口座間の送金を行うサービス
@Service
@RequiredArgsConstructor
public class TransferService {

    // System.out.println ではなくロガーを使う（出力レベル・出力先を制御でき、本番運用に耐える）
    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    // 手数料率 0.5%。マジックナンバーを直書きせず、意味のある名前を付けて1か所に集約する。
    // （本来は application.yml 等の設定ファイルに外出しできるとさらに良い）
    private static final BigDecimal FEE_RATE = new BigDecimal("0.005");

    private final AccountRepository accountRepository;

    // fromId の口座から toId の口座へ amount 円を送金する。
    // 送金額の 0.5% を手数料として送金元から追加で引く。
    //
    // @Transactional：このメソッド内のDB操作を「全部成功 or 全部なかったこと」にまとめる。
    //   途中で例外が飛べば、それまでの出金も自動でロールバックされる（＝お金が片方だけ動く事故を防ぐ）。
    @Transactional
    public void transfer(Long fromId, Long toId, long amount) {
        // ── 入力バリデーション（すぐ弾けるおかしな指示は、DBに触る前に弾く）──
        if (amount <= 0) {
            // マイナス額を許すと「引くつもりが増える」など致命的なので必ず弾く
            throw new InvalidTransferException("送金額は1円以上にしてください: amount=" + amount);
        }
        if (fromId.equals(toId)) {
            // 自分自身への送金は無意味（手数料だけ取られる事故にもなる）
            throw new InvalidTransferException("送金元と送金先が同じです: id=" + fromId);
        }

        // ── 口座を悲観ロック付きで取得する ──
        // 【デッドロック回避】常に「ID の小さい口座から先に」ロックを取る。
        //   A→B と B→A の送金が同時に起きても、ロックを取る順番を全員で揃えれば
        //   「お互いが相手のロック解放を待ち続ける」デッドロックを避けられる。
        Account from;
        Account to;
        if (fromId < toId) {
            from = findForUpdate(fromId);
            to = findForUpdate(toId);
        } else {
            to = findForUpdate(toId);
            from = findForUpdate(fromId);
        }

        // ── 手数料を計算する（円未満の端数は「切り捨て」と明示的に決める）──
        long fee = calculateFee(amount);
        long totalDebit = amount + fee;   // 送金元から引く合計（送金額＋手数料）

        // ── 残高チェック（ロック中に確認するので、この後で他者に抜かれない）──
        if (from.getBalance() < totalDebit) {
            throw new InsufficientBalanceException(
                    "残高不足です: balance=" + from.getBalance() + ", required=" + totalDebit);
        }

        // ── 残高を更新する ──
        // 悲観ロックで両口座を押さえているので、この「読む→計算→書く」に他者は割り込めない（lost update を防止）。
        from.setBalance(from.getBalance() - totalDebit);
        to.setBalance(to.getBalance() + amount);
        // ※ @Transactional 内で管理中のエンティティは、変更が自動で検知・反映される（ダーティチェック）。
        //   そのため save() を何度も呼ぶ必要はない。commit 時に必要なUPDATEだけがまとめて走る。

        log.info("送金完了: from={}, to={}, amount={}, fee={}", fromId, toId, amount, fee);
    }

    // 口座を悲観ロック付きで取得し、無ければ意味のある例外を投げる（.get() で握りつぶさない）
    private Account findForUpdate(Long id) {
        return accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AccountNotFoundException("口座が見つかりません: id=" + id));
    }

    // 手数料 = 送金額 × 0.5%。円未満は切り捨て(FLOOR)すると明示する。
    // 計算だけ BigDecimal を使い、結果は long（円）に戻す。
    private long calculateFee(long amount) {
        return BigDecimal.valueOf(amount)      // 送金額を正確な小数型にする
                .multiply(FEE_RATE)            // × 0.005
                .setScale(0, RoundingMode.FLOOR)  // 小数点以下を切り捨てて「円」に丸める
                .longValueExact();             // long に変換（あふれたら例外で気づける）
    }
}
