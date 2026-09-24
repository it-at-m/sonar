import type { GeschaeftspartnerResponseDTO } from "@/api/generated/sonar-backend";

import { ApiFactory } from "@/api/ApiFactory";
import {
  GeschaeftspartnerControllerApi,
  ResponseError,
} from "@/api/generated/sonar-backend";

export interface GeschaeftspartnerLookupResult {
  data: GeschaeftspartnerResponseDTO | null;
  errorMessage: string;
}

export async function lookupGeschaeftspartner(
  geschaeftspartnerId: string
): Promise<GeschaeftspartnerLookupResult> {
  try {
    const data = await ApiFactory.getInstance(
      GeschaeftspartnerControllerApi
    ).getGeschaeftspartner(geschaeftspartnerId);
    return { data, errorMessage: "" };
  } catch (error) {
    return {
      data: null,
      errorMessage:
        error instanceof ResponseError && error.response.status === 404
          ? "Zu dieser ID wurden keine Geschäftspartnerdaten gefunden."
          : "Die Geschäftspartnerdaten konnten nicht abgerufen werden.",
    };
  }
}
