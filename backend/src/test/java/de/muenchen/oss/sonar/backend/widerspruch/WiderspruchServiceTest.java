package de.muenchen.oss.sonar.backend.widerspruch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungEntity;
import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungRepository;
import de.muenchen.oss.sonar.backend.common.ConflictException;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.widerspruch.domain.Widerspruch;
import java.time.LocalDate;
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
        void givenWiderspruch_thenSaveItAndPointTheAbrechnungAtIt() {
            final Widerspruch widerspruch = new Widerspruch(null, EINGANG, LocalDate.of(2026, 4, 15),
                    LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 1), "Abrechnung wird durchgeführt", true, true,
                    "Bemerkung");

            final AbrechnungEntity abrechnung = new AbrechnungEntity();
            abrechnung.setId(ABRECHNUNG_ID);

            final UUID savedId = UUID.randomUUID();
            when(abrechnungRepository.findById(ABRECHNUNG_ID)).thenReturn(Optional.of(abrechnung));
            when(widerspruchRepository.save(any(WiderspruchEntity.class))).thenAnswer(invocation -> {
                final WiderspruchEntity toSave = invocation.getArgument(0);
                toSave.setId(savedId);
                return toSave;
            });

            final Widerspruch result = unitUnderTest.createWiderspruch(ABRECHNUNG_ID, widerspruch);

            verify(abrechnungRepository).save(abrechnung);
            assertThat(abrechnung.getWiderspruch()).isNotNull();
            assertThat(abrechnung.getWiderspruch().getId()).isEqualTo(savedId);
            assertThat(result.id()).isEqualTo(savedId);
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
            final Widerspruch widerspruch = new Widerspruch(null, EINGANG, null, null, null, null, false, false, null);
            when(abrechnungRepository.findById(ABRECHNUNG_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> unitUnderTest.createWiderspruch(ABRECHNUNG_ID, widerspruch))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(ABRECHNUNG_ID.toString());
            verify(widerspruchRepository, never()).save(any(WiderspruchEntity.class));
        }

        @Test
        void givenAbrechnungThatAlreadyHasOne_thenThrowConflict() {
            final Widerspruch widerspruch = new Widerspruch(null, EINGANG, null, null, null, null, false, false, null);

            final WiderspruchEntity bestehenderWiderspruch = new WiderspruchEntity();
            bestehenderWiderspruch.setId(UUID.randomUUID());
            bestehenderWiderspruch.setDatumEingang(LocalDate.of(2026, 3, 1));

            final AbrechnungEntity abrechnung = new AbrechnungEntity();
            abrechnung.setId(ABRECHNUNG_ID);
            abrechnung.setWiderspruch(bestehenderWiderspruch);

            when(abrechnungRepository.findById(ABRECHNUNG_ID)).thenReturn(Optional.of(abrechnung));

            assertThatThrownBy(() -> unitUnderTest.createWiderspruch(ABRECHNUNG_ID, widerspruch))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining(ABRECHNUNG_ID.toString());
            verify(widerspruchRepository, never()).save(any(WiderspruchEntity.class));
        }
    }
}
