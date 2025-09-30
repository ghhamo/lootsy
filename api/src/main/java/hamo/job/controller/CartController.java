package hamo.job.controller;


import hamo.job.dto.AddToCartRequestDTO;
import hamo.job.dto.CartDTO;
import hamo.job.dto.PaginationDTO;
import hamo.job.entity.Cart;
import hamo.job.service.CartService;
import hamo.job.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @Value("${page.max.size}")
    private Integer pageMaxSize;

    @Autowired
    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    public Iterable<Cart> getAll(@RequestParam int pageIndex, @RequestParam int pageSize) {
        if (pageMaxSize < pageSize) {
            throw new IllegalStateException();
        }
        return cartService.getCarts(new PaginationDTO(pageIndex, pageSize));
    }

    @PostMapping
    public ResponseEntity<HttpStatus> create() {
        Long userId = userService.getCurrentUserId();
        cartService.create(userId);
        return ResponseEntity.ok().body(HttpStatus.CREATED);
    }

    @GetMapping("/current")
    public ResponseEntity<CartDTO> getCurrentUserCart() {
        Long userId = userService.getCurrentUserId();
        return ResponseEntity.ok().body(cartService.getCartByUserId(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody AddToCartRequestDTO req) {
        Long userId = userService.getCurrentUserId();
        cartService.add(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> removeProduct(@PathVariable Long productId) {
        Long userId = userService.getCurrentUserId();
        cartService.removeProduct(userId, productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<CartDTO> getCartByUserId(@PathVariable Long id) {
        return ResponseEntity.ok().body(cartService.getCartByUserId(id));
    }
}
