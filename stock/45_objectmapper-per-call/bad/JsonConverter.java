package bad;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConverter {

    public String toJson(Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T> T fromJson(String json, Class<T> type) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, type);
        } catch (Exception e) {
            return null;
        }
    }
}
