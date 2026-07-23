import { createBrowserRouter, Navigate } from 'react-router-dom'
import { AppLayout } from '@/layouts/AppLayout'
import { DashboardPage } from '@/features/dashboard/pages/DashboardPage'
import { ApplicationsListPage } from '@/features/applications/pages/ApplicationsListPage'
import { CreateApplicationPage } from '@/features/applications/pages/CreateApplicationPage'
import { ApplicationDetailPage } from '@/features/applications/pages/ApplicationDetailPage'
import { ProfilePage } from '@/features/profile/pages/ProfilePage'
import { ResumesPage } from '@/features/resumes/pages/ResumesPage'
import { ProtectedRoute } from '@/routes/ProtectedRoute'
import { ROUTES } from '@/routes/paths'

export const router = createBrowserRouter([
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { path: ROUTES.dashboard, element: <DashboardPage /> },
          { path: ROUTES.applications, element: <ApplicationsListPage /> },
          { path: ROUTES.applicationNew, element: <CreateApplicationPage /> },
          { path: '/applications/:id', element: <ApplicationDetailPage /> },
          { path: ROUTES.resumes, element: <ResumesPage /> },
          { path: ROUTES.profile, element: <ProfilePage /> },
        ],
      },
    ],
  },
  { path: '*', element: <Navigate to={ROUTES.dashboard} replace /> },
])
