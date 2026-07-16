export type ActivityType =
  | 'APPLICATION_CREATED'
  | 'STATUS_CHANGED'
  | 'NOTE_UPDATED'
  | 'OFFER_ADDED'
  | 'OFFER_UPDATED'

export interface Activity {
  id: string
  applicationId: string
  type: ActivityType
  description: string
  createdAt: string
}
