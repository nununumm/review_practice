public interface ReadStatusRepository {

    void save(Long userId, String channel, String message, boolean read);
}
