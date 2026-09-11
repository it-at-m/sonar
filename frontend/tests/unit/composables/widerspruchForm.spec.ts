import { describe, expect, it } from "vitest";

import { useWiderspruchForm } from "@/composables/widerspruchForm";

describe("widerspruchForm.ts", () => {
  describe("widerspruch", () => {
    it("givenNewForm_thenEveryFieldIsEmptyAndBothFlagsAreNein", () => {
      const { widerspruch } = useWiderspruchForm();

      expect(widerspruch.value.datumEingang).toBe("");
      expect(widerspruch.value.datumRuecknahme).toBe("");
      expect(widerspruch.value.datumVorlageRegierung).toBe("");
      expect(widerspruch.value.datumAblehnungRegierung).toBe("");
      expect(widerspruch.value.entscheidungDurchfuehrung).toBe("");
      expect(widerspruch.value.sollAbgesetzt).toBe(false);
      expect(widerspruch.value.neueTeilabrechnungAnlegen).toBe(false);
      expect(widerspruch.value.bemerkung).toBe("");
    });
  });

  describe("isDirty", () => {
    it("givenNewForm_thenReturnFalse", () => {
      const { isDirty } = useWiderspruchForm();

      expect(isDirty()).toBe(false);
    });

    it("givenDatumEingang_thenReturnTrue", () => {
      const { isDirty, widerspruch } = useWiderspruchForm();

      widerspruch.value.datumEingang = "2026-04-01";

      expect(isDirty()).toBe(true);
    });

    it("givenDatumRuecknahme_thenReturnTrue", () => {
      const { isDirty, widerspruch } = useWiderspruchForm();

      widerspruch.value.datumRuecknahme = "2026-04-15";

      expect(isDirty()).toBe(true);
    });

    it("givenDatumVorlageRegierung_thenReturnTrue", () => {
      const { isDirty, widerspruch } = useWiderspruchForm();

      widerspruch.value.datumVorlageRegierung = "2026-05-01";

      expect(isDirty()).toBe(true);
    });

    it("givenDatumAblehnungRegierung_thenReturnTrue", () => {
      const { isDirty, widerspruch } = useWiderspruchForm();

      widerspruch.value.datumAblehnungRegierung = "2026-06-01";

      expect(isDirty()).toBe(true);
    });

    it("givenEntscheidungDurchfuehrung_thenReturnTrue", () => {
      const { isDirty, widerspruch } = useWiderspruchForm();

      widerspruch.value.entscheidungDurchfuehrung = "wird durchgeführt";

      expect(isDirty()).toBe(true);
    });

    it("givenSollAbgesetztSwitchedToJa_thenReturnTrue", () => {
      const { isDirty, widerspruch } = useWiderspruchForm();

      widerspruch.value.sollAbgesetzt = true;

      expect(isDirty()).toBe(true);
    });

    it("givenNeueTeilabrechnungAnlegenSwitchedToJa_thenReturnTrue", () => {
      const { isDirty, widerspruch } = useWiderspruchForm();

      widerspruch.value.neueTeilabrechnungAnlegen = true;

      expect(isDirty()).toBe(true);
    });

    it("givenBemerkung_thenReturnTrue", () => {
      const { isDirty, widerspruch } = useWiderspruchForm();

      widerspruch.value.bemerkung = "Bemerkung";

      expect(isDirty()).toBe(true);
    });
  });
});
