package bad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberPointService {

    @Autowired
    private MemberPointRepository repository;

    /**
     * 会員のランクIDがキャンペーン対象ランクと一致するか判定する。
     */
    public boolean isTargetRank(Integer rankId, Integer targetRankId) {
        return rankId == targetRankId;
    }

    /**
     * 会員の保有ポイント合計がしきい値(1000)以上か判定する。
     */
    public boolean hasEnoughPoints(Long memberId) {
        int total = repository.sumPoints(memberId);
        return total >= 1000;
    }

    /**
     * プレミアム会員かどうかを判定する。
     */
    public boolean isPremium(Long memberId) {
        Boolean flag = repository.findPremiumFlag(memberId);
        if (flag) {
            return true;
        }
        return false;
    }
}
