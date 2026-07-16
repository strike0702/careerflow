import { apiClient } from '@/services/apiClient'
import type { User } from '@/types/user'

export async function fetchCurrentUser(): Promise<User> {
  const { data } = await apiClient.get<User>('/api/v1/users/me')
  return data
}
