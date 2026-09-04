package de.muenchen.oss.sonar.backend.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Getter
@Setter
@ToString
@NoArgsConstructor
public class AdressdatenEmbeddable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @NotNull private Adressart art;

    @Size(max = 255) private String adresse;

    @Size(max = 20) private String hausnummerVon;

    @Size(max = 20) private String hausnummerBis;

    @Size(max = 255) private String flurstueck;

    @Size(max = 255) private String gemarkung;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Nutzung nutzung;

    private LocalDate unerlaubteNutzungVon;

    private LocalDate unerlaubteNutzungBis;

    /**
     * Derived from the period, or entered directly when no period is known. Empty if there was no
     * unauthorized use at all.
     */
    @Min(1) private Integer tageUnerlaubteNutzung;

}
