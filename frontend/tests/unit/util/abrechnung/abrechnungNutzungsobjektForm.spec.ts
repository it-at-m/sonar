import { describe, expect, it } from "vitest";

import { ProjektAdresseRequestDTOArtEnum } from "@/api/generated/sonar-backend";
import {
  createAbrechnungNutzungsobjekt,
  isAbrechnungNutzungsobjektDirty,
} from "@/util/abrechnung/abrechnungNutzungsobjektForm";
import { createAbrechnungPosition } from "@/util/abrechnung/abrechnungPositionForm";

describe("abrechnungNutzungsobjektForm.ts", () => {
  describe("createAbrechnungNutzungsobjekt", () => {
    it("givenNewNutzungsobjekt_thenStartItAsAdresseWithOnePosition", () => {
      const nutzungsobjekt = createAbrechnungNutzungsobjekt();

      expect(nutzungsobjekt.art).toBe(ProjektAdresseRequestDTOArtEnum.ADRESSE);
      expect(nutzungsobjekt.positionen).toHaveLength(1);
    });
  });

  describe("isAbrechnungNutzungsobjektDirty", () => {
    it("givenNewNutzungsobjekt_thenReturnFalse", () => {
      expect(
        isAbrechnungNutzungsobjektDirty(createAbrechnungNutzungsobjekt())
      ).toBe(false);
    });

    it("givenChangedArt_thenReturnTrue", () => {
      const nutzungsobjekt = createAbrechnungNutzungsobjekt();
      nutzungsobjekt.art = ProjektAdresseRequestDTOArtEnum.FLURSTUECK;

      expect(isAbrechnungNutzungsobjektDirty(nutzungsobjekt)).toBe(true);
    });

    it("givenAddedPosition_thenReturnTrue", () => {
      const nutzungsobjekt = createAbrechnungNutzungsobjekt();
      nutzungsobjekt.positionen.push(createAbrechnungPosition());

      expect(isAbrechnungNutzungsobjektDirty(nutzungsobjekt)).toBe(true);
    });

    it("givenFilledPosition_thenReturnTrue", () => {
      const nutzungsobjekt = createAbrechnungNutzungsobjekt();
      const position = nutzungsobjekt.positionen[0];
      if (position === undefined) {
        throw new Error("Das Nutzungsobjekt hat keine Position.");
      }
      position.laenge = 12;

      expect(isAbrechnungNutzungsobjektDirty(nutzungsobjekt)).toBe(true);
    });

    it("givenAufschlag50prozent_thenReturnTrue", () => {
      const nutzungsobjekt = createAbrechnungNutzungsobjekt();
      nutzungsobjekt.aufschlag50prozent = true;

      expect(isAbrechnungNutzungsobjektDirty(nutzungsobjekt)).toBe(true);
    });

    it("givenTageUnerlaubteNutzung_thenReturnTrue", () => {
      const nutzungsobjekt = createAbrechnungNutzungsobjekt();
      nutzungsobjekt.tageUnerlaubteNutzung = 3;

      expect(isAbrechnungNutzungsobjektDirty(nutzungsobjekt)).toBe(true);
    });

    it("givenUebernommenesNutzungsobjekt_thenReturnTrue", () => {
      const nutzungsobjekt = createAbrechnungNutzungsobjekt();
      nutzungsobjekt.uebernommenesNutzungsobjektId =
        "7c6b5a4d-3e2f-4a1b-9c8d-7e6f5a4b3c2d";

      expect(isAbrechnungNutzungsobjektDirty(nutzungsobjekt)).toBe(true);
    });
  });
});
