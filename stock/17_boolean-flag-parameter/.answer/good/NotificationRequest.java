import java.util.EnumSet;
import java.util.Set;

/**
 * 「1回の通知でやりたいこと」をまとめて持つオブジェクト（＝設定をひとまとめにした入れ物）。
 * bad では send(user, msg, true, false, true, false, false, true) のように
 * 真偽値がズラッと並び、呼び出し側を見ても何を指定しているのか読めなかった。
 * ここでは項目に名前を付けて持たせるので、生成コードを読むだけで意図が分かる（＝自己説明的）。
 *
 * さらに、直接 new させず「ビルダー」（＝部品を1つずつ指定して最後に組み立てる作り方）で
 * 作らせることで、呼び出しが request.channel(MAIL).mode(URGENT)... のように読める形になる。
 */
public class NotificationRequest {

    private final User user;                        // 送り先のユーザー
    private final String message;                   // 送る本文
    private final Set<NotificationChannel> channels; // どの手段で送るか（複数可）
    private final DeliveryMode mode;                // 送り方（通常/緊急/静か）
    private final boolean trackRead;               // 既読管理をするか

    // コンストラクタは private。外からは new できず、必ずビルダー経由で作らせる。
    private NotificationRequest(Builder builder) {
        this.user = builder.user;
        this.message = builder.message;
        this.channels = builder.channels;
        this.mode = builder.mode;
        this.trackRead = builder.trackRead;
    }

    public User getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    public Set<NotificationChannel> getChannels() {
        return channels;
    }

    public DeliveryMode getMode() {
        return mode;
    }

    public boolean isTrackRead() {
        return trackRead;
    }

    // ビルダーを作る入口。使う側は NotificationRequest.builder() ... build() と書く。
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 組み立て役。各項目を「名前付きのメソッド」で指定させるので、
     * 呼び出しコードがそのまま説明文のように読める。
     */
    public static class Builder {

        private User user;                                       // 必須
        private String message;                                  // 必須
        private Set<NotificationChannel> channels = EnumSet.noneOf(NotificationChannel.class); // 手段（既定は空）
        private DeliveryMode mode = DeliveryMode.NORMAL;         // 送り方（既定は通常）
        private boolean trackRead = false;                       // 既読管理（既定はしない）

        public Builder to(User user) {              // 送り先を指定
            this.user = user;
            return this;                            // 自分自身を返して、メソッドを続けて書けるようにする
        }

        public Builder message(String message) {   // 本文を指定
            this.message = message;
            return this;
        }

        public Builder via(NotificationChannel... channels) { // 送る手段を1つ以上指定
            this.channels = EnumSet.noneOf(NotificationChannel.class);
            for (NotificationChannel channel : channels) {
                this.channels.add(channel);
            }
            return this;
        }

        public Builder mode(DeliveryMode mode) {    // 送り方を指定（URGENTとSILENTは同時に選べない＝どちらか一方）
            this.mode = mode;
            return this;
        }

        public Builder trackRead() {                // 既読管理を有効にする（trueを渡すのではなく、呼べば有効という自己説明的なAPI）
            this.trackRead = true;
            return this;
        }

        public NotificationRequest build() {        // 最後にオブジェクトを完成させる
            // 必須項目や、手段が空でないかをここで一度だけ検証する（＝おかしな状態のまま作らせない）
            if (user == null) {
                throw new IllegalArgumentException("送り先ユーザーは必須です");
            }
            if (message == null || message.isEmpty()) {
                throw new IllegalArgumentException("本文は必須です");
            }
            if (channels.isEmpty()) {
                throw new IllegalArgumentException("送信手段を1つ以上指定してください");
            }
            return new NotificationRequest(this);
        }
    }
}
