package good;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

/**
 * 会員IDの一覧と申込者の一覧を突き合わせ、「新規」と「既存」に振り分けるサービス。
 *
 * bad 版では「List を毎回なめて探す」二重ループになっていて、
 * 会員数 n × 申込数 m の回数だけ比較が走っていた（＝O(n×m)、実質 O(n²)）。
 * good 版では、探す側を最初に HashSet（＝ハッシュで一発引きできる集合）に入れておくので、
 * 1件ずつの存在チェックが実質1回で済む（＝O(1)）。全体でも申込数に比例するだけ（＝O(n)）で、
 * 件数が数万件に増えても速度が急激に落ちなくなる。
 */
@Service
public class MemberMatchingService {

    /**
     * 既存会員IDの一覧と申込者の一覧を受け取り、新規／既存に振り分けて返す。
     *
     * @param existingMemberIds 既存会員IDの一覧
     * @param applicants        申込者の一覧
     * @return 新規申込者と既存申込者に分けた結果
     */
    public MatchingResult match(List<Long> existingMemberIds, List<Applicant> applicants) {
        // null を黙って NullPointerException にせず、はっきり例外で知らせる
        if (existingMemberIds == null || applicants == null) {
            throw new IllegalArgumentException("会員IDリストと申込者リストは必須です");
        }

        // 【核心】探される側を先に HashSet に入れておく。
        // List.contains は先頭から順に全部見る（＝O(n)：件数に比例して遅くなる）が、
        // HashSet.contains は中身を直接引き当てる（＝O(1)：件数が増えても速さが変わらない）。
        // この1行で全体の計算量が O(n×m) から O(n+m) に変わる。
        Set<Long> existingIdSet = new HashSet<>(existingMemberIds);

        List<Applicant> newApplicants = new ArrayList<>();
        List<Applicant> existingApplicants = new ArrayList<>();

        // 「既に既存として追加済みか」を調べるのも List でなめると遅い。
        // 見たIDを Set に覚えておけば、重複チェックも O(1) で済む。
        Set<Long> seenExistingIds = new HashSet<>();

        for (Applicant applicant : applicants) {
            long memberId = applicant.getMemberId();

            // Set への問い合わせは実質1回。ここが二重ループから解放された部分
            if (existingIdSet.contains(memberId)) {
                // まだ既存リストに入れていないIDだけ追加する（重複除去も Set で高速に判定）
                if (seenExistingIds.add(memberId)) {
                    // add は「新しく追加できたら true」。true のときだけ結果にも足す
                    existingApplicants.add(applicant);
                }
            } else {
                newApplicants.add(applicant);
            }
        }

        return new MatchingResult(newApplicants, existingApplicants);
    }
}

/**
 * 申込者を表す入れ物。ここでは説明用に同じファイルへまとめている。
 */
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

/**
 * 突合結果（新規／既存）を持ち運ぶための入れ物。
 */
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
