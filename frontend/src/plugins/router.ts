import { createRouter, createWebHashHistory } from "vue-router";
import { hasAnyRole } from "@/composables/useHasAnyRole";
import { useUserInfoStore } from "@/stores/userinfo.ts";
import { useSnackbarStore } from "@/stores/snackbar.ts";
import { STATUS_INDICATORS } from "@/constants.ts";
import { mdiShieldLock } from "@mdi/js";
import HomeView from "@/views/HomeView.vue";
import GetStartedView from "@/views/GetStartedView.vue";
import ProjekteOverviewView from "@/views/ProjekteOverviewView.vue";
import ProjektAnlegenView from "@/views/ProjektAnlegenView.vue";




const routes = [
  {
    path: "/",
    name: "home",
    component: HomeView,
    meta: {},
  }
  , {
    path: "/getStarted",
    name: "getStarted",
    component: GetStartedView,
    meta: {},
  }, {
  path: "/projekte",
    name: "projekte",
    component: ProjekteOverviewView,
    meta: {},
  },
  {  path: "/projekte/anlegen",
    name: "projektAnlegen",
    component: ProjektAnlegenView,
    meta: {},
  }
]

const router = createRouter({
  history: createWebHashHistory(),
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

export default router;