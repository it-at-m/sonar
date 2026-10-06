package de.muenchen.oss.sonar.backend.calculation;

import de.muenchen.oss.sonar.backend.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "calculation")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class CalculationEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private static final String TEXT = "text";

    @Column(name = "abrechnung_id", nullable = false, unique = true)
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

    @Column(nullable = false, columnDefinition = TEXT)
    @NotNull private String zusammenfassung;

    @Column(nullable = false, columnDefinition = TEXT)
    @NotNull private String berechnungslog;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "calculation_id", nullable = false)
    @OrderColumn(name = "sort_order", nullable = false)
    @BatchSize(size = 25)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private List<CalculationBescheiddatenFlaecheEntity> bescheiddatenFlaechen = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "calculation_id", nullable = false)
    @OrderColumn(name = "sort_order", nullable = false)
    @BatchSize(size = 25)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private List<CalculationUeberspannungEntity> abrechnungenUeberspannungen = new ArrayList<>();

    public List<CalculationBescheiddatenFlaecheEntity> getBescheiddatenFlaechen() {
        return Collections.unmodifiableList(bescheiddatenFlaechen);
    }

    public void addBescheiddatenFlaeche(final CalculationBescheiddatenFlaecheEntity bescheiddaten) {
        bescheiddatenFlaechen.add(bescheiddaten);
    }

    public List<CalculationUeberspannungEntity> getAbrechnungenUeberspannungen() {
        return Collections.unmodifiableList(abrechnungenUeberspannungen);
    }

    public void addAbrechnungUeberspannungen(final CalculationUeberspannungEntity ueberspannungen) {
        abrechnungenUeberspannungen.add(ueberspannungen);
    }

}
