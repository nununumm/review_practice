package bad;

// 銀行口座を表すエンティティ
public class Account {

    private Long id;
    private String ownerName;   // 口座名義
    private double balance;     // 残高（円）

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

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
