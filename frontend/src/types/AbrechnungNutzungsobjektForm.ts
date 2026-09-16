import type { AbrechnungPositionForm } from "@/types/AbrechnungPositionForm";
import type { Adresse } from "@/types/Adresse";
import type { UnerlaubteNutzung } from "@/types/UnerlaubteNutzung";

export interface AbrechnungNutzungsobjektForm
  extends Adresse, UnerlaubteNutzung {
  id: string;
  bemerkung: string;
  positionen: AbrechnungPositionForm[];
}
