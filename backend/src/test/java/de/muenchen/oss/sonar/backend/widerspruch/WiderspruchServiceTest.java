package de.muenchen.oss.sonar.backend.widerspruch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungRepository;
import de.muenchen.oss.sonar.backend.common.ConflictException;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.widerspruch.domain.Widerspruch;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WiderspruchServiceTest {

    private static final UUID ABRECHNUNG_ID = UUID.randomUUID();
    private static final LocalDate EINGANG = LocalDate.of(2026, 4, 1);

    @Mock
    private WiderspruchRepository widerspruchRepository;

    @Mock
    private AbrechnungRepository abrechnungRepository;

    @Spy
    private final WiderspruchEntityMapper widerspruchEntityMapper = Mappers.getMapper(WiderspruchEntityMapper.class);

    @InjectMocks
    private WiderspruchService unitUnderTest;

    @Nested
    class CreateWiderspruch {
        @Test
        void givenWiderspruch_thenReturnSavedWiderspruch() {
            final Widerspruch widerspruch = new Widerspruch(null, ABRECHNUNG_ID, EINGANG, LocalDate.of(2026, 4, 15),
                    LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 1), "Abrechnung wird durchgeführt", true, true,
                    "Bemerkung");

            final UUID savedId = UUID.randomUUID();
            when(abrechnungRepository.existsById(ABRECHNUNG_ID)).thenReturn(true);
            when(widerspruchRepository.findByAbrechnungId(ABRECHNUNG_ID)).thenReturn(Optional.empty());
            when(widerspruchRepository.save(any(WiderspruchEntity.class))).thenAnswer(invocation -> {
                final WiderspruchEntity toSave = invocation.getArgument(0);
                toSave.setId(savedId);
                return toSave;
            });

            final Widerspruch result = unitUnderTest.createWiderspruch(widerspruch);

            verify(widerspruchRepository).save(any(WiderspruchEntity.class));
            assertThat(result.id()).isEqualTo(savedId);
            assertThat(result.abrechnungId()).isEqualTo(ABRECHNUNG_ID);
            assertThat(result.datumEingang()).isEqualTo(EINGANG);
            assertThat(result.datumRuecknahme()).isEqualTo(LocalDate.of(2026, 4, 15));
            assertThat(result.datumVorlageRegierung()).isEqualTo(LocalDate.of(2026, 5, 1));
            assertThat(result.datumAblehnungRegierung()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(result.entscheidungDurchfuehrung()).isEqualTo("Abrechnung wird durchgeführt");
            assertThat(result.sollAbgesetzt()).isTrue();
            assertThat(result.neueTeilabrechnungAnlegen()).isTrue();
            assertThat(result.bemerkung()).isEqualTo("Bemerkung");
        }

        @Test
        void givenUnknownAbrechnung_thenThrowNotFound() {
            final Widerspruch widerspruch = new Widerspruch(null, ABRECHNUNG_ID, EINGANG, null, null, null, null,
                    false, false, null);
            when(abrechnungRepository.existsById(ABRECHNUNG_ID)).thenReturn(false);

            assertThatThrownBy(() -> unitUnderTest.createWiderspruch(widerspruch))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(ABRECHNUNG_ID.toString());
            verify(widerspruchRepository, never()).save(any(WiderspruchEntity.class));
        }

        @Test
        void givenAbrechnungThatAlreadyHasOne_thenThrowConflict() {
            final Widerspruch widerspruch = new Widerspruch(null, ABRECHNUNG_ID, EINGANG, null, null, null, null,
                    false, false, null);

            final WiderspruchEntity bestehenderWiderspruch = new WiderspruchEntity();
            bestehenderWiderspruch.setId(UUID.randomUUID());
            bestehenderWiderspruch.setAbrechnungId(ABRECHNUNG_ID);
            bestehenderWiderspruch.setDatumEingang(LocalDate.of(2026, 3, 1));

            when(abrechnungRepository.existsById(ABRECHNUNG_ID)).thenReturn(true);
            when(widerspruchRepository.findByAbrechnungId(ABRECHNUNG_ID)).thenReturn(Optional.of(bestehenderWiderspruch));

            assertThatThrownBy(() -> unitUnderTest.createWiderspruch(widerspruch))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining(ABRECHNUNG_ID.toString());
            verify(widerspruchRepository, never()).save(any(WiderspruchEntity.class));
        }
    }

    @Nested
    class GetWiderspruchOfAbrechnung {
        @Test
        void givenAbrechnungWithWiderspruch_thenReturnIt() {
            final WiderspruchEntity widerspruch = new WiderspruchEntity();
            widerspruch.setId(UUID.randomUUID());
            widerspruch.setAbrechnungId(ABRECHNUNG_ID);
            widerspruch.setDatumEingang(EINGANG);
            widerspruch.setBemerkung("Widerspruch der Eigentümerin");

            when(widerspruchRepository.findByAbrechnungId(ABRECHNUNG_ID)).thenReturn(Optional.of(widerspruch));

            final Optional<Widerspruch> result = unitUnderTest.getWiderspruchOfAbrechnung(ABRECHNUNG_ID);

            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo(widerspruch.getId());
            assertThat(result.get().datumEingang()).isEqualTo(EINGANG);
            assertThat(result.get().bemerkung()).isEqualTo("Widerspruch der Eigentümerin");
        }

        @Test
        void givenAbrechnungWithoutWiderspruch_thenReturnEmpty() {
            when(widerspruchRepository.findByAbrechnungId(ABRECHNUNG_ID)).thenReturn(Optional.empty());

            assertThat(unitUnderTest.getWiderspruchOfAbrechnung(ABRECHNUNG_ID)).isEmpty();
        }
    }

    @Nested
    class GetWiderspruecheOfAbrechnungen {
        @Test
        void givenOnlyOneAbrechnungWithWiderspruch_thenKeyTheWiderspruchByItsAbrechnung() {
            final UUID abrechnungOhneWiderspruchId = UUID.randomUUID();

            final WiderspruchEntity widerspruch = new WiderspruchEntity();
            widerspruch.setId(UUID.randomUUID());
            widerspruch.setAbrechnungId(ABRECHNUNG_ID);
            widerspruch.setDatumEingang(EINGANG);
            widerspruch.setBemerkung("Widerspruch der Eigentümerin");

            when(widerspruchRepository.findByAbrechnungIdIn(List.of(ABRECHNUNG_ID, abrechnungOhneWiderspruchId)))
                    .thenReturn(List.of(widerspruch));

            final Map<UUID, Widerspruch> result = unitUnderTest
                    .getWiderspruecheOfAbrechnungen(List.of(ABRECHNUNG_ID, abrechnungOhneWiderspruchId));

            assertThat(result).containsOnlyKeys(ABRECHNUNG_ID);
            assertThat(result.get(ABRECHNUNG_ID).id()).isEqualTo(widerspruch.getId());
            assertThat(result.get(ABRECHNUNG_ID).bemerkung()).isEqualTo("Widerspruch der Eigentümerin");
        }

        @Test
        void givenNoAbrechnungIds_thenAskTheRepositoryForNothing() {
            assertThat(unitUnderTest.getWiderspruecheOfAbrechnungen(List.of())).isEmpty();

            verify(widerspruchRepository, never()).findByAbrechnungIdIn(anyCollection());
        }
    }
}
