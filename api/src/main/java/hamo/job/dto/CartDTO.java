package hamo.job.dto;

import hamo.job.entity.Cart;
import hamo.job.entity.CartItem;

import java.util.HashSet;
import java.util.Set;

public record CartDTO(Long id, Long userId, Set<CartLineDTO> items) {

    public static Cart toCart(CartDTO cartDto) {
        Cart cart = new Cart();
        cart.setId(cartDto.id);
        cart.setUserId(cartDto.userId);
        return cart;
    }


    public static Set<CartLineDTO> mapCartListToCartLineDtoList(Set<CartItem> cartItems) {
        Set<CartLineDTO> cartLineDTOSet = new HashSet<>();
        for (CartItem cartItem : cartItems) {
            cartLineDTOSet.add(CartLineDTO.toCartLineDTO(cartItem));
        }
        return cartLineDTOSet;
    }

}