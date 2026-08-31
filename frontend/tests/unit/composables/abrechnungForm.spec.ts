import { describe, expect, it } from "vitest";
import { nextTick } from "vue";

import { useAbrechnungForm } from "@/composables/abrechnungForm";

describe("abrechnungForm.ts", () => {
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
});
