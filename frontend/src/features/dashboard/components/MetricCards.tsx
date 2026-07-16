import { Briefcase, Handshake, MessageSquare, TrendingUp, XCircle } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { formatPercent } from '@/lib/formatters'
import type { DashboardResponse } from '@/types/application'

const metrics = [
  {
    key: 'totalApplications' as const,
    label: 'Total Applications',
    icon: Briefcase,
  },
  {
    key: 'activeInterviews' as const,
    label: 'Active Interviews',
    icon: MessageSquare,
  },
  {
    key: 'offersReceived' as const,
    label: 'Offers Received',
    icon: Handshake,
  },
  {
    key: 'rejections' as const,
    label: 'Rejections',
    icon: XCircle,
  },
  {
    key: 'responseRate' as const,
    label: 'Response Rate',
    icon: TrendingUp,
    format: (value: number) => formatPercent(value),
  },
]

interface MetricCardsProps {
  data?: DashboardResponse
  isLoading?: boolean
}

export function MetricCards({ data, isLoading }: MetricCardsProps) {
  if (isLoading) {
    return (
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
        {metrics.map((metric) => (
          <Card key={metric.key}>
            <CardHeader className="pb-2">
              <Skeleton className="h-4 w-24" />
            </CardHeader>
            <CardContent>
              <Skeleton className="h-8 w-16" />
            </CardContent>
          </Card>
        ))}
      </div>
    )
  }

  return (
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
      {metrics.map(({ key, label, icon: Icon, format }) => {
        const rawValue = data?.[key] ?? 0
        const value = format ? format(rawValue as number) : rawValue

        return (
          <Card key={key}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">{label}</CardTitle>
              <Icon className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{value}</div>
            </CardContent>
          </Card>
        )
      })}
    </div>
  )
}
