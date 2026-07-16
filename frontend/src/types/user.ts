export interface User {
  id: string
  email: string
  firstName: string
  lastName: string
  role: string
}

export interface CandidateProfile {
  userId: string
  targetRoles: string | null
  targetSalaryMin: number | null
  targetSalaryMax: number | null
  skills: string[]
}

export interface ProfileUpdateRequest {
  targetRoles?: string | null
  targetSalaryMin?: number | null
  targetSalaryMax?: number | null
  skills?: string[]
}
