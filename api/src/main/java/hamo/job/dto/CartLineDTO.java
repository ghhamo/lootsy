package hamo.job.dto;

import hamo.job.entity.CartItem;
import hamo.job.entity.Product;

public record CartLineDTO(Long productId, String name, String price, Integer quantity, String imageUrl) {
    public static CartLineDTO toCartLineDTO(CartItem ci) {
        Product p = ci.getProduct();
        String imageUrl = buildImageUrl(p);
        return new CartLineDTO(
                p.getId(),
                p.getName(),
                p.getPrice().toString(),
                ci.getQty(),
                imageUrl
        );
    }

    private static String buildImageUrl(Product p) {
        String[] paths = {p.getImageUrlS(), p.getImageUrlM(), p.getImageUrlL()};
        for (String path : paths) {
            if (path != null && !path.isBlank()) {
                String filename = java.nio.file.Paths.get(path).getFileName().toString();
                return "/images/products/" + filename;
            }
        }
        return "";
    }
}