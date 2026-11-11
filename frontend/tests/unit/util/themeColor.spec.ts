import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import {
  DEFAULT_THEME_COLOR,
  loadThemeColor,
  saveThemeColor,
  THEME_COLOR_STORAGE_KEY,
} from "@/util/themeColor";

describe("themeColor.ts", () => {
  const storage = { getItem: vi.fn(), setItem: vi.fn() };
  const validColors = [
    ["#a1b2c3", "#A1B2C3"],
    ["#AbCdEf", "#ABCDEF"],
  ];
  const invalidColors = [
    "",
    "#abc",
    "abcdef",
    "#abcdef00",
    "#gggggg",
    " #abcdef ",
    "red",
  ];

  beforeEach(() => {
    storage.getItem.mockReset().mockReturnValue(null);
    storage.setItem.mockReset();
    vi.stubGlobal("localStorage", storage);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  describe("loadThemeColor", () => {
    it("givenMissingStoredColor_thenReturnDefault", () => {
      expect(loadThemeColor()).toBe(DEFAULT_THEME_COLOR);
      expect(storage.getItem).toHaveBeenCalledWith(THEME_COLOR_STORAGE_KEY);
    });

    it.each(validColors)(
      "givenValidStoredColor_%s_thenReturnNormalizedColor",
      (value, expected) => {
        storage.getItem.mockReturnValue(value);

        expect(loadThemeColor()).toBe(expected);
      }
    );

    it.each(invalidColors)(
      "givenInvalidStoredColor_%s_thenReturnDefault",
      (value) => {
        storage.getItem.mockReturnValue(value);

        expect(loadThemeColor()).toBe(DEFAULT_THEME_COLOR);
      }
    );

    it("givenBlockedStorageRead_thenReturnDefault", () => {
      storage.getItem.mockImplementation(() => {
        throw new Error("Storage is blocked");
      });

      expect(loadThemeColor()).toBe(DEFAULT_THEME_COLOR);
    });
  });

  describe("saveThemeColor", () => {
    it.each(validColors)(
      "givenValidColor_%s_thenStoreAndReturnNormalizedColor",
      (value, expected) => {
        expect(saveThemeColor(value)).toBe(expected);
        expect(storage.setItem).toHaveBeenCalledExactlyOnceWith(
          THEME_COLOR_STORAGE_KEY,
          expected
        );
      }
    );

    it.each([...invalidColors, null, undefined, 123456, {}])(
      "givenInvalidColor_%s_thenReturnNullWithoutWriting",
      (value) => {
        expect(saveThemeColor(value)).toBeNull();
        expect(storage.setItem).not.toHaveBeenCalled();
      }
    );

    it("givenBlockedStorageWrite_thenReturnNormalizedColor", () => {
      storage.setItem.mockImplementation(() => {
        throw new Error("Storage is blocked");
      });

      expect(saveThemeColor("#a1b2c3")).toBe("#A1B2C3");
      expect(storage.setItem).toHaveBeenCalledExactlyOnceWith(
        THEME_COLOR_STORAGE_KEY,
        "#A1B2C3"
      );
    });
  });
});
