package co.edu.escuelaing.propertiesapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import co.edu.escuelaing.propertiesapi.model.dto.PropertyDto;
import co.edu.escuelaing.propertiesapi.model.entity.Property;
import co.edu.escuelaing.propertiesapi.repository.PropertyRepository;
import co.edu.escuelaing.propertiesapi.service.PropertyService;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository repo;

    @Override
    public Property create(PropertyDto p) {
        Property property = Property.builder()
                .address(p.getAddress())
                .price(p.getPrice())
                .size(p.getSize())
                .description(p.getDescription())
                .build();
        return repo.save(property);
    }

    @Override
    public Page<Property> list(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Override
    public Property get(Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Property not found"));
    }

    @Override
    public Property update(Long id, PropertyDto p) {
        Property current = get(id);
        current.setAddress(p.getAddress());
        current.setPrice(p.getPrice());
        current.setSize(p.getSize());
        current.setDescription(p.getDescription());
        return repo.save(current);
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new NoSuchElementException("Property not found");
        }
        repo.deleteById(id);
    }

    @Override
    public Page<Property> search(String address, String q,
            BigDecimal minPrice, BigDecimal maxPrice,
            Double minSize, Double maxSize,
            Pageable pageable) {

        boolean hasAddress = address != null && !address.isBlank();
        boolean hasQ = q != null && !q.isBlank();
        boolean hasMinP = minPrice != null;
        boolean hasMaxP = maxPrice != null;
        boolean hasMinS = minSize != null;
        boolean hasMaxS = maxSize != null;

        if (!hasAddress && !hasQ && !hasMinP && !hasMaxP && !hasMinS && !hasMaxS) {
            return repo.findAll(pageable);
        }

        Specification<Property> spec = null;

        if (hasAddress) {
            Specification<Property> s = PropertySpecs.addressContains(address);
            spec = (spec == null) ? s : spec.and(s);
        }

        if (hasQ) {
            Specification<Property> s = PropertySpecs.freeText(q);
            spec = (spec == null) ? s : spec.and(s);
        }

        if (hasMinP || hasMaxP) {
            BigDecimal min = hasMinP ? minPrice : BigDecimal.ZERO;
            BigDecimal max = hasMaxP ? maxPrice : new BigDecimal("9999999999");
            if (min.compareTo(max) > 0) {
                var tmp = min;
                min = max;
                max = tmp;
            }
            Specification<Property> s = PropertySpecs.priceBetween(min, max);
            spec = (spec == null) ? s : spec.and(s);
        }

        if (hasMinS || hasMaxS) {
            double min = hasMinS ? minSize : 0d;
            double max = hasMaxS ? maxSize : Double.MAX_VALUE;
            if (min > max) {
                double t = min;
                min = max;
                max = t;
            }
            Specification<Property> s = PropertySpecs.sizeBetween(min, max);
            spec = (spec == null) ? s : spec.and(s);
        }

        return repo.findAll(spec, pageable);
    }

    private static final class PropertySpecs {
    private PropertySpecs() {}

    public static Specification<Property> addressContains(String text) {
        final String like = "%" + text.toLowerCase() + "%";
        return (root, q, cb) -> cb.like(cb.lower(root.get("address")), like);
    }

    public static Specification<Property> descriptionContains(String text) {
        final String like = "%" + text.toLowerCase() + "%";
        return (root, q, cb) -> cb.like(cb.lower(root.get("description")), like);
    }

    public static Specification<Property> freeText(String text) {
        final String like = "%" + text.toLowerCase() + "%";
        return (root, q, cb) -> cb.or(
                cb.like(cb.lower(root.get("address")), like),
                cb.like(cb.lower(root.get("description")), like)
        );
    }

    public static Specification<Property> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, q, cb) -> cb.between(root.get("price"), min, max);
    }

    public static Specification<Property> sizeBetween(Double min, Double max) {
        return (root, q, cb) -> cb.between(root.get("size"), min, max);
    }
}
}