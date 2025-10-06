package hamo.job.controller;


import hamo.job.dto.AddToCartRequestDTO;
import hamo.job.dto.CartDTO;
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

    @PostMapping
    public ResponseEntity<Void> create() {
        Long userId = userService.getCurrentUserId();
        cartService.create(userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
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
    public ResponseEntity<Void> removeProduct(@PathVariable Long productId) {
        Long userId = userService.getCurrentUserId();
        cartService.removeProduct(userId, productId);
        return ResponseEntity.noContent().build();
    }
}
