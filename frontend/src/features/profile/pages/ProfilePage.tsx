import { toast } from 'sonner'
import { PageHeader } from '@/components/common/PageHeader'
import { ErrorState } from '@/components/common/StateViews'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { ProfileForm } from '@/features/profile/components/ProfileForm'
import { useProfile, useProfileMutation } from '@/features/profile/hooks/useProfile'
import { getMutationErrorMessage } from '@/features/applications/hooks/useApplications'
import { useAuth } from '@/features/auth/AuthProvider'
import type { ProfileUpdateRequest } from '@/types/user'

export function ProfilePage() {
  const { user } = useAuth()
  const { data: profile, isLoading, isError, error, refetch } = useProfile()
  const profileMutation = useProfileMutation()

  const handleSubmit = async (values: ProfileUpdateRequest) => {
    try {
      await profileMutation.mutateAsync(values)
      toast.success('Profile updated successfully')
    } catch (mutationError) {
      toast.error(getMutationErrorMessage(mutationError))
      throw mutationError
    }
  }

  if (isLoading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-8 w-48" />
        <Skeleton className="h-64 w-full" />
      </div>
    )
  }

  if (isError) {
    return (
      <div className="space-y-6">
        <PageHeader title="Profile" description="Manage your candidate profile" />
        <ErrorState
          message={getMutationErrorMessage(error)}
          onRetry={() => void refetch()}
        />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Profile"
        description="Manage your candidate profile and career preferences"
      />

      <Card>
        <CardHeader>
          <CardTitle>Account</CardTitle>
        </CardHeader>
        <CardContent className="grid gap-4 sm:grid-cols-2">
          <InfoField label="Name" value={`${user?.firstName ?? ''} ${user?.lastName ?? ''}`.trim()} />
          <InfoField label="Email" value={user?.email ?? '—'} />
          <InfoField label="Role" value={user?.role ?? '—'} />
        </CardContent>
      </Card>

      <ProfileForm
        profile={profile}
        onSubmit={handleSubmit}
        isSubmitting={profileMutation.isPending}
      />
    </div>
  )
}

function InfoField({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">{label}</p>
      <p className="mt-1 text-sm">{value || '—'}</p>
    </div>
  )
}
