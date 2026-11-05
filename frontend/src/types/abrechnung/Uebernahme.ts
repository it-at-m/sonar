export type Uebernahme =
  | { quelle: "PROJEKT" }
  | { quelle: "NUTZUNGSOBJEKT"; nutzungsobjektId: string };
