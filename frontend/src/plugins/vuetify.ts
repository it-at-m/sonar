import "vuetify/styles";

import { createVuetify } from "vuetify";
import { aliases, mdi } from "vuetify/iconsets/mdi-svg";
import { de } from "vuetify/locale";

import { loadThemeColor } from "@/util/themeColor";

export default createVuetify({
  locale: {
    locale: "de",
    messages: { de },
  },
  icons: {
    defaultSet: "mdi",
    aliases,
    sets: {
      mdi,
    },
  },
  theme: {
    defaultTheme: "light",
    themes: {
      light: {
        colors: {
          primary: loadThemeColor(),
          secondary: "#FFCC00",
          accent: "#7BA4D9",
          success: "#69BE28",
          error: "#FF0000",
        },
      },
    },
  },
});
