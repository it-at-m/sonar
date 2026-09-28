# Offset-Pagination für Listen-Endpunkte

## Status

accepted

## Kontext

Listen-Endpunkte müssen ihre Ergebnisse seitenweise ausliefern. Dafür stehen zwei Verfahren zur Wahl.

Die **Offset-Pagination** überspringt mit `LIMIT`/`OFFSET` die Zeilen der vorangehenden Seiten. Sie erlaubt den Sprung auf eine beliebige Seite, wird aber mit zunehmender Seitentiefe langsamer, weil die übersprungenen Zeilen dennoch erzeugt werden müssen.

Die **Keyset-Pagination** (auch Cursor-Pagination) merkt sich stattdessen die Sortierwerte der letzten ausgelieferten Zeile und setzt die Abfrage mit `WHERE (sortierspalte, id) < (:letzterWert, :letzteId)` dort fort. Bei passendem Index ist der Aufwand unabhängig von der Seitentiefe. Dafür sind nur Vor- und Zurück-Navigation möglich, denn eine Seitenzahl lässt sich nicht in einen Cursor umrechnen.

## Entscheidung

Wir paginieren Listen-Endpunkte offsetbasiert über Spring Data `Page`/`Pageable` und verzichten auf Keyset-Pagination.

- Jeder paginierte Endpunkt nimmt `pageNumber` und `pageSize` und liefert ein `Page` (serialisiert als `PagedModel` mit `content` und `page`-Metadaten).
- Die Sortierung wird **serverseitig ausgeführt**, damit sie für die gesamte Treffermenge gilt und nicht nur für die angezeigte Seite. Der Client wählt sie über die Parameter `sortBy` und `sortDirection`.
- Fehlt einer der beiden Parameter, gilt der Standard des jeweiligen Endpunkts. Die Tabelle initialisiert ihr Sortiermodell auf denselben Wert, damit der Pfeil im Spaltenkopf zur tatsächlichen Reihenfolge passt.

Wir bewerten die Entscheidung neu, wenn ein Endpunkt Infinite Scroll bedient oder eine Tabelle die Größenordnung von etwa 100k Zeilen erreicht.

## Vorteile

- Sprung auf eine beliebige Seite.
- Kein eigenes Cursor-Format: `Page` ist in der OpenAPI-Spezifikation bereits als `PagedModel` beschrieben, der generierte Client kommt ohne Sonderbehandlung aus.
- Deutlich weniger Code als eine Cursor-Implementierung.

## Nachteile

- Tiefe Seiten werden langsamer, weil die übersprungenen Zeilen erzeugt werden müssen.
- Wird zwischen zwei Seitenabrufen geschrieben, kann sich die Ergebnismenge verschieben und eine Zeile doppelt oder gar nicht erscheinen. Die Totalordnung begrenzt den Effekt auf tatsächlich veränderte Daten, beseitigt ihn aber nicht.
- Ein Wechsel auf Keyset würde später eine neue Antwortstruktur und eine andere Navigations-UI erfordern.
