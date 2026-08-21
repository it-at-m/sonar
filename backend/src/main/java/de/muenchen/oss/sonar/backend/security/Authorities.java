package de.muenchen.oss.sonar.backend.security;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Each possible authority in this project is represented by a constant in this class.
 * The constants are used within the {@link org.springframework.stereotype.Controller} or
 * {@link org.springframework.stereotype.Service} classes in the method security annotations
 * (e.g. {@link PreAuthorize}).
 */
@SuppressWarnings("PMD.DataClass")
public final class Authorities {
    /**
     * The two expressions below are named instead of being repeated per constant. Every read grants
     * the same roles and every write does too, so spelling them out once keeps them from drifting
     * apart. Both stay compile time constants, which the method security annotations require.
     */
    private static final String READ = "hasAnyRole('reader', 'writer')";

    private static final String WRITE = "hasAnyRole('writer')";

    // Role based auth (default)
    public static final String THEENTITY_GET = READ;
    public static final String THEENTITY_GET_ALL = READ;
    public static final String THEENTITY_CREATE = WRITE;
    public static final String THEENTITY_UPDATE = WRITE;
    public static final String THEENTITY_DELETE = WRITE;
    public static final String PROJEKT_CREATE = WRITE;

    // Permissions based auth
    // public static final String THEENTITY_GET = "hasAuthority('REFARCH_THEENTITY_READ')";
    // public static final String THEENTITY_GET_ALL = "hasAuthority('REFARCH_THEENTITY_READ')";
    // public static final String THEENTITY_CREATE = "hasAuthority('REFARCH_THEENTITY_WRITE')";
    // public static final String THEENTITY_UPDATE = "hasAuthority('REFARCH_THEENTITY_WRITE')";
    // public static final String THEENTITY_DELETE = "hasAuthority('REFARCH_THEENTITY_DELETE')";

    private Authorities() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
