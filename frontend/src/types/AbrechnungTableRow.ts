export interface AbrechnungTableRow {
  id?: string;
  versionsnummer: number;
  geschaeftspartnerId: string;
  zeitraumVon: string;
  zeitraumBis: string;
  abrechnungsArt: string;
  anzahlNutzungsobjekte: number;
  widerspruchVorhanden: boolean;
}
