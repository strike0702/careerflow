import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { APPLICATION_STATUS_LABELS } from '@/lib/constants'
import { APPLICATION_STATUSES } from '@/features/applications/schemas/createApplicationSchema'
import type { ApplicationStatus } from '@/types/application'

interface ApplicationFiltersProps {
  status: ApplicationStatus | 'ALL'
  company: string
  onStatusChange: (status: ApplicationStatus | 'ALL') => void
  onCompanyChange: (company: string) => void
}

export function ApplicationFilters({
  status,
  company,
  onStatusChange,
  onCompanyChange,
}: ApplicationFiltersProps) {
  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <div className="space-y-2">
        <Label htmlFor="status-filter">Status</Label>
        <Select
          value={status}
          onValueChange={(value) => onStatusChange(value as ApplicationStatus | 'ALL')}
        >
          <SelectTrigger id="status-filter">
            <SelectValue placeholder="All statuses" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">All statuses</SelectItem>
            {APPLICATION_STATUSES.map((item) => (
              <SelectItem key={item} value={item}>
                {APPLICATION_STATUS_LABELS[item]}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      <div className="space-y-2 sm:col-span-2 lg:col-span-2">
        <Label htmlFor="company-filter">Company</Label>
        <Input
          id="company-filter"
          placeholder="Search by company name..."
          value={company}
          onChange={(event) => onCompanyChange(event.target.value)}
        />
      </div>
    </div>
  )
}
