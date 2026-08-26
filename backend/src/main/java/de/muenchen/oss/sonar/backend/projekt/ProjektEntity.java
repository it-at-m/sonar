package de.muenchen.oss.sonar.backend.projekt;

import de.muenchen.oss.sonar.backend.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "projekt")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class ProjektEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(nullable = false, length = 20)
    @NotNull @Size(min = 1, max = 20) private String projektnummer;

    @Column(nullable = false)
    @NotNull private LocalDate abrechnungBeginn;

    @Column(nullable = false)
    @NotNull private LocalDate abrechnungEnde;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "projekt_id", nullable = false)
    @OrderColumn(name = "sort_order", nullable = false)
    @BatchSize(size = 25)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    @NotEmpty private List<ProjektAdresseEntity> adressen = new ArrayList<>();

    public List<ProjektAdresseEntity> getAdressen() {
        return Collections.unmodifiableList(adressen);
    }

    public void addAdresse(final ProjektAdresseEntity adresse) {
        adressen.add(adresse);
    }

}
