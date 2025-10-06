package hamo.job.service;

import hamo.job.dto.PaginationDTO;
import hamo.job.dto.ShippingDTO;
import hamo.job.entity.Shipping;
import hamo.job.exception.exceptions.shipping.ShippingIdNotFoundException;
import hamo.job.repository.ShippingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class ShippingService {

    private final ShippingRepository shippingRepository;

    @Autowired
    public ShippingService(ShippingRepository shippingRepository) {
        this.shippingRepository = shippingRepository;
    }

    @Transactional
    public ShippingDTO createShipping(ShippingDTO shippingDTO) {
        Shipping shipping = ShippingDTO.toShipping(shippingDTO);
        Shipping savedShipping = shippingRepository.save(shipping);
        return ShippingDTO.fromShipping(savedShipping);
    }

    @Transactional(readOnly = true)
    public Iterable<ShippingDTO> getAllShipping(PaginationDTO paginationDTO) {
        PageRequest pageRequest = PageRequest.of(paginationDTO.pageNumber(), paginationDTO.pageSize());
        Page<Shipping> shipping = shippingRepository.findAll(pageRequest);
        return ShippingDTO.mapShippingSetToShippingDto(shipping);
    }

    @Transactional(readOnly = true)
    public ShippingDTO getShippingById(Long id) {
        Objects.requireNonNull(id);
        Shipping shipping = shippingRepository.findById(id)
                .orElseThrow(() -> new ShippingIdNotFoundException(id));
        return ShippingDTO.fromShipping(shipping);
    }

    @Transactional
    public ShippingDTO updateShipping(Long id, ShippingDTO updatedShippingDTO) {
        Objects.requireNonNull(id);
        Shipping existingShipping = shippingRepository.findById(id)
                .orElseThrow(() -> new ShippingIdNotFoundException(id));
        if (updatedShippingDTO.firstName() != null) {
            existingShipping.setFirstName(updatedShippingDTO.firstName());
        }
        if (updatedShippingDTO.lastName() != null) {
            existingShipping.setLastName(updatedShippingDTO.lastName());
        }
        if (updatedShippingDTO.country() != null) {
            existingShipping.setCountry(updatedShippingDTO.country());
        }
        if (updatedShippingDTO.city() != null) {
            existingShipping.setCity(updatedShippingDTO.city());
        }
        if (updatedShippingDTO.streetAddress() != null) {
            existingShipping.setStreetAddress(updatedShippingDTO.streetAddress());
        }
        if (updatedShippingDTO.phoneNumber() != null) {
            existingShipping.setPhoneNumber(updatedShippingDTO.phoneNumber());
        }
        
        Shipping savedShipping = shippingRepository.save(existingShipping);
        return ShippingDTO.fromShipping(savedShipping);
    }

    @Transactional
    public boolean deleteShipping(Long id) {
        Objects.requireNonNull(id);
        if (shippingRepository.existsById(id)) {
            shippingRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
