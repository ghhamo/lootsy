package hamo.job.controller;

import hamo.job.dto.*;
import hamo.job.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Value("${page.max.size}")
    private Integer pageMaxSize;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public void create(@RequestBody CreateProductDTO createProductDTO) {
        productService.create(createProductDTO);
    }

    @GetMapping
    public Iterable<ProductHomePageDTO> getAll(
            @RequestParam(defaultValue = "100") int pageIndex,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) java.util.List<Long> categories,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        if (pageSize > pageMaxSize) {
            throw new IllegalStateException();
        }
        
        PaginationDTO pagination = new PaginationDTO(pageIndex, pageSize);
        
        String trimmedSearch = search != null ? search.trim() : null;
        boolean hasSearch = trimmedSearch != null && !trimmedSearch.isEmpty();
        boolean hasCategories = categories != null && !categories.isEmpty();
        boolean hasPriceRange = minPrice != null && maxPrice != null;
        
        if (hasSearch && hasCategories && hasPriceRange) {
            return productService.searchProductsByCategoriesAndPriceRange(trimmedSearch, categories, minPrice, maxPrice, pagination);
        } else if (hasSearch && hasPriceRange) {
            return productService.searchProductsByPriceRange(trimmedSearch, minPrice, maxPrice, pagination);
        } else if (hasCategories && hasPriceRange) {
            return productService.getProductsByCategoriesAndPriceRange(categories, minPrice, maxPrice, pagination);
        } else if (hasSearch && hasCategories) {
            return productService.searchProductsByCategories(trimmedSearch, categories, pagination);
        } else if (hasSearch) {
            return productService.searchProducts(trimmedSearch, pagination);
        } else if (hasCategories) {
            return productService.getProductsByCategories(categories, pagination);
        } else if (hasPriceRange) {
            return productService.getProductsByPriceRange(minPrice, maxPrice, pagination);
        } else {
            return productService.getProducts(pagination);
        }
    }

    @GetMapping("/{id}")
    public ProductHomePageDTO getOne(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @GetMapping("/by/{id}")
    public ProductDetailsDTO getByIdForDetails(@PathVariable Long id) {
        return productService.getByIdForDetails(id);
    }
}