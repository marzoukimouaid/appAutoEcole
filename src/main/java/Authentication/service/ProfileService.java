package Authentication.service;

import Authentication.dao.ProfileDao;
import Authentication.entite.Profile;
import Utils.ConfigReader;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;

public class ProfileService {
    private final ProfileDao profileDao = new ProfileDao();


    private static final String IMGBB_API_KEY = ConfigReader.getKey("imgbb_api_key");

    /**
     * Retrieves the full profile of a user.
     * @param userId The user's ID.
     * @return The Profile object if found, otherwise an empty optional.
     */
    public Optional<Profile> getProfileByUserId(int userId) {
        return Optional.ofNullable(profileDao.getProfileByUserId(userId));
    }

    public boolean createOrUpdateProfile(Profile profile, File imageFile) {
        // If an image file is provided, upload it before saving the profile
        if (imageFile != null) {
            String uploadedImageUrl = uploadImageToImgBB(imageFile);
            if (uploadedImageUrl != null) {
                profile.setPictureUrl(uploadedImageUrl);
            }
        }

        // Save the profile to the database
        Profile existing = profileDao.getProfileByUserId(profile.getUserId());
        if (existing == null) {
            return profileDao.createProfile(profile);
        } else {
            return profileDao.updateProfile(profile);
        }
    }

    /**
     * Uploads an image file to ImgBB and returns the URL.
     *
     * @param file The image file to upload.
     * @return The URL of the uploaded image, or null if the upload failed.
     */
    public String uploadImageToImgBB(File file) {
        try {
            String apiUrl = "https://api.imgbb.com/1/upload?key=" + IMGBB_API_KEY;
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary");

            OutputStream outputStream = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

            // Prepare file data
            writer.append("------WebKitFormBoundary\r\n");
            writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"" + file.getName() + "\"\r\n");
            writer.append("Content-Type: " + Files.probeContentType(file.toPath()) + "\r\n\r\n");
            writer.flush();
            Files.copy(file.toPath(), outputStream);
            outputStream.flush();
            writer.append("\r\n------WebKitFormBoundary--\r\n");
            writer.close();

            // Read response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parse JSON response
            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getJSONObject("data").getString("display_url");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
