package bad;

import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class MemberQueryService {

    private final MemberRepository memberRepository;

    public MemberQueryService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public String displayName(Long memberId) {
        Optional<Member> member = memberRepository.findById(memberId);
        if (member.isPresent()) {
            if (member.get().getNickname() != null) {
                return member.get().getNickname();
            } else {
                return "ゲスト";
            }
        }
        return "ゲスト";
    }

    public String greeting(Optional<Member> member) {
        return "こんにちは、" + member.get().getName() + " さん";
    }
}
