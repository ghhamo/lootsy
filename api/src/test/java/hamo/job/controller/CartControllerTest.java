
package hamo.job.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hamo.job.dto.AddToCartRequestDTO;
import hamo.job.dto.CartDTO;
import hamo.job.dto.CartLineDTO;
import hamo.job.service.CartService;
import hamo.job.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CartService cartService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CartController cartController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
    }

    @Test
    @DisplayName("POST /api/carts -> 201 CREATED and calls cartService.create(userId)")
    void createCartAndReturns201() throws Exception {
        long userId = 42L;
        when(userService.getCurrentUserId()).thenReturn(userId);
        mockMvc.perform(post("/api/carts")).andExpect(status().isCreated());
        verify(userService).getCurrentUserId();
        verify(cartService).create(userId);
        verifyNoMoreInteractions(cartService, userService);
    }

    @Test
    @DisplayName("GET /api/carts/current -> 200 OK with CartDTO body for current user")
    void getCurrentUserCartAndReturnsCartDTO() throws Exception {
        long userId = 7L;
        when(userService.getCurrentUserId()).thenReturn(userId);
        CartLineDTO line = new CartLineDTO(
                10L, "Sample", "19.99", 2, "/images/products/sample.png");
        CartDTO dto = new CartDTO(100L, userId, Set.of(line));
        when(cartService.getCartByUserId(userId)).thenReturn(dto);
        mockMvc.perform(get("/api/carts/current"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.userId").value((int) userId))
                .andExpect(jsonPath("$.items[0].productId").value(10))
                .andExpect(jsonPath("$.items[0].name").value("Sample"))
                .andExpect(jsonPath("$.items[0].price").value(19.99))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].imageUrl").value("/images/products/sample.png"));
        verify(userService).getCurrentUserId();
        verify(cartService).getCartByUserId(userId);
        verifyNoMoreInteractions(cartService, userService);
    }

    @Test
    @DisplayName("POST /api/carts/add -> 201 CREATED and calls cartService.add(userId, body)")
    void addToCartAndReturns201() throws Exception {
        long userId = 123L;
        when(userService.getCurrentUserId()).thenReturn(userId);
        AddToCartRequestDTO req = new AddToCartRequestDTO(55L, 3);
        String body = objectMapper.writeValueAsString(req);
        mockMvc.perform(post("/api/carts/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
        ArgumentCaptor<AddToCartRequestDTO> captor = ArgumentCaptor.forClass(AddToCartRequestDTO.class);
        verify(userService).getCurrentUserId();
        verify(cartService).add(eq(userId), captor.capture());
        AddToCartRequestDTO captured = captor.getValue();
        assertThat(captured.productId()).isEqualTo(55L);
        assertThat(captured.quantity()).isEqualTo(3);
        verifyNoMoreInteractions(cartService, userService);
    }

    @Test
    @DisplayName("DELETE /api/carts/remove/{productId} -> 204 NO CONTENT and calls cartService.removeProduct")
    void removeProductAndReturns204() throws Exception {
        long userId = 1L;
        long productId = 999L;
        when(userService.getCurrentUserId()).thenReturn(userId);
        mockMvc.perform(delete("/api/carts/remove/{productId}", productId))
                .andExpect(status().isNoContent());
        verify(userService).getCurrentUserId();
        verify(cartService).removeProduct(userId, productId);
        verifyNoMoreInteractions(cartService, userService);
    }
}