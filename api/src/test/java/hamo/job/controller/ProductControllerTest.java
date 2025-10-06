package hamo.job.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hamo.job.dto.CreateProductDTO;
import hamo.job.dto.ProductDetailsDTO;
import hamo.job.dto.ProductHomePageDTO;
import hamo.job.exception.handler.ApiExceptionHandler;
import hamo.job.service.ProductService;
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

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private ProductService productService;
    @InjectMocks
    private ProductController productController;
    @BeforeEach
    void setup() throws Exception {
        mockMvc = MockMvcBuilders
                .standaloneSetup(productController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
        var f = ProductController.class.getDeclaredField("pageMaxSize");
        f.setAccessible(true);
        f.set(productController, 50);
    }

    @Test
    @DisplayName("POST /api/products -> 200 OK and calls productService.create()")
    void createProductAndCallsService() throws Exception {
        CreateProductDTO dto = new CreateProductDTO("Phone", "799.99", 1L);
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
        verify(productService).create(any(CreateProductDTO.class));
    }

    @Test
    @DisplayName("GET /api/products -> 200 OK and returns products list")
    void getAllProductsAndReturnsList() throws Exception {
        ProductHomePageDTO p1 = new ProductHomePageDTO(1L, "Keyboard", "50", "/img/1");
        ProductHomePageDTO p2 = new ProductHomePageDTO(2L, "Mouse", "30", "/img/2");
        when(productService.getProducts(any())).thenReturn(List.of(p1, p2));
        mockMvc.perform(get("/api/products")
                        .param("pageIndex", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].name").value("Mouse"));
    }

    @Test
    @DisplayName("GET /api/products with search param -> calls productService.searchProducts()")
    void getAllProductsWithSearch() throws Exception {
        when(productService.searchProducts(anyString(), any())).thenReturn(Set.of());
        mockMvc.perform(get("/api/products")
                        .param("pageIndex", "0")
                        .param("pageSize", "10")
                        .param("search", "keyboard"))
                .andExpect(status().isOk());
        verify(productService).searchProducts(eq("keyboard"), any());
    }

    @Test
    @DisplayName("GET /api/products with category IDs -> calls productService.getProductsByCategories()")
    void getAllProductsWithCategories() throws Exception {
        when(productService.getProductsByCategories(anyList(), any())).thenReturn(Set.of());
        mockMvc.perform(get("/api/products")
                        .param("pageIndex", "0")
                        .param("pageSize", "10")
                        .param("categories", "1", "2"))
                .andExpect(status().isOk());
        verify(productService).getProductsByCategories(eq(List.of(1L, 2L)), any());
    }

    @Test
    @DisplayName("GET /api/products/{id} -> 200 OK and returns product by ID")
    void getProductByIdAndReturnsProduct() throws Exception {
        ProductHomePageDTO dto = new ProductHomePageDTO(1L, "Phone", "799", "/img");
        when(productService.getProductById(1L)).thenReturn(dto);
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Phone"));
    }

    @Test
    @DisplayName("GET /api/products/by/{id} -> 200 OK and returns detailed product")
    void getByIdForDetailsAndReturnsDetails() throws Exception {
        ProductDetailsDTO details = new ProductDetailsDTO(
                "Laptop", "1200", "Electronics", "Good laptop", "/img");
        when(productService.getByIdForDetails(5L)).thenReturn(details);
        mockMvc.perform(get("/api/products/by/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.categoryName").value("Electronics"))
                .andExpect(jsonPath("$.price").value(1200));
    }

    @Test
    @DisplayName("GET /api/products with pageSize > max -> 400 BAD_REQUEST")
    void getAllProductsAndPageTooLarge() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("pageIndex", "0")
                        .param("pageSize", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Page size exceeds maximum allowed size"));
    }
}
