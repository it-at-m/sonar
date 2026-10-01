package de.muenchen.oss.sonar.backend.abrechnung;

import de.muenchen.oss.sonar.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "calculation")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class CalculationEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "abrechnung_id", nullable = false)
    @NotNull private UUID abrechnungId;

    @Column(name = "lfd_nr", nullable = false)
    @NotNull @Min(1) private Integer lfdNr;

    @Column(nullable = false)
    @NotNull private LocalDate abrechnungszeitraumVon;

    @Column(nullable = false)
    @NotNull private LocalDate abrechnungszeitraumBis;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull @Digits(integer = 10, fraction = 2) private BigDecimal gebuehrFlaechen;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull @Digits(integer = 10, fraction = 2) private BigDecimal gebuehrUeberspannungen;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull @Digits(integer = 10, fraction = 2) private BigDecimal gebuehrVerwaltung;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull @Digits(integer = 10, fraction = 2) private BigDecimal gebuehrGesamt;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull @Digits(integer = 10, fraction = 2) private BigDecimal gebuehrZahlung;

}
