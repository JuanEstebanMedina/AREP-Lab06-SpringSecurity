package co.edu.escuelaing.propertiesapi.controller;

import co.edu.escuelaing.propertiesapi.model.dto.PropertyDto;
import co.edu.escuelaing.propertiesapi.model.entity.Property;
import co.edu.escuelaing.propertiesapi.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/properties")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService service;

    @GetMapping
    public ResponseEntity<Page<Property>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minSize,
            @RequestParam(required = false) Double maxSize,
            @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(
                service.search(address, q, minPrice, maxPrice, minSize, maxSize, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Property> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    public ResponseEntity<Property> create(@Valid @RequestBody PropertyDto p) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(p));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Property> update(@PathVariable Long id, @Valid @RequestBody PropertyDto p) {
        return ResponseEntity.ok(service.update(id, p));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
