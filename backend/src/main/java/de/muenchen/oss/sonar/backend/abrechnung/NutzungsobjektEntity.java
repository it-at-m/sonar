package de.muenchen.oss.sonar.backend.abrechnung;

import de.muenchen.oss.sonar.backend.common.AdressdatenEmbeddable;
import de.muenchen.oss.sonar.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "nutzungsobjekt")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class NutzungsobjektEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Embedded
    @Valid private AdressdatenEmbeddable adressdaten = new AdressdatenEmbeddable();

    @Column(length = 10_000)
    @Size(max = 10_000) private String bemerkung;

    @Column(nullable = false)
    private boolean aufschlag50prozent;

}
