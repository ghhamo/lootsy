package hamo.job.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class ImageResourceConfig implements WebMvcConfigurer {
    
    @Value("${image.folder}")
    private String imageFolder;
    
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        File uploadDir = new File(imageFolder);
        String uploadPath = uploadDir.getAbsolutePath();
        
        registry.addResourceHandler("/images/products/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
