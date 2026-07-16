const STORAGE_KEY_PREFIX = 'careerflow.profile-banner.dismissed:'

export function isProfileBannerDismissed(userId: string): boolean {
  return localStorage.getItem(`${STORAGE_KEY_PREFIX}${userId}`) === 'true'
}

export function dismissProfileBanner(userId: string): void {
  localStorage.setItem(`${STORAGE_KEY_PREFIX}${userId}`, 'true')
}
