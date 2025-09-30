package hamo.job.dto;

import hamo.job.entity.Order;
import hamo.job.util.statusAndRole.OrderStatus;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public record OrderDTO(
    Long id,
    OrderStatus orderStatus,
    double totalAmount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long userId,
    Set<OrderItemDTO> items
) {

    public static OrderDTO fromOrder(Order order) {
        Set<OrderItemDTO> orderItems = new HashSet<>();
        if (order.getItems() != null) {
            for (var item : order.getItems()) {
                orderItems.add(OrderItemDTO.fromOrderItem(item));
            }
        }
        
        return new OrderDTO(
            order.getId(),
            order.getOrderStatus(),
            order.getTotalAmount(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            order.getUser() != null ? order.getUser().getId() : null,
            orderItems
        );
    }

    public static Order toOrder(OrderDTO orderDTO) {
        Order order = new Order();
        order.setId(orderDTO.id);
        order.setOrderStatus(orderDTO.orderStatus);
        order.setTotalAmount(orderDTO.totalAmount);
        order.setCreatedAt(orderDTO.createdAt);
        order.setUpdatedAt(orderDTO.updatedAt);
        return order;
    }

    public static Set<OrderDTO> mapOrderSetToOrderDto(Iterable<Order> orders) {
        Set<OrderDTO> orderDTOSet = new HashSet<>();
        for (Order order : orders) {
            orderDTOSet.add(OrderDTO.fromOrder(order));
        }
        return orderDTOSet;
    }
}
