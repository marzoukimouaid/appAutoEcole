package Utils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Iterator;

public class ImgBBUtil {

    private static final String API_KEY = ConfigReader.getKey("imgbb_api_key");
    private static final String UPLOAD_URL = "https://api.imgbb.com/1/upload?key=" + API_KEY;


    private static final long SIZE_THRESHOLD = 500_000;
    private static final int TARGET_WIDTH = 800;
    private static final int TARGET_HEIGHT = 800;
    private static final float JPEG_QUALITY = 0.7f;


    public static String uploadImageToImgBB(File imageFile) {
        HttpURLConnection connection = null;
        try {
            byte[] imageBytes = getImageBytes(imageFile);
            String encodedImage = Base64.getEncoder().encodeToString(imageBytes);
            String postData = "image=" + URLEncoder.encode(encodedImage, "UTF-8");

            URL url = new URL(UPLOAD_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (OutputStream os = connection.getOutputStream();
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))) {
                writer.write(postData);
                writer.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    String responseString = response.toString();
                    int urlIndex = responseString.indexOf("\"url\":\"");
                    if (urlIndex != -1) {
                        int start = urlIndex + 7;
                        int end = responseString.indexOf("\"", start);
                        if (end != -1) {
                            String urlFromResponse = responseString.substring(start, end);
                            return urlFromResponse.replace("\\/", "/");
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


    private static byte[] getImageBytes(File imageFile) throws IOException {
        if (imageFile.length() > SIZE_THRESHOLD) {
            return compressImage(imageFile, JPEG_QUALITY, TARGET_WIDTH, TARGET_HEIGHT);
        } else {
            return Files.readAllBytes(imageFile.toPath());
        }
    }


    private static byte[] compressImage(File inputFile, float quality, int maxWidth, int maxHeight) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputFile);
        if (originalImage == null) {
            throw new IOException("Could not read image file: " + inputFile.getAbsolutePath());
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        double scale = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) {
                throw new IllegalStateException("No JPEG writers found");
            }
            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }
            writer.setOutput(ios);
            writer.write(null, new IIOImage(scaledImage, null, null), param);
            writer.dispose();
        }
        return baos.toByteArray();
    }
}
