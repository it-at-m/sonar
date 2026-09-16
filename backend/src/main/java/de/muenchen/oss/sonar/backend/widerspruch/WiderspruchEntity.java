package de.muenchen.oss.sonar.backend.widerspruch;

import de.muenchen.oss.sonar.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "widerspruch")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class WiderspruchEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "abrechnung_id", nullable = false, unique = true)
    @NotNull private UUID abrechnungId;

    @Column(nullable = false)
    @NotNull private LocalDate datumEingang;

    private LocalDate datumRuecknahme;

    private LocalDate datumVorlageRegierung;

    private LocalDate datumAblehnungRegierung;

    @Size(max = 255) private String entscheidungDurchfuehrung;

    @Column(nullable = false)
    private boolean sollAbgesetzt;

    @Column(nullable = false)
    private boolean neueTeilabrechnungAnlegen;

    @Column(length = 10_000)
    @Size(max = 10_000) private String bemerkung;

}
