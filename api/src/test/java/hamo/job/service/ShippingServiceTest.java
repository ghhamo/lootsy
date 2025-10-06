package hamo.job.service;

import hamo.job.dto.PaginationDTO;
import hamo.job.dto.ShippingDTO;
import hamo.job.entity.Shipping;
import hamo.job.exception.exceptions.shipping.ShippingIdNotFoundException;
import hamo.job.repository.ShippingRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingServiceTest {

    @Mock
    ShippingRepository shippingRepository;

    @InjectMocks
    ShippingService shippingService;

    @Test
    void createShippingSavesAndReturnsMappedDto() {
        ShippingDTO dto = new ShippingDTO(1L, "John", "Doe", "USA", "NY", "Main St", "12345");
        Shipping entity = new Shipping();
        Shipping saved = new Shipping();
        saved.setId(1L);
        try (MockedStatic<ShippingDTO> ms = mockStatic(ShippingDTO.class)) {
            ms.when(() -> ShippingDTO.toShipping(dto)).thenReturn(entity);
            when(shippingRepository.save(entity)).thenReturn(saved);
            ShippingDTO expected = new ShippingDTO(1L, "John", "Doe", "USA", "NY", "Main St", "12345");
            ms.when(() -> ShippingDTO.fromShipping(saved)).thenReturn(expected);
            ShippingDTO actual = shippingService.createShipping(dto);
            assertSame(expected, actual);
            verify(shippingRepository).save(entity);
        }
    }

    @Test
    void getAllShippingAndReturnsMappedPage() {
        PaginationDTO pagination = new PaginationDTO(0, 5);
        Page<Shipping> page = new PageImpl<>(List.of(new Shipping(), new Shipping()));
        when(shippingRepository.findAll(PageRequest.of(0, 5))).thenReturn(page);
        Set<ShippingDTO> expected = new LinkedHashSet<>(List.of(
                new ShippingDTO(1L, "a", "b", "c", "d", "e", "f"),
                new ShippingDTO(2L, "g", "h", "i", "j", "k", "l")));
        try (MockedStatic<ShippingDTO> ms = mockStatic(ShippingDTO.class)) {
            ms.when(() -> ShippingDTO.mapShippingSetToShippingDto(page)).thenReturn(expected);
            Iterable<ShippingDTO> actual = shippingService.getAllShipping(pagination);
            assertSame(expected, actual);
            verify(shippingRepository).findAll(PageRequest.of(0, 5));
        }
    }

    @Test
    void getShippingByIdFoundAndReturnsMappedDto() {
        Shipping shipping = new Shipping();
        shipping.setId(10L);
        when(shippingRepository.findById(10L)).thenReturn(Optional.of(shipping));
        ShippingDTO dto = new ShippingDTO(10L, "a", "b", "c", "d", "e", "f");
        try (MockedStatic<ShippingDTO> ms = mockStatic(ShippingDTO.class)) {
            ms.when(() -> ShippingDTO.fromShipping(shipping)).thenReturn(dto);
            ShippingDTO actual = shippingService.getShippingById(10L);
            assertSame(dto, actual);
        }
    }

    @Test
    void getShippingByIdNotFoundAndThrows() {
        when(shippingRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ShippingIdNotFoundException.class, () -> shippingService.getShippingById(99L));
    }

    @Test
    void updateShippingUpdatesFieldsSavesAndMaps() {
        Shipping existing = new Shipping();
        existing.setId(1L);
        when(shippingRepository.findById(1L)).thenReturn(Optional.of(existing));
        ShippingDTO update = new ShippingDTO(1L, "Jane", "Smith", "UK", "London", "Street 5", "98765");
        when(shippingRepository.save(existing)).thenReturn(existing);
        try (MockedStatic<ShippingDTO> ms = mockStatic(ShippingDTO.class)) {
            ShippingDTO expected = new ShippingDTO(1L, "Jane", "Smith", "UK", "London", "Street 5", "98765");
            ms.when(() -> ShippingDTO.fromShipping(existing)).thenReturn(expected);
            ShippingDTO actual = shippingService.updateShipping(1L, update);
            assertEquals("Jane", existing.getFirstName());
            assertEquals("Smith", existing.getLastName());
            assertEquals("UK", existing.getCountry());
            assertEquals("London", existing.getCity());
            assertEquals("Street 5", existing.getStreetAddress());
            assertEquals("98765", existing.getPhoneNumber());
            verify(shippingRepository).save(existing);
            assertSame(expected, actual);
        }
    }

    @Test
    void updateShippingNotFoundAndThrows() {
        when(shippingRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ShippingIdNotFoundException.class, () -> shippingService.updateShipping(5L, mock(ShippingDTO.class)));
    }

    @Test
    void deleteShippingExistsDeletesAndReturnsTrue() {
        when(shippingRepository.existsById(1L)).thenReturn(true);
        boolean result = shippingService.deleteShipping(1L);
        assertTrue(result);
        verify(shippingRepository).deleteById(1L);
    }

    @Test
    void deleteShippingNotExistsAndReturnsFalse() {
        when(shippingRepository.existsById(2L)).thenReturn(false);
        boolean result = shippingService.deleteShipping(2L);
        assertFalse(result);
        verify(shippingRepository, never()).deleteById(anyLong());
    }
}
