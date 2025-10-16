package co.edu.escuelaing.propertiesapi.model.dto;

import lombok.*;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyDto {

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Size is required")
    @Positive(message = "Size must be positive")
    private Double size;

    @Size(max = 1000, message = "Description max length is 1000")
    private String description;
}
