import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useQuery } from '@tanstack/react-query'
import { fetchCurrentUser } from '@/features/auth/api/userApi'
import type { User } from '@/types/user'
import {
  getUserDisplayName,
  initKeycloak,
  keycloak,
  login,
  logout,
  register,
} from '@/services/keycloak'

type AuthState = 'initializing' | 'authenticated' | 'unauthenticated'

interface AuthContextValue {
  authState: AuthState
  isAuthenticated: boolean
  user: User | undefined
  displayName: string
  login: () => Promise<void>
  register: () => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [authState, setAuthState] = useState<AuthState>('initializing')

  useEffect(() => {
    let mounted = true

    initKeycloak()
      .then((authenticated) => {
        if (!mounted) return
        setAuthState(authenticated ? 'authenticated' : 'unauthenticated')
      })
      .catch(() => {
        if (!mounted) return
        setAuthState('unauthenticated')
      })

    keycloak.onAuthSuccess = () => setAuthState('authenticated')
    keycloak.onAuthLogout = () => setAuthState('unauthenticated')
    keycloak.onTokenExpired = () => {
      keycloak.updateToken(30).catch(() => {
        setAuthState('unauthenticated')
      })
    }

    return () => {
      mounted = false
      keycloak.onAuthSuccess = undefined
      keycloak.onAuthLogout = undefined
      keycloak.onTokenExpired = undefined
    }
  }, [])

  const { data: user } = useQuery({
    queryKey: ['user', 'me'],
    queryFn: fetchCurrentUser,
    enabled: authState === 'authenticated',
    staleTime: 5 * 60_000,
  })

  const handleLogin = useCallback(async () => {
    await login()
  }, [])

  const handleRegister = useCallback(async () => {
    await register()
  }, [])

  const handleLogout = useCallback(async () => {
    await logout()
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      authState,
      isAuthenticated: authState === 'authenticated',
      user,
      displayName: user ? `${user.firstName} ${user.lastName}` : getUserDisplayName(),
      login: handleLogin,
      register: handleRegister,
      logout: handleLogout,
    }),
    [authState, user, handleLogin, handleRegister, handleLogout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}
