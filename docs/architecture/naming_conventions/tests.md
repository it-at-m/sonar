# Naming Conventions für Tests

Für Tests, Frontend als auch Backend, gelten prinzipiell die gleichen Regeln, welche aber unterschiedlich umzusetzen sind.

## Entscheidung

Die Bezeichnungen sollen dem Schema `given<Input>_then<Expected>` folgen, wobei given (= StateUnderTest, bzw. zu testender Zustand)
und then (= ExpectedBehavior, bzw. erwartetes Ergebnis) in CamelCase gehalten werden. Dem Schema entsprechend sind die Testnamen
auch auf Englisch zu formulieren.

Wir haben uns darauf geeinigt, die zu testenden Methoden im Backend mit `@Nested`, und im Frontend mit
`describe("xyz", () => {})` zu gruppieren. Im Fall von überladenen Methoden werden diese innerhalb der Methodenklasse
zusätzlich verschachtelt und ebenfalls mit `@Nested` annotiert, oder in einen neuen `describe()`-Block eingeordnet.

## Kontext

Damit der Gesamtcode im Projekt übersichtlicher und einheitlicher ist, sollen Naming Conventions eingesetzt werden.
So wird gewährleistet, dass deren Kontext schneller klar ist, ohne den Code lesen zu müssen und die Wartung
und Erweiterung des Codes wird erleichtert.

## Beispiele

### Backend

```java
void givenEntityId_thenReturnEntity() {}
void givenEntity_thenReturnsCorrectDTO() {}
```

### Frontend

Allgemein gilt für den Aufbau:

```typescript
describe("<Dateiname des Testgegenstandes>", () => {
  describe("<zu testende Funktionalität>", () => {
    it("<Testfallbeschreibung>", () => {});
  });
});
```

> [!IMPORTANT]
> Die Tests sollen mit `it` definiert werden und **nicht** mit `test`, da `it` den Lesefluss verbessert.

#### Beispiel für Tests eines Stores

```typescript
import { describe, it } from "vitest";

/* Die Description ist der Dateiname des Testgegenstandes */ // [!code focus]
describe("wahlvorstandStore.ts", () => {
  // [!code focus]

  /* Die Description ist der Name der Funktion die getested wird */ // [!code focus]
  describe("isSchriftfuehrerAnwesend", () => {
    // [!code focus]
    /* Beschreibung des Testcases entsprechend des Schemas */ // [!code focus]
    it("givenNoMitgliedExists_thenReturnFalse", () => {}); // [!code focus]
    it("givenAtLeastOneMitgliedMatches_thenReturnTrue", () => {});
    it("givenMitgliedWithFunktionExistsButIsNotAnwesend_thenReturnFalse", () => {});
    it("givenNoMitgliedMatchesFunktion_thenReturnFalse", () => {});
  });

  describe("sendWahlvorstand", () => {
    // [!code focus]
    it("givenWahlbezirkIDIsGiven_thenSendWahlvorstand", async () => {});
    it("givenWahlvorstandIsSent_thenSetLastSend", async () => {});
    it("givenWahlbezirkIDIsNotGiven_thenNotSendWahlvorstand", async () => {});
  });
});
```
