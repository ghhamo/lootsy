package hamo.job.controller;

import hamo.job.dto.CreateOrderRequestDTO;
import hamo.job.dto.OrderDTO;
import hamo.job.dto.PaginationDTO;
import hamo.job.service.JwtService;
import hamo.job.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final JwtService jwtService;

    @Value("${page.max.size}")
    private Integer pageMaxSize;

    @Autowired
    public OrderController(OrderService orderService, JwtService jwtService) {
        this.orderService = orderService;
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @Valid @RequestBody CreateOrderRequestDTO createOrderRequest,
            HttpServletRequest request) {

        String token = extractTokenFromRequest(request);
        Long userId = jwtService.extractUserId(token);

        OrderDTO createdOrder = orderService.createOrder(userId, createOrderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping
    public ResponseEntity<Iterable<OrderDTO>> getAllOrders(
            @RequestParam int pageIndex,
            @RequestParam int pageSize) {

        if (pageMaxSize < pageSize) {
            throw new IllegalStateException("Page size exceeds maximum allowed size");
        }

        Iterable<OrderDTO> orders = orderService.getAllOrders(new PaginationDTO(pageIndex, pageSize));
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        try {
            OrderDTO order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user")
    public ResponseEntity<Iterable<OrderDTO>> getUserOrders(
            @RequestParam int pageIndex,
            @RequestParam int pageSize,
            HttpServletRequest request) {
        if (pageMaxSize < pageSize) {
            throw new IllegalStateException("Page size exceeds maximum allowed size");
        }
        String token = extractTokenFromRequest(request);
        Long userId = jwtService.extractUserId(token);
        Iterable<OrderDTO> orders = orderService.getOrdersByUserId(userId, new PaginationDTO(pageIndex, pageSize));
        return ResponseEntity.ok(orders);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new RuntimeException("No valid JWT token found in request");
    }
}
