package hamo.job.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.UUID;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.security.SecureRandom;

@Component
public class ImageDownloader implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ImageDownloader.class);

    @Value("${image.folder}")
    private String imageFolder;
    
    private final HttpClient httpClient;

    public ImageDownloader() {
        this.httpClient = createHttpClient();
    }
    
    private HttpClient createHttpClient() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { 
                        return new X509Certificate[0]; 
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            return HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(10))
                    .sslContext(sslContext)
                    .build();
                    
        } catch (Exception e) {
            logger.warn("Failed to create SSL context, falling back to default: {}", e.getMessage());
            return HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
        }
    }

    public File downloadImage(String location) throws IOException {
        try {
            byte[] imageData = downloadImageData(location);
            return saveImageToFile(imageData);
        } catch (Exception e) {
            logger.error("Failed to download image from: {}", location, e);
            throw new IOException("Failed to download image", e);
        }
    }
    
    private byte[] downloadImageData(String location) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(location))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            logger.debug("Successfully downloaded image from: {}", location);
            return response.body();
        } else {
            throw new IOException("HTTP error " + response.statusCode() + " when downloading image from: " + location);
        }
    }
    
    private File saveImageToFile(byte[] imageData) throws IOException {
        Path imageFolderPath = Paths.get(imageFolder);
        
        if (!Files.exists(imageFolderPath)) {
            Files.createDirectories(imageFolderPath);
        }
        
        String fileName = generateUniqueFileName();
        Path imagePath = imageFolderPath.resolve(fileName + ".jpg");
        
        while (Files.exists(imagePath)) {
            fileName = generateUniqueFileName();
            imagePath = imageFolderPath.resolve(fileName + ".jpg");
        }
        
        Files.write(imagePath, imageData, StandardOpenOption.CREATE_NEW);
        logger.debug("Image saved to: {}", imagePath);
        
        return imagePath.toFile();
    }
    
    private String generateUniqueFileName() {
        return String.format("%s_%s", 
                UUID.randomUUID().toString().substring(0, 12), 
                System.currentTimeMillis() / 1000);
    }

    @Override
    public void run() {
        logger.debug("ImageDownloader run method called");
    }
}
