import { getHeaders } from "@/api/fetch-utils.ts";
import { BaseAPI, Configuration } from "@/api/generated/sonar-backend";
import { BASE_API_PATH } from "@/constants.ts";

type ApiCtor<T extends BaseAPI> = new (config: Configuration) => T;

const instances = new Map<ApiCtor<BaseAPI>, BaseAPI>();

async function customFetch(url: string, init?: RequestInit) {
  const customInit: RequestInit = {
    ...init,
    mode: "cors",
    credentials: "same-origin",
    redirect: "manual",
  };

  return fetch(url, customInit);
}

function createConfig(): Configuration {
  return new Configuration({
    basePath: BASE_API_PATH,
    fetchApi: customFetch,
    middleware: [
      {
        pre: async (context) => {
          return {
            url: context.url,
            init: {
              ...context.init,
              headers: mergeHeaders(getHeaders(), context.init.headers),
            },
          };
        },
      },
    ],
  });
}

/**
 * Retrieves the instance of the given OpenAPI class or creates a new one if none exists.
 * This factory pattern makes sure that only one instance of each OpenAPI-class exists (singleton like).
 * @param ApiClass the OpenAPI class to instantiate / retrieve
 */
function getInstance<T extends BaseAPI>(ApiClass: ApiCtor<T>): T {
  const existing = instances.get(ApiClass as ApiCtor<BaseAPI>);
  if (existing) {
    return existing as T;
  }

  const api = new ApiClass(createConfig());
  instances.set(ApiClass as ApiCtor<BaseAPI>, api);
  return api;
}

/**
 * Merging happens via the Headers API because HTTP header names are case-insensitive. The generated
 * client writes "Content-Type" while {@link getHeaders} yields lowercase names, so a plain object
 * spread would keep both keys. fetch would then send them as one invalid header value
 * "application/json, application/json".
 */
function mergeHeaders(
  defaults: Headers,
  headers: HeadersInit | undefined
): Headers {
  const merged = new Headers(defaults);
  new Headers(headers).forEach((value, key) => merged.set(key, value));
  return merged;
}

export const ApiFactory = {
  getInstance,
} as const;
