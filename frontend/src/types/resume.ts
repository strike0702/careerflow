export type ParseStatus =
  | 'NOT_PARSED'
  | 'PENDING'
  | 'PROCESSING'
  | 'COMPLETED'
  | 'FAILED'

export interface Resume {
  id: string
  label: string
  versionNo: number
  fileName: string
  contentType: string | null
  fileSizeBytes: number | null
  storageUrl: string
  primary: boolean
  parseStatus: ParseStatus
  parsedAt: string | null
  parseError: string | null
  notes: string | null
  createdAt: string
  updatedAt: string
  version: number
}

export interface CreateResumeRequest {
  label: string
  storageUrl: string
  fileName?: string | null
  contentType?: string | null
  fileSizeBytes?: number | null
  notes?: string | null
  primary?: boolean
}

export interface UpdateResumeRequest {
  label?: string
  notes?: string | null
  primary?: boolean
}
