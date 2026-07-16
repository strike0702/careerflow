import { apiClient } from '@/services/apiClient'
import type { Page } from '@/types/api'
import type {
  Application,
  ApplicationDetailResponse,
  ApplicationListParams,
  ApplicationStatus,
  ApplicationSummary,
  CreateApplicationRequest,
} from '@/types/application'
import type { UpsertOfferRequest } from '@/types/offer'

export async function fetchApplications(
  params: ApplicationListParams,
): Promise<Page<ApplicationSummary>> {
  const { data } = await apiClient.get<Page<ApplicationSummary>>('/api/v1/applications', {
    params: {
      ...params,
      sort: params.sort ?? 'createdAt,desc',
    },
  })
  return data
}

export async function fetchApplication(id: string): Promise<ApplicationDetailResponse> {
  const { data } = await apiClient.get<ApplicationDetailResponse>(`/api/v1/applications/${id}`)
  return data
}

export async function createApplication(
  request: CreateApplicationRequest,
): Promise<Application> {
  const { data } = await apiClient.post<Application>('/api/v1/applications', request)
  return data
}

export async function updateApplicationStatus(
  id: string,
  status: ApplicationStatus,
): Promise<ApplicationDetailResponse> {
  const { data } = await apiClient.patch<ApplicationDetailResponse>(
    `/api/v1/applications/${id}/status`,
    { status },
  )
  return data
}

export async function upsertOffer(
  id: string,
  request: UpsertOfferRequest,
): Promise<ApplicationDetailResponse> {
  const { data } = await apiClient.put<ApplicationDetailResponse>(
    `/api/v1/applications/${id}/offer`,
    request,
  )
  return data
}
