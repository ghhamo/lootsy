package hamo.job.service;

import hamo.job.dto.CreateProductDTO;
import hamo.job.dto.PaginationDTO;
import hamo.job.dto.ProductDetailsDTO;
import hamo.job.dto.ProductHomePageDTO;
import hamo.job.entity.Category;
import hamo.job.entity.Product;
import hamo.job.exception.exceptions.categoryException.CategoryIdNotFoundException;
import hamo.job.exception.exceptions.productException.ProductIdNotFoundException;
import hamo.job.repository.CategoryRepository;
import hamo.job.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    ProductRepository productRepository;
    @Mock
    CategoryRepository categoryRepository;
    @InjectMocks
    ProductService productService;

    @Test
    void getProductsAndReturnsMappedDtos() {
        PaginationDTO pagination = new PaginationDTO(0, 4);
        Page<Product> page = new PageImpl<>(List.of(new Product(), new Product()));
        when(productRepository.findAll(PageRequest.of(0, 4))).thenReturn(page);
        Set<ProductHomePageDTO> expected = Set.of(
                new ProductHomePageDTO(1L, "a", "43", "img"),
                new ProductHomePageDTO(2L, "b", "34", "img2")
        );
        try (MockedStatic<ProductHomePageDTO> ms = mockStatic(ProductHomePageDTO.class)) {
            ms.when(() -> ProductHomePageDTO.mapProductSetToProductHomePageDto(page)).thenReturn(expected);
            Iterable<ProductHomePageDTO> actual = productService.getProducts(pagination);
            assertSame(expected, actual);
        }
    }

    @Test
    void createSavesProductWithCategory() {
        Category category = new Category();
        category.setId(5L);
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        CreateProductDTO dto = new CreateProductDTO("n", "10.0", 5L);
        Product product = new Product();
        try (MockedStatic<CreateProductDTO> ms = mockStatic(CreateProductDTO.class)) {
            ms.when(() -> CreateProductDTO.toProduct(dto)).thenReturn(product);
            productService.create(dto);
            verify(productRepository).save(product);
            assertSame(category, product.getCategory());
        }
    }

    @Test
    void createWhenCategoryMissingAndThrows() {
        CreateProductDTO dto = new CreateProductDTO("n", "10.0", 9L);
        when(categoryRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(CategoryIdNotFoundException.class, () -> productService.create(dto));
    }

    @Test
    void getCountDelegatesToRepository() {
        when(productRepository.count()).thenReturn(42L);
        assertEquals(42L, productService.getCount());
    }

    @Test
    void getProductByIdFoundAndMapsToDto() {
        Product product = new Product();
        product.setId(3L);
        when(productRepository.findById(3L)).thenReturn(Optional.of(product));
        ProductHomePageDTO dto = new ProductHomePageDTO(3L, "x", "10.0", "img");
        try (MockedStatic<ProductHomePageDTO> ms = mockStatic(ProductHomePageDTO.class)) {
            ms.when(() -> ProductHomePageDTO.fromProduct(product)).thenReturn(dto);
            ProductHomePageDTO actual = productService.getProductById(3L);
            assertSame(dto, actual);
        }
    }

    @Test
    void getProductByIdNotFoundAndThrows() {
        when(productRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(ProductIdNotFoundException.class, () -> productService.getProductById(9L));
    }

    @Test
    void getByIdForDetailsAndMapsToDetailsDto() {
        Product product = new Product();
        product.setId(11L);
        when(productRepository.findById(11L)).thenReturn(Optional.of(product));
        ProductDetailsDTO dto = new ProductDetailsDTO("n", "10.0", "d", "2.0", "img");
        try (MockedStatic<ProductDetailsDTO> ms = mockStatic(ProductDetailsDTO.class)) {
            ms.when(() -> ProductDetailsDTO.fromProduct(product)).thenReturn(dto);
            ProductDetailsDTO actual = productService.getByIdForDetails(11L);
            assertSame(dto, actual);
        }
    }

    @Test
    void getByIdForDetailsNotFoundAndThrows() {
        when(productRepository.findById(12L)).thenReturn(Optional.empty());
        assertThrows(ProductIdNotFoundException.class, () -> productService.getByIdForDetails(12L));
    }

    @Test
    void saveAllCallsRepository() {
        List<Product> products = List.of(new Product(), new Product());
        productService.saveAll(products);
        verify(productRepository).saveAll(products);
    }

    @Test
    void searchProductsAndMapsToDto() {
        PaginationDTO pg = new PaginationDTO(0, 2);
        Page<Product> page = new PageImpl<>(List.of(new Product()));
        when(productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("test", PageRequest.of(0, 2))).thenReturn(page);
        Set<ProductHomePageDTO> expected = Set.of(new ProductHomePageDTO(1L, "x", "3.0", "i"));
        try (MockedStatic<ProductHomePageDTO> ms = mockStatic(ProductHomePageDTO.class)) {
            ms.when(() -> ProductHomePageDTO.mapProductSetToProductHomePageDto(page)).thenReturn(expected);
            Iterable<ProductHomePageDTO> actual = productService.searchProducts("test", pg);
            assertSame(expected, actual);
        }
    }

    @Test
    void getProductsByCategoriesAndMapsToDto() {
        PaginationDTO pg = new PaginationDTO(0, 3);
        Page<Product> page = new PageImpl<>(List.of(new Product()));
        when(productRepository.findByCategoryIdIn(List.of(1L, 2L), PageRequest.of(0, 3))).thenReturn(page);
        Set<ProductHomePageDTO> expected = Set.of(new ProductHomePageDTO(1L, "n", "2.0", "i"));
        try (MockedStatic<ProductHomePageDTO> ms = mockStatic(ProductHomePageDTO.class)) {
            ms.when(() -> ProductHomePageDTO.mapProductSetToProductHomePageDto(page)).thenReturn(expected);
            Iterable<ProductHomePageDTO> actual = productService.getProductsByCategories(List.of(1L, 2L), pg);
            assertSame(expected, actual);
        }
    }

    @Test
    void getProductsByPriceRangeAndMapsToDto() {
        PaginationDTO pg = new PaginationDTO(0, 2);
        Page<Product> page = new PageImpl<>(List.of(new Product()));
        when(productRepository.findByPriceBetween(1.0, 5.0, PageRequest.of(0, 2))).thenReturn(page);
        Set<ProductHomePageDTO> expected = Set.of(new ProductHomePageDTO(1L, "n", "2.0", "img"));
        try (MockedStatic<ProductHomePageDTO> ms = mockStatic(ProductHomePageDTO.class)) {
            ms.when(() -> ProductHomePageDTO.mapProductSetToProductHomePageDto(page)).thenReturn(expected);
            Iterable<ProductHomePageDTO> actual = productService.getProductsByPriceRange(1.0, 5.0, pg);
            assertSame(expected, actual);
        }
    }

    @Test
    void searchProductsByCategoriesAndPriceRangeAndMapsToDto() {
        PaginationDTO pg = new PaginationDTO(0, 2);
        Page<Product> page = new PageImpl<>(List.of(new Product()));
        when(productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndCategoryIdInAndPriceBetween(
                "q", List.of(1L), 1.0, 10.0, PageRequest.of(0, 2)))
                .thenReturn(page);
        Set<ProductHomePageDTO> expected = Set.of(new ProductHomePageDTO(1L, "a", "2.0", "img"));
        try (MockedStatic<ProductHomePageDTO> ms = mockStatic(ProductHomePageDTO.class)) {
            ms.when(() -> ProductHomePageDTO.mapProductSetToProductHomePageDto(page)).thenReturn(expected);
            Iterable<ProductHomePageDTO> actual = productService.searchProductsByCategoriesAndPriceRange("q", List.of(1L), 1.0, 10.0, pg);
            assertSame(expected, actual);
        }
    }
}
