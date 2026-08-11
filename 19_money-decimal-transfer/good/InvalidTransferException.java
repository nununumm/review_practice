package good;

// 送金の指示そのものがおかしいとき（金額がマイナス、自分自身への送金など）に投げる例外
public class InvalidTransferException extends TransferException {
    public InvalidTransferException(String message) {
        super(message);
    }
}
