package hamo.job.controller;

import hamo.job.dto.PaginationDTO;
import hamo.job.dto.ShippingDTO;
import hamo.job.service.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shippings")
public class ShippingController {

    private final ShippingService shippingService;

    @Value("${page.max.size}")
    private Integer pageMaxSize;

    @Autowired
    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @PostMapping
    public ResponseEntity<ShippingDTO> createShipping(@RequestBody ShippingDTO shippingDTO) {
        ShippingDTO createdShipping = shippingService.createShipping(shippingDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdShipping);
    }

    @GetMapping
    public ResponseEntity<Iterable<ShippingDTO>> getAllShipping(@RequestParam int pageIndex, @RequestParam int pageSize) {
        if (pageMaxSize < pageSize) {
            throw new IllegalStateException("Page size exceeds maximum allowed size");
        }
        Iterable<ShippingDTO> shippingDTOs = shippingService.getAllShipping(new PaginationDTO(pageIndex, pageSize));
        return ResponseEntity.ok(shippingDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShippingDTO> getShippingById(@PathVariable Long id) {
        try {
            ShippingDTO shippingDTO = shippingService.getShippingById(id);
            return ResponseEntity.ok(shippingDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShippingDTO> updateShipping(@PathVariable Long id, @RequestBody ShippingDTO shippingDTO) {
        try {
            ShippingDTO updatedShipping = shippingService.updateShipping(id, shippingDTO);
            return ResponseEntity.ok(updatedShipping);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipping(@PathVariable Long id) {
        boolean deleted = shippingService.deleteShipping(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
