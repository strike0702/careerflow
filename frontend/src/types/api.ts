export interface ProblemDetail {
  type?: string
  title?: string
  status?: number
  detail?: string
  requestId?: string
}

export class ApiError extends Error {
  status: number
  title?: string
  requestId?: string

  constructor(problem: ProblemDetail) {
    super(problem.detail ?? problem.title ?? 'An unexpected error occurred')
    this.name = 'ApiError'
    this.status = problem.status ?? 500
    this.title = problem.title
    this.requestId = problem.requestId
  }
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}
