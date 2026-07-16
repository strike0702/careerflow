import { apiClient } from '@/services/apiClient'
import type { CandidateProfile, ProfileUpdateRequest } from '@/types/user'

export async function fetchProfile(): Promise<CandidateProfile> {
  const { data } = await apiClient.get<CandidateProfile>('/api/v1/users/me/profile')
  return data
}

export async function updateProfile(
  request: ProfileUpdateRequest,
): Promise<CandidateProfile> {
  const { data } = await apiClient.put<CandidateProfile>('/api/v1/users/me/profile', request)
  return data
}
