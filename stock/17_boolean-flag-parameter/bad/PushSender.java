public interface PushSender {

    void send(String deviceToken, String message, String priority);

    void sendSilent(String deviceToken, String message, String priority);
}
