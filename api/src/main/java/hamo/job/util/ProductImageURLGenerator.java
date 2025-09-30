package hamo.job.util;

import hamo.job.exception.exceptions.DownloadFailedException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.security.SecureRandom;

@Component
public class ProductImageURLGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ProductImageURLGenerator.class);
    private static final String PICSUM_URL = "https://picsum.photos/1920/1080";
    
    private final HttpClient httpClient;

    public ProductImageURLGenerator() {
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
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .connectTimeout(Duration.ofSeconds(10))
                    .sslContext(sslContext)
                    .build();
                    
        } catch (Exception e) {
            logger.warn("Failed to create SSL context, falling back to default: {}", e.getMessage());
            return HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
        }
    }

    @Transactional
    public String addProductUrl() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PICSUM_URL))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            
            String location = response.headers().firstValue("location").orElse(null);
            
            if (location == null || location.trim().isEmpty()) {
                logger.warn("No location header found in response, using default URL");
                return PICSUM_URL;
            }
            
            logger.debug("Generated image URL: {}", location);
            return location;
            
        } catch (Exception exception) {
            logger.error("Failed to generate product image URL", exception);
            throw new DownloadFailedException();
        }
    }

}
