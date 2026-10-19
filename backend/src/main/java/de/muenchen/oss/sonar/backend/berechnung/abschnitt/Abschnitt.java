package de.muenchen.oss.sonar.backend.berechnung.abschnitt;

public record Abschnitt(int von, int bis, String grund) {

    public int dauer() {
        return bis - von + 1;
    }
}
