export type OfferStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'NEGOTIATING' | 'EXPIRED'

export interface Offer {
  id: string
  applicationId: string
  baseSalary: number | null
  joiningBonus: number | null
  annualBonus: number | null
  stockValue: number | null
  currency: string
  joiningDate: string | null
  offerStatus: OfferStatus | null
  notes: string | null
  createdAt: string
}

export interface UpsertOfferRequest {
  baseSalary?: number | null
  joiningBonus?: number | null
  annualBonus?: number | null
  stockValue?: number | null
  currency: string
  joiningDate?: string | null
  offerStatus?: OfferStatus | null
  notes?: string | null
}
