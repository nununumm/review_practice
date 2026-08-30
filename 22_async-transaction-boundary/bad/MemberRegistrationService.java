package com.example.member.service;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
//コンストラクタを省略するアノテーション
@RequiredArgsConstructor
public class MemberRegistrationService {

    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;
    private final SignUpStatisticsRepository statisticsRepository;
    private final MailSender mailSender;

    /** 会員を登録し、採番された会員IDを返す */
    public Long register(MemberRegisterRequest request) {

        Member member = new Member();
        member.setEmail(request.getEmail());
        member.setName(request.getName());
        member.setStatus("ACTIVE");
        memberRepository.save(member);

        // 新規登録特典のクーポンを付与する
        grantSignUpCoupon(member.getId());

        // ウェルカムメールは時間がかかるので非同期で送る
        sendWelcomeMail(member.getId());

        // 日次の登録件数も裏で更新しておく
        new Thread(() -> statisticsRepository.incrementDailySignUp(LocalDate.now())).start();

        return member.getId();
    }
    
    @Transactional
    public void grantSignUpCoupon(Long memberId) {
        Coupon coupon = new Coupon();
        coupon.setMemberId(memberId);
        coupon.setCode("WELCOME1000");
        coupon.setDiscountAmount(1000);
        couponRepository.save(coupon);

        Member member = memberRepository.findById(memberId).get();
        member.setCouponIssued(true);
        memberRepository.save(member);
    }

    @Async
    public void sendWelcomeMail(Long memberId) {
        try {
            Member member = memberRepository.findById(memberId).get();
            mailSender.send(member.getEmail(),
                    "ご登録ありがとうございます",
                    member.getName() + " 様、1000円クーポンをお送りしました。");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
