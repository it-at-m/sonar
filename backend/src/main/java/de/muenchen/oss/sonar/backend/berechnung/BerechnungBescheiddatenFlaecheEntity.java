package de.muenchen.oss.sonar.backend.berechnung;

import de.muenchen.oss.sonar.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** This class is temporary. It exists for the summary the UI shows, and will be removed. */
@Entity
@Table(name = "berechnung_bescheiddaten_flaeche")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class BerechnungBescheiddatenFlaecheEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private static final String TEXT = "text";

    @Column(nullable = false)
    @NotNull private Integer wocheBeginn;

    @Column(nullable = false)
    @NotNull private Integer wocheEnde;

    @Column(nullable = false)
    @NotNull private Integer wocheDauer;

    @Column(nullable = false, columnDefinition = TEXT)
    @NotNull private String flaechenVerteilung;

    @Column(nullable = false, columnDefinition = TEXT)
    @NotNull private String grund;

    @Column(nullable = false, columnDefinition = TEXT)
    @NotNull private String gebuehrenstufe;

    @Column(nullable = false, columnDefinition = TEXT)
    @NotNull private String berechnung;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull @Digits(integer = 10, fraction = 2) private BigDecimal gebuehr;

}
