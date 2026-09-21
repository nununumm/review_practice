package com.example.member;

import java.util.Optional;

import org.springframework.stereotype.Service;

/**
 * 会員（Member）を ID で照会し、状態やプレミアム判定、表示名の取得を行うサービス。
 */
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * ID で会員を探す。見つからなければ会員なしとして扱う。
     */
    public Member findMember(Long id) {
        Optional<Member> opt = memberRepository.findById(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        return null;
    }

    /**
     * 会員が「有効（ACTIVE）」かどうかを判定する。
     */
    public boolean isActive(Long id) {
        Member member = findMember(id);
        return member.getStatus().equals("ACTIVE");
    }

    /**
     * プレミアム会員かどうかを判定する。
     * プレミアムフラグが立っているか、ランクが 3 の会員をプレミアム扱いにする。
     */
    public boolean isPremium(Long id) {
        Member member = findMember(id);
        if (member.getPremiumFlag() == true) {
            return true;
        }
        if (member.getRank() == 3) {
            return true;
        }
        return false;
    }

    /**
     * 2 人の会員のランクが同じかどうかを判定する。
     */
    public boolean haveSameRank(Long id1, Long id2) {
        Member m1 = findMember(id1);
        Member m2 = findMember(id2);
        return m1.getRank() == m2.getRank();
    }

    /**
     * 画面に表示する名前を返す。名前が未設定ならゲスト表示にする。
     */
    public String getDisplayName(Long id) {
        Member member = findMember(id);
        String name = member.getName();
        if (name != null) {
            return name;
        }
        return "ゲスト";
    }

    /**
     * 会員へのあいさつ文を組み立てる。
     */
    public String buildGreeting(Optional<Member> member) {
        return "こんにちは、" + member.get().getName() + "さん";
    }

    /**
     * 会員のメールアドレスを返す。
     */
    public String getEmail(Long id) {
        return memberRepository.findById(id).get().getEmail();
    }
}
