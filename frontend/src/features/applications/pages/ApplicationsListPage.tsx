import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Briefcase, Plus } from 'lucide-react'
import { PageHeader } from '@/components/common/PageHeader'
import { PaginationControls } from '@/components/common/PaginationControls'
import { EmptyState, ErrorState } from '@/components/common/StateViews'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { ApplicationFilters } from '@/features/applications/components/ApplicationFilters'
import { ApplicationTable } from '@/features/applications/components/ApplicationTable'
import {
  getMutationErrorMessage,
  useApplications,
} from '@/features/applications/hooks/useApplications'
import { useDebounce } from '@/hooks/useDebounce'
import { ROUTES } from '@/routes/paths'
import type { ApplicationStatus } from '@/types/application'

const PAGE_SIZE = 20

export function ApplicationsListPage() {
  const [page, setPage] = useState(0)
  const [status, setStatus] = useState<ApplicationStatus | 'ALL'>('ALL')
  const [company, setCompany] = useState('')
  const debouncedCompany = useDebounce(company)

  const { data, isLoading, isError, error, refetch } = useApplications({
    page,
    size: PAGE_SIZE,
    status: status === 'ALL' ? undefined : status,
    company: debouncedCompany || undefined,
  })

  const handleStatusChange = (value: ApplicationStatus | 'ALL') => {
    setStatus(value)
    setPage(0)
  }

  const handleCompanyChange = (value: string) => {
    setCompany(value)
    setPage(0)
  }

  if (isError) {
    return (
      <div className="space-y-6">
        <PageHeader title="Applications" description="Track and manage your job applications" />
        <ErrorState
          message={getMutationErrorMessage(error)}
          onRetry={() => void refetch()}
        />
      </div>
    )
  }

  const isEmpty = !isLoading && data?.empty

  return (
    <div className="space-y-6">
      <PageHeader
        title="Applications"
        description="Track and manage your job applications"
        action={
          <Button asChild>
            <Link to={ROUTES.applicationNew}>
              <Plus className="h-4 w-4" />
              New Application
            </Link>
          </Button>
        }
      />

      <Card>
        <CardContent className="pt-6">
          <ApplicationFilters
            status={status}
            company={company}
            onStatusChange={handleStatusChange}
            onCompanyChange={handleCompanyChange}
          />
        </CardContent>
      </Card>

      {isEmpty ? (
        <EmptyState
          icon={<Briefcase className="h-10 w-10" />}
          title="No applications found"
          description={
            debouncedCompany || status !== 'ALL'
              ? 'Try adjusting your filters to see more results.'
              : 'Start tracking your job search by adding your first application.'
          }
          action={
            <Button asChild>
              <Link to={ROUTES.applicationNew}>Add Application</Link>
            </Button>
          }
        />
      ) : (
        <>
          <ApplicationTable applications={data?.content} isLoading={isLoading} />
          <PaginationControls
            page={page}
            totalPages={data?.totalPages ?? 0}
            totalElements={data?.totalElements ?? 0}
            onPageChange={setPage}
            isLoading={isLoading}
          />
        </>
      )}
    </div>
  )
}
