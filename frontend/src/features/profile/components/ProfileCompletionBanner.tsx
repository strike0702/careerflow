import { useState } from 'react'
import { Link } from 'react-router-dom'
import { X } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { useAuth } from '@/features/auth/AuthProvider'
import { useProfile } from '@/features/profile/hooks/useProfile'
import {
  dismissProfileBanner,
  isProfileBannerDismissed,
} from '@/features/profile/lib/profileBannerDismiss'
import { isProfileComplete } from '@/features/profile/lib/profileCompleteness'
import { ROUTES } from '@/routes/paths'

export function ProfileCompletionBanner() {
  const { user } = useAuth()
  const { data: profile } = useProfile()
  const [dismissed, setDismissed] = useState(() =>
    user?.id ? isProfileBannerDismissed(user.id) : false,
  )

  if (!user?.id || isProfileComplete(profile) || dismissed) {
    return null
  }

  const handleDismiss = () => {
    dismissProfileBanner(user.id)
    setDismissed(true)
  }

  return (
    <div
      role="status"
      className="flex items-start gap-3 rounded-lg border border-primary/20 bg-primary/5 px-4 py-3"
    >
      <div className="min-w-0 flex-1 space-y-1">
        <p className="text-sm font-medium">Complete your profile</p>
        <p className="text-sm text-muted-foreground">
          Add your target roles and skills so CareerFlow can better reflect your job search
          preferences.
        </p>
        <Button variant="link" className="h-auto p-0 text-sm" asChild>
          <Link to={ROUTES.profile}>Go to profile</Link>
        </Button>
      </div>
      <Button
        variant="ghost"
        size="icon"
        className="h-8 w-8 shrink-0"
        onClick={handleDismiss}
        aria-label="Dismiss profile reminder"
      >
        <X className="h-4 w-4" />
      </Button>
    </div>
  )
}
