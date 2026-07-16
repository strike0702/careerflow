import { Badge } from '@/components/ui/badge'
import { APPLICATION_STATUS_COLORS, APPLICATION_STATUS_LABELS } from '@/lib/constants'
import type { ApplicationStatus } from '@/types/application'
import { cn } from '@/lib/utils'

interface StatusBadgeProps {
  status: ApplicationStatus
  className?: string
}

export function StatusBadge({ status, className }: StatusBadgeProps) {
  return (
    <Badge className={cn('border-transparent font-medium', APPLICATION_STATUS_COLORS[status], className)}>
      {APPLICATION_STATUS_LABELS[status]}
    </Badge>
  )
}
