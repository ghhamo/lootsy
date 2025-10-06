package hamo.job.dto;

import hamo.job.entity.Product;

public record ProductDetailsDTO(String name, String price, String categoryName, String description, String imageUrl) {

    public static ProductDetailsDTO fromProduct(Product product) {
        String filename = java.nio.file.Paths.get(product.getImageUrlM()).getFileName().toString();
        String imageUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/images/products/")
                .path(filename)
                .toUriString();
        return new ProductDetailsDTO(
                product.getName(),
                product.getPrice().toString(),
                product.getCategory().getName(),
                product.getDescription(),
                imageUrl);
    }
}