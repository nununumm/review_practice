package good;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ポイントを景品と交換するサービス（改善版）。
 *
 * 【bad の何が問題だったか】
 * bad ではメソッドに @Transactional を付けていたが、実は「安心」ではなかった。
 * Spring の @Transactional は、そのまま付けただけだと
 * 「RuntimeException（＝実行時例外。書かなくても投げられる例外）」と
 * 「Error（＝致命的な異常）」が飛んだときだけロールバック（＝やりかけを無かったことに戻す）する。
 * つまり OutOfStockException のような「チェック例外（＝コンパイラが catch / throws を強制する例外。
 * Exception を継承したもの）」では、自動でロールバックしてくれない。
 * その結果、① のポイント減算だけがコミット（＝確定保存）され、② の在庫処理は失敗、という
 * 「部分コミット」＝お客様のポイントだけ消えて景品はもらえない、という大事故になる。
 *
 * 【good の直し方】
 * (メイン) @Transactional(rollbackFor = Exception.class) と明示して、
 *          チェック例外が飛んでもきちんとロールバックさせる。
 * (別案)   そもそも業務エラーを RuntimeException を継承した例外にすれば、
 *          デフォルトのままロールバックされる（下の OutOfStockException のコメント参照）。
 *
 * ポイント：@Transactional は「付ければ効く魔法」ではない。
 * どの例外で巻き戻すか（rollbackFor）、読み取り専用か（readOnly）、
 * どう伝播するか（propagation）まで意識して初めて意図どおりに効く。
 */
@Service
public class PointExchangeService {

    // 依存する部品はコンストラクタで受け取る（＝コンストラクタインジェクション）
    private final MemberRepository memberRepository;
    private final RewardRepository rewardRepository;

    public PointExchangeService(MemberRepository memberRepository,
                                RewardRepository rewardRepository) {
        this.memberRepository = memberRepository;
        this.rewardRepository = rewardRepository;
    }

    // rollbackFor = Exception.class を明示：
    // チェック例外（OutOfStockException など）でも「全部無かったこと」に巻き戻す。
    @Transactional(rollbackFor = Exception.class)
    public void exchange(Long memberId, Long rewardId) throws OutOfStockException {
        // ① 会員と景品を取得する
        Member member = memberRepository.findById(memberId);
        Reward reward = rewardRepository.findById(rewardId);

        // ② 先に在庫を確認する（＝失敗する可能性のあるチェックは、更新の前に済ませておくと安全）
        if (reward.getStock() <= 0) {
            // ここで例外を投げても、rollbackFor のおかげでポイント減算まで含めて巻き戻る
            throw new OutOfStockException("景品の在庫が足りません: rewardId=" + rewardId);
        }

        // ③ ポイントを減らして保存する
        member.setPoint(member.getPoint() - reward.getRequiredPoint());
        memberRepository.save(member);

        // ④ 在庫を減らして保存する
        reward.setStock(reward.getStock() - 1);
        rewardRepository.save(reward);

        // ①〜④ はひとつのトランザクション（＝一連の処理を「全部成功か、全部無かったこと」にする仕組み）。
        // 途中で例外が飛べば、rollbackFor の指定により全部きれいに巻き戻る。
    }
}
