export const TABS = {
  BASIS: "basis",
  BERECHNUNG: "berechnung",
} as const;

export type Tab = (typeof TABS)[keyof typeof TABS];

const NUTZUNGSOBJEKT_PREFIX = `${TABS.BERECHNUNG}-nutzungsobjekt`;

export function tabOfError(id: string): Tab | undefined {
  if (id.startsWith(`${TABS.BASIS}-`)) {
    return TABS.BASIS;
  }
  if (id.startsWith(`${TABS.BERECHNUNG}-`)) {
    return TABS.BERECHNUNG;
  }
  return undefined;
}

export function nutzungsobjektIdPrefix(index: number): string {
  return `${NUTZUNGSOBJEKT_PREFIX}-${index}`;
}

export function nutzungsobjektOfError(id: string): number | undefined {
  const index = new RegExp(`^${NUTZUNGSOBJEKT_PREFIX}-(\\d+)-`).exec(id)?.[1];
  return index === undefined ? undefined : Number(index);
}
