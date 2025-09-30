package hamo.job.dto;

import hamo.job.entity.Shipping;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public record ShippingDTO(Long id, String firstName, String lastName, String country, 
                         String city, String streetAddress, String phoneNumber) {
    
    public ShippingDTO {
        Objects.requireNonNull(firstName, "First name cannot be null");
        Objects.requireNonNull(lastName, "Last name cannot be null");
        Objects.requireNonNull(country, "Country cannot be null");
        Objects.requireNonNull(city, "City cannot be null");
        Objects.requireNonNull(streetAddress, "Street address cannot be null");
        Objects.requireNonNull(phoneNumber, "Phone number cannot be null");
    }

    public static ShippingDTO fromShipping(Shipping shipping) {
        return new ShippingDTO(shipping.getId(), shipping.getFirstName(), shipping.getLastName(),
                shipping.getCountry(), shipping.getCity(), shipping.getStreetAddress(), 
                shipping.getPhoneNumber());
    }

    public static Shipping toShipping(ShippingDTO shippingDTO) {
        Shipping shipping = new Shipping();
        shipping.setId(shippingDTO.id);
        shipping.setFirstName(shippingDTO.firstName);
        shipping.setLastName(shippingDTO.lastName);
        shipping.setCountry(shippingDTO.country);
        shipping.setCity(shippingDTO.city);
        shipping.setStreetAddress(shippingDTO.streetAddress);
        shipping.setPhoneNumber(shippingDTO.phoneNumber);
        return shipping;
    }

    public static Set<ShippingDTO> mapShippingSetToShippingDto(Iterable<Shipping> shippings) {
        Set<ShippingDTO> shippingDTOSet = new HashSet<>();
        for (Shipping shipping : shippings) {
            shippingDTOSet.add(ShippingDTO.fromShipping(shipping));
        }
        return shippingDTOSet;
    }
}
