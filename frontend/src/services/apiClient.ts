import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { ApiError, type ProblemDetail } from '@/types/api'
import { ensureFreshToken, keycloak, login } from '@/services/keycloak'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? ''

export const apiClient = axios.create({
  baseURL: apiBaseUrl,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.request.use(async (config: InternalAxiosRequestConfig) => {
  const token = await ensureFreshToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  config.headers['X-Request-ID'] = crypto.randomUUID()
  return config
})

let isRefreshing = false

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ProblemDetail>) => {
    const originalRequest = error.config

    if (error.response?.status === 401 && originalRequest && !originalRequest.headers['X-Retry']) {
      if (!isRefreshing) {
        isRefreshing = true
        try {
          await keycloak.updateToken(-1)
          isRefreshing = false
          originalRequest.headers['X-Retry'] = 'true'
          originalRequest.headers.Authorization = `Bearer ${keycloak.token}`
          return apiClient(originalRequest)
        } catch {
          isRefreshing = false
          await login()
          return Promise.reject(error)
        }
      }
    }

    if (error.response?.data) {
      const problem = error.response.data
      if (problem.detail || problem.title) {
        throw new ApiError({
          ...problem,
          status: problem.status ?? error.response.status,
        })
      }
    }

    throw new ApiError({
      status: error.response?.status ?? 500,
      title: 'Request Failed',
      detail: error.message,
    })
  },
)
