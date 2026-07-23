import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createInterview,
  deleteInterview,
  fetchInterviews,
  fetchInterviewStats,
  updateInterviewStatus,
  upsertRetrospective,
} from '@/features/interviews/api/interviewsApi'
import type {
  CreateInterviewRequest,
  InterviewListParams,
  InterviewStatus,
  UpsertRetrospectiveRequest,
} from '@/types/interview'

export function useInterviewStats() {
  return useQuery({
    queryKey: ['interview-stats'],
    queryFn: fetchInterviewStats,
  })
}

export function useInterviews(params: InterviewListParams) {
  return useQuery({
    queryKey: ['interviews', params],
    queryFn: () => fetchInterviews(params),
  })
}

export function useInterviewMutations(applicationId?: string) {
  const queryClient = useQueryClient()

  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['interviews'] })
    void queryClient.invalidateQueries({ queryKey: ['interview-stats'] })
    void queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    if (applicationId) {
      void queryClient.invalidateQueries({ queryKey: ['interviews', { applicationId }] })
    }
  }

  const createMutation = useMutation({
    mutationFn: createInterview,
    onSuccess: invalidate,
  })

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: InterviewStatus }) =>
      updateInterviewStatus(id, status),
    onSuccess: invalidate,
  })

  const upsertRetrospectiveMutation = useMutation({
    mutationFn: ({ id, request }: { id: string; request: UpsertRetrospectiveRequest }) =>
      upsertRetrospective(id, request),
    onSuccess: invalidate,
  })

  const deleteMutation = useMutation({
    mutationFn: deleteInterview,
    onSuccess: invalidate,
  })

  return { createMutation, updateStatusMutation, upsertRetrospectiveMutation, deleteMutation }
}

export type { CreateInterviewRequest, UpsertRetrospectiveRequest }
