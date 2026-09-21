import java.time.LocalDateTime;

public class Product {

    private Long id;
    private String name;
    private int score;
    private long salesCount;
    private LocalDateTime updatedAt;

    public Product(Long id, String name, int score, long salesCount, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.salesCount = salesCount;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public long getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(long salesCount) {
        this.salesCount = salesCount;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
