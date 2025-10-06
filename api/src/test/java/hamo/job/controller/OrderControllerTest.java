package hamo.job.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import hamo.job.dto.CreateOrderRequestDTO;
import hamo.job.dto.OrderDTO;
import hamo.job.dto.OrderItemDTO;
import hamo.job.dto.PaginationDTO;
import hamo.job.exception.handler.ApiExceptionHandler;
import hamo.job.service.JwtService;
import hamo.job.service.OrderService;
import hamo.job.util.statusAndRole.OrderStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private OrderService orderService;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setup() throws Exception {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders
                .standaloneSetup(orderController)
                .setControllerAdvice(new ApiExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
        var field = OrderController.class.getDeclaredField("pageMaxSize");
        field.setAccessible(true);
        field.set(orderController, 50);
    }

    @Test
    @DisplayName("POST /api/orders -> 201 CREATED and returns created OrderDTO")
    void createOrderAndReturnsCreatedOrder() throws Exception {
        CreateOrderRequestDTO requestDTO = new CreateOrderRequestDTO(10L, 150.0, "USD");
        OrderItemDTO item = new OrderItemDTO(
                1L, 2L, "Keyboard", 1, BigDecimal.valueOf(150.0), 150.0);
        OrderDTO responseDTO = new OrderDTO(
                1L, null, 150.0, LocalDateTime.now(), null, 5L, Set.of(item));
        when(jwtService.extractUserId(anyString())).thenReturn(5L);
        when(orderService.createOrder(eq(5L), any(CreateOrderRequestDTO.class))).thenReturn(responseDTO);
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer testtoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.totalAmount").value(150.0))
                .andExpect(jsonPath("$.userId").value(5));
    }


    @Test
    @DisplayName("GET /api/orders/{id} -> 200 OK when found")
    void getOrderByIdAndReturnsOrder() throws Exception {
        OrderDTO dto = new OrderDTO(1L, null, 100.0, LocalDateTime.now(), null, 7L, Set.of());
        when(orderService.getOrderById(1L)).thenReturn(dto);
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(7));
    }

    @Test
    @DisplayName("GET /api/orders/{id} -> 404 when service throws exception")
    void getOrderByIdNotFound() throws Exception {
        when(orderService.getOrderById(99L)).thenThrow(new RuntimeException("Not found"));
        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound());
    }
    @Test
    @DisplayName("GET /api/orders/user -> 200 OK with user orders")
    void getUserOrdersAndReturnsPagedOrders() throws Exception {
        OrderDTO order1 = new OrderDTO(1L, null, 200.0, LocalDateTime.now(), null, 2L, Set.of());
        OrderDTO order2 = new OrderDTO(2L, null, 300.0, LocalDateTime.now(), null, 2L, Set.of());
        Page<OrderDTO> page = new PageImpl<>(List.of(order1, order2));

        when(jwtService.extractUserId("userToken")).thenReturn(2L);
        when(orderService.getOrdersByUserId(eq(2L), any(PaginationDTO.class))).thenReturn(page);

        mockMvc.perform(get("/api/orders/user")
                        .header("Authorization", "Bearer userToken")
                        .param("pageIndex", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    private Page<OrderDTO> samplePage(Pageable p) {
        var c1 = new OrderDTO(1L, OrderStatus.PENDING, 43.34, LocalDateTime.now(), LocalDateTime.now(), 1L, Set.of());
        var c2 = new OrderDTO(2L, OrderStatus.PENDING, 433.34, LocalDateTime.now(), LocalDateTime.now(), 2L, Set.of());
        return new PageImpl<>(List.of(c1, c2), p, 40); // totalElements=40 (arbitrary)
    }

    private static final String AUTH = "Bearer testtoken";
    private static final long USER_ID = 123L;

    @Test
    void getMyOrdersDefaultPagingApplied() throws Exception {
        when(jwtService.extractUserId("testtoken")).thenReturn(USER_ID);
        when(orderService.getMyOrders(eq(USER_ID), any(Pageable.class), isNull(), isNull()))
                .thenAnswer(inv -> samplePage(inv.getArgument(1)));
        mockMvc.perform(get("/api/orders/me")
                        .header("Authorization", AUTH)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[1].id", is(2)))
                .andExpect(jsonPath("$.size", is(20)))   // default size
                .andExpect(jsonPath("$.number", is(0))); // default page
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(orderService).getMyOrders(userIdCaptor.capture(), pageableCaptor.capture(),
                fromCaptor.capture(), toCaptor.capture());
        Pageable p = pageableCaptor.getValue();
        Assertions.assertEquals(USER_ID, userIdCaptor.getValue());
        Assertions.assertEquals(0, p.getPageNumber(), "default page should be 0");
        Assertions.assertEquals(20, p.getPageSize(), "default size should be 20");
        Sort.Order order = p.getSort().getOrderFor("createdAt");
        Assertions.assertNotNull(order, "createdAt sort should be present by default");
        Assertions.assertEquals(Sort.Direction.DESC, order.getDirection());
        Assertions.assertNull(fromCaptor.getValue(), "from should be null when not provided");
        Assertions.assertNull(toCaptor.getValue(), "to should be null when not provided");
    }

    @Test
    void getMyOrdersCustomPagingAndSort() throws Exception {
        when(jwtService.extractUserId("testtoken")).thenReturn(USER_ID);
        when(orderService.getMyOrders(eq(USER_ID), any(Pageable.class), isNull(), isNull()))
                .thenAnswer(inv -> samplePage(inv.getArgument(1)));
        mockMvc.perform(get("/api/orders/me")
                        .header("Authorization", AUTH)
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "createdAt,desc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.size", is(5)))
                .andExpect(jsonPath("$.number", is(1)))
                .andExpect(jsonPath("$.sort.sorted", is(true)));
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(orderService).getMyOrders(eq(USER_ID), pageableCaptor.capture(), isNull(), isNull());
        Pageable p = pageableCaptor.getValue();
        Assertions.assertEquals(1, p.getPageNumber());
        Assertions.assertEquals(5, p.getPageSize());
        Sort.Order createdAt = p.getSort().getOrderFor("createdAt");
        Assertions.assertNotNull(createdAt);
        Assertions.assertEquals(Sort.Direction.DESC, createdAt.getDirection());
    }

    @Test
    void getMyOrdersWithDateRangeForwardsBounds() throws Exception {
        when(jwtService.extractUserId("testtoken")).thenReturn(USER_ID);
        when(orderService.getMyOrders(eq(USER_ID), any(Pageable.class), any(), any()))
                .thenAnswer(inv -> samplePage(inv.getArgument(1)));
        String from = "2025-10-01T00:00:00Z";
        String to = "2025-10-05T23:59:59Z";
        mockMvc.perform(get("/api/orders/me")
                        .header("Authorization", AUTH)
                        .param("from", from)
                        .param("to", to)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is(1)));
        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(orderService).getMyOrders(eq(USER_ID), pageableCaptor.capture(),
                fromCaptor.capture(), toCaptor.capture());
        Assertions.assertEquals(
                OffsetDateTime.parse(from).toLocalDateTime(),
                fromCaptor.getValue()
        );
        Assertions.assertEquals(
                OffsetDateTime.parse(to).toLocalDateTime(),
                toCaptor.getValue()
        );
        Pageable p = pageableCaptor.getValue();
        Assertions.assertEquals(0, p.getPageNumber());
        Assertions.assertEquals(20, p.getPageSize());
    }

    @Test
    @DisplayName("POST /api/orders without Bearer token -> 401 with JSON error")
    void createOrderWithoutTokenAndReturns401() throws Exception {
        CreateOrderRequestDTO requestDTO = new CreateOrderRequestDTO(1L, 100.0, "USD");
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("No valid JWT token found in request"));
    }
}