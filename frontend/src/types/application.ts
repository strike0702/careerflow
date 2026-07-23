import type { Activity } from '@/types/activity'
import type { Offer } from '@/types/offer'

export type ApplicationStatus =
  | 'WISHLIST'
  | 'APPLIED'
  | 'ASSESSMENT'
  | 'INTERVIEWING'
  | 'OFFERED'
  | 'HIRED'
  | 'REJECTED'
  | 'WITHDRAWN'

export type ApplicationSource =
  | 'LINKEDIN'
  | 'REFERRAL'
  | 'COMPANY_WEBSITE'
  | 'INDEED'
  | 'NAUKRI'
  | 'WELLFOUND'
  | 'INSTAHYRE'
  | 'OTHER'

export interface ReferralInfo {
  referred: boolean
  referrerName?: string | null
  referrerCompanyEmail?: string | null
  relationship?: string | null
}

export interface Application {
  id: string
  companyName: string
  jobTitle: string
  location: string | null
  jobUrl: string | null
  source: ApplicationSource
  status: ApplicationStatus
  applicationDate: string | null
  notes: string | null
  resumeId: string | null
  referralInfo: ReferralInfo | null
  createdAt: string
  updatedAt: string
  version: number
}

export interface ApplicationSummary {
  id: string
  companyName: string
  jobTitle: string
  status: ApplicationStatus
  applicationDate: string | null
  createdAt: string
}

export interface DashboardResponse {
  totalApplications: number
  activeInterviews: number
  offersReceived: number
  rejections: number
  responseRate: number
  applicationsByStatus: Partial<Record<ApplicationStatus, number>>
}

export interface CreateApplicationRequest {
  companyName: string
  jobTitle: string
  location?: string | null
  jobUrl?: string | null
  source: ApplicationSource
  status?: ApplicationStatus
  applicationDate?: string | null
  notes?: string | null
  resumeId?: string | null
  referralInfo?: ReferralInfo | null
}

export interface ApplicationDetailResponse {
  application: Application
  offer: Offer | null
  recentActivities: Activity[]
}

export interface ApplicationListParams {
  status?: ApplicationStatus
  company?: string
  page?: number
  size?: number
  sort?: string
}
