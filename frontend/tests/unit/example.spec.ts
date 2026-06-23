import { createTestingPinia } from "@pinia/testing";
import { shallowMount } from "@vue/test-utils";
import { describe, expect, test, vi } from "vitest";
import { nextTick } from "vue";

import TheSnackbarQueue from "@/components/TheSnackbarQueue.vue";
import { useSnackbarStore } from "@/stores/snackbar";

describe("TheSnackbarQueue.vue", () => {
  test("passes store queue to v-snackbar-queue", async () => {
    // given
    const wrapper = shallowMount(TheSnackbarQueue, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            stubActions: false,
          })
        ],
      },
    });
    const text = "Test_Message";

    // when
    const store = useSnackbarStore();
    store.push({ text });
    await nextTick();

    // then
    const queueComponent = wrapper.findComponent({ name: "v-snackbar-queue" });
    expect(queueComponent.props("modelValue")).toContainEqual(
      expect.objectContaining({ text })
    );
  });
});
