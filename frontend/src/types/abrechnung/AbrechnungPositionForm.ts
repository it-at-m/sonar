export interface AbrechnungPositionForm {
  id: string;
  beginn: string;
  ende: string;
  laenge: number | null;
  breite: number | null;
  flaeche: number | null;
  isHaelfte: boolean;
  anteilAnFlaeche: number | null;
}
