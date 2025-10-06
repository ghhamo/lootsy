package hamo.job.dto;

import hamo.job.entity.Product;

import java.math.BigDecimal;
import java.util.Objects;

public record CreateProductDTO(String name,
                               String price, Long categoryId) {
    public CreateProductDTO {
        Objects.requireNonNull(name, "Product name cannot be null");
        Objects.requireNonNull(price, "Product price cannot be null");
        Objects.requireNonNull(categoryId, "Category ID cannot be null");
    }


    public static Product toProduct(CreateProductDTO createProductDTO) {
        Product product = new Product();
        product.setName(createProductDTO.name);
        product.setPrice(new BigDecimal(createProductDTO.price));
        return product;
    }
}