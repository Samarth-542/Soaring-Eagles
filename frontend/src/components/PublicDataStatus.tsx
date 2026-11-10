interface PublicDataStatusProps {
  isLoading: boolean
  isRefreshing: boolean
  error: boolean
  hasData: boolean
  errorMessage: string
  onRetry: () => void
}

function PublicDataStatus({
  isLoading,
  isRefreshing,
  error,
  hasData,
  errorMessage,
  onRetry,
}: PublicDataStatusProps) {
  if (isLoading) {
    return (
      <div className="public-data-skeleton" role="status" aria-label="Loading content">
        <span />
        <span />
        <span />
      </div>
    )
  }

  if (error) {
    return (
      <div className={`public-data-notice${hasData ? ' public-data-notice--stale' : ''}`} role={hasData ? 'status' : 'alert'}>
        <span>{hasData ? 'Showing saved content; the latest update could not be loaded.' : errorMessage}</span>
        <button type="button" onClick={onRetry}>Retry</button>
      </div>
    )
  }

  if (isRefreshing && hasData) {
    return <p className="public-data-refresh" role="status">Refreshing saved content…</p>
  }

  return null
}

export default PublicDataStatus
