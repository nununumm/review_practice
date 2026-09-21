package bad;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {

    public String loadApiKey() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/opt/app/config.properties"));
            Properties props = new Properties();
            props.load(reader);
            return props.getProperty("api.key");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                return "";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
