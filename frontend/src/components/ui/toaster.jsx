import { useToast } from './use-toast'
import { X } from 'lucide-react'
import { cn } from '../../lib/utils'

export function Toaster() {
  const { toasts } = useToast()
  return (
    <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2 w-80">
      {toasts.map(({ id, title, description, variant, dismiss }) => (
        <div key={id}
          className={cn(
            'flex items-start gap-3 p-4 rounded-xl border shadow-lg animate-slide-in',
            variant === 'destructive' ? 'bg-red-50 border-red-200 text-red-800' : 'bg-card border text-foreground'
          )}>
          <div className="flex-1 min-w-0">
            {title && <p className="font-medium text-sm">{title}</p>}
            {description && <p className="text-xs text-muted-foreground mt-0.5">{description}</p>}
          </div>
          <button onClick={dismiss} className="shrink-0 p-0.5 hover:opacity-70">
            <X size={14} />
          </button>
        </div>
      ))}
    </div>
  )
}
