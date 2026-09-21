import org.springframework.stereotype.Repository;

// 会員をDBに保存する担当（中身は割愛：save で保存されるものとする）
@Repository
public class MemberRepository {

    public Member save(Member member) {
        // 実際にはここでDBへINSERTする（今回は本題ではないので省略）
        member.setId(1L);
        return member;
    }
}
