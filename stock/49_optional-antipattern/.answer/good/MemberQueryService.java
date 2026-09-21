package good;

import org.springframework.stereotype.Service;

/**
 * 会員の表示名を組み立てるサービス（修正版）。
 *
 * bad 版は Optional を使ってはいたが、使い方が「昔ながらの null チェック」のままだった。
 *  ・isPresent() で確認して get() で取り出す、を繰り返すのは Optional の意味が無い
 *    （if (x != null) x.foo() と同じ冗長さで、get() の呼び忘れ・二度呼びのミスも招く）。
 *  ・さらに greeting() は「引数」に Optional を取っていた。これはアンチパターン。
 *    引数の Optional は「null かもしれない Optional」という二重の曖昧さを生むし、
 *    中で get() すると中身が無いとき NoSuchElementException で落ちる。
 *
 * Optional は map / filter / orElse をつないで「値があれば〜、無ければ既定値」を
 * 宣言的に書くための道具。get()/isPresent() はできるだけ使わない。
 */
@Service
public class MemberQueryService {

    private static final String GUEST = "ゲスト";

    private final MemberRepository memberRepository;

    public MemberQueryService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public String displayName(Long memberId) {
        // 「会員を探す → ニックネームを取り出す → 無ければ "ゲスト"」を1本の流れで表現する。
        //   map(...)    : 中身があれば変換、無ければ空のまま素通り
        //   orElse(...) : 最終的に空なら既定値を返す
        // これで isPresent()/get() の入れ子や "ゲスト" の重複がなくなる。
        return memberRepository.findById(memberId)
                .map(Member::getNickname)   // Member → ニックネーム（null ならこの後 orElse で拾う）
                .orElse(GUEST);
    }

    /**
     * 引数は Optional ではなく「実体」で受け取る。
     * 「会員が確実にいる」ことは呼び出し側で保証し、このメソッドは中身を素直に使う。
     */
    public String greeting(Member member) {
        // null で呼ばれる設計にしない。必要なら requireNonNull で契約を明示してもよい。
        return "こんにちは、" + member.getName() + " さん";
    }
}
