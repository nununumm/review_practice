package com.example.member.service;

import org.slf4j.Logger;                                              // ログ出力の道具（printStackTraceの代わり）
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;              // 別スレッドで動かす印
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;      // トランザクションのどの段階で動かすか
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

/**
 * ウェルカムメールを送る専用のリスナー（＝出来事を受け取って処理する係）。
 *
 * ポイント（bad版の問題をどう解決しているか）：
 *  - {@link MemberRegistrationService} とは "別のBean" なので、@Async がプロキシを通って本当に非同期になる
 *    （bad版は同じクラス内から呼んでいたため @Async が効かず、実は同期実行だった）。
 *  - @TransactionalEventListener(AFTER_COMMIT) により、会員登録が"コミット確定した後だけ"送信する。
 *    → 登録がロールバックされたのに「登録ありがとう」メールが飛ぶ事故を防ぐ。
 *    → 別スレッドがコミット前の会員を読みにいって「見つからない」となるレースも起きない。
 *  - 例外はここで受け止めてロガーに記録する。メール送信は"やり直せる周辺処理"であり、
 *    すでに確定した会員登録を巻き戻す理由にはならないため（ただし握りつぶさず必ずログに残す）。
 */
@Component
@RequiredArgsConstructor
public class WelcomeMailListener {

    private static final Logger log = LoggerFactory.getLogger(WelcomeMailListener.class);

    private final MemberRepository memberRepository;
    private final MailSender mailSender;

    @Async // 呼び出し元を待たせず、設定した専用スレッドプール（AsyncConfig参照）の上で実行する
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // コミット成功後にだけ発火
    public void onMemberRegistered(MemberRegisteredEvent event) {
        try {
            // get() ではなく orElseThrow で「いない場合」を明示的に扱う（存在チェック）
            Member member = memberRepository.findById(event.memberId())
                    .orElseThrow(() -> new IllegalStateException(
                            "会員が見つかりません memberId=" + event.memberId()));

            mailSender.send(member.getEmail(),
                    "ご登録ありがとうございます",
                    member.getName() + " 様、1000円クーポンをお送りしました。");

        } catch (Exception e) {
            // 握りつぶさない：原因(e)ごとログに残す。監視・再送の判断材料になる
            log.error("ウェルカムメールの送信に失敗しました memberId={}", event.memberId(), e);
        }
    }
}
