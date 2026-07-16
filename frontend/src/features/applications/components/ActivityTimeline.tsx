import { formatRelativeTime } from '@/lib/formatters'
import type { Activity } from '@/types/activity'

interface ActivityTimelineProps {
  activities: Activity[]
  emptyMessage?: string
}

export function ActivityTimeline({
  activities,
  emptyMessage = 'No activity recorded yet.',
}: ActivityTimelineProps) {
  if (!activities.length) {
    return <p className="text-sm text-muted-foreground">{emptyMessage}</p>
  }

  return (
    <ul className="space-y-4">
      {activities.map((activity) => (
        <li key={activity.id} className="relative border-l-2 border-muted pl-4">
          <span className="absolute -left-[5px] top-1.5 h-2 w-2 rounded-full bg-primary" />
          <p className="text-sm">{activity.description}</p>
          <p className="mt-1 text-xs text-muted-foreground">
            {formatRelativeTime(activity.createdAt)}
          </p>
        </li>
      ))}
    </ul>
  )
}
