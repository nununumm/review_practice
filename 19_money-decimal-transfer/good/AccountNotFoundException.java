package good;

// 指定された口座が存在しないときに投げる例外
public class AccountNotFoundException extends TransferException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
