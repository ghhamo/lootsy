package hamo.job.dto;

import hamo.job.entity.Product;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public record ProductHomePageDTO(Long id, String name, BigDecimal price, String imageUrl) {
    public static ProductHomePageDTO fromProduct(Product product) {
        String filename = java.nio.file.Paths.get(product.getImageUrlM()).getFileName().toString();
        String imageUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/images/products/")
                .path(filename)
                .toUriString();
        return new ProductHomePageDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                imageUrl);
    }

    public static Set<ProductHomePageDTO> mapProductSetToProductHomePageDto(Iterable<Product> products) {
        Set<ProductHomePageDTO> productHomePageDTOSet = new HashSet<>();
        for (Product product : products) {
            productHomePageDTOSet.add(ProductHomePageDTO.fromProduct(product));
        }
        return productHomePageDTOSet;
    }
}


