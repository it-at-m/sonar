import type { Adresse } from "@/types/common/Adresse";
import type { UnerlaubteNutzung } from "@/types/common/UnerlaubteNutzung";

export interface NutzungsobjektSuggestion extends Adresse, UnerlaubteNutzung {
  id: string;
  bemerkung: string;
  aufschlag50prozent: boolean;
}
