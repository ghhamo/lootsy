package hamo.job.controller;

import hamo.job.entity.Category;
import hamo.job.service.CategoryService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
    }

    @Test
    @DisplayName("GET /api/categories -> 200 OK and returns list of categories")
    void getAllCategoriesAndReturnsList() throws Exception {
        Category cat1 = new Category("Electronics", "Devices", LocalDateTime.now());
        cat1.setId(1L);
        Category cat2 = new Category("Books", "Fiction & Non-fiction", LocalDateTime.now());
        cat2.setId(2L);
        when(categoryService.getAllCategories()).thenReturn(List.of(cat1, cat2));
        mockMvc.perform(get("/api/categories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Electronics"))
                .andExpect(jsonPath("$[0].description").value("Devices"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Books"))
                .andExpect(jsonPath("$[1].description").value("Fiction & Non-fiction"));
        verify(categoryService, times(1)).getAllCategories();
        verifyNoMoreInteractions(categoryService);
    }

    @Test
    @DisplayName("GET /api/categories -> 200 OK and returns empty list when no categories exist")
    void getAllCategoriesEmptyList() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of());
        mockMvc.perform(get("/api/categories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
        verify(categoryService, times(1)).getAllCategories();
        verifyNoMoreInteractions(categoryService);
    }
}
