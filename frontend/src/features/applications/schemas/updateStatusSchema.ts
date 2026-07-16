import { z } from 'zod'
import { APPLICATION_STATUSES } from '@/features/applications/schemas/createApplicationSchema'

export const updateStatusSchema = z.object({
  status: z.enum(APPLICATION_STATUSES, { message: 'Status is required' }),
})

export type UpdateStatusFormValues = z.infer<typeof updateStatusSchema>
