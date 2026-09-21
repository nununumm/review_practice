// 通知を「どの手段で送るか」を表す列挙型（＝あらかじめ決めた選択肢だけを持てる型）。
// boolean の useMail / useSms / usePush を並べる代わりに、この3択で意図をはっきり表す。
public enum NotificationChannel {

    MAIL,  // メールで送る
    SMS,   // SMS（ショートメッセージ）で送る
    PUSH   // アプリのプッシュ通知で送る
}
