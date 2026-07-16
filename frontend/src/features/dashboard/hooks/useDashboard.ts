import { useQuery } from '@tanstack/react-query'
import { fetchActivities, fetchDashboard } from '@/features/dashboard/api/dashboardApi'

export function useDashboard() {
  return useQuery({
    queryKey: ['dashboard'],
    queryFn: fetchDashboard,
  })
}

export function useRecentActivities(limit = 10) {
  return useQuery({
    queryKey: ['activities', limit],
    queryFn: () => fetchActivities(limit),
  })
}
