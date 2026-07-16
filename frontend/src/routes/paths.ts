export const ROUTES = {
  dashboard: '/',
  applications: '/applications',
  applicationNew: '/applications/new',
  applicationDetail: (id: string) => `/applications/${id}`,
  profile: '/profile',
} as const
