import type { Adresse } from "@/types/Adresse";
import type { UnerlaubteNutzung } from "@/types/UnerlaubteNutzung";

export interface ProjektAdresseForm extends Adresse, UnerlaubteNutzung {
  id: string;
  anzahlMahnungen: number;
  sondernutzungErlaubt: boolean;
}
