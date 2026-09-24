import type {
  ProjektAdresseRequestDTOArtEnum,
  ProjektAdresseRequestDTONutzungEnum,
} from "@/api/generated/sonar-backend";

// The Nutzungsobjekt DTO declares the same Art and Nutzung values as the Projekt Adresse DTO.
// Naming the Projekt enums here keeps the shared form in step with both requests.
export interface Adresse {
  art: ProjektAdresseRequestDTOArtEnum;
  adresse: string;
  hausnummerVon: string;
  hausnummerBis: string;
  flurstueck: string;
  gemarkung: string;
  nutzung: ProjektAdresseRequestDTONutzungEnum | null;
}
