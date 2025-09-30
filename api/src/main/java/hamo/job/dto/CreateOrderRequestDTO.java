package hamo.job.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequestDTO(
    @NotNull(message = "Shipping ID is required")
    Long shippingId,
    
    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    Double totalAmount,
    
    String currency
) {
}
