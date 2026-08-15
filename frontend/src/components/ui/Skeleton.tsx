export function Skeleton({ className }: { className?: string }) {
  return <div className={`skeleton rounded-[2px] ${className ?? ''}`} />
}

export function CardSkeleton() {
  return (
    <div className="border border-ink-200 bg-paper-raised p-6">
      <Skeleton className="mb-3 h-5 w-2/3" />
      <Skeleton className="mb-2 h-3.5 w-full" />
      <Skeleton className="mb-4 h-3.5 w-4/5" />
      <Skeleton className="h-9 w-full" />
    </div>
  )
}

export function RowSkeleton() {
  return (
    <div className="flex items-center justify-between border border-ink-200 bg-paper-raised px-5 py-4">
      <div className="flex items-center gap-3">
        <Skeleton className="size-9" />
        <div>
          <Skeleton className="mb-2 h-3.5 w-40" />
          <Skeleton className="h-3 w-24" />
        </div>
      </div>
      <Skeleton className="h-5 w-20" />
    </div>
  )
}
