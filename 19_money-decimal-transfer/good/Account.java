package good;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

// 銀行口座を表すエンティティ
@Entity
public class Account {

    @Id
    private Long id;

    private String ownerName;   // 口座名義

    // 【重要】残高は double ではなく long（整数）で「円」を持つ。
    //   double は「だいたいの数」しか表せず端数がズレる（0.1 + 0.2 が 0.3 にならない）。
    //   お金は1円たりともズレてはいけないので、ピッタリ表せる整数型を使う。
    //   int だと約21億が上限でオーバーフローするため long にする。
    private long balance;       // 残高（円）

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }
}
