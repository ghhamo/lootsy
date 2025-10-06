package hamo.job.controller;

import hamo.job.dto.CreateOrderRequestDTO;
import hamo.job.dto.OrderDTO;
import hamo.job.dto.PaginationDTO;
import hamo.job.exception.exceptions.token.InvalidJwtTokenException;
import hamo.job.service.JwtService;
import hamo.job.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;

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
            @Valid @RequestBody CreateOrderRequestDTO createOrderRequest, HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        Long userId = jwtService.extractUserId(token);
        OrderDTO createdOrder = orderService.createOrder(userId, createOrderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
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
        String token = extractTokenFromRequest(request);
        Long userId = jwtService.extractUserId(token);
        Page<OrderDTO> orders = orderService.getOrdersByUserId(userId, new PaginationDTO(pageIndex, pageSize));
        return ResponseEntity.ok(orders.getContent());
    }

    @GetMapping("/me")
    public Page<OrderDTO> getMyOrders(
            HttpServletRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        String token = extractTokenFromRequest(request);
        Long userId = jwtService.extractUserId(token);
        LocalDateTime fromBound = from != null ? from : LocalDateTime.MIN;
        LocalDateTime toBound   = to   != null ? to   : LocalDateTime.now();
        return orderService.getMyOrders(userId, pageable,
                (from != null || to != null) ? fromBound : null,
                (from != null || to != null) ? toBound   : null);
    }


    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new InvalidJwtTokenException("No valid JWT token found in request");
    }
}
