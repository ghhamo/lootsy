package hamo.job.service;

import hamo.job.dto.*;
import hamo.job.entity.*;
import hamo.job.exception.exceptions.cartException.*;
import hamo.job.exception.exceptions.productException.ProductIdNotFoundException;
import hamo.job.exception.exceptions.userException.UserIdNotFoundException;
import hamo.job.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    @Mock
    CartRepository cartRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    CartItemRepository cartItemRepository;
    @InjectMocks
    CartService cartService;

    @Test
    void addCreatesCartAndAddsNewItem() {
        Long userId = 1L;
        Long productId = 2L;
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> {
            Cart c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });
        Product product = new Product();
        product.setId(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartItemRepository.findById(any())).thenReturn(Optional.empty());
        AddToCartRequestDTO req = new AddToCartRequestDTO(productId, 2);
        cartService.add(userId, req);
        verify(cartRepository, atLeastOnce()).save(any(Cart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addIncrementsExistingItemQuantity() {
        Long userId = 1L;
        Long productId = 2L;
        Cart cart = new Cart();
        cart.setId(5L);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        Product product = new Product();
        product.setId(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        CartItem existing = new CartItem();
        existing.setCart(cart);
        existing.setProduct(product);
        existing.setQty(3);
        when(cartItemRepository.findById(any())).thenReturn(Optional.of(existing));
        AddToCartRequestDTO req = new AddToCartRequestDTO(productId, 2);
        cartService.add(userId, req);
        assertEquals(5, existing.getQty());
        verify(cartItemRepository).save(existing);
    }

    @Test
    void addWhenProductNotFoundAndThrows() {
        Long userId = 1L;
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(new Cart()));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        AddToCartRequestDTO req = new AddToCartRequestDTO(99L, 1);
        assertThrows(ProductIdNotFoundException.class, () -> cartService.add(userId, req));
    }

    @Test
    void getCartByUserIdExistingCartAndReturnsDto() {
        Long userId = 1L;
        Cart cart = new Cart();
        cart.setId(7L);
        cart.setUserId(userId);
        cart.setItems(new HashSet<>());
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        CartDTO dto = cartService.getCartByUserId(userId);
        assertEquals(7L, dto.id());
        assertEquals(userId, dto.userId());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void getCartByUserIdCreatesWhenMissing() {
        Long userId = 1L;
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> {
            Cart c = inv.getArgument(0);
            c.setId(42L);
            return c;
        });
        CartDTO dto = cartService.getCartByUserId(userId);
        assertEquals(42L, dto.id());
        assertEquals(userId, dto.userId());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void createCreatesCartIfNotExists() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        cartService.create(userId);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void createWhenUserMissingThrows() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserIdNotFoundException.class, () -> cartService.create(1L));
    }

    @Test
    void createWhenCartAlreadyExistsAndThrows() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(new Cart()));
        assertThrows(CartIsAlreadyExistsByUserIdException.class, () -> cartService.create(userId));
    }

    @Test
    void saveAllCallsRepository() {
        List<Cart> carts = List.of(new Cart());
        cartService.saveAll(carts);
        verify(cartRepository).saveAll(carts);
    }

    @Test
    void removeProductRemovesItemAndUpdatesCart() {
        Long userId = 1L;
        Long productId = 2L;
        Cart cart = new Cart();
        cart.setId(9L);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        cartService.removeProduct(userId, productId);
        verify(cartItemRepository).deleteById(any());
        verify(cartRepository).save(cart);
    }

    @Test
    void removeProductCartNotFoundAndThrows() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertThrows(CartNotFoundException.class, () -> cartService.removeProduct(1L, 2L));
    }

    @Test
    void clearCartDeletesAllItemsAndSavesCart() {
        Long userId = 1L;
        Cart cart = new Cart();
        cart.setId(5L);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        cartService.clearCart(userId);
        verify(cartItemRepository).deleteByCartId(5L);
        verify(cartRepository).save(cart);
    }

    @Test
    void clearCartCartNotFoundAndThrows() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertThrows(CartNotFoundException.class, () -> cartService.clearCart(1L));
    }
}
