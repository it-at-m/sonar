import type { ProjektAdresseForm } from "@/types/projekt/ProjektAdresseForm";
import type { ProjektSuggestion } from "@/types/projekt/ProjektSuggestion";

export interface ProjektForm {
  projektnummer: string;
  abrechnungBeginn: string;
  abrechnungEnde: string;
  vorgaengerProjekt: ProjektSuggestion | null;
  adressen: ProjektAdresseForm[];
}
