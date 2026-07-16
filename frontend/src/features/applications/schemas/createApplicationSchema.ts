import { z } from 'zod'

const applicationSources = [
  'LINKEDIN',
  'REFERRAL',
  'COMPANY_WEBSITE',
  'INDEED',
  'NAUKRI',
  'WELLFOUND',
  'INSTAHYRE',
  'OTHER',
] as const

const applicationStatuses = [
  'WISHLIST',
  'APPLIED',
  'ASSESSMENT',
  'INTERVIEWING',
  'OFFERED',
  'HIRED',
  'REJECTED',
  'WITHDRAWN',
] as const

export const createApplicationSchema = z
  .object({
    companyName: z.string().min(1, 'Company name is required'),
    jobTitle: z.string().min(1, 'Job title is required'),
    location: z.string().optional(),
    jobUrl: z.union([z.literal(''), z.string().url('Must be a valid URL')]).optional(),
    source: z.enum(applicationSources, { message: 'Source is required' }),
    status: z.enum(applicationStatuses),
    applicationDate: z.string().optional(),
    notes: z.string().optional(),
    referred: z.boolean(),
    referrerName: z.string().optional(),
    referrerCompanyEmail: z
      .union([z.literal(''), z.string().email('Must be a valid email')])
      .optional(),
    relationship: z.string().optional(),
  })
  .superRefine((data, ctx) => {
    if (data.referred) {
      if (!data.referrerName?.trim()) {
        ctx.addIssue({
          code: 'custom',
          message: 'Referrer name is required when referred',
          path: ['referrerName'],
        })
      }
    }
  })

export type CreateApplicationFormValues = z.infer<typeof createApplicationSchema>

export const APPLICATION_STATUSES = applicationStatuses
export const APPLICATION_SOURCES = applicationSources
