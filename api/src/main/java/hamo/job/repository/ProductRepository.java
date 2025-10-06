package hamo.job.repository;

import hamo.job.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.id IN :categoryIds")
    Page<Product> findByCategoryIdIn(@Param("categoryIds") java.util.List<Long> categoryIds, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND p.category.id IN :categoryIds")
    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndCategoryIdIn(@Param("searchTerm") String searchTerm, @Param("categoryIds") java.util.List<Long> categoryIds, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceBetween(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.id IN :categoryIds AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByCategoryIdInAndPriceBetween(@Param("categoryIds") java.util.List<Long> categoryIds, @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndPriceBetween(@Param("searchTerm") String searchTerm, @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND p.category.id IN :categoryIds AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndCategoryIdInAndPriceBetween(@Param("searchTerm") String searchTerm, @Param("categoryIds") java.util.List<Long> categoryIds, @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice, Pageable pageable);
}
