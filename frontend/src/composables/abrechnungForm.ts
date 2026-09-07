import { ref, watch } from "vue";

export interface AbrechnungForm {
  geschaeftspartnerId: string;
  zustellungsbevollmaechtigterGenutzt: boolean;
  zustellungsbevollmaechtigterId: string;
  zustellungsbevollmaechtigterTyp: string | null;
}

export function useAbrechnungForm() {
  const abrechnung = ref<AbrechnungForm>({
    geschaeftspartnerId: "",
    zustellungsbevollmaechtigterGenutzt: false,
    zustellungsbevollmaechtigterId: "",
    zustellungsbevollmaechtigterTyp: null,
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

  return { abrechnung };
}
