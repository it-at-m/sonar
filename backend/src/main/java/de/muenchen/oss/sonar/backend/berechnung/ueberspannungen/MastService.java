package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungMast;
import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Mastzeitindex;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MastService {

    public static Mast berechne(final AbrechnungMast mast, final int nummer, final LocalDate beginnAdresse) {
        return new Mast(nummer, mast.beginn(), mast.ende(), firstMonth(mast.beginn(), beginnAdresse));
    }

    private static int firstMonth(final LocalDate beginn, final LocalDate beginnAdresse) {
        return (int) ChronoUnit.MONTHS.between(beginnAdresse, beginn) + 1;
    }

    public static Optional<String> zwischenabrechnungGrund(final Mast mast, final int monat) {

        final Mastzeitindex mastzeitindex = Mastzeitindex.from(mast.runningMonth(monat));

        if (mastzeitindex == Mastzeitindex.from(mast.runningMonth(monat) - 1)) {
            return Optional.empty();
        }
        return Optional.of("Mast " + mast.nummer() + " wechselt in " + mastzeitindex.getBezeichnung());
    }
}
