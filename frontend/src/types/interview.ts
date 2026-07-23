export type RoundType =
  | 'PHONE_SCREEN'
  | 'TECHNICAL'
  | 'SYSTEM_DESIGN'
  | 'BEHAVIORAL'
  | 'HIRING_MANAGER'
  | 'HR'
  | 'ONSITE'

export type InterviewMode = 'REMOTE' | 'ONSITE' | 'PHONE'

export type InterviewStatus =
  | 'SCHEDULED'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'RESCHEDULED'
  | 'NO_SHOW'

export type InterviewOutcome = 'PENDING' | 'PASSED' | 'FAILED' | 'PENDING_DECISION'

export interface Interview {
  id: string
  applicationId: string
  roundNumber: number
  roundType: RoundType
  title: string | null
  mode: InterviewMode
  scheduledAt: string
  durationMinutes: number | null
  meetingLink: string | null
  location: string | null
  interviewerNames: string | null
  status: InterviewStatus
  outcome: InterviewOutcome
  notes: string | null
  createdAt: string
  updatedAt: string
  version: number
}

export interface InterviewStatsResponse {
  totalInterviews: number
  activeInterviews: number
  upcomingInterviews: number
  completedInterviews: number
  interviewsByStatus: Partial<Record<InterviewStatus, number>>
}

export interface CreateInterviewRequest {
  applicationId: string
  roundType: RoundType
  title?: string | null
  mode: InterviewMode
  scheduledAt: string
  durationMinutes?: number | null
  meetingLink?: string | null
  location?: string | null
  interviewerNames?: string | null
  notes?: string | null
}

export interface UpsertRetrospectiveRequest {
  whatWentWell?: string | null
  whatToImprove?: string | null
  questionsAsked?: string | null
  selfRating?: number | null
  followUpActions?: string | null
}

export interface Retrospective {
  id: string
  interviewId: string
  whatWentWell: string | null
  whatToImprove: string | null
  questionsAsked: string | null
  selfRating: number | null
  followUpActions: string | null
  createdAt: string
  updatedAt: string
}

export interface InterviewListParams {
  applicationId?: string
  status?: InterviewStatus
  page?: number
  size?: number
}

export interface InterviewListResponse {
  content: Interview[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
