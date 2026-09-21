public interface MailSender {

    void send(String email, String subject, String body);

    void sendHighPriority(String email, String subject, String body);
}
