public interface SmsSender {

    void send(String phoneNumber, String message);

    void sendUrgent(String phoneNumber, String message);

    void sendWithoutSound(String phoneNumber, String message);
}
