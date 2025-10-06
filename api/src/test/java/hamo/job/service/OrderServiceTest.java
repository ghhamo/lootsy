package hamo.job.service;

import hamo.job.dto.*;
import hamo.job.entity.*;
import hamo.job.entity.Order;
import hamo.job.exception.exceptions.cartException.CartEmptyException;
import hamo.job.exception.exceptions.orderException.OrderIdNotFoundException;
import hamo.job.exception.exceptions.orderException.OrderTotalMismatchException;
import hamo.job.exception.exceptions.productException.ProductIdNotFoundException;
import hamo.job.exception.exceptions.userException.UserIdNotFoundException;
import hamo.job.repository.*;
import hamo.job.util.statusAndRole.OrderStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    UserService userService;
    @Mock
    CartService cartService;
    @Mock
    ShippingRepository shippingRepository;
    @Mock
    ProductRepository productRepository;

    @InjectMocks
    OrderService orderService;

    @Test
    void getOrdersByUserIdAndReturnsMappedDtos() {
        PaginationDTO pg = new PaginationDTO(0, 2);
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Order order = new Order();
        order.setId(1L);
        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findByUser(eq(user), any(Pageable.class))).thenReturn(page);
        try (MockedStatic<OrderDTO> ms = mockStatic(OrderDTO.class)) {
            OrderDTO dto = new OrderDTO(1L, OrderStatus.PENDING, 100.0,
                    LocalDateTime.now(), LocalDateTime.now(), 1L, Set.of());
            ms.when(() -> OrderDTO.fromOrder(order)).thenReturn(dto);
            Page<OrderDTO> result = orderService.getOrdersByUserId(1L, pg);
            assertEquals(1, result.getTotalElements());
            assertSame(dto, result.getContent().getFirst());
        }
    }

    @Test
    void getOrdersByUserIdUserNotFoundThrows() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(UserIdNotFoundException.class,
                () -> orderService.getOrdersByUserId(5L, new PaginationDTO(0, 1)));
    }

    @Test
    void getMyOrdersMapsDtosNoDateFilter() {
        Pageable pageable = PageRequest.of(0, 1);
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Order order = new Order();
        order.setId(9L);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findByUser(eq(user), eq(pageable))).thenReturn(page);
        try (MockedStatic<OrderDTO> ms = mockStatic(OrderDTO.class)) {
            OrderDTO dto = new OrderDTO(9L, OrderStatus.PENDING, 55.0,
                    LocalDateTime.now(), LocalDateTime.now(), 1L, Set.of());
            ms.when(() -> OrderDTO.fromOrder(order)).thenReturn(dto);
            Page<OrderDTO> result = orderService.getMyOrders(1L, pageable, null, null);
            assertEquals(1, result.getTotalElements());
            assertSame(dto, result.getContent().getFirst());
            verify(orderRepository).findByUser(eq(user), eq(pageable));
            verify(orderRepository, never()).findByUserIdAndCreatedAtBetween(anyLong(), any(), any(), any());
        }
    }


    @Test
    void getMyOrdersMapsDtosWithDateFilterAndUsesBetweenQuery() {
        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        LocalDateTime from = OffsetDateTime.parse("2025-10-01T00:00:00Z").toLocalDateTime();
        LocalDateTime to = OffsetDateTime.parse("2025-10-05T23:59:59Z").toLocalDateTime();
        Order order = new Order();
        order.setId(42L);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findByUserIdAndCreatedAtBetween(1L, from, to, pageable)).thenReturn(page);
        try (MockedStatic<OrderDTO> ms = mockStatic(OrderDTO.class)) {
            OrderDTO dto = new OrderDTO(42L, OrderStatus.COMPLETED, 10.0,
                    LocalDateTime.now(), LocalDateTime.now(), 1L, Set.of());
            ms.when(() -> OrderDTO.fromOrder(order)).thenReturn(dto);
            Page<OrderDTO> result = orderService.getMyOrders(1L, pageable, from, to);
            assertEquals(1, result.getTotalElements());
            assertEquals(42L, result.getContent().getFirst().id());
            verify(orderRepository).findByUserIdAndCreatedAtBetween(1L, from, to, pageable);
            verify(orderRepository, never()).findByUser(any(), any());
        }
    }


    @Test
    void createOrderSuccessCreatesOrderAndClearsCart() {
        Long userId = 1L;
        Long shippingId = 2L;
        User user = new User();
        user.setId(userId);
        Shipping shipping = new Shipping();
        shipping.setId(shippingId);
        Product product = new Product();
        product.setId(10L);
        product.setName("Laptop");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(shipping));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        CartLineDTO line = new CartLineDTO(10L, "Laptop",
                "50.0", 2, "img.jpg");
        CartDTO cart = new CartDTO(1L, userId, Set.of(line));
        when(cartService.getCartByUserId(userId)).thenReturn(cart);
        Order orderSaved = new Order();
        orderSaved.setId(99L);
        when(orderRepository.save(any(Order.class))).thenReturn(orderSaved);
        CreateOrderRequestDTO req = new CreateOrderRequestDTO(shippingId, 100.0, "USD");
        try (MockedStatic<OrderDTO> ms = mockStatic(OrderDTO.class)) {
            OrderDTO expected = new OrderDTO(99L, OrderStatus.PENDING, 100.0,
                    LocalDateTime.now(), LocalDateTime.now(), userId, Set.of());
            ms.when(() -> OrderDTO.fromOrder(orderSaved)).thenReturn(expected);
            OrderDTO actual = orderService.createOrder(userId, req);
            assertSame(expected, actual);
            verify(cartService).clearCart(userId);
        }
    }

    @Test
    void createOrderWhenCartEmptyThrows() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(shippingRepository.findById(1L)).thenReturn(Optional.of(new Shipping()));
        CartDTO cart = new CartDTO(1L, 1L, Set.of());
        when(cartService.getCartByUserId(1L)).thenReturn(cart);
        assertThrows(CartEmptyException.class,
                () -> orderService.createOrder(1L, new CreateOrderRequestDTO(1L, 10.0, "USD")));
    }

    @Test
    void createOrderWhenProductMissingAndThrows() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(shippingRepository.findById(1L)).thenReturn(Optional.of(new Shipping()));
        CartLineDTO line = new CartLineDTO(99L, "Missing",
                "1", 1, "img");
        CartDTO cart = new CartDTO(1L, 1L, Set.of(line));
        when(cartService.getCartByUserId(1L)).thenReturn(cart);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ProductIdNotFoundException.class,
                () -> orderService.createOrder(1L, new CreateOrderRequestDTO(1L, 10.0, "USD")));
    }

    @Test
    void createOrderWhenTotalMismatchAndThrows() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(shippingRepository.findById(2L)).thenReturn(Optional.of(new Shipping()));
        Product product = new Product();
        product.setId(1L);
        product.setName("Mouse");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        CartLineDTO line = new CartLineDTO(1L, "Mouse",
                "10", 1, "img");
        CartDTO cart = new CartDTO(1L, userId, Set.of(line));
        when(cartService.getCartByUserId(userId)).thenReturn(cart);
        CreateOrderRequestDTO req = new CreateOrderRequestDTO(2L, 50.0, "USD");
        assertThrows(OrderTotalMismatchException.class,
                () -> orderService.createOrder(userId, req));
    }

    @Test
    void getOrderByIdFoundAndReturnsDto() {
        Order order = new Order();
        order.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        try (MockedStatic<OrderDTO> ms = mockStatic(OrderDTO.class)) {
            OrderDTO dto = new OrderDTO(1L, OrderStatus.PENDING, 10.0,
                    LocalDateTime.now(), LocalDateTime.now(), 1L, Set.of());
            ms.when(() -> OrderDTO.fromOrder(order)).thenReturn(dto);
            assertSame(dto, orderService.getOrderById(1L));
        }
    }

    @Test
    void getOrderByIdNotFoundAndThrows() {
        when(orderRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(OrderIdNotFoundException.class,
                () -> orderService.getOrderById(5L));
    }

    @Test
    void updateOrderStatusUpdatesAndReturnsDto() {
        Order order = new Order();
        order.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        try (MockedStatic<OrderDTO> ms = mockStatic(OrderDTO.class)) {
            OrderDTO dto = new OrderDTO(1L, OrderStatus.PAID, 10.0,
                    LocalDateTime.now(), LocalDateTime.now(), 1L, Set.of());
            ms.when(() -> OrderDTO.fromOrder(order)).thenReturn(dto);
            OrderDTO actual = orderService.updateOrderStatus(1L, OrderStatus.PAID);
            assertSame(dto, actual);
            assertEquals(OrderStatus.PAID, order.getOrderStatus());
        }
    }

    @Test
    void updateOrderStatusNotFoundAndThrows() {
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(OrderIdNotFoundException.class,
                () -> orderService.updateOrderStatus(2L, OrderStatus.CANCELLED));
    }
}
