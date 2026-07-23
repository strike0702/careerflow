import { useState } from 'react'
import { Calendar, CheckCircle2, Trash2 } from 'lucide-react'
import { toast } from 'sonner'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'
import { useInterviewMutations, useInterviews } from '@/features/interviews/hooks/useInterviews'
import { formatDateTime } from '@/lib/formatters'
import { ApiError } from '@/types/api'
import type { InterviewMode, RoundType } from '@/types/interview'

const ROUND_TYPES: RoundType[] = [
  'PHONE_SCREEN',
  'TECHNICAL',
  'SYSTEM_DESIGN',
  'BEHAVIORAL',
  'HIRING_MANAGER',
  'HR',
  'ONSITE',
]

const MODES: InterviewMode[] = ['REMOTE', 'ONSITE', 'PHONE']

interface InterviewPanelProps {
  applicationId: string
}

export function InterviewPanel({ applicationId }: InterviewPanelProps) {
  const { data, isLoading, refetch } = useInterviews({ applicationId, size: 50 })
  const { createMutation, updateStatusMutation, deleteMutation } = useInterviewMutations(applicationId)
  const [open, setOpen] = useState(false)
  const [roundType, setRoundType] = useState<RoundType>('TECHNICAL')
  const [mode, setMode] = useState<InterviewMode>('REMOTE')
  const [scheduledAt, setScheduledAt] = useState('')
  const [title, setTitle] = useState('')
  const [notes, setNotes] = useState('')

  const interviews = data?.content ?? []

  const handleCreate = async () => {
    try {
      await createMutation.mutateAsync({
        applicationId,
        roundType,
        mode,
        scheduledAt: new Date(scheduledAt).toISOString(),
        title: title || null,
        notes: notes || null,
      })
      toast.success('Interview scheduled')
      setOpen(false)
      setTitle('')
      setNotes('')
      setScheduledAt('')
    } catch (mutationError) {
      toast.error(mutationError instanceof ApiError ? mutationError.message : 'Failed to schedule interview')
    }
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle>Interview Rounds</CardTitle>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild>
            <Button size="sm">Schedule Round</Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Schedule Interview</DialogTitle>
            </DialogHeader>
            <div className="space-y-4">
              <div className="space-y-2">
                <Label>Round type</Label>
                <Select value={roundType} onValueChange={(v) => setRoundType(v as RoundType)}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    {ROUND_TYPES.map((type) => (
                      <SelectItem key={type} value={type}>{type.replaceAll('_', ' ')}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>Mode</Label>
                <Select value={mode} onValueChange={(v) => setMode(v as InterviewMode)}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    {MODES.map((m) => (
                      <SelectItem key={m} value={m}>{m}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="scheduledAt">Scheduled at</Label>
                <Input id="scheduledAt" type="datetime-local" value={scheduledAt} onChange={(e) => setScheduledAt(e.target.value)} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="title">Title</Label>
                <Input id="title" value={title} onChange={(e) => setTitle(e.target.value)} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="notes">Notes</Label>
                <Textarea id="notes" value={notes} onChange={(e) => setNotes(e.target.value)} />
              </div>
            </div>
            <DialogFooter>
              <Button onClick={() => void handleCreate()} disabled={!scheduledAt || createMutation.isPending}>
                Schedule
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </CardHeader>
      <CardContent className="space-y-4">
        {isLoading ? (
          <div className="h-24 animate-pulse rounded bg-muted" />
        ) : interviews.length === 0 ? (
          <p className="text-sm text-muted-foreground">No interviews scheduled for this application.</p>
        ) : (
          interviews.map((interview) => (
            <div key={interview.id} className="flex flex-col gap-2 rounded-lg border p-4 sm:flex-row sm:items-center sm:justify-between">
              <div className="space-y-1">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="font-medium">Round {interview.roundNumber}</span>
                  <Badge className="border">{interview.roundType.replaceAll('_', ' ')}</Badge>
                  <Badge>{interview.status}</Badge>
                </div>
                <p className="flex items-center gap-1 text-sm text-muted-foreground">
                  <Calendar className="h-3.5 w-3.5" />
                  {formatDateTime(interview.scheduledAt)} · {interview.mode}
                </p>
                {interview.notes && <p className="text-sm">{interview.notes}</p>}
              </div>
              <div className="flex gap-2">
                {interview.status === 'SCHEDULED' && (
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={async () => {
                      try {
                        await updateStatusMutation.mutateAsync({ id: interview.id, status: 'COMPLETED' })
                        toast.success('Interview marked complete')
                      } catch (mutationError) {
                        toast.error(mutationError instanceof ApiError ? mutationError.message : 'Update failed')
                      }
                    }}
                  >
                    <CheckCircle2 className="mr-1 h-3.5 w-3.5" />
                    Complete
                  </Button>
                )}
                <Button
                  variant="outline"
                  size="sm"
                  onClick={async () => {
                    try {
                      await deleteMutation.mutateAsync(interview.id)
                      toast.success('Interview deleted')
                      void refetch()
                    } catch (mutationError) {
                      toast.error(mutationError instanceof ApiError ? mutationError.message : 'Delete failed')
                    }
                  }}
                >
                  <Trash2 className="h-3.5 w-3.5" />
                </Button>
              </div>
            </div>
          ))
        )}
      </CardContent>
    </Card>
  )
}
