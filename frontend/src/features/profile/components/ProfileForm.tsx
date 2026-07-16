import { zodResolver } from '@hookform/resolvers/zod'
import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import {
  profileSchema,
  profileToFormValues,
  toProfileUpdateRequest,
  type ProfileFormValues,
} from '@/features/profile/schemas/profileSchema'
import type { CandidateProfile } from '@/types/user'
import type { ProfileUpdateRequest } from '@/types/user'

interface ProfileFormProps {
  profile?: CandidateProfile
  onSubmit: (values: ProfileUpdateRequest) => Promise<void>
  isSubmitting?: boolean
}

export function ProfileForm({ profile, onSubmit, isSubmitting }: ProfileFormProps) {
  const form = useForm<ProfileFormValues>({
    resolver: zodResolver(profileSchema),
    defaultValues: profileToFormValues(profile),
  })

  useEffect(() => {
    form.reset(profileToFormValues(profile))
  }, [profile, form])

  const handleSubmit = form.handleSubmit(async (values) => {
    await onSubmit(toProfileUpdateRequest(values))
  })

  return (
    <Card>
      <CardHeader>
        <CardTitle>Candidate Profile</CardTitle>
      </CardHeader>
      <CardContent>
        <Form {...form}>
          <form onSubmit={(event) => void handleSubmit(event)} className="grid gap-4 sm:grid-cols-2">
            <FormField
              control={form.control}
              name="targetRoles"
              render={({ field }) => (
                <FormItem className="sm:col-span-2">
                  <FormLabel>Target Roles</FormLabel>
                  <FormControl>
                    <Textarea
                      placeholder="Senior Java Developer, Staff Engineer"
                      rows={3}
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="targetSalaryMin"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Target Salary Min</FormLabel>
                  <FormControl>
                    <Input type="number" min={0} step="1000" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="targetSalaryMax"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Target Salary Max</FormLabel>
                  <FormControl>
                    <Input type="number" min={0} step="1000" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="skillsInput"
              render={({ field }) => (
                <FormItem className="sm:col-span-2">
                  <FormLabel>Skills</FormLabel>
                  <FormControl>
                    <Input placeholder="Java, Spring Boot, PostgreSQL" {...field} />
                  </FormControl>
                  <FormMessage />
                  <p className="text-xs text-muted-foreground">Separate skills with commas</p>
                </FormItem>
              )}
            />
            <div className="sm:col-span-2 flex justify-end">
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Saving...' : 'Save Profile'}
              </Button>
            </div>
          </form>
        </Form>
      </CardContent>
    </Card>
  )
}
