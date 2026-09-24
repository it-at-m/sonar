import type {
  GetAbrechnungenByPageAndSizeSortByEnum,
  GetAbrechnungenByPageAndSizeSortDirectionEnum,
} from "@/api/generated/sonar-backend";

export interface AbrechnungSort {
  sortBy?: GetAbrechnungenByPageAndSizeSortByEnum[];
  sortDirection?: GetAbrechnungenByPageAndSizeSortDirectionEnum[];
}
