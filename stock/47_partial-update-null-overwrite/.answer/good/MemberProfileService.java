package good;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会員プロフィールの部分更新（PATCH）サービス（修正版）。
 *
 * bad 版の最大の罠は「部分更新なのに、送られてこなかった項目まで null で上書き」していたこと。
 *   例）ユーザーがニックネームだけ変えたくてニックネームだけ送る
 *       → phone / email / birthday は req では null
 *       → member.setPhone(null) ... と既存データを片っ端から消してしまう（データ消失）
 *
 * 部分更新では「値が入っている項目だけ」を反映するのが正解。
 * good 版では、各項目が null（＝今回は変更しない）でないときだけセットする。
 */
@Service
public class MemberProfileService {

    private final MemberRepository memberRepository;

    public MemberProfileService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 更新系は @Transactional を付け、取得〜保存を1つのまとまりにする
    @Transactional
    public MemberProfileResponse updateProfile(Long memberId, ProfileUpdateRequest req) {
        // 存在チェックは get() ではなく orElseThrow で「いない」を明示的に知らせる
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        // ★部分更新の肝：値が入っている項目だけ反映する。
        //   null（＝今回は変更しない）の項目は、既存の値をそのまま残す。
        if (req.getNickname() != null) {
            member.setNickname(req.getNickname());
        }
        if (req.getPhone() != null) {
            member.setPhone(req.getPhone());
        }
        if (req.getEmail() != null) {
            // 変更がある項目だけ、必要なバリデーション（形式・重複）を行う
            validateEmail(req.getEmail());
            member.setEmail(req.getEmail());
        }
        if (req.getBirthday() != null) {
            member.setBirthday(req.getBirthday());
        }

        Member saved = memberRepository.save(member);

        // Entity をそのまま返さず、公開してよい項目だけの応答用オブジェクト(DTO)に詰め替えて返す
        return MemberProfileResponse.from(saved);
    }

    private void validateEmail(String email) {
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("メールアドレスの形式が不正です: " + email);
        }
    }
}
