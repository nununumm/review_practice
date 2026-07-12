package com.example.shop.service;

import org.slf4j.Logger;                 // ログ出力の道具（System.out.println の代わり）
import org.slf4j.LoggerFactory;          // Logger を作るための工場
import org.springframework.stereotype.Service;

import java.time.Clock;                  // 「時計」を表す部品。now() の代わりに“外から渡せる時刻源”
import java.time.LocalTime;              // 「時刻（時:分:秒）」だけを表す型

/**
 * 商品の「支払い金額」を計算するサービス（模範解答版）。
 *
 * 【bad版からの主な改善】
 *   1. マジックナンバー(10,20,100…)を「名前付き定数」にした → 何の数字か一目で分かる
 *   2. System.out.println をやめて Logger にした → 本番のログ基盤に乗る／レベル制御できる
 *   3. 時刻・セール情報・会員ランクを「メソッドの中で自前調達」せず、
 *      コンストラクタで“外から受け取る”形（＝DI）にした → テストで差し替え可能になった
 */
@Service   // このクラスがサービス層（ビジネスロジック担当）だとSpringに知らせる注釈
public class PriceService {

    // ===== マジックナンバーを「名前付きの定数」にする（static final = 全員共通・変更不可の値） =====
    private static final int LATE_NIGHT_START_HOUR   = 22;   // 深夜割増の開始時刻（22時）
    private static final int LATE_NIGHT_END_HOUR     = 5;    // 深夜割増の終了時刻（5時）
    private static final int LATE_NIGHT_SURCHARGE_PCT = 10;  // 深夜割増率（10%）
    private static final int SALE_DISCOUNT_PCT        = 20;  // タイムセール割引率（20%）
    private static final int GOLD_DISCOUNT_YEN        = 100; // GOLD会員の割引額（100円）
    private static final String GOLD_RANK             = "GOLD"; // GOLD会員を表す文字列

    // ログ出力口。クラスに1つあれば十分なので static final で持つ
    private static final Logger logger = LoggerFactory.getLogger(PriceService.class);

    // ===== 必要な部品は「外から受け取って」フィールドに保持する（final=後から変わらない） =====
    private final Clock clock;                    // 時刻の供給源（テストでは“固定時計”を渡せる）
    private final SaleRepository saleRepository;  // セール情報の取得先（テストでは偽物を渡せる）
    private final MemberService memberService;    // 会員ランクの取得先（テストでは偽物を渡せる）

    /**
     * コンストラクタ。ここで必要な部品を“外から差し込む”＝DI（依存性の注入）。
     * Spring が起動時に、登録済みの Clock / SaleRepository / MemberService を自動で渡してくれる。
     */
    public PriceService(Clock clock, SaleRepository saleRepository, MemberService memberService) {
        this.clock = clock;                       // 渡された時計を保持
        this.saleRepository = saleRepository;     // 渡されたセール窓口を保持
        this.memberService = memberService;       // 渡された会員窓口を保持
    }

    /**
     * ベース価格に、時間帯・セール・会員ランクの割増/割引を適用して支払い金額を返す。
     *
     * @param basePrice 商品のベース価格（円）
     * @return 実際に支払う金額（円）
     */
    public int calculatePrice(int basePrice) {
        int price = basePrice;                    // 計算用の変数（ベース価格からスタート）

        // ★ now() を直接呼ばず、外から渡された clock から“今の時刻”を取り出す
        //   → テストでは「23時固定の時計」を渡せるので、深夜割増を安定して検証できる
        LocalTime now = LocalTime.now(clock);

        // 深夜なら割増（分子・分母を分けて「10%」の意味を明示）
        if (isLateNight(now)) {
            price += price * LATE_NIGHT_SURCHARGE_PCT / 100;
        }

        // 差し込まれた窓口に「今セール中？」と尋ねる（本物かテスト用の偽物かは呼ぶ側の関心事ではない）
        if (saleRepository.isSaleNow()) {
            price -= price * SALE_DISCOUNT_PCT / 100;
        }

        // 差し込まれた窓口に会員ランクを尋ね、GOLDなら定額割引
        //   equals は「定数.equals(相手)」の順で書くと、相手が null でも落ちない安全な書き方
        if (GOLD_RANK.equals(memberService.getCurrentRank())) {
            price -= GOLD_DISCOUNT_YEN;
        }

        // println ではなく logger。プレースホルダ {} を使うと文字列連結より軽く安全
        logger.info("価格計算 basePrice={} finalPrice={}", basePrice, price);

        return price;                             // 計算結果を返す
    }

    /**
     * 指定時刻が「深夜（22時以降 または 5時より前）」かどうかを判定する。
     * ロジックを小さなメソッドに切り出すと、条件式に名前が付いて読みやすく、単体でもテストしやすい。
     */
    private boolean isLateNight(LocalTime time) {
        return time.isAfter(LocalTime.of(LATE_NIGHT_START_HOUR, 0))   // 22時より後 か
                || time.isBefore(LocalTime.of(LATE_NIGHT_END_HOUR, 0)); // 5時より前
    }
}
