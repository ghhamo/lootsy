package hamo.job.service;

import hamo.job.dto.*;
import hamo.job.entity.Category;
import hamo.job.entity.Product;
import hamo.job.exception.exceptions.categoryException.CategoryIdNotFoundException;
import hamo.job.exception.exceptions.productException.ProductIdNotFoundException;
import hamo.job.repository.CategoryRepository;
import hamo.job.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;


    @Autowired
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;

    }


    @Transactional(readOnly = true)
    public Iterable<ProductHomePageDTO> getProducts(PaginationDTO paginationDTO) {
        PageRequest pageRequest = PageRequest.of(paginationDTO.pageNumber(), paginationDTO.pageSize());
        Page<Product> products = productRepository.findAll(pageRequest);
        return ProductHomePageDTO.mapProductSetToProductHomePageDto(products);
    }

    @Transactional
    public void create(CreateProductDTO createProductDTO) {
        Category category = categoryRepository.findById(createProductDTO.categoryId()).orElseThrow(() -> new CategoryIdNotFoundException(createProductDTO.categoryId()));
        Product product = CreateProductDTO.toProduct(createProductDTO);
        product.setCategory(category);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public long getCount() {
        return productRepository.count();
    }

    @Transactional(readOnly = true)
    public ProductHomePageDTO getProductById(Long id) {
        Objects.requireNonNull(id);
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductIdNotFoundException(id));
        return ProductHomePageDTO.fromProduct(product);
    }

    @Transactional(readOnly = true)
    public ProductDetailsDTO getByIdForDetails(Long id) {
        Objects.requireNonNull(id);
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductIdNotFoundException(id));
        return ProductDetailsDTO.fromProduct(product);
    }

    @Transactional
    public void saveAll(List<Product> products) {
        productRepository.saveAll(products);
    }

    @Transactional(readOnly = true)
    public Iterable<ProductHomePageDTO> searchProducts(String searchTerm, PaginationDTO paginationDTO) {
        PageRequest pageRequest = PageRequest.of(paginationDTO.pageNumber(), paginationDTO.pageSize());
        Page<Product> products = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(searchTerm, pageRequest);
        return ProductHomePageDTO.mapProductSetToProductHomePageDto(products);
    }

    @Transactional(readOnly = true)
    public Iterable<ProductHomePageDTO> getProductsByCategories(List<Long> categoryIds, PaginationDTO paginationDTO) {
        PageRequest pageRequest = PageRequest.of(paginationDTO.pageNumber(), paginationDTO.pageSize());
        Page<Product> products = productRepository.findByCategoryIdIn(categoryIds, pageRequest);
        return ProductHomePageDTO.mapProductSetToProductHomePageDto(products);
    }

    @Transactional(readOnly = true)
    public Iterable<ProductHomePageDTO> searchProductsByCategories(String searchTerm, List<Long> categoryIds, PaginationDTO paginationDTO) {
        PageRequest pageRequest = PageRequest.of(paginationDTO.pageNumber(), paginationDTO.pageSize());
        Page<Product> products = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndCategoryIdIn(searchTerm, categoryIds, pageRequest);
        return ProductHomePageDTO.mapProductSetToProductHomePageDto(products);
    }

    @Transactional(readOnly = true)
    public Iterable<ProductHomePageDTO> getProductsByPriceRange(Double minPrice, Double maxPrice, PaginationDTO paginationDTO) {
        PageRequest pageRequest = PageRequest.of(paginationDTO.pageNumber(), paginationDTO.pageSize());
        Page<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice, pageRequest);
        return ProductHomePageDTO.mapProductSetToProductHomePageDto(products);
    }

    @Transactional(readOnly = true)
    public Iterable<ProductHomePageDTO> getProductsByCategoriesAndPriceRange(List<Long> categoryIds, Double minPrice, Double maxPrice, PaginationDTO paginationDTO) {
        PageRequest pageRequest = PageRequest.of(paginationDTO.pageNumber(), paginationDTO.pageSize());
        Page<Product> products = productRepository.findByCategoryIdInAndPriceBetween(categoryIds, minPrice, maxPrice, pageRequest);
        return ProductHomePageDTO.mapProductSetToProductHomePageDto(products);
    }

    @Transactional(readOnly = true)
    public Iterable<ProductHomePageDTO> searchProductsByPriceRange(String searchTerm, Double minPrice, Double maxPrice, PaginationDTO paginationDTO) {
        PageRequest pageRequest = PageRequest.of(paginationDTO.pageNumber(), paginationDTO.pageSize());
        Page<Product> products = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndPriceBetween(searchTerm, minPrice, maxPrice, pageRequest);
        return ProductHomePageDTO.mapProductSetToProductHomePageDto(products);
    }

    @Transactional(readOnly = true)
    public Iterable<ProductHomePageDTO> searchProductsByCategoriesAndPriceRange(String searchTerm, List<Long> categoryIds, Double minPrice, Double maxPrice, PaginationDTO paginationDTO) {
        PageRequest pageRequest = PageRequest.of(paginationDTO.pageNumber(), paginationDTO.pageSize());
        Page<Product> products = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndCategoryIdInAndPriceBetween(searchTerm, categoryIds, minPrice, maxPrice, pageRequest);
        return ProductHomePageDTO.mapProductSetToProductHomePageDto(products);
    }

}
