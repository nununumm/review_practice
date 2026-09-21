package bad;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class CampaignCalcService {

    // 目標金額に対する達成率(%)を計算する
    public int achievementRate(long actual, long target) {
        int rate = (int) (actual / target) * 100;
        return rate;
    }

    // 合計割引額 total を count 個の対象商品へ均等に按分する
    public List<Long> distributeDiscount(long total, int count) {
        List<Long> result = new ArrayList<>();
        long perItem = total / count;
        for (int i = 0; i < count; i++) {
            result.add(perItem);
        }
        return result;
    }
}
