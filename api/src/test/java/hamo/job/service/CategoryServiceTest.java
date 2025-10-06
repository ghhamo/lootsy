package hamo.job.service;

import hamo.job.entity.Category;
import hamo.job.repository.CategoryRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void saveAllSavesCategoriesAndReturnsList() {
        List<Category> categories = List.of(
                new Category("Tech", "Technology", LocalDateTime.now()),
                new Category("Books", "All kinds of books", LocalDateTime.now()));
        when(categoryRepository.saveAll(categories)).thenReturn(categories);
        List<Category> result = categoryService.saveAll(categories);
        verify(categoryRepository).saveAll(categories);
        assertEquals(2, result.size());
        assertEquals("Tech", result.get(0).getName());
    }

    @Test
    void getAllCategoriesAndReturnsListFromRepository() {
        List<Category> expected = List.of(
                new Category("Sports", "Outdoor and indoor sports", LocalDateTime.now()));
        when(categoryRepository.findAll()).thenReturn(expected);
        List<Category> actual = categoryService.getAllCategories();
        verify(categoryRepository).findAll();
        assertEquals(expected, actual);
        assertEquals("Sports", actual.get(0).getName());
    }

    @Test
    void getAllCategoriesEmptyListWhenRepositoryEmpty() {
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());
        List<Category> actual = categoryService.getAllCategories();
        verify(categoryRepository).findAll();
        assertTrue(actual.isEmpty());
    }
}
