package de.muenchen.oss.sonar.backend.projekt;

import de.muenchen.oss.sonar.backend.common.AdressdatenEmbeddable;
import de.muenchen.oss.sonar.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

    @Embedded
    @Valid private AdressdatenEmbeddable adressdaten = new AdressdatenEmbeddable();

    @Column(nullable = false)
    @NotNull @Min(0) private Integer anzahlMahnungen;

    @Column(nullable = false)
    private boolean sondernutzungErlaubt;

}
