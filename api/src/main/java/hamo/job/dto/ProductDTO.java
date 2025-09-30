package hamo.job.dto;

import hamo.job.entity.Product;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public record ProductDTO(Long id, String name, BigDecimal price, Long categoryId) {
    
    public ProductDTO {
        Objects.requireNonNull(id, "Product ID cannot be null");
        Objects.requireNonNull(name, "Product name cannot be null");
        Objects.requireNonNull(price, "Product price cannot be null");
        Objects.requireNonNull(categoryId, "Category ID cannot be null");
    }
    public static ProductDTO fromProduct(Product product) {
        return new ProductDTO(product.getId(), product.getName(), product.getPrice(), product.getCategory().getId());
    }

    public static Product toProduct(ProductDTO productDTO) {
        Product product = new Product();
        product.setId(productDTO.id);
        product.setName(productDTO.name);
        product.setPrice(productDTO.price);
        return product;
    }

    public static Set<ProductDTO> mapProductSetToProductDto(Iterable<Product> products) {
        Set<ProductDTO> productDTOSet = new HashSet<>();
        for (Product product : products) {
            productDTOSet.add(ProductDTO.fromProduct(product));
        }
        return productDTOSet;
    }
}

