export const ROUTES = {
  dashboard: '/',
  applications: '/applications',
  applicationNew: '/applications/new',
  applicationDetail: (id: string) => `/applications/${id}`,
  resumes: '/resumes',
  profile: '/profile',
} as const
