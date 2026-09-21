import javax.persistence.Entity;
import javax.persistence.Id;

// @Entity = このクラスがDBのテーブル1行に対応する「エンティティ」であることを示す
@Entity
public class Product {

    // @Id = このフィールドが主キー（＝1行を一意に決める番号）であることを示す
    @Id
    private Long id;
    private String name;
    private long price;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getPrice() {
        return price;
    }
}
