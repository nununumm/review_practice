package good;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * JSON 変換ユーティリティ（修正版）。
 *
 * bad 版は toJson / fromJson が呼ばれるたびに new ObjectMapper() していた。
 * ObjectMapper は生成時に内部で多くの準備（各種設定やキャッシュの構築）を行う「重い」オブジェクトで、
 * 毎回作ると高頻度呼び出しで無駄な CPU・GC 負荷になる。しかも各所で個別に new すると、
 * 日付の書式や「未知フィールドを無視するか」などの設定がバラバラになりやすい。
 *
 * ObjectMapper は「一度設定したら、複数スレッドから同時に使っても安全（スレッドセーフ）」なので、
 * アプリで1個だけ作って使い回すのが正解。ここでは Spring の Bean（＝アプリに1個だけ存在する部品）
 * として1つ持ち、設定も1か所に集約する。
 */
@Component
public class JsonConverter {

    // アプリ全体で1個だけ生成し、使い回す。final で差し替え不可を表明。
    private final ObjectMapper mapper;

    public JsonConverter() {
        this.mapper = new ObjectMapper();
        // 設定は1か所に集約する。例：知らないフィールドが来ても例外にしない（前方互換のため）。
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // 依存性注入(DI)しやすいよう、外から設定済みの ObjectMapper を受け取れるコンストラクタも用意
    public JsonConverter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            // 握りつぶして null を返さない。原因(e)を包んで投げ、呼び出し側が失敗に気づけるようにする。
            throw new IllegalArgumentException("JSON への変換に失敗しました", e);
        }
    }

    public <T> T fromJson(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON の解析に失敗しました: type=" + type.getSimpleName(), e);
        }
    }
}
