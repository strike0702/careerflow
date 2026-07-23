import { apiClient } from '@/services/apiClient'
import type {
  CreateInterviewRequest,
  Interview,
  InterviewListParams,
  InterviewListResponse,
  InterviewStatsResponse,
  InterviewStatus,
  Retrospective,
  UpsertRetrospectiveRequest,
} from '@/types/interview'

export async function fetchInterviewStats(): Promise<InterviewStatsResponse> {
  const { data } = await apiClient.get<InterviewStatsResponse>('/api/v1/interviews/stats')
  return data
}

export async function fetchInterviews(params: InterviewListParams): Promise<InterviewListResponse> {
  const { data } = await apiClient.get<InterviewListResponse>('/api/v1/interviews', { params })
  return data
}

export async function createInterview(request: CreateInterviewRequest): Promise<Interview> {
  const { data } = await apiClient.post<Interview>('/api/v1/interviews', request)
  return data
}

export async function updateInterviewStatus(id: string, status: InterviewStatus): Promise<Interview> {
  const { data } = await apiClient.patch<Interview>(`/api/v1/interviews/${id}/status`, { status })
  return data
}

export async function upsertRetrospective(
  id: string,
  request: UpsertRetrospectiveRequest,
): Promise<Retrospective> {
  const { data } = await apiClient.put<Retrospective>(`/api/v1/interviews/${id}/retrospective`, request)
  return data
}

export async function deleteInterview(id: string): Promise<void> {
  await apiClient.delete(`/api/v1/interviews/${id}`)
}
