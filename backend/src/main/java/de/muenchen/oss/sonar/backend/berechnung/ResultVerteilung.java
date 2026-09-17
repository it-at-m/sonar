package de.muenchen.oss.sonar.backend.berechnung;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class ResultVerteilung {

    private final Map<String, BigDecimal> flaecheQmProZeitindex = new HashMap<>();

    public void addFlaecheQm(final String zeitindex, final BigDecimal flaecheQm) {
        flaecheQmProZeitindex.merge(zeitindex, flaecheQm, BigDecimal::add);
    }

}
