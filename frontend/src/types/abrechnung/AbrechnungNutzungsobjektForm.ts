import type { AbrechnungPositionForm } from "@/types/abrechnung/AbrechnungPositionForm";
import type { Adresse } from "@/types/common/Adresse";
import type { UnerlaubteNutzung } from "@/types/common/UnerlaubteNutzung";

export interface AbrechnungNutzungsobjektForm
  extends Adresse, UnerlaubteNutzung {
  id: string;
  bemerkung: string;
  positionen: AbrechnungPositionForm[];
}
