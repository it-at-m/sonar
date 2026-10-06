package de.muenchen.oss.sonar.backend.abrechnung;

import de.muenchen.oss.sonar.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "abrechnung_mast")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class AbrechnungMastEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(nullable = false)
    @NotNull private LocalDate beginn;

    @Column(nullable = false)
    @NotNull private LocalDate ende;

}
