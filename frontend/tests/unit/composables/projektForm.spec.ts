import { describe, expect, it } from "vitest";

import { useProjektForm } from "@/composables/projektForm";

/**
 * `noUncheckedIndexedAccess` makes every index access possibly undefined. Failing here keeps the
 * non-null assertion out of each single test.
 */
function itemAt<T>(items: readonly T[], index = 0): T {
  const item = items[index];
  if (item === undefined) {
    throw new Error(`Kein Element an Position ${index}.`);
  }
  return item;
}

describe("projektForm.ts", () => {
  describe("isDirty", () => {
    it("givenUntouchedForm_thenReturnFalse", () => {
      const { isDirty } = useProjektForm();

      expect(isDirty()).toBe(false);
    });

    it("givenFilledProjektnummer_thenReturnTrue", () => {
      const { isDirty, projekt } = useProjektForm();
      projekt.projektnummer = "2026-0001";

      expect(isDirty()).toBe(true);
    });

    it("givenAddedAdresse_thenReturnTrue", () => {
      const { addAdresse, isDirty } = useProjektForm();
      addAdresse();

      expect(isDirty()).toBe(true);
    });

    it("givenFilledAdresse_thenReturnTrue", () => {
      const { isDirty, projekt } = useProjektForm();
      itemAt(projekt.adressen).adresse = "Marienplatz";

      expect(isDirty()).toBe(true);
    });
  });

  describe("removeAdresse", () => {
    it("givenIndex_thenRemoveOnlyThatAdresse", () => {
      const { addAdresse, projekt, removeAdresse } = useProjektForm();
      itemAt(projekt.adressen).adresse = "erste";
      addAdresse();
      itemAt(projekt.adressen, 1).adresse = "zweite";

      removeAdresse(0);

      expect(projekt.adressen).toHaveLength(1);
      expect(itemAt(projekt.adressen).adresse).toBe("zweite");
    });
  });
});
