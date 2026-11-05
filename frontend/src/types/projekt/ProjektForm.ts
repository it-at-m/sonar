import type { ProjektRequestDTOStadtbezirkEnum } from "@/api/generated/sonar-backend";
import type { ProjektAdresseForm } from "@/types/projekt/ProjektAdresseForm";

export interface ProjektForm {
  projektnummer: string;
  stadtbezirk: ProjektRequestDTOStadtbezirkEnum | null;
  abrechnungBeginn: string;
  abrechnungEnde: string;
  adressen: ProjektAdresseForm[];
}
