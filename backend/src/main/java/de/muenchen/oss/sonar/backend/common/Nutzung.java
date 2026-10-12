package de.muenchen.oss.sonar.backend.common;

import java.util.List;
import lombok.Getter;

@Getter
public enum Nutzung {

    AUFZUEGE("01"),

    AUTOKRAENE("02"),

    BAGGER("03"),

    BAUWAGEN("04"),

    BAUZAUN("06"),

    CONTAINER("07"),

    BAUGERUESTE("08"),

    HAUSKANALANSCHLUSS("09"),

    KRAENE("10"),

    MATERIALLAGERUNG("11"),

    UEBERSPANNUNG("13"),

    HEBEBUEHNEN("14"),

    SONSTIGES("15");

    private final String code;

    Nutzung(final String code) {
        this.code = code;
    }

    public static List<Nutzung> distinctSorted(final List<Nutzung> nutzungen) {
        return nutzungen == null ? List.of() : nutzungen.stream().distinct().sorted().toList();
    }

}
