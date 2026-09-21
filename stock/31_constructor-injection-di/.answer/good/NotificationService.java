package good;

import org.springframework.stereotype.Service;

/**
 * 会員通知サービスの依存の受け取り方。
 *
 * bad 版は、3つの依存（会員リポジトリ・メール送信・履歴リポジトリ）を
 * すべて「フィールドインジェクション」（＝フィールドに @Autowired を直付けして、
 * Spring にこっそり差し込んでもらう受け取り方）で受け取っていた。これには
 *  ・コンストラクタ（＝オブジェクトを作るときの入口）を見ても「何が必要か」が分からない（隠れた依存）
 *  ・フィールドを final（＝一度入れたら差し替え不可）にできず、あとから書き換わりうる
 *  ・Spring 無しで new して単体テストできない（依存が null のまま → ぬるぽ）
 *  ・依存が増えても気付きにくく、クラスが肥大化しても警告が出ない
 *  ・循環参照（＝お互いがお互いを必要とする関係）が実行時まで隠れる
 * という弱点があった。
 *
 * good 版では「コンストラクタインジェクション」（＝必要な部品をコンストラクタの
 * 引数で受け取る受け取り方）に統一し、フィールドは private final にする。
 * こうすると「このクラスは何が必要か」がコンストラクタ1本を見れば一目で分かり、
 * new でモック（＝本物の代わりの偽物部品）を渡すだけでテストできる。
 */
@Service
public class NotificationService {

    // final（＝作った後は差し替えられない）にすることで「必須で不変な依存」だと表明できる。
    // フィールドに @Autowired は付けない。受け取りはコンストラクタに一本化する。
    private final MemberRepository memberRepository;
    private final MailSender mailSender;
    private final NotificationHistoryRepository historyRepository;

    /**
     * コンストラクタで必要な部品を全部受け取る。
     * Spring 4.3 以降は「コンストラクタが1本だけ」なら @Autowired を書かなくても
     * Spring が自動でここへ部品を差し込んでくれる（＝省略可）。
     * 引数の並びを見れば「このクラスは3つの部品に依存している」と即座に読み取れる。
     */
    public NotificationService(MemberRepository memberRepository,
                               MailSender mailSender,
                               NotificationHistoryRepository historyRepository) {
        // 受け取った部品を final フィールドへ詰める。以降このクラスの中では差し替わらない
        this.memberRepository = memberRepository;
        this.mailSender = mailSender;
        this.historyRepository = historyRepository;
    }

    public void notifyMember(Long memberId, String message) {
        // 存在チェックを orElseThrow に。会員がいなければ「いない」と明示的に例外で知らせる
        // （bad 版の get() は、いないと中身が空のまま謎の NoSuchElementException で落ちていた）
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("会員が見つかりません: id=" + memberId));

        // メールを送る（依存はコンストラクタで受け取った本物 or テスト時はモック）
        mailSender.send(member.getEmail(), message);

        // 送信履歴を残す
        NotificationHistory history = new NotificationHistory();
        history.setMemberId(memberId);
        history.setMessage(message);
        historyRepository.save(history);
    }
}
