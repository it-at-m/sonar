package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungMast;
import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MastServiceTest {

    private static final LocalDate BEGINN_ADRESSE = LocalDate.of(2026, 1, 1);

    @Nested
    class Berechne {

        @Test
        void givenMastStartingWithTheAdresse_thenBeginItInTheFirstMonat() {
            final AbrechnungMast erfasst = new AbrechnungMast(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));

            final Mast mast = MastService.berechne(erfasst, 1, BEGINN_ADRESSE);

            assertThat(mast.ersterMonat()).isEqualTo(1);
            assertThat(mast.letzterMonat()).isEqualTo(3);
        }

        @Test
        void givenMastStartingLater_thenCountItsErsterMonatFromTheBeginnOfTheAdresse() {
            final AbrechnungMast erfasst = new AbrechnungMast(null, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30));

            final Mast mast = MastService.berechne(erfasst, 2, BEGINN_ADRESSE);

            assertThat(mast.ersterMonat()).isEqualTo(3);
            assertThat(mast.letzterMonat()).isEqualTo(6);
        }

        @Test
        void givenMastEndingWithinItsFirstMonat_thenStillChargeThatMonat() {
            final AbrechnungMast erfasst = new AbrechnungMast(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15));

            final Mast mast = MastService.berechne(erfasst, 1, BEGINN_ADRESSE);

            assertThat(mast.ersterMonat()).isEqualTo(1);
            assertThat(mast.letzterMonat()).isEqualTo(1);
        }

        @Test
        void givenMast_thenKeepItsNummerAndItsDates() {
            final AbrechnungMast erfasst = new AbrechnungMast(null, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 4, 30));

            final Mast mast = MastService.berechne(erfasst, 7, BEGINN_ADRESSE);

            assertThat(mast.nummer()).isEqualTo(7);
            assertThat(mast.beginn()).isEqualTo(LocalDate.of(2026, 2, 1));
            assertThat(mast.ende()).isEqualTo(LocalDate.of(2026, 4, 30));
        }
    }

    @Nested
    class ZwischenabrechnungGrund {

        @Test
        void givenErsterMonatOfTheMast_thenGiveNoGrund() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 6, 30), 1);

            assertThat(MastService.zwischenabrechnungGrund(mast, 1)).isEmpty();
        }

        @Test
        void givenMonatWithinAnAltersstufe_thenGiveNoGrund() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 6, 30), 1);

            assertThat(MastService.zwischenabrechnungGrund(mast, 5)).isEmpty();
        }

        @Test
        void givenMonatOfTheAltersstufenwechsel_thenNameTheMastAndTheAltersstufe() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 6, 30), 1);

            assertThat(MastService.zwischenabrechnungGrund(mast, 13)).contains("Mast 1 wechselt in Altersstufe 2");
        }

        @Test
        void givenMastStartingLater_thenCountTheAltersstufeFromItsOwnBeginn() {
            final Mast mast = new Mast(2, LocalDate.of(2026, 3, 1), LocalDate.of(2027, 8, 31), 3);

            assertThat(MastService.zwischenabrechnungGrund(mast, 13)).isEmpty();
            assertThat(MastService.zwischenabrechnungGrund(mast, 15)).contains("Mast 2 wechselt in Altersstufe 2");
        }
    }
}
