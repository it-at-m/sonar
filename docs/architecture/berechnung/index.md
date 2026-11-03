# Berechnung

Die Berechnung bildet aus einem `Projekt` und einer `Abrechnung` das `Berechnungsergebnis`: die Gebühren für Flächen, Überspannungen und Verwaltung, dazu die Texte, die Bescheid und Oberfläche anzeigen.

Der Code liegt unter `backend/src/main/java/de/muenchen/oss/sonar/backend/berechnung`.

```mermaid
flowchart TD
    BLS["<b>BerechnungslaufService</b><br/>baut die Eingabe aus Projekt und Abrechnung"]
    BES["<b>BerechnungsergebnisService</b><br/>rechnet den Lauf durch, setzt das Ergebnis zusammen"]
    ZF["<b>Zusammenfassung</b><br/>schreibt die Zusammenfassung"]
    BL["<b>Berechnungslog</b><br/>schreibt das Protokoll jeder Periode"]
    GS["<b>GebuehrenService</b><br/>addiert die Gebühren, zieht die Vorabrechnung ab"]

    subgraph FLAECHEN["Berechnung Flaechen"]
        FABS["<b>FlaechenabrechnungService</b><br/>rechnet alle Wochen, bündelt sie zu Abschnitten"]
        FWS["<b>FlaechenwocheService</b><br/>rechnet eine Woche: Stufe und Gebührenposten"]
        FAS["<b>FlaechenadresseService</b><br/>baut die Adresse, liefert je Woche die Beiträge"]
        FBS["<b>FlaechenbeitragService</b><br/>erkennt den Grund für eine Zwischenabrechnung"]
        FS["<b>FlaecheService</b><br/>erzeugt eine Fläche für die Berechnung"]
        FT["<b>Flaechentext</b><br/>schreibt die Textzeilen der Flächen"]
    end

    subgraph UEBER["Berechnung Ueberspannungen"]
        UABS["<b>UeberspannungsabrechnungService</b><br/>rechnet jede Adresse ab und summiert"]
        UAAS["<b>UeberspannungsadressabrechnungService</b><br/>rechnet alle Monate, bündelt sie zu Abschnitten"]
        UMS["<b>UeberspannungsmonatService</b><br/>rechnet einen Monat: Ränge und Mastposten"]
        UAS["<b>UeberspannungsadresseService</b><br/>baut die Adresse, liefert je Monat die aktiven Masten"]
        MS["<b>MastService</b><br/>erzeugt einen Mast für die Berechnung"]
        UT["<b>Ueberspannungstext</b><br/>schreibt die Textzeilen der Überspannungen"]
    end

    BLS --> FAS
    BLS --> UAS

    BES --> FABS
    BES --> UABS
    BES --> GS
    BES -->|"schreibe"| ZF
    BES -->|"schreibe"| BL

    FABS --> FWS
    FABS -->|"Textbausteine"| FT
    FWS -->|"beitraege"| FAS
    FWS -->|"zwischenabrechnungGrund"| FBS
    FAS -->|"berechne, hasTakenEffectBy"| FS

    UABS --> UAAS
    UAAS --> UMS
    UAAS -->|"Textbausteine"| UT
    UMS -->|"activeMastenOldestFirst"| UAS
    UMS -->|"zwischenabrechnungGrund"| MS
    UAS --> MS
```
