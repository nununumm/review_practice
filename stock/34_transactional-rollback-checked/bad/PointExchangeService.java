package bad;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ポイントを景品と交換するサービス。
 * ① 会員のポイント残高を減らして保存する
 * ② 景品の在庫を1つ減らす（在庫不足なら例外を投げる）
 */
@Service
public class PointExchangeService {

    private final MemberRepository memberRepository;
    private final RewardRepository rewardRepository;

    public PointExchangeService(MemberRepository memberRepository,
                                RewardRepository rewardRepository) {
        this.memberRepository = memberRepository;
        this.rewardRepository = rewardRepository;
    }

    @Transactional
    public void exchange(Long memberId, Long rewardId) throws OutOfStockException {
        // ① 会員を取得してポイントを減らし、保存する
        Member member = memberRepository.findById(memberId);
        Reward reward = rewardRepository.findById(rewardId);

        member.setPoint(member.getPoint() - reward.getRequiredPoint());
        memberRepository.save(member);

        // ② 在庫を確認して減らす。足りなければ業務エラーを投げる
        if (reward.getStock() <= 0) {
            throw new OutOfStockException("景品の在庫が足りません: rewardId=" + rewardId);
        }
        reward.setStock(reward.getStock() - 1);
        rewardRepository.save(reward);
    }
}
