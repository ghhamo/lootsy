package hamo.job.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hamo.job.dto.ShippingDTO;
import hamo.job.exception.handler.ApiExceptionHandler;
import hamo.job.service.ShippingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ShippingControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ShippingService shippingService;

    @InjectMocks
    private ShippingController shippingController;

    @BeforeEach
    void setup() throws Exception {
        mockMvc = MockMvcBuilders
                .standaloneSetup(shippingController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
        var field = ShippingController.class.getDeclaredField("pageMaxSize");
        field.setAccessible(true);
        field.set(shippingController, 50);
    }

    @Test
    @DisplayName("POST /api/shippings -> 201 CREATED and returns created shipping")
    void createShippingAndReturnsCreated() throws Exception {
        ShippingDTO request = new ShippingDTO(null, "John", "Doe", "USA", "NY", "Main St", "123456789");
        ShippingDTO response = new ShippingDTO(1L, "John", "Doe", "USA", "NY", "Main St", "123456789");
        when(shippingService.createShipping(any(ShippingDTO.class))).thenReturn(response);
        mockMvc.perform(post("/api/shippings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.city").value("NY"));
        verify(shippingService).createShipping(any(ShippingDTO.class));
    }

    @Test
    @DisplayName("GET /api/shippings/{id} -> 200 OK when found")
    void getShippingByIdAndReturnsOk() throws Exception {
        ShippingDTO dto = new ShippingDTO(1L, "Anna", "Smith", "France", "Paris", "Rue 1", "987654321");
        when(shippingService.getShippingById(1L)).thenReturn(dto);
        mockMvc.perform(get("/api/shippings/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.country").value("France"));
        verify(shippingService).getShippingById(1L);
    }

    @Test
    @DisplayName("GET /api/shippings/{id} -> 404 when not found")
    void getShippingByIdNotFound() throws Exception {
        when(shippingService.getShippingById(5L)).thenThrow(new RuntimeException("Not found"));
        mockMvc.perform(get("/api/shippings/5"))
                .andExpect(status().isNotFound());
        verify(shippingService).getShippingById(5L);
    }

    @Test
    @DisplayName("PUT /api/shippings/{id} -> 200 OK when updated successfully")
    void updateShippingAndReturnsOk() throws Exception {
        ShippingDTO request = new ShippingDTO(null, "Updated", "User", "USA", "LA", "Sunset Blvd", "111222333");
        ShippingDTO updated = new ShippingDTO(2L, "Updated", "User", "USA", "LA", "Sunset Blvd", "111222333");
        when(shippingService.updateShipping(eq(2L), any(ShippingDTO.class))).thenReturn(updated);
        mockMvc.perform(put("/api/shippings/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.city").value("LA"));
        verify(shippingService).updateShipping(eq(2L), any(ShippingDTO.class));
    }

    @Test
    @DisplayName("PUT /api/shippings/{id} -> 404 when not found")
    void updateShippingNotFound() throws Exception {
        when(shippingService.updateShipping(eq(9L), any())).thenThrow(new RuntimeException("Not found"));
        mockMvc.perform(put("/api/shippings/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ShippingDTO(null, "A", "B", "C", "D", "E", "F"))))
                .andExpect(status().isNotFound());
        verify(shippingService).updateShipping(eq(9L), any());
    }

    @Test
    @DisplayName("DELETE /api/shippings/{id} -> 204 NO_CONTENT when deleted")
    void deleteShippingAndReturnsNoContent() throws Exception {
        when(shippingService.deleteShipping(1L)).thenReturn(true);
        mockMvc.perform(delete("/api/shippings/1"))
                .andExpect(status().isNoContent());
        verify(shippingService).deleteShipping(1L);
    }

    @Test
    @DisplayName("DELETE /api/shippings/{id} -> 404 when not found")
    void deleteShippingNotFound() throws Exception {
        when(shippingService.deleteShipping(2L)).thenReturn(false);
        mockMvc.perform(delete("/api/shippings/2"))
                .andExpect(status().isNotFound());
        verify(shippingService).deleteShipping(2L);
    }
}