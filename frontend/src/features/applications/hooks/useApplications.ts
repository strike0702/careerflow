import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createApplication,
  fetchApplication,
  fetchApplications,
  updateApplicationStatus,
  upsertOffer,
} from '@/features/applications/api/applicationsApi'
import type { ApplicationListParams, ApplicationStatus } from '@/types/application'
import type { UpsertOfferRequest } from '@/types/offer'
import { ApiError } from '@/types/api'

export function useApplications(params: ApplicationListParams) {
  return useQuery({
    queryKey: ['applications', params],
    queryFn: () => fetchApplications(params),
  })
}

export function useApplicationDetail(id: string) {
  return useQuery({
    queryKey: ['application', id],
    queryFn: () => fetchApplication(id),
    enabled: Boolean(id),
  })
}

function invalidateApplicationQueries(queryClient: ReturnType<typeof useQueryClient>, id?: string) {
  void queryClient.invalidateQueries({ queryKey: ['applications'] })
  void queryClient.invalidateQueries({ queryKey: ['dashboard'] })
  void queryClient.invalidateQueries({ queryKey: ['activities'] })
  if (id) {
    void queryClient.invalidateQueries({ queryKey: ['application', id] })
  }
}

export function useApplicationMutations() {
  const queryClient = useQueryClient()

  const createMutation = useMutation({
    mutationFn: createApplication,
    onSuccess: () => invalidateApplicationQueries(queryClient),
  })

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: ApplicationStatus }) =>
      updateApplicationStatus(id, status),
    onSuccess: (data) => invalidateApplicationQueries(queryClient, data.application.id),
  })

  const upsertOfferMutation = useMutation({
    mutationFn: ({ id, request }: { id: string; request: UpsertOfferRequest }) =>
      upsertOffer(id, request),
    onSuccess: (data) => invalidateApplicationQueries(queryClient, data.application.id),
  })

  return {
    createMutation,
    updateStatusMutation,
    upsertOfferMutation,
  }
}

export function getMutationErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    return error.message
  }
  if (error instanceof Error) {
    return error.message
  }
  return 'An unexpected error occurred'
}
