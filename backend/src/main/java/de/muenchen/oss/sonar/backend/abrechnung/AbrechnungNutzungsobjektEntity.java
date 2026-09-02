package de.muenchen.oss.sonar.backend.abrechnung;

import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.BaseEntity;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    @Min(1) private Integer tageUnerlaubteNutzung;

    @Column(length = 10_000)
    @Size(max = 10_000) private String bemerkung;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "nutzungsobjekt_id", nullable = false)
    @OrderColumn(name = "sort_order", nullable = false)
    @BatchSize(size = 25)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    @NotEmpty private List<AbrechnungPositionEntity> positionen = new ArrayList<>();

    public List<AbrechnungPositionEntity> getPositionen() {
        return Collections.unmodifiableList(positionen);
    }

    public void addPosition(final AbrechnungPositionEntity position) {
        positionen.add(position);
    }

}
