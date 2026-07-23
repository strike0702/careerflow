import { apiClient } from '@/services/apiClient'
import type { CreateResumeRequest, Resume, UpdateResumeRequest } from '@/types/resume'

export async function fetchResumes(): Promise<Resume[]> {
  const { data } = await apiClient.get<Resume[]>('/api/v1/resumes')
  return data
}

export async function createResume(request: CreateResumeRequest): Promise<Resume> {
  const { data } = await apiClient.post<Resume>('/api/v1/resumes', request)
  return data
}

export async function updateResume(id: string, request: UpdateResumeRequest): Promise<Resume> {
  const { data } = await apiClient.put<Resume>(`/api/v1/resumes/${id}`, request)
  return data
}

export async function deleteResume(id: string): Promise<void> {
  await apiClient.delete(`/api/v1/resumes/${id}`)
}

export async function setPrimaryResume(id: string): Promise<Resume> {
  const { data } = await apiClient.put<Resume>(`/api/v1/resumes/${id}/primary`)
  return data
}
