package co.edu.escuelaing.propertiesapi.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 80)
    private String username;

    @Column(nullable = false, length = 120)
    private String password; // BCrypt hash

    @Column(nullable = false, length = 30)
    private String role; // Ej: "USER" o "ADMIN"

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;
}
