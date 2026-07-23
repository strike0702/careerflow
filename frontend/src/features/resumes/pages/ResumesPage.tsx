import { useState } from 'react'
import { ExternalLink, FileText, Plus, Star, Trash2 } from 'lucide-react'
import { toast } from 'sonner'
import { PageHeader } from '@/components/common/PageHeader'
import { EmptyState, ErrorState } from '@/components/common/StateViews'
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
import { Textarea } from '@/components/ui/textarea'
import { useResumeMutations, useResumes } from '@/features/resumes/hooks/useResumes'
import { formatDateTime } from '@/lib/formatters'
import { ApiError } from '@/types/api'

function isValidUrl(value: string): boolean {
  try {
    new URL(value)
    return true
  } catch {
    return false
  }
}

export function ResumesPage() {
  const { data, isLoading, isError, error, refetch } = useResumes()
  const { createMutation, deleteMutation, setPrimaryMutation } = useResumeMutations()
  const [open, setOpen] = useState(false)
  const [label, setLabel] = useState('')
  const [storageUrl, setStorageUrl] = useState('')
  const [fileName, setFileName] = useState('')
  const [notes, setNotes] = useState('')

  const handleCreate = async () => {
    try {
      await createMutation.mutateAsync({
        label,
        storageUrl,
        fileName: fileName.trim() || null,
        contentType: 'application/pdf',
        notes: notes || null,
        primary: (data?.length ?? 0) === 0,
      })
      toast.success('Resume version created')
      setOpen(false)
      setLabel('')
      setStorageUrl('')
      setFileName('')
      setNotes('')
    } catch (mutationError) {
      toast.error(mutationError instanceof ApiError ? mutationError.message : 'Failed to create resume')
    }
  }

  if (isError) {
    return (
      <div className="space-y-6">
        <PageHeader title="Resumes" description="Manage resume versions for your applications" />
        <ErrorState message={error.message} onRetry={() => void refetch()} />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Resumes"
        description="Manage resume versions for your applications"
        action={
          <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="h-4 w-4" />
                Add Resume
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Add Resume Version</DialogTitle>
              </DialogHeader>
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="label">Label</Label>
                  <Input id="label" value={label} onChange={(e) => setLabel(e.target.value)} placeholder="Backend v2" />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="storageUrl">Storage URL *</Label>
                  <Input
                    id="storageUrl"
                    value={storageUrl}
                    onChange={(e) => setStorageUrl(e.target.value)}
                    placeholder="https://drive.google.com/file/d/..."
                  />
                  <p className="text-xs text-muted-foreground">Paste your Google Drive or other share link.</p>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="fileName">File name (optional)</Label>
                  <Input
                    id="fileName"
                    value={fileName}
                    onChange={(e) => setFileName(e.target.value)}
                    placeholder="resume.pdf"
                  />
                  <p className="text-xs text-muted-foreground">Auto-derived from the URL when left empty.</p>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="notes">Notes</Label>
                  <Textarea id="notes" value={notes} onChange={(e) => setNotes(e.target.value)} />
                </div>
              </div>
              <DialogFooter>
                <Button
                  onClick={() => void handleCreate()}
                  disabled={!label || !storageUrl || !isValidUrl(storageUrl) || createMutation.isPending}
                >
                  Create
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        }
      />

      {isLoading ? (
        <div className="h-48 animate-pulse rounded-lg bg-muted" />
      ) : !data?.length ? (
        <EmptyState
          icon={<FileText className="h-10 w-10" />}
          title="No resumes yet"
          description="Register a resume version to link it with job applications."
        />
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {data.map((resume) => (
            <Card key={resume.id}>
              <CardHeader className="flex flex-row items-start justify-between space-y-0">
                <div>
                  <CardTitle className="text-base">{resume.label}</CardTitle>
                  <p className="text-sm text-muted-foreground">v{resume.versionNo} · {resume.fileName}</p>
                </div>
                {resume.primary && <Badge>Primary</Badge>}
              </CardHeader>
              <CardContent className="space-y-3">
                <p className="text-xs text-muted-foreground">Added {formatDateTime(resume.createdAt)}</p>
                <a
                  href={resume.storageUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1 text-sm text-primary hover:underline"
                >
                  Open Resume
                  <ExternalLink className="h-3.5 w-3.5" />
                </a>
                {resume.notes && <p className="text-sm">{resume.notes}</p>}
                <div className="flex gap-2">
                  {!resume.primary && (
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => void setPrimaryMutation.mutateAsync(resume.id)}
                      disabled={setPrimaryMutation.isPending}
                    >
                      <Star className="mr-1 h-3.5 w-3.5" />
                      Set primary
                    </Button>
                  )}
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={async () => {
                      try {
                        await deleteMutation.mutateAsync(resume.id)
                        toast.success('Resume deleted')
                      } catch (mutationError) {
                        toast.error(mutationError instanceof ApiError ? mutationError.message : 'Delete failed')
                      }
                    }}
                    disabled={deleteMutation.isPending}
                  >
                    <Trash2 className="mr-1 h-3.5 w-3.5" />
                    Delete
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
