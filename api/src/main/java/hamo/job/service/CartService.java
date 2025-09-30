package hamo.job.service;

import hamo.job.dto.*;
import hamo.job.entity.*;
import hamo.job.exception.exceptions.cartException.CartIdNotFoundException;
import hamo.job.exception.exceptions.cartException.CartIsAlreadyExistsByUserIdException;
import hamo.job.exception.exceptions.productException.ProductIdNotFoundException;
import hamo.job.exception.exceptions.userException.UserIdNotFoundException;
import hamo.job.repository.CartItemRepository;
import hamo.job.repository.CartRepository;
import hamo.job.repository.ProductRepository;
import hamo.job.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartService(CartRepository cartRepository,
                       UserRepository userRepository,
                       ProductRepository productRepository,
                       CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional(readOnly = true)
    public Iterable<Cart> getCarts(PaginationDTO paginationDTO) {
        PageRequest pageRequest = PageRequest.of(paginationDTO.pageNumber(), paginationDTO.pageSize());
        Page<Cart> carts = cartRepository.findAll(pageRequest);
        return mapPageCartToCart(carts);
    }

    private Iterable<Cart> mapPageCartToCart(Iterable<Cart> carts) {
        Set<Cart> cartSet = new HashSet<>();
        for (Cart cart : carts) {
            cartSet.add(cart);
        }
        return cartSet;
    }


    @Transactional
    public void add(Long userId, AddToCartRequestDTO req) {
        int qty = (req.quantity() != null && req.quantity() > 0) ? req.quantity() : 1;

        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setCreatedAt(LocalDateTime.now());
            return cartRepository.save(newCart);
        });
        Product product = productRepository.findById(req.productId()).orElseThrow(() -> new ProductIdNotFoundException(req.productId()));
        CartItemId id = new CartItemId(cart.getId(), product.getId());
        CartItem item = cartItemRepository.findById(id).orElse(null);
        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQty(qty);
        } else {
            item.setQty(item.getQty() + qty);
        }
        item.setAddedAt(LocalDateTime.now());
        cartItemRepository.save(item);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    @Transactional
    public CartDTO getCartByUserId(Long id) {
        Optional<Cart> cartOptional = cartRepository.findByUserId(id);
        if (cartOptional.isEmpty()) {
            Cart cart = new Cart();
            cart.setUserId(id);
            cart.setCreatedAt(LocalDateTime.now());
            cart.setUpdatedAt(LocalDateTime.now());
            cart = cartRepository.save(cart);
            return new CartDTO(cart.getId(), cart.getUserId(), new HashSet<>());
        }
        
        Cart cart = cartOptional.get();
        cart.getItems().size();
        Set<CartLineDTO> lines = CartDTO.mapCartListToCartLineDtoList(cart.getItems());
        return new CartDTO(cart.getId(), cart.getUserId(), lines);
    }

    @Transactional
    public void create(Long userId) {
        Objects.requireNonNull(userId);
        userRepository.findById(userId).orElseThrow(() -> new UserIdNotFoundException(userId));
        Optional<Cart> cartFromDB = cartRepository.findByUserId(userId);
        if (cartFromDB.isEmpty()) {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cartRepository.save(cart);
        } else {
            throw new CartIsAlreadyExistsByUserIdException(userId);
        }
    }

    @Transactional
    public void saveAll(List<Cart> carts) {
        cartRepository.saveAll(carts);
    }

    @Transactional
    public void removeProduct(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new CartIdNotFoundException(userId));
        CartItemId id = new CartItemId(cart.getId(), productId);
        cartItemRepository.deleteById(id);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new CartIdNotFoundException(userId));
        
        cartItemRepository.deleteByCartId(cart.getId());
        
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }
}
