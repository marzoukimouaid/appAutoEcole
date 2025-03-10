package Utils;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Base64;
import java.net.URLEncoder;

public class ImgBBUtil {

    // Load the API key from your config.json via ConfigReader
    private static final String API_KEY = ConfigReader.getKey("imgbb_api_key");
    private static final String UPLOAD_URL = "https://api.imgbb.com/1/upload?key=" + API_KEY;

    /**
     * Uploads an image file to ImgBB.
     *
     * @param imageFile the image file to upload.
     * @return the URL of the uploaded image if successful; otherwise, null.
     */
    public static String uploadImageToImgBB(File imageFile) {
        HttpURLConnection connection = null;
        try {
            // Read and encode the image file to Base64
            byte[] fileContent = Files.readAllBytes(imageFile.toPath());
            String encodedImage = Base64.getEncoder().encodeToString(fileContent);
            String postData = "image=" + URLEncoder.encode(encodedImage, "UTF-8");

            URL url = new URL(UPLOAD_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            // Set connection and read timeouts (5 seconds)
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Send the POST data
            try (OutputStream os = connection.getOutputStream();
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))) {
                writer.write(postData);
                writer.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    String responseString = response.toString();
                    // A simple parsing; in production, use a JSON parser
                    int urlIndex = responseString.indexOf("\"url\":\"");
                    if (urlIndex != -1) {
                        int start = urlIndex + 7;
                        int end = responseString.indexOf("\"", start);
                        if (end != -1) {
                            return responseString.substring(start, end);
                        }
                    }
                }
            } else {
                System.err.println("ImgBB upload failed with HTTP response code: " + responseCode);
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    System.err.println("Error response: " + errorResponse.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
}
