import type { GeschaeftspartnerResponseDTO } from "@/api/generated/sonar-backend";

import { ref } from "vue";

import { ApiFactory } from "@/api/ApiFactory";
import {
  GeschaeftspartnerControllerApi,
  ResponseError,
} from "@/api/generated/sonar-backend";

export function useGeschaeftspartnerLookup() {
  const daten = ref<GeschaeftspartnerResponseDTO | null>(null);
  const loading = ref(false);
  const fehlermeldung = ref("");

  async function lookup(geschaeftspartnerId: string): Promise<void> {
    daten.value = null;
    fehlermeldung.value = "";
    if (!geschaeftspartnerId.trim()) {
      return;
    }
    loading.value = true;
    try {
      daten.value = await ApiFactory.getInstance(
        GeschaeftspartnerControllerApi
      ).getGeschaeftspartner(geschaeftspartnerId.trim());
    } catch (error) {
      fehlermeldung.value =
        error instanceof ResponseError && error.response.status === 404
          ? "Zu dieser ID wurden keine Geschäftspartnerdaten gefunden."
          : "Die Geschäftspartnerdaten konnten nicht abgerufen werden.";
    } finally {
      loading.value = false;
    }
  }

  return {
    daten,
    fehlermeldung,
    loading,
    lookup,
  };
}
