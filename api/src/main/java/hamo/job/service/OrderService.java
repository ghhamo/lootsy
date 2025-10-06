package hamo.job.service;

import hamo.job.dto.*;
import hamo.job.entity.*;
import hamo.job.exception.exceptions.cartException.CartEmptyException;
import hamo.job.exception.exceptions.orderException.OrderIdNotFoundException;
import hamo.job.exception.exceptions.orderException.OrderTotalMismatchException;
import hamo.job.exception.exceptions.productException.ProductIdNotFoundException;
import hamo.job.exception.exceptions.shipping.ShippingIdNotFoundException;
import hamo.job.exception.exceptions.userException.UserIdNotFoundException;
import hamo.job.repository.*;
import hamo.job.util.statusAndRole.OrderStatus;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final ShippingRepository shippingRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        UserService userService,
                        CartService cartService,
                        ShippingRepository shippingRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.cartService = cartService;
        this.shippingRepository = shippingRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByUserId(Long userId, PaginationDTO paginationDTO) {
        var pageable = PageRequest.of(paginationDTO.pageNumber(), paginationDTO.pageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserIdNotFoundException(userId));
        return orderRepository.findByUser(user, pageable)
                .map(OrderDTO::fromOrder);
    }

    @Transactional
    public Page<OrderDTO> getMyOrders(Long userId, Pageable pageable,
                                      @Nullable LocalDateTime from, @Nullable LocalDateTime to) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserIdNotFoundException(userId));
        Page<Order> page = (from != null && to != null)
                ? orderRepository.findByUserIdAndCreatedAtBetween(userId, from, to, pageable)
                : orderRepository.findByUser(user, pageable);
        return page.map(OrderDTO::fromOrder);
    }

    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequestDTO createOrderRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserIdNotFoundException(userId));
        Shipping shipping = shippingRepository.findById(createOrderRequest.shippingId())
                .orElseThrow(() -> new ShippingIdNotFoundException(createOrderRequest.shippingId()));
        CartDTO cartDTO = cartService.getCartByUserId(userId);
        if (cartDTO.items() == null || cartDTO.items().isEmpty()) {
            throw new CartEmptyException("Cannot create order: Cart is empty");
        }
        Order order = new Order();
        order.setUser(user);
        order.setShipping(shipping);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setTotalAmount(createOrderRequest.totalAmount());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        double calculatedTotal = 0.0;
        for (CartLineDTO cartLine : cartDTO.items()) {
            OrderItem orderItem = new OrderItem();
            Product product = productRepository.findById(cartLine.productId())
                    .orElseThrow(() -> new ProductIdNotFoundException(cartLine.productId()));
            orderItem.setProduct(product);
            int quantity = cartLine.quantity();
            orderItem.setQty(quantity);
            orderItem.setUnitPrie(new BigDecimal(cartLine.price()));
            orderItem.setSubtotal(Double.parseDouble(cartLine.price()) * quantity);
            calculatedTotal += orderItem.getSubtotal();
            order.addItem(orderItem);
        }
        if (Math.abs(calculatedTotal - createOrderRequest.totalAmount()) > 0.01) {
            throw new OrderTotalMismatchException(calculatedTotal, createOrderRequest.totalAmount());
        }
        order = orderRepository.save(order);
        cartService.clearCart(userId);
        return OrderDTO.fromOrder(order);
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderIdNotFoundException(id));
        return OrderDTO.fromOrder(order);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderIdNotFoundException(orderId));
        order.setOrderStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);
        return OrderDTO.fromOrder(order);
    }
}
