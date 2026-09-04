import type { ProjektAdresseSuggestion } from "@/types/ProjektAdresseSuggestion";

import { ref } from "vue";

import { ApiFactory } from "@/api/ApiFactory";
import { ProjektControllerApi } from "@/api/generated/sonar-backend";
import { toProjektAdresseSuggestion } from "@/util/projektAdresseMapper";

export function useProjektAdresseSuggestions() {
  const suggestions = ref<ProjektAdresseSuggestion[]>([]);

  async function load(projektId: string): Promise<void> {
    const projekt =
      await ApiFactory.getInstance(ProjektControllerApi).getProjekt(projektId);
    suggestions.value = (projekt.adressen ?? []).map(
      toProjektAdresseSuggestion
    );
  }

  return { load, suggestions };
}
