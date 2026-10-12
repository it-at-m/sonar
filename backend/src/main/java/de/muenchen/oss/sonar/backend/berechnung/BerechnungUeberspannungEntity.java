package de.muenchen.oss.sonar.backend.berechnung;

import de.muenchen.oss.sonar.backend.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

/** This class is temporary. It exists for the summary the UI shows, and will be removed. */
@Entity
@Table(name = "berechnung_ueberspannung")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class BerechnungUeberspannungEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private static final String TEXT = "text";

    @Column(nullable = false, columnDefinition = TEXT)
    @NotNull private String adresse;

    @Column(nullable = false, columnDefinition = TEXT)
    @NotNull private String berechnungslog;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull @Digits(integer = 10, fraction = 2) private BigDecimal gebuehrUeberspannungen;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ueberspannung_id", nullable = false)
    @OrderColumn(name = "sort_order", nullable = false)
    @BatchSize(size = 25)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    private List<BerechnungBescheiddatenUeberspannungEntity> bescheiddatenUeberspannungen = new ArrayList<>();

    public List<BerechnungBescheiddatenUeberspannungEntity> getBescheiddatenUeberspannungen() {
        return Collections.unmodifiableList(bescheiddatenUeberspannungen);
    }

    public void addBescheiddatenUeberspannung(final BerechnungBescheiddatenUeberspannungEntity bescheiddaten) {
        bescheiddatenUeberspannungen.add(bescheiddaten);
    }

}
