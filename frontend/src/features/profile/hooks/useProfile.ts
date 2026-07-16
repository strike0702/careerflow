import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { fetchProfile, updateProfile } from '@/features/profile/api/profileApi'
import type { ProfileUpdateRequest } from '@/types/user'

export function useProfile() {
  return useQuery({
    queryKey: ['profile'],
    queryFn: fetchProfile,
    staleTime: 2 * 60_000,
  })
}

export function useProfileMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: ProfileUpdateRequest) => updateProfile(request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['profile'] })
    },
  })
}
