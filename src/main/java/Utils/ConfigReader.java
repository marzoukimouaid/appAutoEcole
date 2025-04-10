package Utils;

import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ConfigReader {
    private static JSONObject configData;

    
    private static void loadConfig() {
        if (configData == null) {
            try {

                File file = new File("src/main/java/Utils/config.json");

                if (!file.exists()) {
                    throw new RuntimeException("config.json file not found in utils folder.");
                }

                FileInputStream inputStream = new FileInputStream(file);
                Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
                String jsonText = scanner.useDelimiter("\\A").next();
                scanner.close();

                configData = new JSONObject(jsonText);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to load config.json");
            }
        }
    }

    
    public static String getKey(String keyName) {
        loadConfig();
        return configData.optString(keyName, "KEY_NOT_FOUND");
    }
}
