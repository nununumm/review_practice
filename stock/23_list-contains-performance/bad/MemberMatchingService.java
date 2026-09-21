package bad;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class MemberMatchingService {

    public MatchingResult match(List<Long> existingMemberIds, List<Applicant> applicants) {

        List<Applicant> newApplicants = new ArrayList<>();
        List<Applicant> existingApplicants = new ArrayList<>();

        for (Applicant applicant : applicants) {

            // 既存会員IDの一覧に申込者のIDが含まれているか調べる
            boolean found = false;
            for (int i = 0; i < existingMemberIds.size(); i++) {
                if (existingMemberIds.get(i).longValue() == applicant.getMemberId()) {
                    found = true;
                    break;
                }
            }

            if (found == true) {
                // 既存リストに重複が無いか、都度なめてから追加する
                boolean already = false;
                for (Applicant e : existingApplicants) {
                    if (e.getMemberId() == applicant.getMemberId()) {
                        already = true;
                    }
                }
                if (already == false) {
                    existingApplicants.add(applicant);
                }
            } else {
                newApplicants.add(applicant);
            }
        }

        return new MatchingResult(newApplicants, existingApplicants);
    }
}

class Applicant {
    private final long memberId;
    private final String name;

    Applicant(long memberId, String name) {
        this.memberId = memberId;
        this.name = name;
    }

    long getMemberId() {
        return memberId;
    }

    String getName() {
        return name;
    }
}

class MatchingResult {
    private final List<Applicant> newApplicants;
    private final List<Applicant> existingApplicants;

    MatchingResult(List<Applicant> newApplicants, List<Applicant> existingApplicants) {
        this.newApplicants = newApplicants;
        this.existingApplicants = existingApplicants;
    }

    List<Applicant> getNewApplicants() {
        return newApplicants;
    }

    List<Applicant> getExistingApplicants() {
        return existingApplicants;
    }
}
