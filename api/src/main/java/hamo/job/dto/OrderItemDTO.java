package hamo.job.dto;

import hamo.job.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemDTO(
    Long orderId,
    Long productId,
    String productName,
    int quantity,
    BigDecimal unitPrice,
    double subtotal) {

    public static OrderItemDTO fromOrderItem(OrderItem orderItem) {
        return new OrderItemDTO(
            orderItem.getOrder() != null ? orderItem.getOrder().getId() : null,
            orderItem.getProduct() != null ? orderItem.getProduct().getId() : null,
            orderItem.getProduct() != null ? orderItem.getProduct().getName() : null,
            orderItem.getQty(),
            orderItem.getUnitPrie(),
            orderItem.getSubtotal()
        );
    }
}
