import Keycloak from 'keycloak-js'

const keycloakConfig = {
  url: import.meta.env.VITE_KEYCLOAK_URL ?? 'http://localhost:8080',
  realm: import.meta.env.VITE_KEYCLOAK_REALM ?? 'careerflow-realm',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'careerflow-api-gateway',
}

export const keycloak = new Keycloak(keycloakConfig)

let initPromise: Promise<boolean> | null = null

export function initKeycloak(): Promise<boolean> {
  if (!initPromise) {
    initPromise = keycloak.init({
      onLoad: 'check-sso',
      pkceMethod: 'S256',
      checkLoginIframe: false,
    })
  }
  return initPromise
}

export async function ensureFreshToken(minValiditySeconds = 30): Promise<string | undefined> {
  if (!keycloak.authenticated) {
    return undefined
  }

  await keycloak.updateToken(minValiditySeconds)
  return keycloak.token
}

export function login(): Promise<void> {
  return keycloak.login({
    redirectUri: window.location.origin,
  })
}

export function register(): Promise<void> {
  return keycloak.register({
    redirectUri: window.location.origin,
  })
}

export function logout(): Promise<void> {
  return keycloak.logout({
    redirectUri: window.location.origin,
  })
}

export function getUserDisplayName(): string {
  const parsed = keycloak.tokenParsed as
    | { given_name?: string; family_name?: string; preferred_username?: string; email?: string }
    | undefined

  if (parsed?.given_name && parsed?.family_name) {
    return `${parsed.given_name} ${parsed.family_name}`
  }

  return parsed?.preferred_username ?? parsed?.email ?? 'User'
}
