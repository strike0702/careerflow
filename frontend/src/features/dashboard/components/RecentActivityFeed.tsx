import { Activity as ActivityIcon } from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { formatRelativeTime } from '@/lib/formatters'
import type { Activity } from '@/types/activity'

interface RecentActivityFeedProps {
  activities?: Activity[]
  isLoading?: boolean
}

export function RecentActivityFeed({ activities, isLoading }: RecentActivityFeedProps) {
  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <Skeleton className="h-6 w-40" />
          <Skeleton className="h-4 w-56" />
        </CardHeader>
        <CardContent className="space-y-4">
          {Array.from({ length: 5 }).map((_, index) => (
            <div key={index} className="space-y-2">
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-3 w-24" />
            </div>
          ))}
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Recent Activity</CardTitle>
        <CardDescription>Latest updates across your job search pipeline</CardDescription>
      </CardHeader>
      <CardContent>
        {!activities?.length ? (
          <div className="flex flex-col items-center justify-center gap-2 py-8 text-center text-sm text-muted-foreground">
            <ActivityIcon className="h-8 w-8" />
            <p>No activity yet. Create an application to get started.</p>
          </div>
        ) : (
          <ul className="space-y-4">
            {activities.map((activity) => (
              <li key={activity.id} className="border-b pb-4 last:border-0 last:pb-0">
                <p className="text-sm">{activity.description}</p>
                <p className="mt-1 text-xs text-muted-foreground">
                  {formatRelativeTime(activity.createdAt)}
                </p>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  )
}
