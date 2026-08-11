package good;

// 残高が足りないときに投げる例外
public class InsufficientBalanceException extends TransferException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
