import type { Adresse } from "@/types/common/Adresse";
import type { UnerlaubteNutzung } from "@/types/common/UnerlaubteNutzung";

export interface ProjektAdresseForm extends Adresse, UnerlaubteNutzung {
  id: string;
  anzahlMahnungen: number;
  sondernutzungErlaubt: boolean;
}
