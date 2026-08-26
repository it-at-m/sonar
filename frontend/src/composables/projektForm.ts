import type { ProjektRequestDTO } from "@/api/generated/sonar-backend";
import type { ProjektAdresseForm } from "@/types/ProjektAdresseForm";

import { ref } from "vue";

import {
  createProjektAdresse,
  isProjektAdresseDirty,
  toProjektAdresseRequestDTO,
} from "@/util/projektAdresseForm";
import { endeNotBeforeBeginn } from "@/util/validationRules";

export function useProjektForm() {
  const projektnummer = ref("");
  const abrechnungBeginn = ref("");
  const abrechnungEnde = ref("");
  const adressen = ref<ProjektAdresseForm[]>([createProjektAdresse()]);

  function addAdresse(): void {
    adressen.value.push(createProjektAdresse());
  }

  function removeAdresse(index: number): void {
    adressen.value.splice(index, 1);
  }

  function abrechnungEndeRule(value: string): boolean | string {
    return endeNotBeforeBeginn(abrechnungBeginn.value, value);
  }

  function isDirty(): boolean {
    if (projektnummer.value || abrechnungBeginn.value || abrechnungEnde.value) {
      return true;
    }
    if (adressen.value.length > 1) {
      return true;
    }
    return adressen.value.some(isProjektAdresseDirty);
  }

  function toRequestDTO(): ProjektRequestDTO {
    return {
      projektnummer: projektnummer.value.trim(),
      abrechnungBeginn: new Date(abrechnungBeginn.value),
      abrechnungEnde: new Date(abrechnungEnde.value),
      adressen: adressen.value.map(toProjektAdresseRequestDTO),
    };
  }

  return {
    abrechnungBeginn,
    abrechnungEnde,
    abrechnungEndeRule,
    addAdresse,
    adressen,
    isDirty,
    projektnummer,
    removeAdresse,
    toRequestDTO,
  };
}
