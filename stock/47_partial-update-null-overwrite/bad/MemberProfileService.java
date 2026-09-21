package bad;

import org.springframework.stereotype.Service;

@Service
public class MemberProfileService {

    private final MemberRepository memberRepository;

    public MemberProfileService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member updateProfile(Long memberId, ProfileUpdateRequest req) {
        Member member = memberRepository.findById(memberId).get();

        member.setNickname(req.getNickname());
        member.setPhone(req.getPhone());
        member.setEmail(req.getEmail());
        member.setBirthday(req.getBirthday());

        return memberRepository.save(member);
    }
}
