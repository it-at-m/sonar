import type { Adresse } from "@/types/common/Adresse";

import { describe, expect, it } from "vitest";

import {
  ProjektAdresseRequestDTOArtEnum,
  ProjektAdresseRequestDTONutzungEnum,
} from "@/api/generated/sonar-backend";
import {
  adresseLabel,
  adresseTitle,
  nutzungTitle,
} from "@/util/common/adresseLabel";

function adresse(overrides: Partial<Adresse> = {}): Adresse {
  return {
    art: ProjektAdresseRequestDTOArtEnum.ADRESSE,
    adresse: "Marienplatz",
    hausnummerVon: "8",
    hausnummerBis: "",
    flurstueck: "",
    gemarkung: "",
    nutzung: null,
    ...overrides,
  };
}

describe("adresseLabel.ts", () => {
  describe("adresseLabel", () => {
    it("givenAdresse_thenNameItByItsPosition", () => {
      expect(adresseLabel(adresse(), 1)).toBe("Adresse 1");
    });

    it("givenFlurstueck_thenUseTheOtherWord", () => {
      const label = adresseLabel(
        adresse({ art: ProjektAdresseRequestDTOArtEnum.FLURSTUECK }),
        2
      );

      expect(label).toBe("Flurstück 2");
    });
  });

  describe("adresseTitle", () => {
    it("givenAdresse_thenNameItWithItsHausnummer", () => {
      expect(adresseTitle(adresse())).toBe("Marienplatz 8");
    });

    it("givenSpanOfHausnummern_thenNameBothEnds", () => {
      const title = adresseTitle(adresse({ hausnummerBis: "10" }));

      expect(title).toBe("Marienplatz 8–10");
    });

    it("givenAdresseWithoutHausnummer_thenNameOnlyTheStreet", () => {
      const title = adresseTitle(adresse({ hausnummerVon: "" }));

      expect(title).toBe("Marienplatz");
    });

    it("givenFlurstueck_thenNameItWithItsGemarkung", () => {
      const title = adresseTitle(
        adresse({
          art: ProjektAdresseRequestDTOArtEnum.FLURSTUECK,
          adresse: "",
          hausnummerVon: "",
          flurstueck: "1234/5",
          gemarkung: "Sendling",
        })
      );

      expect(title).toBe("Flurstück 1234/5, Sendling");
    });
  });

  describe("nutzungTitle", () => {
    it("givenNutzung_thenNameIt", () => {
      const title = nutzungTitle(
        adresse({ nutzung: ProjektAdresseRequestDTONutzungEnum.NUTZUNG_B })
      );

      expect(title).toBe("Nutzung B");
    });

    it("givenNoNutzung_thenStayEmptySoTheEntryShowsOneLine", () => {
      expect(nutzungTitle(adresse())).toBe("");
    });
  });
});
