import type { ProjektAdresseForm } from "@/types/ProjektAdresseForm";

export interface Projekt {
  projektnummer: string;
  abrechnungBeginn: string;
  abrechnungEnde: string;
  adressen: ProjektAdresseForm[];
}
