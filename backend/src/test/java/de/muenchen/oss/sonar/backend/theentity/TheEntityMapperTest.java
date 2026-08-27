package de.muenchen.oss.sonar.backend.theentity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.muenchen.oss.sonar.backend.theentity.dto.TheEntityMapper;
import de.muenchen.oss.sonar.backend.theentity.dto.TheEntityRequestDTO;
import de.muenchen.oss.sonar.backend.theentity.dto.TheEntityResponseDTO;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@AllArgsConstructor
class TheEntityMapperTest {

    private final TheEntityMapper theEntityMapper = Mappers.getMapper(TheEntityMapper.class);

    @Nested
    class ToDTO {
        @Test
        void givenEntity_thenReturnsCorrectDTO() {
            final UUID uuid = UUID.randomUUID();
            final TheEntity theEntity = new TheEntity();
            theEntity.setId(uuid);
            theEntity.setTextAttribute("test");

            final TheEntityResponseDTO result = theEntityMapper.toDTO(theEntity);

            assertNotNull(result);
            assertThat(result).usingRecursiveComparison().isEqualTo(theEntity);
        }
    }

    @Nested
    class ToEntity {
        @Test
        void givenRequestDTO_thenReturnsCorrectEntity() {
            final TheEntityRequestDTO requestDTO = new TheEntityRequestDTO("test");

            final TheEntity result = theEntityMapper.toEntity(requestDTO);

            assertThat(result).usingRecursiveComparison().ignoringFields("id").isEqualTo(requestDTO);
        }
    }

}
