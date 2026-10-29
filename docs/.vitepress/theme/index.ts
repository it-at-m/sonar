import type { Theme } from "vitepress";

import { createMermaidRenderer } from "vitepress-mermaid-renderer";
import DefaultTheme from "vitepress/theme";

import LhmThemeExtension from "./LhmThemeExtension.vue";

import "./style.css";

export default <Theme>{
  extends: DefaultTheme,
  Layout: LhmThemeExtension,
  enhanceApp() {
    createMermaidRenderer();
  },
};
