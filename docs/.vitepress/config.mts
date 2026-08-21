import { defineConfig } from "vitepress";
import { withMermaid } from "vitepress-plugin-mermaid";

// https://vitepress.dev/reference/site-config
const vitepressConfig = defineConfig({
  base: "/sonar/", // needs to be changed if delivered via sub path (e.g. "/docs/" for example.com/docs)
  title: "Sonar Docs",
  description: "Documentation for Sonar",
  head: [
    [
      "link",
      {
        rel: "icon",
        href: `https://assets.muenchen.de/logos/lhm/icon-lhm-muenchen-32.png`,
      },
    ],
  ],
  lastUpdated: true,
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: "Home", link: "/" },
      { text: "Designentscheidungen", link: "/architecture/decisions/" },
      { text: "Naming Conventions", link: "/architecture/naming_conventions/" },
    ],
    sidebar: [
      {
        text: "Naming Conventions",
        link: "/architecture/naming_conventions/",
        collapsed: true,
        items: [
          { text: "Tests", link: "/architecture/naming_conventions/tests" },
        ],
      },
      {
        text: "Designentscheidungen",
        link: "/architecture/decisions/",
        collapsed: true,
        items: [
          {
            text: "Verzicht auf i18n",
            link: "/architecture/decisions/0001-kein-i18n",
          },
          {
            text: "OpenAPI für Schnittstellen",
            link: "/architecture/decisions/0002-openapi-fuer-schnittstellen",
          },
          {
            text: "Gendergerechte Sprache",
            link: "/architecture/decisions/0003-gendergerechte-sprache",
          },
          {
            text: "Offset-Paginierung für Listen-Endpunkte",
            link: "/architecture/decisions/0004-offset-paginierung",
          },
        ],
      },
    ],
    socialLinks: [
      { icon: "github", link: "https://github.com/it-at-m/refarch-templates" },
    ],
    editLink: {
      pattern:
        "https://github.com/it-at-m/refarch-templates/blob/main/docs/:path",
      text: "View this page on GitHub",
    },
    footer: {
      message: `<a href="https://opensource.muenchen.de/impress.html">Impress and Contact</a>`,
    },
    outline: {
      level: "deep",
    },
    search: {
      provider: "local",
    },
  },
  markdown: {
    image: {
      lazyLoading: true,
    },
  },
});

export default withMermaid(vitepressConfig);
