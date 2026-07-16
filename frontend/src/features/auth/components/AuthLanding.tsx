import { Briefcase } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { useAuth } from '@/features/auth/AuthProvider'

export function AuthLanding() {
  const { login, register } = useAuth()

  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4">
      <div className="flex w-full max-w-sm flex-col items-center gap-6 text-center">
        <div className="flex items-center gap-2">
          <Briefcase className="h-6 w-6" />
          <span className="text-xl font-bold tracking-tight">CareerFlow</span>
        </div>
        <p className="text-sm text-muted-foreground">
          Track job applications, offers, and interviews in one place.
        </p>
        <div className="flex w-full flex-col gap-3">
          <Button className="w-full" onClick={() => void login()}>
            Sign In
          </Button>
          <Button variant="outline" className="w-full" onClick={() => void register()}>
            Create Account
          </Button>
        </div>
      </div>
    </div>
  )
}
