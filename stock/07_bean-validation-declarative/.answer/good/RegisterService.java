import org.springframework.stereotype.Service;

/**
 * 会員登録の業務ロジックだけを担当するサービス。
 *
 * ポイント：入力チェックは境界（Controller + Bean Validation）で済ませてある前提なので、
 * ここには「検証のif」が一切ない。やることは「会員を作って保存する」だけに絞られ、
 * 読みやすくテストもしやすい（＝検証と業務ロジックの分離）。
 */
@Service
public class RegisterService {

    private final MemberRepository memberRepository;

    public RegisterService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 検証済みのリクエストから会員を作って保存する
    public Member register(RegisterRequest request) {
        // リクエストの内容を会員エンティティに詰め替える
        Member member = new Member();
        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setAge(request.getAge());
        // 実際の現場ではパスワードはハッシュ化して保存する（平文保存はNG）
        member.setPassword(request.getPassword());

        // 保存して、保存後の会員（採番されたIDなど）を返す
        return memberRepository.save(member);
    }
}
