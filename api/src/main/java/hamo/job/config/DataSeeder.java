package hamo.job.config;

import hamo.job.entity.*;
import hamo.job.service.CartService;
import hamo.job.service.CategoryService;
import hamo.job.service.ProductService;
import hamo.job.service.UserService;
import hamo.job.util.ImageDownloader;
import hamo.job.util.ImageResizer;
import hamo.job.util.ProductImageURLGenerator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private record ProductData(String name, String description, BigDecimal price, int categoryIndex) {
    }

    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartService cartService;
    private final ProductImageURLGenerator urlGenerator;
    private final ImageDownloader imageDownloader;
    private final ImageResizer imageResizer;

    @Value("${image.medium.size}")
    private Integer imageMediumSize;
    @Value("${image.small.size}")
    private Integer imageSmallSize;

    @Autowired
    public DataSeeder(UserService userService,
                      ProductService productService,
                      CategoryService categoryService,
                      CartService cartService,
                      ProductImageURLGenerator urlGenerator,
                      ImageDownloader imageDownloader,
                      ImageResizer imageResizer) {
        this.userService = userService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.cartService = cartService;
        this.urlGenerator = urlGenerator;
        this.imageDownloader = imageDownloader;
        this.imageResizer = imageResizer;
    }

    @Value("${image.folder}")
    private String imageFolder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        logger.info("=== DataSeeder started - Initializing application data ===");
        
        logger.info("Purging existing product images from folder: {}", imageFolder);
        purgeProductImages(Paths.get(imageFolder));
        
        long productCount = productService.getCount();
        logger.info("Current product count in database: {}", productCount);
        
        if (productCount == 0) {
            logger.info("Database is empty - Starting data seeding process...");
            String[] categoryNames = {
                "Gaming Consoles", "PC Gaming", "Gaming Accessories", "Gaming Headsets", "Gaming Chairs",
                "Video Games", "Gaming Keyboards & Mice", "Gaming Monitors", "VR & AR", "Mobile Gaming"
            };
            
            String[] categoryDescriptions = {
                "Latest gaming consoles including PlayStation, Xbox, and Nintendo Switch",
                "High-performance gaming PCs, graphics cards, and PC gaming components", 
                "Controllers, charging stations, and essential gaming accessories",
                "Premium gaming headsets with surround sound and noise cancellation",
                "Ergonomic gaming chairs designed for long gaming sessions",
                "Popular video games across all platforms and genres",
                "Mechanical keyboards, gaming mice, and input devices for competitive gaming",
                "High refresh rate monitors and displays optimized for gaming",
                "Virtual Reality headsets, AR devices, and immersive gaming technology",
                "Mobile gaming accessories, phone controllers, and portable gaming gear"
            };
            
            logger.info("Creating {} gaming categories...", categoryNames.length);
            List<Category> categories = new ArrayList<>();
            for (int i = 0; i < categoryNames.length; i++) {
                categories.add(new Category(categoryNames[i], categoryDescriptions[i], LocalDateTime.now()));
                logger.debug("Added category: {}", categoryNames[i]);
            }
            categories = categoryService.saveAll(categories);
            logger.info("Successfully saved {} categories to database", categories.size());
            
            String[] firstNames = {
                "John", "Jane", "Michael", "Sarah", "David", "Emily", "Robert", "Jessica", 
                "William", "Ashley", "James", "Amanda", "Christopher", "Stephanie", "Daniel"
            };
            
            String[] lastNames = {
                "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
                "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson"
            };
            
            String[] phoneNumbers = {
                "+1-555-0101", "+1-555-0102", "+1-555-0103", "+1-555-0104", "+1-555-0105",
                "+1-555-0106", "+1-555-0107", "+1-555-0108", "+1-555-0109", "+1-555-0110",
                "+1-555-0111", "+1-555-0112", "+1-555-0113", "+1-555-0114", "+1-555-0115"
            };
            
            logger.info("Creating test users...");
            List<User> users = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                String firstName = firstNames[i];
                String lastName = lastNames[i];
                String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "@example.com";
                users.add(new User(firstName, lastName, email, "password123", phoneNumbers[i]));
                logger.debug("Added user: {} {} ({})", firstName, lastName, email);
            }
            users.add(new User("Hamlet", "Ghukasyan", "hamoghukasyan98@gmail.com", "zxcvbnm,./", "1234567890"));
            logger.debug("Added admin user: Hamlet Ghukasyan");
            
            users = userService.saveAll(users);
            logger.info("Successfully saved {} users to database", users.size());
            
            logger.info("Creating shopping carts for users...");
            List<Cart> carts = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                Cart cart = new Cart();
                cart.setUserId(users.get(i).getId());
                carts.add(cart);
                logger.debug("Created cart for user ID: {}", users.get(i).getId());
            }
            cartService.saveAll(carts);
            logger.info("Successfully created {} shopping carts", carts.size());

            ProductData[] productData = {
                new ProductData("PlayStation 5 Console", "Next-generation PlayStation 5 console with ultra-high speed SSD and ray tracing support", new BigDecimal("499.99"), 0),
                new ProductData("Xbox Series X", "Microsoft's most powerful gaming console with 4K gaming and Quick Resume technology", new BigDecimal("499.99"), 0),
                new ProductData("Nintendo Switch OLED", "Nintendo Switch with vibrant 7-inch OLED screen and enhanced audio for handheld mode", new BigDecimal("349.99"), 0),
                new ProductData("NVIDIA RTX 4080 Graphics Card", "High-performance graphics card with 16GB GDDR6X memory for 4K gaming excellence", new BigDecimal("1199.99"), 1),
                new ProductData("Razer DeathAdder V3 Gaming Mouse", "Precision gaming mouse with Focus Pro 30K sensor and 90-hour battery life", new BigDecimal("79.99"), 6),
                new ProductData("SteelSeries Arctis 7P Wireless Headset", "Premium wireless gaming headset with lossless 2.4GHz connection and 24-hour battery", new BigDecimal("179.99"), 3),
                new ProductData("Secretlab Titan Evo Gaming Chair", "Ergonomic gaming chair with cold-cure foam and premium materials for ultimate comfort", new BigDecimal("449.99"), 4),
                new ProductData("Cyberpunk 2077 Ultimate Edition", "Open-world action RPG set in the dystopian Night City with all DLC included", new BigDecimal("59.99"), 5),
                new ProductData("Corsair K95 RGB Mechanical Keyboard", "Premium mechanical gaming keyboard with Cherry MX switches and per-key RGB lighting", new BigDecimal("199.99"), 6),
                new ProductData("ASUS ROG Swift 27\" 144Hz Monitor", "27-inch gaming monitor with 144Hz refresh rate and G-Sync compatibility", new BigDecimal("399.99"), 7),
                new ProductData("Meta Quest 3 VR Headset", "Advanced VR headset with mixed reality capabilities and intuitive hand tracking", new BigDecimal("499.99"), 8),
                new ProductData("Razer Kishi V2 Mobile Controller", "Universal mobile gaming controller compatible with iPhone and Android devices", new BigDecimal("99.99"), 9),
                new ProductData("Xbox Wireless Controller", "Official Xbox controller with textured grips and Bluetooth connectivity", new BigDecimal("59.99"), 2),
                new ProductData("HyperX Cloud II Gaming Headset", "Comfortable gaming headset with virtual 7.1 surround sound and noise cancellation", new BigDecimal("99.99"), 3),
                new ProductData("Herman Miller x Logitech G Embody Chair", "Ergonomic gaming chair designed in collaboration with Herman Miller for pro gamers", new BigDecimal("1395.00"), 4),
                new ProductData("The Legend of Zelda: Tears of the Kingdom", "Epic adventure game featuring Link's journey through Hyrule and the Sky Islands", new BigDecimal("69.99"), 5),
                new ProductData("Logitech G Pro X Superlight Mouse", "Ultra-lightweight wireless gaming mouse weighing only 63 grams with HERO 25K sensor", new BigDecimal("149.99"), 6),
                new ProductData("Samsung Odyssey G7 32\" Curved Monitor", "32-inch curved QLED gaming monitor with 240Hz refresh rate and HDR600", new BigDecimal("699.99"), 7),
                new ProductData("PlayStation VR2 Headset", "Next-gen VR headset for PlayStation 5 with haptic feedback and eye tracking", new BigDecimal("549.99"), 8),
                new ProductData("Backbone One Mobile Gaming Controller", "Premium mobile gaming controller with clickable analog triggers and tactile buttons", new BigDecimal("99.99"), 9),
                new ProductData("Nintendo Pro Controller", "Official Nintendo Switch Pro Controller with motion controls and HD rumble", new BigDecimal("69.99"), 2),
                new ProductData("Corsair HS80 RGB Wireless Headset", "Wireless gaming headset with Dolby Atmos and broadcast-grade microphone", new BigDecimal("149.99"), 3),
                new ProductData("RESPAWN 110 Racing Style Gaming Chair", "Racing-style gaming chair with lumbar support and adjustable armrests", new BigDecimal("299.99"), 4),
                new ProductData("Elden Ring Deluxe Edition", "Action RPG masterpiece with challenging combat and vast open world exploration", new BigDecimal("79.99"), 5),
                new ProductData("Razer Huntsman V2 Keyboard", "Premium mechanical keyboard with Razer Linear Optical switches and doubleshot keycaps", new BigDecimal("179.99"), 6),
                new ProductData("LG UltraGear 38\" Ultrawide Monitor", "38-inch ultrawide gaming monitor with Nano IPS technology and 144Hz refresh rate", new BigDecimal("1299.99"), 7),
                new ProductData("HTC Vive Pro 2 VR System", "Professional VR system with 5K resolution and 120Hz refresh rate for immersive gaming", new BigDecimal("1399.99"), 8),
                new ProductData("GameSir X2 Bluetooth Mobile Controller", "Bluetooth mobile gaming controller with hall effect joysticks and programmable buttons", new BigDecimal("79.99"), 9),
                new ProductData("DualSense Wireless Controller", "PlayStation 5's innovative controller with haptic feedback and adaptive triggers", new BigDecimal("69.99"), 2),
                new ProductData("Audio-Technica ATH-G1WL Gaming Headset", "Wireless gaming headset with 90mm drivers and crystal-clear communication", new BigDecimal("299.99"), 3),
                new ProductData("Arozzi Vernazza Gaming Chair", "Premium gaming chair with Italian leather and adjustable lumbar support", new BigDecimal("399.99"), 4),
                new ProductData("God of War Ragnarök", "Norse mythology action-adventure featuring Kratos and Atreus in the Nine Realms", new BigDecimal("69.99"), 5)
            };
            
            logger.info("Creating {} gaming products with images...", productData.length);
            List<Product> products = new ArrayList<>();
            for (int i = 0; i < productData.length; i++) {
                ProductData data = productData[i];
                logger.debug("Processing product {}/{}: {}", i + 1, productData.length, data.name);
                String imageUrl = urlGenerator.addProductUrl();
                logger.debug("Generated image URL: {}", imageUrl);
                File imagePath = imageDownloader.downloadImage(imageUrl);
                logger.debug("Downloaded image to: {}", imagePath.getName());
                String mediumImage = imageResizer.resizeImage(imagePath, imageMediumSize);
                String smallImage = imageResizer.resizeImage(imagePath, imageSmallSize);
                logger.debug("Created resized images - Medium: {}, Small: {}", mediumImage, smallImage);
                products.add(
                        new Product(data.name,
                                data.price,
                                categories.get(data.categoryIndex),
                                data.description,
                                imagePath.getName(),
                                mediumImage,
                                smallImage)
                );
                if ((i + 1) % 10 == 0) {
                    logger.info("Processed {}/{} products...", i + 1, productData.length);
                }
            }
            productService.saveAll(products);
            logger.info("Successfully saved {} products to database", products.size());
            logger.info("=== DataSeeder completed successfully ===");
        } else {
            logger.info("Database already contains {} products - Skipping data seeding", productCount);
        }
    }

    private void purgeProductImages(Path root) {
        logger.debug("Checking if image directory exists: {}", root);
        if (!Files.isDirectory(root)) {
            logger.debug("Image directory does not exist, skipping purge");
            return;
        }
        
        logger.info("Purging all files from image directory: {}", root);
        try (Stream<Path> paths = Files.walk(root)) {
            long deletedCount = paths
                    .filter(p -> !p.equals(root))
                    .sorted(Comparator.reverseOrder())
                    .peek(p -> logger.debug("Deleting: {}", p))
                    .mapToLong(p -> {
                        try {
                            boolean deleted = Files.deleteIfExists(p);
                            return deleted ? 1 : 0;
                        } catch (IOException e) {
                            logger.error("Failed to delete: {} -> {}", p, e.getMessage());
                            return 0;
                        }
                    })
                    .sum();
            logger.info("Successfully deleted {} files/directories from image folder", deletedCount);
        } catch (IOException e) {
            logger.error("Failed to purge images directory: {}", e.getMessage());
        }
    }
}