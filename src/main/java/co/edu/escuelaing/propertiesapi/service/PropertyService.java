package co.edu.escuelaing.propertiesapi.service;

import co.edu.escuelaing.propertiesapi.model.dto.PropertyDto;
import co.edu.escuelaing.propertiesapi.model.entity.Property;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface PropertyService {
    Property create(PropertyDto p);

    Page<Property> list(Pageable pageable);

    Property get(Long id);

    Property update(Long id, PropertyDto p);

    void delete(Long id);

    Page<Property> search(String address,
            String q,
            BigDecimal minPrice, BigDecimal maxPrice,
            Double minSize, Double maxSize,
            Pageable pageable);
}
