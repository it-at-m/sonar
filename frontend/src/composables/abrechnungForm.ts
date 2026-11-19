import type { AbrechnungForm } from "@/types/abrechnung/AbrechnungForm";

import { ref, watch } from "vue";

import {
  createAbrechnungNutzungsobjekt,
  isAbrechnungNutzungsobjektDirty,
} from "@/util/abrechnung/abrechnungNutzungsobjektForm";

export function useAbrechnungForm() {
  const abrechnung = ref<AbrechnungForm>({
    geschaeftspartnerId: "",
    zustellungsbevollmaechtigterGenutzt: false,
    zustellungsbevollmaechtigterId: "",
    zustellungsbevollmaechtigterTyp: null,
    zeitraumVon: "",
    zeitraumBis: "",
    abrechnungsArt: null,
    nutzungsobjekte: [createAbrechnungNutzungsobjekt()],
  });

  const unchangedState = ref<string | null>(null);

  watch(
    () => abrechnung.value.zustellungsbevollmaechtigterGenutzt,
    (genutzt) => {
      if (!genutzt) {
        abrechnung.value.zustellungsbevollmaechtigterId = "";
        abrechnung.value.zustellungsbevollmaechtigterTyp = null;
      }
    }
  );

  function uebernehmen(form: AbrechnungForm): void {
    abrechnung.value = form;
    unchangedState.value = JSON.stringify(form);
  }

  function markSaved(): void {
    unchangedState.value = JSON.stringify(abrechnung.value);
  }

  /**
   * Covers both tabs, because both fill the same Abrechnung and the guard has to fire wherever the
   * entry happened. A single Nutzungsobjekt is there from the start, so only a second one counts.
   * Taken over or saved data fills the form without anyone entering it, so there a filled field
   * says nothing. Only a difference to what was taken over or saved counts as a change.
   */
  function isDirty(): boolean {
    if (unchangedState.value !== null) {
      return JSON.stringify(abrechnung.value) !== unchangedState.value;
    }
    const form = abrechnung.value;
    if (
      form.geschaeftspartnerId ||
      form.zustellungsbevollmaechtigterGenutzt ||
      form.zustellungsbevollmaechtigterId ||
      form.zustellungsbevollmaechtigterTyp !== null ||
      form.zeitraumVon ||
      form.zeitraumBis ||
      form.abrechnungsArt !== null
    ) {
      return true;
    }
    if (form.nutzungsobjekte.length > 1) {
      return true;
    }
    return form.nutzungsobjekte.some(isAbrechnungNutzungsobjektDirty);
  }

  return { abrechnung, isDirty, markSaved, uebernehmen };
}
