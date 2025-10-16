package co.edu.escuelaing.propertiesapi.repository;

import co.edu.escuelaing.propertiesapi.model.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.math.BigDecimal;

public interface PropertyRepository extends JpaRepository<Property, Long>, JpaSpecificationExecutor<Property> {
    Page<Property> findByAddressContainingIgnoreCase(String address, Pageable pageable);

    Page<Property> findByPriceBetween(BigDecimal min, BigDecimal max, Pageable pageable);
}
