import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { PageHeader } from '@/components/common/PageHeader'
import { ApplicationForm } from '@/features/applications/components/ApplicationForm'
import {
  getMutationErrorMessage,
  useApplicationMutations,
} from '@/features/applications/hooks/useApplications'
import { ROUTES } from '@/routes/paths'
import type { CreateApplicationRequest } from '@/types/application'

export function CreateApplicationPage() {
  const navigate = useNavigate()
  const { createMutation } = useApplicationMutations()

  const handleSubmit = async (values: CreateApplicationRequest) => {
    try {
      const created = await createMutation.mutateAsync(values)
      toast.success('Application created successfully')
      navigate(ROUTES.applicationDetail(created.id))
    } catch (error) {
      toast.error(getMutationErrorMessage(error))
      throw error
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="New Application"
        description="Add a job application to your pipeline"
      />
      <ApplicationForm
        onSubmit={handleSubmit}
        isSubmitting={createMutation.isPending}
      />
    </div>
  )
}
