package Utils;

import Authentication.entite.User;

import java.io.*;
import java.time.Instant;
import java.util.Properties;

public class SessionManager {
    private static User currentUser;
    private static final String SESSION_FILE = "session.enc";
    private static final long SESSION_EXPIRY_TIME = 24 * 60 * 60;

    public static void setCurrentUser(User user) {
        currentUser = user;
        saveSessionToFile(user);
    }

    public static User getCurrentUser() {
        if (currentUser == null) {
            currentUser = loadSessionFromFile();
        }
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
        clearSessionFile();
    }

    private static void saveSessionToFile(User user) {
        try (FileWriter writer = new FileWriter(SESSION_FILE)) {
            Properties properties = new Properties();
            properties.setProperty("id", String.valueOf(user.getId()));
            properties.setProperty("username", user.getUsername());
            properties.setProperty("role", user.getRole());
            properties.setProperty("timestamp", String.valueOf(Instant.now().getEpochSecond()));

            StringWriter stringWriter = new StringWriter();
            properties.store(stringWriter, "User Session Data");

            String encryptedData = EncryptionUtils.encrypt(stringWriter.toString());
            writer.write(encryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static User loadSessionFromFile() {
        File file = new File(SESSION_FILE);
        if (!file.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String encryptedData = reader.readLine();
            if (encryptedData == null) return null;

            String decryptedData = EncryptionUtils.decrypt(encryptedData);

            Properties properties = new Properties();
            properties.load(new StringReader(decryptedData));

            long sessionTimestamp = Long.parseLong(properties.getProperty("timestamp"));
            long currentTime = Instant.now().getEpochSecond();

            if (currentTime - sessionTimestamp > SESSION_EXPIRY_TIME) {
                clearSessionFile();
                return null;
            }

            int id = Integer.parseInt(properties.getProperty("id"));
            String username = properties.getProperty("username");
            String role = properties.getProperty("role");

            return new User(id, username, role);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void clearSessionFile() {
        File file = new File(SESSION_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}
