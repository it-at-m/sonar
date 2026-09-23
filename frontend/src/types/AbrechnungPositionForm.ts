export interface AbrechnungPositionForm {
  id: string;
  beginn: string;
  ende: string;
  laenge: number | null;
  breite: number | null;
  flaeche: number | null;
  aufschlag50prozent: boolean;
  anteilAnFlaeche: number | null;
}
