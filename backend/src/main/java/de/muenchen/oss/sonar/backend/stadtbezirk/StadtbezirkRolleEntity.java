package de.muenchen.oss.sonar.backend.stadtbezirk;

import de.muenchen.oss.sonar.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "stadtbezirk_rolle")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class StadtbezirkRolleEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    @NotNull private Stadtbezirk stadtbezirk;

    @Column(nullable = false)
    @NotNull private String rolle;

}
