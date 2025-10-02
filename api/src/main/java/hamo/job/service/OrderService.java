package hamo.job.service;

import hamo.job.dto.*;
import hamo.job.entity.*;
import hamo.job.exception.exceptions.productException.ProductIdNotFoundException;
import hamo.job.exception.exceptions.userException.UserIdNotFoundException;
import hamo.job.repository.*;
import hamo.job.util.statusAndRole.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final ShippingRepository shippingRepository;
    private final ProductRepository productRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                       UserRepository userRepository,
                       CartService cartService,
                       ShippingRepository shippingRepository,
                       ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.shippingRepository = shippingRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequestDTO createOrderRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserIdNotFoundException(userId));
        shippingRepository.findById(createOrderRequest.shippingId())
                .orElseThrow(() -> new RuntimeException("Shipping information not found with ID: " + createOrderRequest.shippingId()));
        CartDTO cartDTO = cartService.getCartByUserId(userId);
        if (cartDTO.items() == null || cartDTO.items().isEmpty()) {
            throw new RuntimeException("Cannot create order: Cart is empty");
        }
        Order order = new Order();
        order.setUser(user);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setTotalAmount(createOrderRequest.totalAmount());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);
        double calculatedTotal = 0.0;
        for (CartLineDTO cartLine : cartDTO.items()) {
            OrderItem orderItem = new OrderItem();
            Product product = productRepository.findById(cartLine.productId())
                    .orElseThrow(() -> new ProductIdNotFoundException(cartLine.productId()));
            orderItem.setProduct(product);
            double unitPrice = Double.parseDouble(cartLine.price());
            int quantity = cartLine.quantity();
            orderItem.setQty(quantity);
            orderItem.setUnitPrie(unitPrice);
            orderItem.setSubtotal(unitPrice * quantity);
            calculatedTotal += orderItem.getSubtotal();
            order.addItem(orderItem);
        }
        if (Math.abs(calculatedTotal - createOrderRequest.totalAmount()) > 0.01) {
            throw new RuntimeException("Order total mismatch. Expected: " + calculatedTotal + ", Provided: " + createOrderRequest.totalAmount());
        }
        order = orderRepository.save(order);
        cartService.clearCart(userId);
        return OrderDTO.fromOrder(order);
    }

    @Transactional(readOnly = true)
    public Iterable<OrderDTO> getAllOrders(PaginationDTO paginationDTO) {
        PageRequest pageRequest = PageRequest.of(paginationDTO.pageNumber(), paginationDTO.pageSize());
        Page<Order> orders = orderRepository.findAll(pageRequest);
        return OrderDTO.mapOrderSetToOrderDto(orders.getContent());
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
        return OrderDTO.fromOrder(order);
    }

    @Transactional(readOnly = true)
    public Iterable<OrderDTO> getOrdersByUserId(Long userId, PaginationDTO paginationDTO) {
        userRepository.findById(userId).orElseThrow(() -> new UserIdNotFoundException(userId));
        PageRequest pageRequest = PageRequest.of(paginationDTO.pageNumber(), paginationDTO.pageSize());
        Page<Order> allOrders = orderRepository.findAll(pageRequest);
        Set<OrderDTO> userOrders = new HashSet<>();
        for (Order order : allOrders.getContent()) {
            if (order.getUser() != null && order.getUser().getId().equals(userId)) {
                userOrders.add(OrderDTO.fromOrder(order));
            }
        }
        return userOrders;
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        order.setOrderStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);
        
        return OrderDTO.fromOrder(order);
    }
}
