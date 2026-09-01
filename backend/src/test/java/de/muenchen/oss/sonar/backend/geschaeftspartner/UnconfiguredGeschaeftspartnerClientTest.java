package de.muenchen.oss.sonar.backend.geschaeftspartner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class UnconfiguredGeschaeftspartnerClientTest {

    private final UnconfiguredGeschaeftspartnerClient unitUnderTest = new UnconfiguredGeschaeftspartnerClient();

    private final ListAppender<ILoggingEvent> loggedEvents = new ListAppender<>();

    private static Logger clientLogger() {
        return (Logger) LoggerFactory.getLogger(UnconfiguredGeschaeftspartnerClient.class);
    }

    @BeforeEach
    void setUp() {
        loggedEvents.start();
        clientLogger().addAppender(loggedEvents);
    }

    @AfterEach
    void tearDown() {
        clientLogger().detachAppender(loggedEvents);
    }

    @Nested
    class FindById {
        @Test
        void givenAnyId_thenThrowGatewayError() {
            assertThatThrownBy(() -> unitUnderTest.findById("4711"))
                    .isInstanceOfSatisfying(ResponseStatusException.class,
                            exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY));
        }

        @Test
        void givenAnyId_thenLogTheMissingConfiguration() {
            assertThatThrownBy(() -> unitUnderTest.findById("4711")).isInstanceOf(ResponseStatusException.class);

            assertThat(loggedEvents.list).singleElement().satisfies(event -> {
                assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                assertThat(event.getFormattedMessage()).contains("sonar.geschaeftspartner.client.url");
            });
        }

        @Test
        void givenIdWithLineBreak_thenSanitizeItInTheLog() {
            assertThatThrownBy(() -> unitUnderTest.findById("4711\nERROR forged"))
                    .isInstanceOf(ResponseStatusException.class);

            assertThat(loggedEvents.list).singleElement()
                    .satisfies(event -> assertThat(event.getFormattedMessage()).doesNotContain("\n"));
        }
    }
}
