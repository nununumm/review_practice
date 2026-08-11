package good;

// 送金にまつわる業務エラーの親クラス。
// RuntimeException（＝非チェック例外）を継承しているのがポイント。
//   Spring の @Transactional は、既定では「RuntimeException が飛んだときだけ」自動ロールバックする。
//   （checked例外だとロールバックされないので、業務エラーは RuntimeException 系にするのが定石）
public class TransferException extends RuntimeException {
    public TransferException(String message) {
        super(message);
    }
}
