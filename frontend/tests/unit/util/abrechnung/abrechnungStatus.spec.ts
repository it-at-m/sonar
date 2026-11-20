import { describe, expect, it } from "vitest";

import { abrechnungStatusLabel } from "@/util/abrechnung/abrechnungStatus";

describe("abrechnungStatus.ts", () => {
  describe("abrechnungStatusLabel", () => {
    it("givenEntwurf_thenCallItEntwurf", () => {
      expect(abrechnungStatusLabel(true)).toBe("Entwurf");
    });

    it("givenFinalizedAbrechnung_thenCallItAbgeschlossen", () => {
      expect(abrechnungStatusLabel(false)).toBe("Abgeschlossen");
    });
  });
});
