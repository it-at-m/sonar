package de.muenchen.oss.sonar.backend.projekt;

import de.muenchen.oss.sonar.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "projekt_adresse")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class ProjektAdresseEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** The address with its house number, or the cadastral parcel. */
    @Column(nullable = false)
    @NotNull @Size(min = 1, max = 255) private String bezeichnung;

    @Size(max = 255) private String baunutzung;

    private LocalDate unerlaubteNutzungVon;

    private LocalDate unerlaubteNutzungBis;

    /**
     * Derived from the period, or entered directly when no period is known. Empty if there was no
     * unauthorized use at all.
     */
    @Min(1) private Integer tageUnerlaubteNutzung;

    @Column(nullable = false)
    @NotNull @Min(0) private Integer anzahlMahnungen;

    @Column(nullable = false)
    private boolean sondernutzungErlaubt;

}
