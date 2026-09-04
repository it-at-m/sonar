import type {
  AbrechnungRequestDTOAbrechnungsArtEnum,
  AbrechnungRequestDTOZustellungsbevollmaechtigterTypEnum,
} from "@/api/generated/sonar-backend";
import type { AbrechnungNutzungsobjektForm } from "@/types/AbrechnungNutzungsobjektForm";

import { ref, watch } from "vue";

import {
  createAbrechnungNutzungsobjekt,
  isAbrechnungNutzungsobjektDirty,
} from "@/util/abrechnungNutzungsobjektForm";

export interface AbrechnungForm {
  geschaeftspartnerId: string;
  zustellungsbevollmaechtigterGenutzt: boolean;
  zustellungsbevollmaechtigterId: string;
  zustellungsbevollmaechtigterTyp: AbrechnungRequestDTOZustellungsbevollmaechtigterTypEnum | null;
  zeitraumVon: string;
  zeitraumBis: string;
  abrechnungsArt: AbrechnungRequestDTOAbrechnungsArtEnum | null;
  nutzungsobjekte: AbrechnungNutzungsobjektForm[];
}

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

  watch(
    () => abrechnung.value.zustellungsbevollmaechtigterGenutzt,
    (genutzt) => {
      if (!genutzt) {
        abrechnung.value.zustellungsbevollmaechtigterId = "";
        abrechnung.value.zustellungsbevollmaechtigterTyp = null;
      }
    }
  );

  /**
   * Covers both tabs, because both fill the same Abrechnung and the guard has to fire wherever the
   * entry happened. A single Nutzungsobjekt is there from the start, so only a second one counts.
   */
  function isDirty(): boolean {
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

  return { abrechnung, isDirty };
}
