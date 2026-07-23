import { APPLICATION_STATUS_LABELS } from '@/lib/constants'
import type { ApplicationStatus } from '@/types/application'

/** Parses ISO strings or epoch seconds/millis from legacy API responses. */
function toDate(value: string | number): Date {
  if (typeof value === 'number') {
    const millis = value < 1_000_000_000_000 ? value * 1000 : value
    return new Date(millis)
  }
  return new Date(value)
}

export function formatDate(value: string | number | null | undefined): string {
  if (value == null || value === '') return '—'
  const date = toDate(value)
  if (Number.isNaN(date.getTime())) return '—'
  return new Intl.DateTimeFormat(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  }).format(date)
}

export function formatDateTime(value: string | number | null | undefined): string {
  if (value == null || value === '') return '—'

  const date = toDate(value)
  if (Number.isNaN(date.getTime())) return '—'

  return new Intl.DateTimeFormat(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  }).format(date)
}

export function formatRelativeTime(value: string | number): string {
  const date = toDate(value)
  const diffMs = date.getTime() - Date.now()
  const diffMinutes = Math.round(diffMs / 60_000)
  const rtf = new Intl.RelativeTimeFormat(undefined, { numeric: 'auto' })

  if (Math.abs(diffMinutes) < 60) {
    return rtf.format(diffMinutes, 'minute')
  }

  const diffHours = Math.round(diffMinutes / 60)
  if (Math.abs(diffHours) < 24) {
    return rtf.format(diffHours, 'hour')
  }

  const diffDays = Math.round(diffHours / 24)
  if (Math.abs(diffDays) < 30) {
    return rtf.format(diffDays, 'day')
  }

  return formatDate(value)
}

export function formatPercent(value: number): string {
  return `${value.toFixed(1)}%`
}

export function formatStatusLabel(status: ApplicationStatus): string {
  return APPLICATION_STATUS_LABELS[status]
}

export function formatCurrency(
  value: number | null | undefined,
  currency = 'INR',
): string {
  if (value == null) return '—'
  return new Intl.NumberFormat(undefined, {
    style: 'currency',
    currency,
    maximumFractionDigits: 0,
  }).format(value)
}

export function getInitials(firstName?: string, lastName?: string, email?: string): string {
  if (firstName && lastName) {
    return `${firstName[0]}${lastName[0]}`.toUpperCase()
  }
  if (email) {
    return email.slice(0, 2).toUpperCase()
  }
  return 'CF'
}
