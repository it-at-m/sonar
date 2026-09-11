import { ref } from "vue";

export interface WiderspruchForm {
  datumEingang: string;
  datumRuecknahme: string;
  datumVorlageRegierung: string;
  datumAblehnungRegierung: string;
  entscheidungDurchfuehrung: string;
  sollAbgesetzt: boolean;
  neueTeilabrechnungAnlegen: boolean;
  bemerkung: string;
}

export function useWiderspruchForm() {
  const widerspruch = ref<WiderspruchForm>({
    datumEingang: "",
    datumRuecknahme: "",
    datumVorlageRegierung: "",
    datumAblehnungRegierung: "",
    entscheidungDurchfuehrung: "",
    sollAbgesetzt: false,
    neueTeilabrechnungAnlegen: false,
    bemerkung: "",
  });

  function isDirty(): boolean {
    const form = widerspruch.value;
    return Boolean(
      form.datumEingang ||
      form.datumRuecknahme ||
      form.datumVorlageRegierung ||
      form.datumAblehnungRegierung ||
      form.entscheidungDurchfuehrung ||
      form.sollAbgesetzt ||
      form.neueTeilabrechnungAnlegen ||
      form.bemerkung
    );
  }

  return { isDirty, widerspruch };
}
