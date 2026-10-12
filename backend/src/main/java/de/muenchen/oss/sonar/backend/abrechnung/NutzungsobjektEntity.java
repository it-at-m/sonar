package de.muenchen.oss.sonar.backend.abrechnung;

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
@Table(name = "nutzungsobjekt")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class NutzungsobjektEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Embedded
    @Valid private AdressdatenEmbeddable adressdaten = new AdressdatenEmbeddable();

    @ElementCollection
    @CollectionTable(name = "nutzungsobjekt_nutzung", joinColumns = @JoinColumn(name = "nutzungsobjekt_id", nullable = false))
    @Column(name = "nutzung", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @BatchSize(size = 25)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private Set<Nutzung> nutzungen = new LinkedHashSet<>();

    @Size(max = 255) private String nutzungSonstiges;

    @Column(length = 10_000)
    @Size(max = 10_000) private String bemerkung;

    @Column(nullable = false)
    private boolean aufschlag50prozent;

    public Set<Nutzung> getNutzungen() {
        return Collections.unmodifiableSet(nutzungen);
    }

    public void addNutzung(final Nutzung nutzung) {
        nutzungen.add(nutzung);
    }

}
