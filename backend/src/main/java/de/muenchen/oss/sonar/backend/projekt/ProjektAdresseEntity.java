package de.muenchen.oss.sonar.backend.projekt;

import de.muenchen.oss.sonar.backend.common.AdressdatenEmbeddable;
import de.muenchen.oss.sonar.backend.common.BaseEntity;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

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

    @ElementCollection
    @CollectionTable(name = "projekt_adresse_nutzung", joinColumns = @JoinColumn(name = "projekt_adresse_id", nullable = false))
    @Column(name = "nutzung", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @BatchSize(size = 25)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private Set<Nutzung> nutzungen = new LinkedHashSet<>();

    @Size(max = 255) private String nutzungSonstiges;

    @Column(nullable = false)
    @NotNull @Min(0) private Integer anzahlMahnungen;

    @Column(nullable = false)
    private boolean sondernutzungErlaubt;

    public Set<Nutzung> getNutzungen() {
        return Collections.unmodifiableSet(nutzungen);
    }

    public void addNutzung(final Nutzung nutzung) {
        nutzungen.add(nutzung);
    }

}
