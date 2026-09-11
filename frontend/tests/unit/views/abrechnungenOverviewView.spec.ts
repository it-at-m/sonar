import { createTestingPinia } from "@pinia/testing";
import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import AbrechnungTable from "@/components/AbrechnungTable.vue";
import vuetify from "@/plugins/vuetify";
import { useSnackbarStore } from "@/stores/snackbar";
import AbrechnungenOverviewView from "@/views/AbrechnungenOverviewView.vue";

const PROJEKT_ID = "123e4567-e89b-12d3-a456-426614174000";

function stubFetch(): ReturnType<typeof vi.fn> {
  const fetchSpy = vi.fn().mockImplementation(
    () =>
      new Response(
        JSON.stringify({ content: [], page: { totalElements: 0 } }),
        {
          status: 200,
          headers: { "Content-Type": "application/json" },
        }
      )
  );
  vi.stubGlobal("fetch", fetchSpy);
  return fetchSpy;
}

function lastRequestedUrl(fetchSpy: ReturnType<typeof vi.fn>): string {
  return fetchSpy.mock.calls.at(-1)?.[0] as string;
}

function stubFailingFetch(): ReturnType<typeof vi.fn> {
  const fetchSpy = vi.fn().mockRejectedValue(new Error("offline"));
  vi.stubGlobal("fetch", fetchSpy);
  return fetchSpy;
}

async function mountView(fetchSpy = stubFetch()) {
  const wrapper = mount(AbrechnungenOverviewView, {
    props: { projektId: PROJEKT_ID },
    global: {
      plugins: [
        vuetify,
        createTestingPinia({ createSpy: vi.fn, stubActions: false }),
      ],
    },
  });
  await flushPromises();
  return { fetchSpy, wrapper };
}

async function clickColumn(
  wrapper: Awaited<ReturnType<typeof mountView>>["wrapper"],
  title: string
): Promise<void> {
  const header = wrapper
    .findAll("th")
    .find((candidate) => candidate.text().startsWith(title));
  await header?.trigger("click");
  await flushPromises();
}

describe("AbrechnungenOverviewView.vue", () => {
  beforeEach(() => {
    vi.stubGlobal(
      "ResizeObserver",
      class {
        observe = vi.fn();
        unobserve = vi.fn();
        disconnect = vi.fn();
      }
    );
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("givenMount_thenRequestTheFirstPageWithTheDefaultSort", async () => {
    const { fetchSpy } = await mountView();

    expect(lastRequestedUrl(fetchSpy)).toContain("pageNumber=0");
    expect(lastRequestedUrl(fetchSpy)).toContain("sortBy=ZEITRAUM_VON");
    expect(lastRequestedUrl(fetchSpy)).toContain("sortDirection=DESC");
  });

  it("givenSecondColumnClicked_thenRequestBothColumnsInOrder", async () => {
    const { fetchSpy, wrapper } = await mountView();

    await clickColumn(wrapper, "Art");

    expect(lastRequestedUrl(fetchSpy)).toContain(
      "sortBy=ZEITRAUM_VON%2CABRECHNUNGS_ART"
    );
    expect(lastRequestedUrl(fetchSpy)).toContain("sortDirection=DESC%2CASC");
  });

  it("givenThirdColumnClicked_thenRequestAllThreeColumns", async () => {
    const { fetchSpy, wrapper } = await mountView();

    await clickColumn(wrapper, "Art");
    await clickColumn(wrapper, "Geschäftspartner:in");

    expect(lastRequestedUrl(fetchSpy)).toContain(
      "sortBy=ZEITRAUM_VON%2CABRECHNUNGS_ART%2CGESCHAEFTSPARTNER_ID"
    );
    expect(lastRequestedUrl(fetchSpy)).toContain(
      "sortDirection=DESC%2CASC%2CASC"
    );
  });

  it("givenSecondColumnClickedTwice_thenRequestItsFlippedDirection", async () => {
    const { fetchSpy, wrapper } = await mountView();

    await clickColumn(wrapper, "Art");
    await clickColumn(wrapper, "Art");

    expect(lastRequestedUrl(fetchSpy)).toContain(
      "sortBy=ZEITRAUM_VON%2CABRECHNUNGS_ART"
    );
    expect(lastRequestedUrl(fetchSpy)).toContain("sortDirection=DESC%2CDESC");
  });

  it("givenUnsortableColumnClicked_thenSendNoRequest", async () => {
    const { fetchSpy, wrapper } = await mountView();
    const callsAfterMount = fetchSpy.mock.calls.length;

    await clickColumn(wrapper, "Nutzungsobjekte");

    expect(fetchSpy.mock.calls.length).toBe(callsAfterMount);
  });

  it("givenLoadedPage_thenShowItsRowsAndTotal", async () => {
    const fetchSpy = vi.fn().mockImplementation(
      () =>
        new Response(
          JSON.stringify({
            content: [
              {
                id: "123e4567-e89b-12d3-a456-426614174001",
                geschaeftspartnerId: "1000000001",
                zeitraumVon: "2026-01-01",
                zeitraumBis: "2026-03-31",
                abrechnungsArt: "ENDABRECHNUNG",
                nutzungsobjekte: [{ adresse: "A" }],
              },
            ],
            page: { size: 10, number: 0, totalElements: 42, totalPages: 5 },
          }),
          { status: 200, headers: { "Content-Type": "application/json" } }
        )
    );
    vi.stubGlobal("fetch", fetchSpy);

    const { wrapper } = await mountView(fetchSpy);
    const table = wrapper.findComponent(AbrechnungTable);

    expect(table.props("rows")).toHaveLength(1);
    expect(table.props("totalAbrechnungen")).toBe(42);
    expect(table.props("loading")).toBe(false);
  });

  it("givenFailingRequest_thenReportItAndStopTheLoadingIndicator", async () => {
    const { wrapper } = await mountView(stubFailingFetch());

    expect(wrapper.findComponent(AbrechnungTable).props("loading")).toBe(false);
    expect(useSnackbarStore().queue).toHaveLength(1);
  });
});
