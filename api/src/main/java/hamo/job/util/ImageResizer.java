package hamo.job.util;

import jakarta.transaction.Transactional;
import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ImageResizer {

    @Value("${image.folder}")
    private String imageFolder;

    @Transactional
    public String resizeImage(File sourceFile, Integer imageSize) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(sourceFile);
        BufferedImage outputImage = Scalr.resize(bufferedImage, imageSize);
        String newFileName = FilenameUtils.getBaseName(sourceFile.getName())
                + "_" + imageSize + "." + FilenameUtils.getExtension(sourceFile.getName());
        Path path = Paths.get(imageFolder, newFileName);
        File newImageFile = path.toFile();
        ImageIO.write(outputImage, "jpg", newImageFile);
        outputImage.flush();
        return newImageFile.getName();
    }
}