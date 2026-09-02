import { mdiShieldLock } from "@mdi/js";
import { createRouter, createWebHistory } from "vue-router";
import { handleHotUpdate } from "vue-router/auto-routes";

import { hasAnyRole } from "@/composables/useHasAnyRole";
import { STATUS_INDICATORS } from "@/constants.ts";
import { useSnackbarStore } from "@/stores/snackbar.ts";
import { useUserInfoStore } from "@/stores/userinfo.ts";
import AbrechnungAnlegenView from "@/views/AbrechnungAnlegenView.vue";
import AbrechnungenOverviewView from "@/views/AbrechnungenOverviewView.vue";
import GetStartedView from "@/views/GetStartedView.vue";
import HomeView from "@/views/HomeView.vue";
import ProjektAnlegenView from "@/views/ProjektAnlegenView.vue";
import ProjekteOverviewView from "@/views/ProjekteOverviewView.vue";

const routes = [
  {
    path: "/",
    name: "home",
    component: HomeView,
    meta: {},
  },
  {
    path: "/get-started",
    name: "get-started",
    component: GetStartedView,
    meta: {},
  },
  {
    path: "/projekte",
    name: "projekte",
    component: ProjekteOverviewView,
    meta: {},
  },
  {
    path: "/projekte/anlegen",
    name: "projekt-anlegen",
    component: ProjektAnlegenView,
    meta: {},
  },
  {
    path: "/projekte/:projektId/abrechnungen",
    name: "abrechnungen",
    component: AbrechnungenOverviewView,
    props: true,
    meta: {},
  },
  {
    path: "/projekte/:projektId/abrechnungen/anlegen",
    name: "abrechnung-anlegen",
    component: AbrechnungAnlegenView,
    props: true,
    meta: {},
  },
  { path: "/:catchAll(.*)*", redirect: "/" }, // CatchAll route
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return {
      top: 0,
      left: 0,
    };
  },
});

router.beforeEach(async (to, from) => {
  const userInfoStore = useUserInfoStore();
  const snackbarStore = useSnackbarStore();
  if (!userInfoStore.userInfo) {
    await userInfoStore.fetchUserInfo();
  }

  if (
    !to.meta.hasAnyRole ||
    hasAnyRole(to.meta.hasAnyRole, userInfoStore.currentRoles)
  ) {
    return true;
  }

  snackbarStore.push({
    color: STATUS_INDICATORS.ERROR,
    text: "Du hast nicht die erforderlichen Berechtigungen, um diese Seite anzuzeigen.",
    icon: mdiShieldLock,
  });

  // Check if application was already running in browser
  if (from.name) {
    return false;
  }

  return { path: "/" };
});

if (import.meta.hot) {
  handleHotUpdate(router);
}

export default router;
