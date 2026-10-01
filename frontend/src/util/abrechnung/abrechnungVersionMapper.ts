import type { AbrechnungVersionResponseDTO } from "@/api/generated/sonar-backend";
import type { AbrechnungVersion } from "@/types/abrechnung/AbrechnungVersion";

export function toAbrechnungVersionen(
  versionen: AbrechnungVersionResponseDTO[]
): AbrechnungVersion[] {
  const sortiert = versionen
    .map((version) =>
      version.id === undefined
        ? undefined
        : { id: version.id, versionsnummer: version.versionsnummer ?? 1 }
    )
    .filter((version) => version !== undefined)
    .sort((links, rechts) => rechts.versionsnummer - links.versionsnummer);

  return sortiert.map((version, index) => ({
    ...version,
    aktuell: index === 0,
  }));
}
