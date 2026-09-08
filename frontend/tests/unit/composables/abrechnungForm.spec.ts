import { describe, expect, it } from "vitest";
import { nextTick } from "vue";

import { AbrechnungRequestDTOAbrechnungsArtEnum } from "@/api/generated/sonar-backend";
import { useAbrechnungForm } from "@/composables/abrechnungForm";
import { createAbrechnungNutzungsobjekt } from "@/util/abrechnungNutzungsobjektForm";

function itemAt<T>(items: readonly T[], index = 0): T {
  const item = items[index];
  if (item === undefined) {
    throw new Error(`Kein Element an Position ${index}.`);
  }
  return item;
}

describe("abrechnungForm.ts", () => {
  describe("nutzungsobjekte", () => {
    it("givenNewForm_thenStartWithOneNutzungsobjekt", () => {
      const { abrechnung } = useAbrechnungForm();

      expect(abrechnung.value.nutzungsobjekte).toHaveLength(1);
    });
  });

  describe("zustellungsbevollmaechtigterGenutzt", () => {
    it("givenToggleSwitchedOff_thenClearTheDetails", async () => {
      const { abrechnung } = useAbrechnungForm();
      abrechnung.value.zustellungsbevollmaechtigterGenutzt = true;
      await nextTick();
      abrechnung.value.zustellungsbevollmaechtigterId = "ZB-1";
      abrechnung.value.zustellungsbevollmaechtigterTyp =
        "GESETZLICHER_VERTRETER";

      abrechnung.value.zustellungsbevollmaechtigterGenutzt = false;
      await nextTick();

      expect(abrechnung.value.zustellungsbevollmaechtigterId).toBe("");
      expect(abrechnung.value.zustellungsbevollmaechtigterTyp).toBeNull();
    });

    it("givenToggleSwitchedOn_thenKeepTheDetails", async () => {
      const { abrechnung } = useAbrechnungForm();
      abrechnung.value.zustellungsbevollmaechtigterId = "ZB-1";

      abrechnung.value.zustellungsbevollmaechtigterGenutzt = true;
      await nextTick();

      expect(abrechnung.value.zustellungsbevollmaechtigterId).toBe("ZB-1");
    });
  });

  describe("isDirty", () => {
    it("givenUntouchedForm_thenReturnFalse", () => {
      const { isDirty } = useAbrechnungForm();

      expect(isDirty()).toBe(false);
    });

    it("givenFilledGeschaeftspartnerId_thenReturnTrue", () => {
      const { abrechnung, isDirty } = useAbrechnungForm();
      abrechnung.value.geschaeftspartnerId = "1000000001";

      expect(isDirty()).toBe(true);
    });

    it("givenSwitchedZustellungsbevollmaechtigter_thenReturnTrue", () => {
      const { abrechnung, isDirty } = useAbrechnungForm();
      abrechnung.value.zustellungsbevollmaechtigterGenutzt = true;

      expect(isDirty()).toBe(true);
    });

    it("givenFilledZeitraum_thenReturnTrue", () => {
      const { abrechnung, isDirty } = useAbrechnungForm();
      abrechnung.value.zeitraumVon = "2026-01-01";

      expect(isDirty()).toBe(true);
    });

    it("givenChosenAbrechnungsArt_thenReturnTrue", () => {
      const { abrechnung, isDirty } = useAbrechnungForm();
      abrechnung.value.abrechnungsArt =
        AbrechnungRequestDTOAbrechnungsArtEnum.ENDABRECHNUNG;

      expect(isDirty()).toBe(true);
    });

    it("givenFilledNutzungsobjekt_thenReturnTrue", () => {
      const { abrechnung, isDirty } = useAbrechnungForm();
      itemAt(abrechnung.value.nutzungsobjekte).adresse = "Marienplatz";

      expect(isDirty()).toBe(true);
    });

    it("givenFilledPosition_thenReturnTrue", () => {
      const { abrechnung, isDirty } = useAbrechnungForm();
      itemAt(itemAt(abrechnung.value.nutzungsobjekte).positionen).laenge = 12;

      expect(isDirty()).toBe(true);
    });

    it("givenSecondNutzungsobjekt_thenReturnTrue", () => {
      const { abrechnung, isDirty } = useAbrechnungForm();
      abrechnung.value.nutzungsobjekte.push(createAbrechnungNutzungsobjekt());

      expect(isDirty()).toBe(true);
    });
  });
});
