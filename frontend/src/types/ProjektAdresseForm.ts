export interface ProjektAdresseForm {
  id: string;
  bezeichnung: string;
  baunutzung: string;
  unerlaubteNutzungVon: string;
  unerlaubteNutzungBis: string;
  tageUnerlaubteNutzung: number | null;
  anzahlMahnungen: number;
  sondernutzungErlaubt: boolean;
}
