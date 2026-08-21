# Verwendung von OpenAPI für Schnittstellen und Codegenerierung

## Status

accepted

## Kontext

Die Anwendung stellt REST-Schnittstellen bereit, die von mehreren Konsumenten genutzt werden (Frontend sowie ggf. weitere Services). Manuell geschriebene API-Clients sind fehleranfällig, führen zu Versionsdrift und erhöhen Pflegeaufwand. Eine maschinenlesbare, versionierte Schnittstellenbeschreibung ermöglicht konsistente Implementierungen und eine automatisierte Clientgenerierung.

Im Projekt existieren OpenAPI-Spezifikationen und Build-Schritte zur Codegenerierung (z. B. Spezifikationen unter `backend/api-spec/` und Clientgenerierung im Frontend über `openapi-generator-cli`).

## Entscheidung

Wir verwenden OpenAPI 3 als „Single Source of Truth“ für öffentliche/externe REST-Schnittstellen. Die Spezifikation wird dabei aus dem Code erzeugt (Code-First), der Client-Code wiederum aus der Spezifikation:

- Die Spezifikation wird von springdoc aus den `@RestController`-Methoden und den `*RequestDTO`/`*ResponseDTO`-Records abgeleitet: Bean-Validation-Annotationen werden zu Schema-Constraints (`required`, `maxLength`, `minimum`, `minItems`), das Javadoc der Controller wird über `therapi-runtime-javadoc` zu `summary` und `description`.
- Die erzeugte Spezifikation wird unter `backend/api-spec/` versioniert eingecheckt und ist damit der verbindliche Vertrag für alle Konsumenten.
- Frontend und weitere Services generieren benötigte Clients automatisiert anhand der OpenAPI-Spezifikationen (z. B. via `openapi-generator`).
- Handschriftliche Clients werden vermieden; notwendige Anpassungen erfolgen über Generator-Optionen bzw. Templating.
- Änderungen an Schnittstellen erfolgen daher zuerst im Code (Controller und DTOs). Anschließend wird die Spezifikation mit `mvn clean test -Dtest=OpenApiSpecTest -Dopenapi.generate=true` neu erzeugt und mit eingecheckt; die generierten Clients ziehen automatisch nach.
- Alternativ kann die Spezifikation mit `mvn springdoc-openapi:generate` gegen eine bereits laufende Anwendung erzeugt werden (Profil `local`, Port 8086). Beide Wege erzeugen dieselbe Datei; weichen sie voneinander ab, ist `OpenApiSpecTest` maßgeblich.
- `OpenApiSpecTest` vergleicht die eingecheckte Spezifikation bei jedem Build mit dem Dokument der laufenden Anwendung und lässt den Build fehlschlagen, wenn beide auseinanderlaufen. Der Test benötigt weder Datenbank noch Docker, da das Dokument nur aus Handler-Mappings und DTO-Schemata entsteht.

## Vorteile

- Einheitliche, versionierte Quelle der Wahrheit für Schnittstellen (weniger Drift zwischen Backend und Clients).
- Automatisierte, typsichere Clientgenerierung reduziert Boilerplate und Fehler.
- Bessere Dokumentation und Entwicklererfahrung (Swagger/OpenAPI-UI, klare Vertragsdefinitionen).
- Schnellere Integration neuer Endpunkte bei mehreren Konsumenten.
- Erleichterte Testbarkeit und Mocking auf Basis der Spezifikation.

## Nachteile

- Zusätzliche Disziplin erforderlich: Spezifikation muss aktuell gehalten und versioniert werden.
- Die Spezifikation muss nach Schnittstellenänderungen bewusst neu erzeugt und mit eingecheckt werden. Der Build erkennt das Versäumnis, erledigt es aber nicht selbst.
- Der Vergleich im Build setzt voraus, dass der Anwendungskontext ohne Datenbank startet. Dafür schließt `OpenApiSpecTest` die Persistenz-Autokonfiguration aus und ersetzt die Repositories durch Mocks. Neue Repositories müssen dort ergänzt werden.
- Generatoren können Einschränkungen/Meinungen mitbringen; Feintuning über Optionen/Templates nötig.
- Längere Build-Zeiten durch Codegenerierung und ggf. zusätzliche Toolchain-Anforderungen.
- Potenziell mehr PR-„Rauschen“ durch generierte Artefakte (sofern eingecheckt) und Versionsanpassungen.
