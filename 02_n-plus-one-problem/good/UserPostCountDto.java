public class UserPostCountDto {
    
    private String username;
    private long postCount; // ※ DBの COUNT() 関数の戻り値は long 型になる

    // ⭕️ JPQLの "SELECT new com...UserPostCountDto(u.username, COUNT(p))" 
    // から直接データを受け取るためのコンストラクタ
    public UserPostCountDto(String username, long postCount) {
        this.username = username;
        this.postCount = postCount;
    }

    // --- 以下、Getter と Setter ---
    // (※ 現場では Lombok の @Data アノテーションを使って省略することが多いよ)

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getPostCount() {
        return postCount;
    }

    public void setPostCount(long postCount) {
        this.postCount = postCount;
    }
}