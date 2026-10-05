package de.muenchen.oss.sonar.backend.abrechnung;

import de.muenchen.oss.sonar.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "abrechnung_position")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class AbrechnungPositionEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nutzungsobjekt_id", nullable = false)
    @ToString.Exclude
    @NotNull private NutzungsobjektEntity nutzungsobjekt;

    @Column(nullable = false)
    @NotNull private LocalDate beginn;

    @Column(nullable = false)
    @NotNull private LocalDate ende;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 10, fraction = 2) private BigDecimal laenge;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 10, fraction = 2) private BigDecimal breite;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 10, fraction = 2) private BigDecimal flaeche;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 10, fraction = 2) private BigDecimal anteilAnFlaeche;

}
