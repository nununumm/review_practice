package com.example.member;

import java.util.Objects;                                   // null 安全に「中身が等しいか」を比べるためのユーティリティ
import java.util.Optional;                                  // 「値があるかもしれない／ないかもしれない」を表す箱

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;                // hasText（null でも空文字でもない文字か）を使うため

/**
 * 会員（Member）を ID で照会し、状態やプレミアム判定、表示名の取得を行うサービス。
 *
 * 【bad/ からの主な改善】
 *   1. 「見つからないとき null を返す」設計をやめた。
 *      ・そもそも探すだけの findMember は Optional を返す（＝空かもしれないことを型で表す）。
 *      ・「いて当然」の処理用に、いなければ例外を投げる getMember を用意した。
 *   2. 文字列比較は "定数".equals(...) の順にして、status が null でも NPE にならないようにした。
 *   3. ラッパ型（Boolean / Integer）の比較を安全にした。
 *      ・Boolean は Boolean.TRUE.equals(...) で null でも false 扱い。
 *      ・Integer は == ではなく Objects.equals(...) で「中身」を比べる（Integer キャッシュの罠を回避）。
 *   4. Optional を引数に取る誤用をやめ、素直に Member を受け取るようにした。
 *   5. Optional は get() で取り出さず、orElseThrow / map など安全な方法で扱う。
 *   6. 空文字("") と null をまとめて「未設定」とみなす（StringUtils.hasText）。
 */
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    // コンストラクタで依存（Repository）を受け取る＝コンストラクタインジェクション。
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * ID で会員を探す。「いないかもしれない」ことを Optional で正直に表す。
     * → 呼び出し側は空チェックを“型で”強制されるので、null チェック忘れによる NPE が起きない。
     */
    public Optional<Member> findMember(Long id) {
        return memberRepository.findById(id);
    }

    /**
     * ID で会員を取得する。「いて当然」の場面ではこちらを使う。
     * 見つからなければ null を返さず、独自例外を投げて呼び出し側に明確に知らせる。
     */
    public Member getMember(Long id) {
        // orElseThrow ＝「中身があればそれを返す／なければ例外を投げる」を1行で書ける安全な取り出し方。
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("会員が見つかりません。id=" + id));
    }

    /**
     * 会員が「有効（ACTIVE）」かどうかを判定する。
     */
    public boolean isActive(Long id) {
        Member member = getMember(id);
        // 定数側から .equals を呼ぶので、status が null でも NPE にならず false になる。
        return "ACTIVE".equals(member.getStatus());
    }

    /**
     * プレミアム会員かどうかを判定する。
     * プレミアムフラグが立っているか、ランクが 3 の会員をプレミアム扱いにする。
     */
    public boolean isPremium(Long id) {
        Member member = getMember(id);
        // Boolean.TRUE.equals(...) なら、フラグが null でも例外にならず false になる。
        if (Boolean.TRUE.equals(member.getPremiumFlag())) {
            return true;
        }
        // Integer どうしは Objects.equals で「中身」を比較（null も安全に扱える）。
        return Objects.equals(member.getRank(), 3);
    }

    /**
     * 2 人の会員のランクが同じかどうかを判定する。
     */
    public boolean haveSameRank(Long id1, Long id2) {
        Member m1 = getMember(id1);
        Member m2 = getMember(id2);
        // == だと Integer の“キャッシュの罠”で 128 以上の値が false になりうる。
        // Objects.equals なら中身で比較でき、両方 null のときも正しく true になる。
        return Objects.equals(m1.getRank(), m2.getRank());
    }

    /**
     * 画面に表示する名前を返す。名前が未設定（null または空文字）ならゲスト表示にする。
     */
    public String getDisplayName(Long id) {
        Member member = getMember(id);
        // hasText は「null でも 空文字 でも 空白だけ でもない＝中身のある文字」のときだけ true。
        if (StringUtils.hasText(member.getName())) {
            return member.getName();
        }
        return "ゲスト";
    }

    /**
     * 会員へのあいさつ文を組み立てる。
     * Optional を引数にせず、確実に存在する Member を受け取る（呼び出し側で取得しておく）。
     */
    public String buildGreeting(Member member) {
        return "こんにちは、" + member.getName() + "さん";
    }

    /**
     * 会員のメールアドレスを返す。
     * Optional から get() で無理に取り出さず、map + orElseThrow で安全に取り出す。
     */
    public String getEmail(Long id) {
        return memberRepository.findById(id)
                .map(Member::getEmail)  // 会員がいればメールに変換、いなければ空のまま
                .orElseThrow(() -> new MemberNotFoundException("会員が見つかりません。id=" + id));
    }
}
