package de.muenchen.oss.sonar.backend.projekt;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.oss.sonar.backend.common.ForbiddenException;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.stadtbezirk.Stadtbezirk;
import de.muenchen.oss.sonar.backend.stadtbezirk.StadtbezirkService;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjektZugriffServiceTest {

    private static final UUID PROJEKT_ID = UUID.randomUUID();

    @Mock
    private ProjektRepository projektRepository;

    @Mock
    private StadtbezirkService stadtbezirkService;

    @InjectMocks
    private ProjektZugriffService unitUnderTest;

    @Nested
    class CheckZugriffOnProjekt {
        @Test
        void givenProjektInAStadtbezirkOfTheUser_thenPermitAccess() {
            when(projektRepository.findStadtbezirkById(PROJEKT_ID))
                    .thenReturn(Optional.of(new ProjektStadtbezirk(PROJEKT_ID, Stadtbezirk.BEZIRK_06_SENDLING)));
            when(stadtbezirkService.getStadtbezirkeOfCurrentUser())
                    .thenReturn(Set.of(Stadtbezirk.BEZIRK_01_ALTSTADT_LEHEL, Stadtbezirk.BEZIRK_06_SENDLING));

            assertThatCode(() -> unitUnderTest.checkZugriffOnProjekt(PROJEKT_ID)).doesNotThrowAnyException();
        }

        @Test
        void givenProjektOutsideTheStadtbezirkeOfTheUser_thenThrowForbidden() {
            when(projektRepository.findStadtbezirkById(PROJEKT_ID))
                    .thenReturn(Optional.of(new ProjektStadtbezirk(PROJEKT_ID, Stadtbezirk.BEZIRK_10_MOOSACH)));
            when(stadtbezirkService.getStadtbezirkeOfCurrentUser())
                    .thenReturn(Set.of(Stadtbezirk.BEZIRK_01_ALTSTADT_LEHEL, Stadtbezirk.BEZIRK_06_SENDLING));

            assertThatThrownBy(() -> unitUnderTest.checkZugriffOnProjekt(PROJEKT_ID))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining(Stadtbezirk.BEZIRK_10_MOOSACH.name());
        }

        @Test
        void givenUserWithoutStadtbezirke_thenThrowForbidden() {
            when(projektRepository.findStadtbezirkById(PROJEKT_ID))
                    .thenReturn(Optional.of(new ProjektStadtbezirk(PROJEKT_ID, Stadtbezirk.BEZIRK_01_ALTSTADT_LEHEL)));
            when(stadtbezirkService.getStadtbezirkeOfCurrentUser()).thenReturn(Set.of());

            assertThatThrownBy(() -> unitUnderTest.checkZugriffOnProjekt(PROJEKT_ID))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        void givenUnknownProjekt_thenThrowNotFoundWithoutAskingForTheStadtbezirke() {
            when(projektRepository.findStadtbezirkById(PROJEKT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> unitUnderTest.checkZugriffOnProjekt(PROJEKT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(PROJEKT_ID.toString());
            verify(stadtbezirkService, never()).getStadtbezirkeOfCurrentUser();
        }
    }

    @Nested
    class CheckZugriffOnStadtbezirk {
        @Test
        void givenStadtbezirkOfTheUser_thenPermitAccess() {
            when(stadtbezirkService.getStadtbezirkeOfCurrentUser())
                    .thenReturn(Set.of(Stadtbezirk.BEZIRK_01_ALTSTADT_LEHEL, Stadtbezirk.BEZIRK_06_SENDLING));

            assertThatCode(() -> unitUnderTest.checkZugriffOnStadtbezirk(Stadtbezirk.BEZIRK_06_SENDLING))
                    .doesNotThrowAnyException();
        }

        @Test
        void givenStadtbezirkOutsideTheStadtbezirkeOfTheUser_thenThrowForbidden() {
            when(stadtbezirkService.getStadtbezirkeOfCurrentUser())
                    .thenReturn(Set.of(Stadtbezirk.BEZIRK_01_ALTSTADT_LEHEL, Stadtbezirk.BEZIRK_06_SENDLING));

            assertThatThrownBy(() -> unitUnderTest.checkZugriffOnStadtbezirk(Stadtbezirk.BEZIRK_10_MOOSACH))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining(Stadtbezirk.BEZIRK_10_MOOSACH.name());
        }
    }
}
