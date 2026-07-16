import { apiClient } from '@/services/apiClient'
import type { Activity } from '@/types/activity'
import type { DashboardResponse } from '@/types/application'

export async function fetchDashboard(): Promise<DashboardResponse> {
  const { data } = await apiClient.get<DashboardResponse>('/api/v1/applications/dashboard')
  return data
}

export async function fetchActivities(limit = 10): Promise<Activity[]> {
  const { data } = await apiClient.get<Activity[]>('/api/v1/applications/activities', {
    params: { limit },
  })
  return data
}
