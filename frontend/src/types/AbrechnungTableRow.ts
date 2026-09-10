export interface AbrechnungTableRow {
  id?: string;
  geschaeftspartnerId: string;
  zeitraumVon: string;
  zeitraumBis: string;
  abrechnungsArt: string;
  anzahlNutzungsobjekte: number;
  widerspruchVorhanden: boolean;
}
