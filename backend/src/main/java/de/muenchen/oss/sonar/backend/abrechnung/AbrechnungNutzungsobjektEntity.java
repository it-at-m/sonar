package de.muenchen.oss.sonar.backend.abrechnung;

import de.muenchen.oss.sonar.backend.common.AdressdatenEmbeddable;
import de.muenchen.oss.sonar.backend.common.BaseEntity;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "abrechnung_nutzungsobjekt")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class AbrechnungNutzungsobjektEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Embedded
    @Valid private AdressdatenEmbeddable adressdaten = new AdressdatenEmbeddable();

    @ElementCollection
    @CollectionTable(name = "abrechnung_nutzungsobjekt_nutzung", joinColumns = @JoinColumn(name = "nutzungsobjekt_id", nullable = false))
    @Column(name = "nutzung", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @BatchSize(size = 25)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private Set<Nutzung> nutzungen = new LinkedHashSet<>();

    @Column(length = 10_000)
    @Size(max = 10_000) private String bemerkung;

    @Column(nullable = false)
    private boolean aufschlag50prozent;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "nutzungsobjekt_id", nullable = false)
    @OrderColumn(name = "sort_order", nullable = false)
    @BatchSize(size = 25)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    @NotEmpty private List<AbrechnungPositionEntity> positionen = new ArrayList<>();

    public Set<Nutzung> getNutzungen() {
        return Collections.unmodifiableSet(nutzungen);
    }

    public void addNutzung(final Nutzung nutzung) {
        nutzungen.add(nutzung);
    }

    public List<AbrechnungPositionEntity> getPositionen() {
        return Collections.unmodifiableList(positionen);
    }

    public void addPosition(final AbrechnungPositionEntity position) {
        positionen.add(position);
    }

}
