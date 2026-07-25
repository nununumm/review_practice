package com.example.campaign;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * 抽選キャンペーンの応募を受け付けるサービス（改善版）。
 *
 * ＜設計方針＞
 *  ・「1ユーザー1回だけ」という“本当に守りたいルール”は、データの最終的な置き場所である
 *    DB のユニーク制約に守らせる（アプリのメモリでは守らない）。
 *  ・そのため、このクラスは「状態（フィールド）」をいっさい持たない。
 *    → 状態を持たない＝どのスレッドが同時に呼んでも壊れる共有物がない＝スレッドセーフ。
 */
@Service
public class CampaignApplicationService {

    // 依存はコンストラクタで受け取る（DI）。@Autowired フィールド注入ではなく、
    //   final + コンストラクタにすることで「必ず渡される・後から差し替わらない」を保証できる。
    private final CampaignRepository campaignRepository;

    public CampaignApplicationService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    // ★bad版から消えたもの★
    //   private Map<Long, Boolean> appliedUsers ... → 削除（DBが「応募済みか」の真実を持つ）
    //   private int totalApplications ...           → 削除（合計はDBに count() させる）
    //   どちらも「複数スレッドが同時に触る共有の箱」で、競合状態（レースコンディション）の原因だった。

    public String apply(Long userId) {
        try {
            // ★発想の逆転★
            //   bad版：「応募済み？」を先に確認 → その“すきま”に別スレッドが割り込めた（Check-Then-Act）。
            //   good版：確認せず、まず保存を試みる。DBの user_id ユニーク制約が門番なので、
            //           同じ人の2件目は必ずDBが弾いてくれる（＝分解できない1動作で守られる）。
            campaignRepository.save(new Application(userId));

        } catch (DataIntegrityViolationException e) {
            // DataIntegrityViolationException = 「DBの整合性ルール（ここではユニーク制約）に違反した」
            //   ときに Spring が投げる例外。＝“もうこの人は応募済みだった”ということ。
            //   2つのリクエストが同時に走って両方が保存を試みても、DBが1件だけ通し、
            //   もう1件をここに落としてくれる。だから安全に「すでに応募済み」と返せる。
            return "すでに応募済みです";
        }

        // 合計応募数は「今この瞬間」をDBに数えさせる。メモリで自前カウントしないのでズレない。
        long totalApplications = campaignRepository.count();
        return "応募を受け付けました（現在の応募数: " + totalApplications + "）";
    }
}
