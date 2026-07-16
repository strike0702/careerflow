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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'
import {
  OFFER_STATUSES,
  offerToFormValues,
  toUpsertOfferRequest,
  upsertOfferSchema,
  type UpsertOfferFormValues,
} from '@/features/applications/schemas/upsertOfferSchema'
import { OFFER_STATUS_LABELS } from '@/lib/constants'
import type { Offer } from '@/types/offer'
import type { UpsertOfferRequest } from '@/types/offer'

interface OfferFormProps {
  offer?: Offer | null
  onSubmit: (values: UpsertOfferRequest) => Promise<void>
  isSubmitting?: boolean
}

export function OfferForm({ offer, onSubmit, isSubmitting }: OfferFormProps) {
  const form = useForm<UpsertOfferFormValues>({
    resolver: zodResolver(upsertOfferSchema),
    defaultValues: offerToFormValues(offer),
  })

  useEffect(() => {
    form.reset(offerToFormValues(offer))
  }, [offer, form])

  const handleSubmit = form.handleSubmit(async (values) => {
    await onSubmit(toUpsertOfferRequest(values))
  })

  return (
    <Card>
      <CardHeader>
        <CardTitle>{offer ? 'Update Offer' : 'Add Offer'}</CardTitle>
      </CardHeader>
      <CardContent>
        <Form {...form}>
          <form onSubmit={(event) => void handleSubmit(event)} className="grid gap-4 sm:grid-cols-2">
            <FormField
              control={form.control}
              name="baseSalary"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Base Salary</FormLabel>
                  <FormControl>
                    <Input type="number" min={0} step="1000" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="joiningBonus"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Joining Bonus</FormLabel>
                  <FormControl>
                    <Input type="number" min={0} step="1000" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="annualBonus"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Annual Bonus</FormLabel>
                  <FormControl>
                    <Input type="number" min={0} step="1000" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="stockValue"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Stock Value</FormLabel>
                  <FormControl>
                    <Input type="number" min={0} step="1000" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="currency"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Currency *</FormLabel>
                  <FormControl>
                    <Input
                      placeholder="INR"
                      maxLength={3}
                      {...field}
                      onChange={(event) => field.onChange(event.target.value.toUpperCase())}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="joiningDate"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Joining Date</FormLabel>
                  <FormControl>
                    <Input type="date" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="offerStatus"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Offer Status</FormLabel>
                  <Select onValueChange={field.onChange} value={field.value}>
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="Select status" />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {OFFER_STATUSES.map((status) => (
                        <SelectItem key={status} value={status}>
                          {OFFER_STATUS_LABELS[status]}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="notes"
              render={({ field }) => (
                <FormItem className="sm:col-span-2">
                  <FormLabel>Notes</FormLabel>
                  <FormControl>
                    <Textarea rows={3} placeholder="Offer notes..." {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <div className="sm:col-span-2 flex justify-end">
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Saving...' : offer ? 'Update Offer' : 'Save Offer'}
              </Button>
            </div>
          </form>
        </Form>
      </CardContent>
    </Card>
  )
}
