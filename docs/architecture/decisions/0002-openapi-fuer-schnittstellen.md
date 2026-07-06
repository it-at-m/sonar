# Verwendung von OpenAPI für Schnittstellen und Codegenerierung

## Status

accepted

## Kontext

Die Anwendung stellt REST-Schnittstellen bereit, die von mehreren Konsumenten genutzt werden (Frontend sowie ggf. weitere Services). Manuell geschriebene API-Clients sind fehleranfällig, führen zu Versionsdrift und erhöhen Pflegeaufwand. Eine maschinenlesbare, versionierte Schnittstellenbeschreibung ermöglicht konsistente Implementierungen und eine automatisierte Clientgenerierung.

Im Projekt existieren OpenAPI-Spezifikationen und Build-Schritte zur Codegenerierung (z. B. Spezifikationen unter `backend/api-spec/` und Clientgenerierung im Frontend über `openapi-generator-cli`).

## Entscheidung

Wir verwenden OpenAPI 3 als „Single Source of Truth“ für öffentliche/externe REST-Schnittstellen und generieren Client-Code daraus:
- Pflege der OpenAPI-Spezifikationen im Repository (z. B. unter `backend/api-spec/`).
- Frontend und weitere Services generieren benötigte Clients automatisiert anhand der OpenAPI-Spezifikationen (z. B. via `openapi-generator`).
- Handschriftliche Clients werden vermieden; notwendige Anpassungen erfolgen über Generator-Optionen bzw. Templating.
- Änderungen an Schnittstellen werden zuerst in der Spezifikation vorgenommen und versioniert; erst danach Umsetzung im Code und Aktualisierung der generierten Clients.

## Vorteile

- Einheitliche, versionierte Quelle der Wahrheit für Schnittstellen (weniger Drift zwischen Backend und Clients).
- Automatisierte, typsichere Clientgenerierung reduziert Boilerplate und Fehler.
- Bessere Dokumentation und Entwicklererfahrung (Swagger/OpenAPI-UI, klare Vertragsdefinitionen).
- Schnellere Integration neuer Endpunkte bei mehreren Konsumenten.
- Erleichterte Testbarkeit und Mocking auf Basis der Spezifikation.

## Nachteile

- Zusätzliche Disziplin erforderlich: Spezifikation muss aktuell gehalten und versioniert werden.
- Generatoren können Einschränkungen/Meinungen mitbringen; Feintuning über Optionen/Templates nötig.
- Längere Build-Zeiten durch Codegenerierung und ggf. zusätzliche Toolchain-Anforderungen.
- Potenziell mehr PR-„Rauschen“ durch generierte Artefakte (sofern eingecheckt) und Versionsanpassungen.
