import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createResume,
  deleteResume,
  fetchResumes,
  setPrimaryResume,
  updateResume,
} from '@/features/resumes/api/resumesApi'
import type { CreateResumeRequest, UpdateResumeRequest } from '@/types/resume'

export function useResumes() {
  return useQuery({
    queryKey: ['resumes'],
    queryFn: fetchResumes,
  })
}

export function useResumeMutations() {
  const queryClient = useQueryClient()

  const invalidate = () => void queryClient.invalidateQueries({ queryKey: ['resumes'] })

  const createMutation = useMutation({
    mutationFn: createResume,
    onSuccess: invalidate,
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, request }: { id: string; request: UpdateResumeRequest }) =>
      updateResume(id, request),
    onSuccess: invalidate,
  })

  const deleteMutation = useMutation({
    mutationFn: deleteResume,
    onSuccess: invalidate,
  })

  const setPrimaryMutation = useMutation({
    mutationFn: setPrimaryResume,
    onSuccess: invalidate,
  })

  return { createMutation, updateMutation, deleteMutation, setPrimaryMutation }
}

export type { CreateResumeRequest, UpdateResumeRequest }
