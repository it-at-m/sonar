export const DEFAULT_THEME_COLOR = "#333333";
export const THEME_COLOR_STORAGE_KEY = "sonar.theme.primary";

function normalizeThemeColor(value: unknown): string | null {
  return typeof value === "string" && /^#[\da-f]{6}$/i.test(value)
    ? value.toUpperCase()
    : null;
}

export function loadThemeColor(): string {
  try {
    return (
      normalizeThemeColor(localStorage.getItem(THEME_COLOR_STORAGE_KEY)) ??
      DEFAULT_THEME_COLOR
    );
  } catch {
    return DEFAULT_THEME_COLOR;
  }
}

export function saveThemeColor(value: unknown): string | null {
  const color = normalizeThemeColor(value);
  if (color === null) return null;

  try {
    localStorage.setItem(THEME_COLOR_STORAGE_KEY, color);
  } catch {
    // Keep live theme changes available when browser storage is blocked.
  }
  return color;
}
