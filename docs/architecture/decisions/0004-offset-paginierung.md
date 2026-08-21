# Offset-Paginierung für Listen-Endpunkte

## Status

accepted

## Kontext

Listen-Endpunkte müssen ihre Ergebnisse seitenweise ausliefern. Dafür stehen zwei Verfahren zur Wahl.

Die **Offset-Paginierung** überspringt mit `LIMIT`/`OFFSET` die Zeilen der vorangehenden Seiten und ermittelt die Gesamttrefferzahl über eine zusätzliche `COUNT`-Abfrage. Sie erlaubt den Sprung auf eine beliebige Seite und die Anzeige der Gesamtzahl, wird aber mit zunehmender Seitentiefe langsamer, weil die übersprungenen Zeilen dennoch erzeugt werden müssen.

Die **Keyset-Paginierung** (auch Cursor-Paginierung) merkt sich stattdessen die Sortierwerte der letzten ausgelieferten Zeile und setzt die Abfrage mit `WHERE (sortierspalte, id) < (:letzterWert, :letzteId)` dort fort. Bei passendem Index ist der Aufwand unabhängig von der Seitentiefe, und die `COUNT`-Abfrage entfällt. Dafür sind nur Vor- und Zurück-Navigation möglich: es gibt weder Seitenzahlen noch eine Gesamttrefferzahl.

Beide Verfahren setzen eine **Totalordnung** voraus. Ist die Sortierung nicht eindeutig, darf die Datenbank Zeilen in beliebiger Reihenfolge liefern. Dieselbe Zeile kann dann auf zwei Seiten erscheinen oder ganz fehlen. Ohne `ORDER BY` gilt das uneingeschränkt, aber auch eine Sortierung nach einer nicht-eindeutigen Spalte genügt nicht, weil Zeilen mit gleichem Wert an der Seitengrenze verloren gehen können.

## Entscheidung

Wir paginieren Listen-Endpunkte offsetbasiert über Spring Data `Page`/`Pageable` und verzichten auf Keyset-Paginierung.

- Jeder paginierte Endpunkt nimmt `pageNumber` und `pageSize` und liefert ein `Page` (serialisiert als `PagedModel` mit `content` und `page`-Metadaten).
- Die Sortierung ist **verbindlich Teil des `Pageable`** und muss eine Totalordnung sein: fachliche Sortierspalte plus die technische `id` als eindeutiger Tiebreaker. Alle Spalten sortieren in derselben Richtung, damit ein einfacher aufsteigender Index sie bedienen kann, vorwärts wie rückwärts gelesen.
- Die Sortierung wird **serverseitig ausgeführt**, damit sie für die gesamte Treffermenge gilt und nicht nur für die angezeigte Seite. Der Client wählt sie über die Parameter `sortBy` und `sortDirection`.
- `sortBy` ist ein **Enum, keine freie Property-Angabe**. Ein unbekannter Name würde die Persistenzschicht als ungültige Property erreichen und mit einem 500 enden; ein gültiger Name würde jedes Attribut der Entität sortierbar machen, auch nicht exportierte. Spring weist Werte außerhalb des Enums mit 400 ab, bevor der Controller erreicht wird.
- Fehlt einer der beiden Parameter, gilt der Standard: absteigend nach Projektnummer. Die Tabelle initialisiert ihr Sortiermodell auf denselben Wert, damit der Pfeil im Spaltenkopf zur tatsächlichen Reihenfolge passt.

Wir bewerten die Entscheidung neu, wenn ein Endpunkt Infinite Scroll bedient oder eine Tabelle die Größenordnung von etwa 100k Zeilen erreicht.

## Vorteile

- Seitenzahlen, Sprung auf eine beliebige Seite und Gesamttrefferzahl: genau das, was die Fachbereichs-Übersichten mit `v-data-table-server` benötigen.
- Kein eigenes Cursor-Format: `Page` ist in der OpenAPI-Spezifikation bereits als `PagedModel` beschrieben, der generierte Client kommt ohne Sonderbehandlung aus.
- Entspricht der Refarch-Konvention, dadurch ohne Einarbeitung lesbar und in allen Endpunkten gleich.
- Deutlich weniger Code als eine Cursor-Implementierung: keine Kodierung, Validierung und Versionierung opaker Tokens.

## Nachteile

- Tiefe Seiten werden langsamer, weil die übersprungenen Zeilen erzeugt werden müssen; die zusätzliche `COUNT`-Abfrage kostet einen weiteren Durchlauf.
- Wird zwischen zwei Seitenabrufen geschrieben, kann sich die Ergebnismenge verschieben und eine Zeile doppelt oder gar nicht erscheinen. Die Totalordnung begrenzt den Effekt auf tatsächlich veränderte Daten, beseitigt ihn aber nicht.
- Der Nutzen der Sortierung hängt an einem passenden Index. Ohne ihn sortiert die Datenbank die gesamte Treffermenge, bevor sie eine Seite ausschneidet.
- Ein Wechsel auf Keyset würde später eine neue Antwortstruktur und eine andere Navigations-UI erfordern.
