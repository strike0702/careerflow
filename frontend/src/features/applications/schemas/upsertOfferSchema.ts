import { z } from 'zod'

const offerStatuses = ['PENDING', 'ACCEPTED', 'REJECTED', 'NEGOTIATING', 'EXPIRED'] as const

function parseOptionalAmount(value?: string): number | undefined {
  if (!value?.trim()) return undefined
  const parsed = Number(value)
  return Number.isNaN(parsed) ? undefined : parsed
}

export const upsertOfferSchema = z
  .object({
    baseSalary: z.string().optional(),
    joiningBonus: z.string().optional(),
    annualBonus: z.string().optional(),
    stockValue: z.string().optional(),
    currency: z
      .string()
      .length(3, 'Currency must be exactly 3 letters')
      .regex(/^[A-Z]{3}$/, 'Currency must be uppercase ISO 4217 (e.g. USD)'),
    joiningDate: z.string().optional(),
    offerStatus: z.enum(offerStatuses).optional(),
    notes: z.string().optional(),
  })
  .superRefine((data, ctx) => {
    const fields = ['baseSalary', 'joiningBonus', 'annualBonus', 'stockValue'] as const
    for (const field of fields) {
      const raw = data[field]
      if (!raw?.trim()) continue
      const parsed = Number(raw)
      if (Number.isNaN(parsed)) {
        ctx.addIssue({ code: 'custom', message: 'Must be a valid number', path: [field] })
      } else if (parsed < 0) {
        ctx.addIssue({ code: 'custom', message: 'Must be 0 or greater', path: [field] })
      }
    }
  })

export type UpsertOfferFormValues = z.infer<typeof upsertOfferSchema>
export const OFFER_STATUSES = offerStatuses

export function toUpsertOfferRequest(values: UpsertOfferFormValues) {
  return {
    baseSalary: parseOptionalAmount(values.baseSalary),
    joiningBonus: parseOptionalAmount(values.joiningBonus),
    annualBonus: parseOptionalAmount(values.annualBonus),
    stockValue: parseOptionalAmount(values.stockValue),
    currency: values.currency,
    joiningDate: values.joiningDate || null,
    offerStatus: values.offerStatus,
    notes: values.notes || null,
  }
}

export function offerToFormValues(offer?: {
  baseSalary: number | null
  joiningBonus: number | null
  annualBonus: number | null
  stockValue: number | null
  currency: string
  joiningDate: string | null
  offerStatus: (typeof offerStatuses)[number] | null
  notes: string | null
} | null): UpsertOfferFormValues {
  const formatAmount = (value: number | null | undefined) =>
    value == null ? '' : String(value)

  return {
    baseSalary: formatAmount(offer?.baseSalary),
    joiningBonus: formatAmount(offer?.joiningBonus),
    annualBonus: formatAmount(offer?.annualBonus),
    stockValue: formatAmount(offer?.stockValue),
    currency: offer?.currency ?? 'USD',
    joiningDate: offer?.joiningDate ?? '',
    offerStatus: offer?.offerStatus ?? 'PENDING',
    notes: offer?.notes ?? '',
  }
}
