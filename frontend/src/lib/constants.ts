import type { ApplicationSource, ApplicationStatus } from '@/types/application'

export const APPLICATION_STATUS_LABELS: Record<ApplicationStatus, string> = {
  WISHLIST: 'Wishlist',
  APPLIED: 'Applied',
  ASSESSMENT: 'Assessment',
  INTERVIEWING: 'Interviewing',
  OFFERED: 'Offered',
  HIRED: 'Hired',
  REJECTED: 'Rejected',
  WITHDRAWN: 'Withdrawn',
}

export const APPLICATION_STATUS_COLORS: Record<ApplicationStatus, string> = {
  WISHLIST: 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300',
  APPLIED: 'bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-300',
  ASSESSMENT: 'bg-violet-100 text-violet-700 dark:bg-violet-900/40 dark:text-violet-300',
  INTERVIEWING: 'bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-300',
  OFFERED: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/40 dark:text-emerald-300',
  HIRED: 'bg-green-100 text-green-800 dark:bg-green-900/40 dark:text-green-300',
  REJECTED: 'bg-red-100 text-red-700 dark:bg-red-900/40 dark:text-red-300',
  WITHDRAWN: 'bg-orange-100 text-orange-700 dark:bg-orange-900/40 dark:text-orange-300',
}

export const APPLICATION_SOURCE_LABELS: Record<ApplicationSource, string> = {
  LINKEDIN: 'LinkedIn',
  REFERRAL: 'Referral',
  COMPANY_WEBSITE: 'Company Website',
  INDEED: 'Indeed',
  NAUKRI: 'Naukri',
  WELLFOUND: 'Wellfound',
  INSTAHYRE: 'Instahyre',
  OTHER: 'Other',
}

export const OFFER_STATUS_LABELS: Record<string, string> = {
  PENDING: 'Pending',
  ACCEPTED: 'Accepted',
  REJECTED: 'Rejected',
  NEGOTIATING: 'Negotiating',
  EXPIRED: 'Expired',
}

export const CHART_COLORS = [
  'hsl(var(--chart-1))',
  'hsl(var(--chart-2))',
  'hsl(var(--chart-3))',
  'hsl(var(--chart-4))',
  'hsl(var(--chart-5))',
]

export const STATUS_CHART_COLORS: Record<ApplicationStatus, string> = {
  WISHLIST: '#94a3b8',
  APPLIED: '#3b82f6',
  ASSESSMENT: '#8b5cf6',
  INTERVIEWING: '#f59e0b',
  OFFERED: '#10b981',
  HIRED: '#22c55e',
  REJECTED: '#ef4444',
  WITHDRAWN: '#f97316',
}
