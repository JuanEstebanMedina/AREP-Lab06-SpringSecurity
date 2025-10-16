package co.edu.escuelaing.propertiesapi.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "properties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Address is required")
    @Column(nullable = false)
    private String address;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Size is required")
    @Positive(message = "Size must be positive")
    @Column(nullable = false)
    private Double size;

    @Size(max = 1000, message = "Description max length is 1000")
    @Column(length = 1000)
    private String description;
}
