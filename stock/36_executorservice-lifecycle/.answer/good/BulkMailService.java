package good;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

/**
 * 会員全員へお知らせメールを一斉送信するサービス（模範解答）。
 *
 * ■ bad のどこがダメだったか
 *   1) メソッドの中で毎回 Executors.newCachedThreadPool() を new していた。
 *      スレッドプール（＝作業員を待機させて使い回す仕組み）は本来 1つ作って使い回す資源。
 *      毎回作ると「プールにする意味」が無く、呼ぶたびに作業員の束が新しく生まれる。
 *   2) 作ったプールを shutdown（＝もう仕事は来ないので作業員を解散させる合図）していなかった。
 *      解散させないと作業員（スレッド）は仕事が終わっても居座り続ける。
 *      sendToAll を呼ぶたびに居残りが増え続け（スレッドリーク）、やがてアプリが終了できない／
 *      メモリを食いつぶして OOM（＝メモリ不足でアプリthat落ちること）に至る。
 *   3) newCachedThreadPool() は上限なし。来たタスクの数だけ作業員を無限に増やす。
 *      数万人へ一斉送信すれば作業員が爆発的に増えてサーバが悲鳴を上げる。
 *      （newFixedThreadPool(n) も内部の順番待ち行列が無制限で、タスクが溜まり OOM の恐れ。）
 *   4) submit の戻り値 Future を捨てていた。タスクの中で例外が起きても Future を見ないと
 *      握りつぶされ、「誰にメールが失敗したか」が一切分からない。
 *
 * ■ good の方針
 *   - スレッドプールは Spring の Bean（＝アプリで1つだけ用意して共有する部品）として
 *     DI（＝必要な部品を外から渡してもらう仕組み）で受け取り、毎回 new しない。
 *   - ライフサイクル（＝生成から後始末まで）は Spring に任せる。ThreadPoolTaskExecutor は
 *     アプリ終了時に Spring が自動で shutdown してくれるので、自分で解散処理を書かなくてよい。
 *   - プールは上限つき・順番待ち行列も有限で用意する（設定クラス側で定義）。あふれたら
 *     呼び出しスレッド自身に実行させる CallerRunsPolicy（＝手が空くまで新規受付を抑える安全弁）。
 *   - 各タスクの結果・例外を CompletableFuture で集約し、失敗した宛先をログに残す。
 */
@Service
public class BulkMailService {

    // メール送信部品。テストで差し替えられるよう外から受け取る（DI）。
    private final MailSender mailSender;

    // アプリで共有する1つのスレッドプール。毎回 new せず、これを使い回す。
    private final Executor mailExecutor;

    /**
     * コンストラクタで依存を受け取る（コンストラクタインジェクション）。
     * mailExecutor はどのプールを使うか名前で指定して受け取る。
     */
    public BulkMailService(MailSender mailSender,
                           @Qualifier("mailExecutor") ThreadPoolTaskExecutor mailExecutor) {
        this.mailSender = mailSender;   // 送信部品を保持
        this.mailExecutor = mailExecutor; // 共有プールを保持
    }

    /**
     * 会員リスト全員へメールを送る。
     * 共有プールに投げ、全タスクの完了を待って、失敗があればまとめて報告する。
     */
    public void sendToAll(List<Member> members) {
        // 各会員の送信タスクを CompletableFuture（＝あとで結果を受け取れる非同期の約束）にする。
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Member m : members) {
            // runAsync に共有プールを渡す。プールを毎回作らないのがポイント。
            CompletableFuture<Void> future = CompletableFuture.runAsync(
                    () -> mailSender.send(m.getEmail(), "お知らせ", "いつもご利用ありがとうございます。"),
                    mailExecutor
            ).exceptionally(ex -> {
                // タスク内で例外が出ても握りつぶさず、どの宛先が失敗したかを記録する。
                System.err.println("メール送信失敗: " + m.getEmail() + " / 原因: " + ex.getMessage());
                return null; // 1件の失敗で全体を止めないよう、ここで飲み込んでログ化する。
            });
            futures.add(future);
        }

        // 全タスクの完了を待つ（呼び出し元が「送り終わった」と分かるように）。
        try {
            CompletableFuture
                    .allOf(futures.toArray(new CompletableFuture[0])) // すべての約束をまとめる
                    .join(); // 全部終わるまで待機
        } catch (CompletionException e) {
            // ここに来るのは想定外の失敗のみ。個別失敗は上で処理済み。
            System.err.println("一斉送信の待機中に想定外のエラー: " + e.getMessage());
        }
    }
}
