export interface AbrechnungPositionForm {
  id: string;
  beginn: string;
  ende: string;
  laenge: number | null;
  breite: number | null;
  flaeche: number | null;
  haelfte: boolean;
  anteilAnFlaeche: number | null;
}
