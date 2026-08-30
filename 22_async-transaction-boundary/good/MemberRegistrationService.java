package com.example.member.service;

import java.time.Clock;                                             // 「時計」そのものを表す型（テストで固定できる）
import java.time.LocalDate;

import org.springframework.context.ApplicationEventPublisher;       // イベント（出来事）を発行する道具
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 会員を登録し、後続処理（クーポン付与・集計・ウェルカムメール）を行うサービス（改善版）。
 *
 * bad版からの主な改善：
 *  - register() 自身に @Transactional を付け、「会員保存＋クーポン付与＋集計」を1つのトランザクションに束ねる
 *    → 途中でコケたら全部ロールバック。「クーポンだけ残る」ような部分コミットを防ぐ。
 *  - クーポン付与は同一クラスの private メソッドとして"同じトランザクション内"で実行する
 *    （bad版は @Transactional を自己呼び出ししていたため、プロキシを通らず効いていなかった）。
 *  - 取り消せない副作用（メール送信）は「イベント」として予約するだけにし、
 *    実際の送信は別Beanが "コミット確定後(AFTER_COMMIT)" に非同期で行う（{@link WelcomeMailListener}）。
 *  - 集計の更新に new Thread() を使わない（無制限なスレッド生成をやめる）。
 *  - 現在日付は Clock を注入して取得し、テストで日付を固定できるようにする。
 */
@Service
// コンストラクタ（依存を受け取る入り口）を自動生成するLombokのアノテーション
@RequiredArgsConstructor
public class MemberRegistrationService {

    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;
    private final SignUpStatisticsRepository statisticsRepository;
    private final ApplicationEventPublisher eventPublisher; // 「会員登録された」という出来事を発行する
    private final Clock clock;                              // 現在日時の供給源（テスト時は固定した時計を注入）

    // 新規登録特典クーポンの内容。ソース直書き（マジック値）をやめ、設定として名前を付けて外に出す
    private static final String WELCOME_COUPON_CODE = "WELCOME1000";
    private static final int WELCOME_COUPON_AMOUNT = 1000;

    /**
     * 会員を登録し、採番された会員IDを返す。
     * このメソッド全体が1つのトランザクション（＝全部成功 or 全部なかったことに）。
     * 外部（Controller）から呼ばれるのでプロキシを通り、@Transactional が正しく効く。
     */
    @Transactional
    public Long register(MemberRegisterRequest request) {

        // 業務ルールのチェック（DBを見て判断する＝サービス層の責務）。メール重複は登録させない
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("すでに登録済みのメールアドレスです: " + request.getEmail());
        }

        // 1) 会員レコードを保存
        Member member = new Member();
        member.setEmail(request.getEmail());
        member.setName(request.getName());
        member.setStatus(MemberStatus.ACTIVE); // マジック文字列"ACTIVE"をやめてenumで型安全に
        memberRepository.save(member);

        // 2) 新規登録特典クーポンを付与（同じトランザクション内で実行＝会員保存と運命を共にする）
        grantSignUpCoupon(member);

        // 3) 日次の登録件数を加算（これも登録の一部としてDBに書く。new Thread は使わない）
        statisticsRepository.incrementDailySignUp(LocalDate.now(clock));

        // 4) ウェルカムメールは"取り消せない副作用"。ここでは「登録された」という出来事を予約するだけ。
        //    実際の送信は WelcomeMailListener がコミット確定後に非同期で行う。
        //    → こうすればロールバック時にメールが飛ばない／レスポンスも待たされない。
        eventPublisher.publishEvent(new MemberRegisteredEvent(member.getId()));

        return member.getId();
    }

    /**
     * 新規登録特典クーポンを付与する。
     * private かつ register() の中から呼ばれるので、register() のトランザクションにそのまま含まれる
     * （別Beanにして @Transactional を付ける必要はない＝同一トランザクションでよいから）。
     */
    private void grantSignUpCoupon(Member member) {
        Coupon coupon = new Coupon();
        coupon.setMemberId(member.getId());
        coupon.setCode(WELCOME_COUPON_CODE);
        coupon.setDiscountAmount(WELCOME_COUPON_AMOUNT);
        couponRepository.save(coupon);

        // 会員側の「クーポン発行済み」フラグを立てる。findById で取り直す必要はない（すでに member を持っている）
        member.setCouponIssued(true);
        memberRepository.save(member);
    }
}
