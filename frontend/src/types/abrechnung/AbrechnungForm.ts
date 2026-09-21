import type {
  AbrechnungRequestDTOAbrechnungsArtEnum,
  AbrechnungRequestDTOZustellungsbevollmaechtigterTypEnum,
} from "@/api/generated/sonar-backend";
import type { AbrechnungNutzungsobjektForm } from "@/types/abrechnung/AbrechnungNutzungsobjektForm";

export interface AbrechnungForm {
  geschaeftspartnerId: string;
  isZustellungsbevollmaechtigterGenutzt: boolean;
  zustellungsbevollmaechtigterId: string;
  zustellungsbevollmaechtigterTyp: AbrechnungRequestDTOZustellungsbevollmaechtigterTypEnum | null;
  zeitraumVon: string;
  zeitraumBis: string;
  abrechnungsArt: AbrechnungRequestDTOAbrechnungsArtEnum | null;
  nutzungsobjekte: AbrechnungNutzungsobjektForm[];
}
