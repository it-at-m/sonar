package de.muenchen.oss.sonar.backend.abrechnung;

import de.muenchen.oss.sonar.backend.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "abrechnung")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class AbrechnungEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "projekt_id", nullable = false)
    @NotNull private UUID projektId;

    @Column(nullable = false, length = 10)
    @NotNull @Size(min = 1, max = 10) private String geschaeftspartnerId;

    @Column(nullable = false)
    private boolean zustellungsbevollmaechtigterGenutzt;

    @Column(length = 10)
    @Size(min = 1, max = 10) private String zustellungsbevollmaechtigterId;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ZustellungsbevollmaechtigterTyp zustellungsbevollmaechtigterTyp;

    @Column(nullable = false)
    @NotNull private LocalDate zeitraumVon;

    @Column(nullable = false)
    @NotNull private LocalDate zeitraumBis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @NotNull private AbrechnungsArt abrechnungsArt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "abrechnung_id", nullable = false)
    @OrderColumn(name = "sort_order", nullable = false)
    @BatchSize(size = 25)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    @NotEmpty private List<AbrechnungNutzungsobjektEntity> nutzungsobjekte = new ArrayList<>();

    public List<AbrechnungNutzungsobjektEntity> getNutzungsobjekte() {
        return Collections.unmodifiableList(nutzungsobjekte);
    }

    public void addNutzungsobjekt(final AbrechnungNutzungsobjektEntity nutzungsobjekt) {
        nutzungsobjekte.add(nutzungsobjekt);
    }

}
