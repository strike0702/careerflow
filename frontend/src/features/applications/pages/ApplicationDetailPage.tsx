import { useState, type ReactNode } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArrowLeft, ExternalLink } from 'lucide-react'
import { toast } from 'sonner'
import { PageHeader } from '@/components/common/PageHeader'
import { ErrorState } from '@/components/common/StateViews'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ActivityTimeline } from '@/features/applications/components/ActivityTimeline'
import { InterviewPanel } from '@/features/interviews/components/InterviewPanel'
import { OfferForm } from '@/features/applications/components/OfferForm'
import { StatusBadge } from '@/features/applications/components/StatusBadge'
import { StatusUpdateDialog } from '@/features/applications/components/StatusUpdateDialog'
import {
  getMutationErrorMessage,
  useApplicationDetail,
  useApplicationMutations,
} from '@/features/applications/hooks/useApplications'
import { useResumes } from '@/features/resumes/hooks/useResumes'
import { APPLICATION_SOURCE_LABELS, OFFER_STATUS_LABELS } from '@/lib/constants'
import { formatCurrency, formatDate, formatDateTime } from '@/lib/formatters'
import { ROUTES } from '@/routes/paths'
import { ApiError } from '@/types/api'
import type { ApplicationStatus } from '@/types/application'
import type { UpsertOfferRequest } from '@/types/offer'

export function ApplicationDetailPage() {
  const { id = '' } = useParams()
  const [statusDialogOpen, setStatusDialogOpen] = useState(false)

  const { data, isLoading, isError, error, refetch } = useApplicationDetail(id)
  const { data: resumes = [] } = useResumes()
  const { updateStatusMutation, upsertOfferMutation } = useApplicationMutations()

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="h-8 w-48 animate-pulse rounded bg-muted" />
        <div className="h-64 animate-pulse rounded-lg bg-muted" />
      </div>
    )
  }

  if (isError || !data) {
    const isNotFound = error instanceof ApiError && error.status === 404
    return (
      <div className="space-y-6">
        <Button variant="ghost" asChild>
          <Link to={ROUTES.applications}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Applications
          </Link>
        </Button>
        <ErrorState
          title={isNotFound ? 'Application not found' : 'Failed to load application'}
          message={getMutationErrorMessage(error)}
          onRetry={() => void refetch()}
        />
      </div>
    )
  }

  const { application, offer, recentActivities } = data
  const linkedResume = application.resumeId
    ? resumes.find((resume) => resume.id === application.resumeId)
    : undefined

  const handleStatusUpdate = async (status: ApplicationStatus) => {
    try {
      await updateStatusMutation.mutateAsync({ id: application.id, status })
      toast.success('Status updated successfully')
    } catch (mutationError) {
      toast.error(getMutationErrorMessage(mutationError))
      throw mutationError
    }
  }

  const handleOfferSubmit = async (values: UpsertOfferRequest) => {
    try {
      await upsertOfferMutation.mutateAsync({ id: application.id, request: values })
      toast.success(offer ? 'Offer updated successfully' : 'Offer saved successfully')
    } catch (mutationError) {
      toast.error(getMutationErrorMessage(mutationError))
      throw mutationError
    }
  }

  return (
    <div className="space-y-6">
      <Button variant="ghost" asChild className="w-fit">
        <Link to={ROUTES.applications}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Applications
        </Link>
      </Button>

      <PageHeader
        title={`${application.companyName} — ${application.jobTitle}`}
        description={application.location ?? 'Location not specified'}
        action={
          <div className="flex items-center gap-2">
            <StatusBadge status={application.status} />
            <Button variant="outline" onClick={() => setStatusDialogOpen(true)}>
              Update Status
            </Button>
          </div>
        }
      />

      <Tabs defaultValue="overview">
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="interviews">Interviews</TabsTrigger>
          <TabsTrigger value="offer">Offer</TabsTrigger>
          <TabsTrigger value="activity">Activity</TabsTrigger>
        </TabsList>

        <TabsContent value="overview">
          <Card>
            <CardHeader>
              <CardTitle>Application Details</CardTitle>
            </CardHeader>
            <CardContent className="grid gap-4 sm:grid-cols-2">
              <DetailField label="Company" value={application.companyName} />
              <DetailField label="Job Title" value={application.jobTitle} />
              <DetailField label="Location" value={application.location ?? '—'} />
              <DetailField
                label="Source"
                value={APPLICATION_SOURCE_LABELS[application.source]}
              />
              <DetailField label="Status">
                <StatusBadge status={application.status} />
              </DetailField>
              <DetailField label="Application Date" value={formatDate(application.applicationDate)} />
              <DetailField label="Resume">
                {linkedResume ? (
                  <a
                    href={linkedResume.storageUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex items-center gap-1 text-primary hover:underline"
                  >
                    {linkedResume.label} (v{linkedResume.versionNo})
                    <ExternalLink className="h-3.5 w-3.5" />
                  </a>
                ) : application.resumeId ? (
                  application.resumeId
                ) : (
                  '—'
                )}
              </DetailField>
              <DetailField label="Created" value={formatDateTime(application.createdAt)} />
              <DetailField label="Last Updated" value={formatDateTime(application.updatedAt)} />
              <DetailField label="Job URL" className="sm:col-span-2">
                {application.jobUrl ? (
                  <a
                    href={application.jobUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex items-center gap-1 text-primary hover:underline"
                  >
                    View posting
                    <ExternalLink className="h-3.5 w-3.5" />
                  </a>
                ) : (
                  '—'
                )}
              </DetailField>
              {application.notes && (
                <DetailField label="Notes" value={application.notes} className="sm:col-span-2" />
              )}
              {application.referralInfo?.referred && (
                <>
                  <Separator className="sm:col-span-2" />
                  <DetailField
                    label="Referrer"
                    value={application.referralInfo.referrerName ?? '—'}
                  />
                  <DetailField
                    label="Referrer Email"
                    value={application.referralInfo.referrerCompanyEmail ?? '—'}
                  />
                  <DetailField
                    label="Relationship"
                    value={application.referralInfo.relationship ?? '—'}
                    className="sm:col-span-2"
                  />
                </>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="interviews">
          <InterviewPanel applicationId={application.id} />
        </TabsContent>

        <TabsContent value="offer" className="space-y-4">
          {offer && (
            <Card>
              <CardHeader>
                <CardTitle>Current Offer</CardTitle>
              </CardHeader>
              <CardContent className="grid gap-4 sm:grid-cols-2">
                <DetailField
                  label="Base Salary"
                  value={formatCurrency(offer.baseSalary, offer.currency)}
                />
                <DetailField
                  label="Joining Bonus"
                  value={formatCurrency(offer.joiningBonus, offer.currency)}
                />
                <DetailField
                  label="Annual Bonus"
                  value={formatCurrency(offer.annualBonus, offer.currency)}
                />
                <DetailField
                  label="Stock Value"
                  value={formatCurrency(offer.stockValue, offer.currency)}
                />
                <DetailField label="Status" value={offer.offerStatus ? OFFER_STATUS_LABELS[offer.offerStatus] : '—'} />
                <DetailField label="Joining Date" value={formatDate(offer.joiningDate)} />
                {offer.notes && (
                  <DetailField label="Notes" value={offer.notes} className="sm:col-span-2" />
                )}
              </CardContent>
            </Card>
          )}
          <OfferForm
            offer={offer}
            onSubmit={handleOfferSubmit}
            isSubmitting={upsertOfferMutation.isPending}
          />
        </TabsContent>

        <TabsContent value="activity">
          <Card>
            <CardHeader>
              <CardTitle>Recent Activity</CardTitle>
            </CardHeader>
            <CardContent>
              <ActivityTimeline activities={recentActivities} />
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      <StatusUpdateDialog
        open={statusDialogOpen}
        currentStatus={application.status}
        onOpenChange={setStatusDialogOpen}
        onSubmit={handleStatusUpdate}
        isSubmitting={updateStatusMutation.isPending}
      />
    </div>
  )
}

function DetailField({
  label,
  value,
  children,
  className,
}: {
  label: string
  value?: string
  children?: ReactNode
  className?: string
}) {
  return (
    <div className={className}>
      <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">{label}</p>
      <div className="mt-1 text-sm">{children ?? value}</div>
    </div>
  )
}
