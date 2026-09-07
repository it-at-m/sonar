package de.muenchen.oss.sonar.backend.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import de.muenchen.oss.sonar.backend.geschaeftspartner.GeschaeftspartnerProperties;
import de.muenchen.oss.sonar.backend.geschaeftspartner.client.SoapGeschaeftspartnerClient;
import de.muenchen.oss.sonar.backend.geschaeftspartner.client.UnconfiguredGeschaeftspartnerClient;
import de.muenchen.oss.sonar.backend.geschaeftspartner.dto.GeschaeftspartnerDTOMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.slf4j.LoggerFactory;

class GeschaeftspartnerClientConfigurationTest {

    private final GeschaeftspartnerClientConfiguration unitUnderTest = new GeschaeftspartnerClientConfiguration();

    private final GeschaeftspartnerDTOMapper geschaeftspartnerDTOMapper = Mappers.getMapper(GeschaeftspartnerDTOMapper.class);

    private final ListAppender<ILoggingEvent> loggedEvents = new ListAppender<>();

    private static Logger configurationLogger() {
        return (Logger) LoggerFactory.getLogger(GeschaeftspartnerClientConfiguration.class);
    }

    @BeforeEach
    void setUp() {
        loggedEvents.start();
        configurationLogger().addAppender(loggedEvents);
    }

    @AfterEach
    void tearDown() {
        configurationLogger().detachAppender(loggedEvents);
    }

    @Nested
    class GeschaeftspartnerClientBean {
        @Test
        void givenMissingUrl_thenCreateTheUnconfiguredClient() {
            final GeschaeftspartnerProperties properties = new GeschaeftspartnerProperties();

            assertThat(unitUnderTest.geschaeftspartnerClient(properties, geschaeftspartnerDTOMapper))
                    .isInstanceOf(UnconfiguredGeschaeftspartnerClient.class);
        }

        @Test
        void givenBlankUrl_thenCreateTheUnconfiguredClient() {
            final GeschaeftspartnerProperties properties = new GeschaeftspartnerProperties();
            properties.setUrl("  ");

            assertThat(unitUnderTest.geschaeftspartnerClient(properties, geschaeftspartnerDTOMapper))
                    .isInstanceOf(UnconfiguredGeschaeftspartnerClient.class);
        }

        @Test
        void givenMissingUrl_thenLogAnError() {
            final GeschaeftspartnerProperties properties = new GeschaeftspartnerProperties();

            unitUnderTest.geschaeftspartnerClient(properties, geschaeftspartnerDTOMapper);

            assertThat(loggedEvents.list).singleElement().satisfies(event -> {
                assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                assertThat(event.getFormattedMessage()).contains("sonar.geschaeftspartner.client.url");
            });
        }

        @Test
        void givenUrl_thenCreateTheSoapClient() {
            final GeschaeftspartnerProperties properties = new GeschaeftspartnerProperties();
            properties.setUrl("https://example.muenchen.de/v1/geschaeftspartner");

            assertThat(unitUnderTest.geschaeftspartnerClient(properties, geschaeftspartnerDTOMapper))
                    .isInstanceOf(SoapGeschaeftspartnerClient.class);
            assertThat(loggedEvents.list).isEmpty();
        }
    }
}
