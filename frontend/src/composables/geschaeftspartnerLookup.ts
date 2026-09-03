import type { GeschaeftspartnerResponseDTO } from "@/api/generated/sonar-backend";

import { ref } from "vue";

import { ApiFactory } from "@/api/ApiFactory";
import {
  GeschaeftspartnerControllerApi,
  ResponseError,
} from "@/api/generated/sonar-backend";

export function useGeschaeftspartnerLookup() {
  const data = ref<GeschaeftspartnerResponseDTO | null>(null);
  const loading = ref(false);
  const errorMessage = ref("");
  let latestSequence = 0;

  async function lookup(geschaeftspartnerId: string): Promise<void> {
    const sequence = ++latestSequence;
    data.value = null;
    errorMessage.value = "";
    if (!geschaeftspartnerId.trim()) {
      loading.value = false;
      return;
    }
    loading.value = true;
    try {
      const response = await ApiFactory.getInstance(
        GeschaeftspartnerControllerApi
      ).getGeschaeftspartner(geschaeftspartnerId.trim());
      if (sequence === latestSequence) {
        data.value = response;
      }
    } catch (error) {
      if (sequence === latestSequence) {
        errorMessage.value =
          error instanceof ResponseError && error.response.status === 404
            ? "Zu dieser ID wurden keine Geschäftspartnerdaten gefunden."
            : "Die Geschäftspartnerdaten konnten nicht abgerufen werden.";
      }
    } finally {
      if (sequence === latestSequence) {
        loading.value = false;
      }
    }
  }

  return {
    data,
    errorMessage,
    loading,
    lookup,
  };
}
