import { Link } from 'react-router-dom'
import { Skeleton } from '@/components/ui/skeleton'
import { StatusBadge } from '@/features/applications/components/StatusBadge'
import { formatDate } from '@/lib/formatters'
import { ROUTES } from '@/routes/paths'
import type { ApplicationSummary } from '@/types/application'

interface ApplicationTableProps {
  applications?: ApplicationSummary[]
  isLoading?: boolean
}

export function ApplicationTable({ applications, isLoading }: ApplicationTableProps) {
  if (isLoading) {
    return (
      <div className="space-y-3">
        {Array.from({ length: 5 }).map((_, index) => (
          <Skeleton key={index} className="h-14 w-full" />
        ))}
      </div>
    )
  }

  if (!applications?.length) {
    return null
  }

  return (
    <div className="overflow-x-auto rounded-lg border">
      <table className="w-full min-w-[640px] text-left text-sm">
        <thead className="border-b bg-muted/50">
          <tr>
            <th className="px-4 py-3 font-medium">Company</th>
            <th className="px-4 py-3 font-medium">Title</th>
            <th className="px-4 py-3 font-medium">Status</th>
            <th className="px-4 py-3 font-medium">Applied</th>
            <th className="px-4 py-3 font-medium">Created</th>
          </tr>
        </thead>
        <tbody>
          {applications.map((application) => (
            <tr
              key={application.id}
              className="border-b transition-colors last:border-0 hover:bg-muted/30"
            >
              <td className="px-4 py-3">
                <Link
                  to={ROUTES.applicationDetail(application.id)}
                  className="font-medium text-primary hover:underline"
                >
                  {application.companyName}
                </Link>
              </td>
              <td className="px-4 py-3 text-muted-foreground">{application.jobTitle}</td>
              <td className="px-4 py-3">
                <StatusBadge status={application.status} />
              </td>
              <td className="px-4 py-3 text-muted-foreground">
                {formatDate(application.applicationDate)}
              </td>
              <td className="px-4 py-3 text-muted-foreground">
                {formatDate(application.createdAt)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
