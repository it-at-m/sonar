package de.muenchen.oss.sonar.backend.projekt;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProjektSpecifications {

    private static final char LIKE_ESCAPE = '!';

    private static final String PROJEKTNUMMER = "projektnummer";
    private static final String ABRECHNUNG_BEGINN = "abrechnungBeginn";
    private static final String ABRECHNUNG_ENDE = "abrechnungEnde";

    public static Specification<ProjektEntity> matching(final ProjektFilter filter) {
        final List<Specification<ProjektEntity>> specifications = new ArrayList<>();

        if (filter.projektnummer() != null) {
            final String pattern = toLikePattern(filter.projektnummer().toLowerCase(Locale.ROOT));
            specifications.add((root, query, builder) -> builder.like(builder.lower(root.get(PROJEKTNUMMER)), pattern, LIKE_ESCAPE));
        }
        if (filter.abrechnungBeginn() != null) {
            specifications.add((root, query, builder) -> builder.equal(root.get(ABRECHNUNG_BEGINN), filter.abrechnungBeginn()));
        }
        if (filter.abrechnungEnde() != null) {
            specifications.add((root, query, builder) -> builder.equal(root.get(ABRECHNUNG_ENDE), filter.abrechnungEnde()));
        }

        return specifications.isEmpty() ? Specification.unrestricted() : Specification.allOf(specifications);
    }

    /**
     * Wildcards typed by the user are escaped, so searching for "%" finds a Projektnummer
     * containing a percent sign instead of every Projekt.
     */
    /* default */ static String toLikePattern(final String value) {
        final String escape = String.valueOf(LIKE_ESCAPE);
        final String escaped = value
                .replace(escape, escape + escape)
                .replace("%", escape + "%")
                .replace("_", escape + "_");
        return "%" + escaped + "%";
    }
}
