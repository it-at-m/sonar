import type {
  GetProjekteByPageAndSizeSortByEnum,
  GetProjekteByPageAndSizeSortDirectionEnum,
} from "@/api/generated/sonar-backend";

export interface ProjektSort {
  sortBy?: GetProjekteByPageAndSizeSortByEnum;
  sortDirection?: GetProjekteByPageAndSizeSortDirectionEnum;
}
