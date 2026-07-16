import { zodResolver } from '@hookform/resolvers/zod'
import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  updateStatusSchema,
  type UpdateStatusFormValues,
} from '@/features/applications/schemas/updateStatusSchema'
import { APPLICATION_STATUSES } from '@/features/applications/schemas/createApplicationSchema'
import { APPLICATION_STATUS_LABELS } from '@/lib/constants'
import type { ApplicationStatus } from '@/types/application'

interface StatusUpdateDialogProps {
  open: boolean
  currentStatus: ApplicationStatus
  onOpenChange: (open: boolean) => void
  onSubmit: (status: ApplicationStatus) => Promise<void>
  isSubmitting?: boolean
}

export function StatusUpdateDialog({
  open,
  currentStatus,
  onOpenChange,
  onSubmit,
  isSubmitting,
}: StatusUpdateDialogProps) {
  const form = useForm<UpdateStatusFormValues>({
    resolver: zodResolver(updateStatusSchema),
    defaultValues: { status: currentStatus },
  })

  useEffect(() => {
    if (open) {
      form.reset({ status: currentStatus })
    }
  }, [open, currentStatus, form])

  const handleSubmit = form.handleSubmit(async (values) => {
    await onSubmit(values.status)
    onOpenChange(false)
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Update Status</DialogTitle>
          <DialogDescription>
            Change the application status. Any transition is allowed.
          </DialogDescription>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={(event) => void handleSubmit(event)} className="space-y-4">
            <FormField
              control={form.control}
              name="status"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>New Status</FormLabel>
                  <Select onValueChange={field.onChange} value={field.value}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="Select status" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {APPLICATION_STATUSES.map((status) => (
                        <SelectItem key={status} value={status}>
                          {APPLICATION_STATUS_LABELS[status]}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                Cancel
              </Button>
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Updating...' : 'Update Status'}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
