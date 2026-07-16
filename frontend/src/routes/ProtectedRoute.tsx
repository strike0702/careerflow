import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { Loader2 } from 'lucide-react'
import { useAuth } from '@/features/auth/AuthProvider'
import { AuthLanding } from '@/features/auth/components/AuthLanding'

export function ProtectedRoute() {
  const { authState, isAuthenticated } = useAuth()
  const location = useLocation()

  if (authState === 'initializing') {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    )
  }

  if (!isAuthenticated) {
    return <AuthLanding />
  }

  if (location.pathname === '/login') {
    return <Navigate to="/" replace />
  }

  return <Outlet />
}
