package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungMast;
import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MastTest {

    private static final LocalDate BEGINN_ADRESSE = LocalDate.of(2026, 1, 1);

    @Nested
    class From {

        @Test
        void givenMastStartingWithTheAdresse_thenBeginItInTheFirstMonat() {
            final AbrechnungMast erfasst = new AbrechnungMast(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));

            final Mast mast = Mast.from(erfasst, 1, BEGINN_ADRESSE);

            assertThat(mast.ersterMonat()).isEqualTo(1);
            assertThat(mast.letzterMonat()).isEqualTo(3);
        }

        @Test
        void givenMastStartingLater_thenCountItsErsterMonatFromTheBeginnOfTheAdresse() {
            final AbrechnungMast erfasst = new AbrechnungMast(null, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30));

            final Mast mast = Mast.from(erfasst, 2, BEGINN_ADRESSE);

            assertThat(mast.ersterMonat()).isEqualTo(3);
            assertThat(mast.letzterMonat()).isEqualTo(6);
        }

        @Test
        void givenMastEndingWithinItsFirstMonat_thenStillChargeThatMonat() {
            final AbrechnungMast erfasst = new AbrechnungMast(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15));

            final Mast mast = Mast.from(erfasst, 1, BEGINN_ADRESSE);

            assertThat(mast.ersterMonat()).isEqualTo(1);
            assertThat(mast.letzterMonat()).isEqualTo(1);
        }

        @Test
        void givenMast_thenKeepItsNummerAndItsDates() {
            final AbrechnungMast erfasst = new AbrechnungMast(null, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 4, 30));

            final Mast mast = Mast.from(erfasst, 7, BEGINN_ADRESSE);

            assertThat(mast.nummer()).isEqualTo(7);
            assertThat(mast.beginn()).isEqualTo(LocalDate.of(2026, 2, 1));
            assertThat(mast.ende()).isEqualTo(LocalDate.of(2026, 4, 30));
        }
    }

    @Nested
    class DauerMonate {

        @Test
        void givenMastOverSeveralMonate_thenCountBothEnds() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3, 6);

            assertThat(mast.dauerMonate()).isEqualTo(4);
        }
    }

    @Nested
    class IsActive {

        @Test
        void givenMonatBeforeTheMast_thenItIsNotActive() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3, 6);

            assertThat(mast.isActive(2)).isFalse();
        }

        @Test
        void givenErsterMonatOfTheMast_thenItIsActive() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3, 6);

            assertThat(mast.isActive(3)).isTrue();
        }

        @Test
        void givenLetzterMonatOfTheMast_thenItIsActive() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3, 6);

            assertThat(mast.isActive(6)).isTrue();
        }

        @Test
        void givenMonatAfterTheMast_thenItIsNotActive() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3, 6);

            assertThat(mast.isActive(7)).isFalse();
        }
    }

    @Nested
    class RunningMonth {

        @Test
        void givenErsterMonatOfTheMast_thenItIsInItsFirstMonat() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3, 6);

            assertThat(mast.runningMonth(3)).isEqualTo(1);
            assertThat(mast.runningMonth(6)).isEqualTo(4);
        }
    }

    @Nested
    class ZwischenabrechnungGrund {

        @Test
        void givenErsterMonatOfTheMast_thenGiveNoGrund() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 6, 30), 1, 18);

            assertThat(mast.zwischenabrechnungGrund(1)).isEmpty();
        }

        @Test
        void givenMonatWithinAnAltersstufe_thenGiveNoGrund() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 6, 30), 1, 18);

            assertThat(mast.zwischenabrechnungGrund(5)).isEmpty();
        }

        @Test
        void givenMonatOfTheAltersstufenwechsel_thenNameTheMastAndTheAltersstufe() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 6, 30), 1, 18);

            assertThat(mast.zwischenabrechnungGrund(13)).contains("Mast 1 wechselt in Altersstufe 2");
        }

        @Test
        void givenMastStartingLater_thenCountTheAltersstufeFromItsOwnBeginn() {
            final Mast mast = new Mast(2, LocalDate.of(2026, 3, 1), LocalDate.of(2027, 8, 31), 3, 20);

            assertThat(mast.zwischenabrechnungGrund(13)).isEmpty();
            assertThat(mast.zwischenabrechnungGrund(15)).contains("Mast 2 wechselt in Altersstufe 2");
        }
    }
}
