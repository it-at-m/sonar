import type { Projekt } from "@/types/Projekt";

import { reactive } from "vue";

import {
  createProjektAdresse,
  isProjektAdresseDirty,
} from "@/util/projektAdresseForm";

export function useProjektForm() {
  const projekt = reactive<Projekt>({
    projektnummer: "",
    abrechnungBeginn: "",
    abrechnungEnde: "",
    adressen: [createProjektAdresse()],
  });

  function addAdresse(): void {
    projekt.adressen.push(createProjektAdresse());
  }

  function removeAdresse(index: number): void {
    projekt.adressen.splice(index, 1);
  }

  function isDirty(): boolean {
    if (
      projekt.projektnummer ||
      projekt.abrechnungBeginn ||
      projekt.abrechnungEnde
    ) {
      return true;
    }
    if (projekt.adressen.length > 1) {
      return true;
    }
    return projekt.adressen.some(isProjektAdresseDirty);
  }

  return {
    addAdresse,
    isDirty,
    projekt,
    removeAdresse,
  };
}
