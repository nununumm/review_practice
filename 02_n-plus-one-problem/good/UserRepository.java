public interface UserRepository extends JpaRepository<User, Long> {
    
    // ⭕️ 複雑な集計は @Query を使ってDBに直接指示を出す（1発のSQLで済む）
    @Query("SELECT new com.example.dto.UserPostCountDto(u.username, COUNT(p)) " +
           "FROM User u LEFT JOIN u.posts p " +
           "GROUP BY u.username")
    List<UserPostCountDto> findAllWithPostCount();
}