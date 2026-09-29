import type { ProjektAdresseForm } from "@/types/projekt/ProjektAdresseForm";

export interface ProjektForm {
  projektnummer: string;
  abrechnungBeginn: string;
  abrechnungEnde: string;
  adressen: ProjektAdresseForm[];
}
