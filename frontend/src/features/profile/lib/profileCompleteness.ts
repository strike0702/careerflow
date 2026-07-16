import type { CandidateProfile } from '@/types/user'

/**
 * A profile is considered complete once the candidate has stated at least one
 * target role and one skill. Salary range remains optional.
 */
export function isProfileComplete(profile: CandidateProfile | undefined): boolean {
  if (!profile) return false
  return Boolean(profile.targetRoles?.trim()) && profile.skills.length > 0
}
