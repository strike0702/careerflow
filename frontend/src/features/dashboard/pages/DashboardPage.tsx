import { Link } from 'react-router-dom'
import { Briefcase, Plus } from 'lucide-react'
import { PageHeader } from '@/components/common/PageHeader'
import { EmptyState, ErrorState } from '@/components/common/StateViews'
import { Button } from '@/components/ui/button'
import { MetricCards } from '@/features/dashboard/components/MetricCards'
import { RecentActivityFeed } from '@/features/dashboard/components/RecentActivityFeed'
import { StatusChart } from '@/features/dashboard/components/StatusChart'
import { useDashboard, useRecentActivities } from '@/features/dashboard/hooks/useDashboard'
import { ProfileCompletionBanner } from '@/features/profile/components/ProfileCompletionBanner'
import { ROUTES } from '@/routes/paths'

export function DashboardPage() {
  const dashboardQuery = useDashboard()
  const activitiesQuery = useRecentActivities(10)

  const isLoading = dashboardQuery.isLoading || activitiesQuery.isLoading
  const isEmpty = dashboardQuery.data?.totalApplications === 0

  if (dashboardQuery.isError) {
    return (
      <div className="space-y-6">
        <PageHeader
          title="Dashboard"
          description="Overview of your job search pipeline"
        />
        <ErrorState
          message={dashboardQuery.error.message}
          onRetry={() => void dashboardQuery.refetch()}
        />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Dashboard"
        description="Overview of your job search pipeline"
        action={
          <Button asChild>
            <Link to={ROUTES.applicationNew}>
              <Plus className="h-4 w-4" />
              New Application
            </Link>
          </Button>
        }
      />

      <ProfileCompletionBanner />

      {isEmpty && !isLoading ? (
        <EmptyState
          icon={<Briefcase className="h-10 w-10" />}
          title="No applications yet"
          description="Start tracking your job search by adding your first application."
          action={
            <Button asChild>
              <Link to={ROUTES.applicationNew}>Add Application</Link>
            </Button>
          }
        />
      ) : (
        <>
          <MetricCards data={dashboardQuery.data} isLoading={dashboardQuery.isLoading} />
          <div className="grid gap-6 lg:grid-cols-2">
            <StatusChart data={dashboardQuery.data} isLoading={dashboardQuery.isLoading} />
            <RecentActivityFeed
              activities={activitiesQuery.data}
              isLoading={activitiesQuery.isLoading}
            />
          </div>
        </>
      )}
    </div>
  )
}
