package Utils;

import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ConfigReader {
    private static JSONObject configData;

    /**
     * Loads the JSON configuration file only when needed.
     */
    private static void loadConfig() {
        if (configData == null) {
            try {
                // Adjusted path to look inside "utils/config.json"
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

    /**
     * Get a key from the config.json file.
     *
     * @param keyName The name of the key in the JSON file.
     * @return The value as a String.
     */
    public static String getKey(String keyName) {
        loadConfig(); // Ensure config is loaded before accessing
        return configData.optString(keyName, "KEY_NOT_FOUND");
    }
}
