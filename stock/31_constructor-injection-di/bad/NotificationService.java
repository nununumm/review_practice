package bad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private NotificationHistoryRepository historyRepository;

    public void notifyMember(Long memberId, String message) {
        Member member = memberRepository.findById(memberId).get();

        mailSender.send(member.getEmail(), message);

        NotificationHistory history = new NotificationHistory();
        history.setMemberId(memberId);
        history.setMessage(message);
        historyRepository.save(history);
    }
}
