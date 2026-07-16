import { z } from 'zod'

export const profileSchema = z
  .object({
    targetRoles: z.string().optional(),
    targetSalaryMin: z.string().optional(),
    targetSalaryMax: z.string().optional(),
    skillsInput: z.string().optional(),
  })
  .superRefine((data, ctx) => {
    const min = data.targetSalaryMin?.trim() ? Number(data.targetSalaryMin) : undefined
    const max = data.targetSalaryMax?.trim() ? Number(data.targetSalaryMax) : undefined

    if (min !== undefined && Number.isNaN(min)) {
      ctx.addIssue({
        code: 'custom',
        message: 'Must be a valid number',
        path: ['targetSalaryMin'],
      })
    } else if (min !== undefined && min < 0) {
      ctx.addIssue({
        code: 'custom',
        message: 'Must be 0 or greater',
        path: ['targetSalaryMin'],
      })
    }

    if (max !== undefined && Number.isNaN(max)) {
      ctx.addIssue({
        code: 'custom',
        message: 'Must be a valid number',
        path: ['targetSalaryMax'],
      })
    } else if (max !== undefined && max < 0) {
      ctx.addIssue({
        code: 'custom',
        message: 'Must be 0 or greater',
        path: ['targetSalaryMax'],
      })
    }

    if (
      min !== undefined &&
      max !== undefined &&
      !Number.isNaN(min) &&
      !Number.isNaN(max) &&
      min > max
    ) {
      ctx.addIssue({
        code: 'custom',
        message: 'Minimum salary cannot exceed maximum salary',
        path: ['targetSalaryMax'],
      })
    }
  })

export type ProfileFormValues = z.infer<typeof profileSchema>

export function parseSkillsInput(input?: string): string[] {
  if (!input?.trim()) return []
  return input
    .split(',')
    .map((skill) => skill.trim())
    .filter(Boolean)
}

export function formatSkillsForInput(skills: string[]): string {
  return skills.join(', ')
}

export function toProfileUpdateRequest(values: ProfileFormValues) {
  const min = values.targetSalaryMin?.trim() ? Number(values.targetSalaryMin) : undefined
  const max = values.targetSalaryMax?.trim() ? Number(values.targetSalaryMax) : undefined

  return {
    targetRoles: values.targetRoles || null,
    targetSalaryMin: min ?? null,
    targetSalaryMax: max ?? null,
    skills: parseSkillsInput(values.skillsInput),
  }
}

export function profileToFormValues(profile?: {
  targetRoles: string | null
  targetSalaryMin: number | null
  targetSalaryMax: number | null
  skills: string[]
}): ProfileFormValues {
  return {
    targetRoles: profile?.targetRoles ?? '',
    targetSalaryMin: profile?.targetSalaryMin != null ? String(profile.targetSalaryMin) : '',
    targetSalaryMax: profile?.targetSalaryMax != null ? String(profile.targetSalaryMax) : '',
    skillsInput: formatSkillsForInput(profile?.skills ?? []),
  }
}
