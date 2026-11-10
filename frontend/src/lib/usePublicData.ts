import { useCallback, useEffect, useRef, useState } from 'react'
import { isPublicDataCacheFresh, loadPublicData, readPublicDataCache } from './publicDataCache'
import type { PublicDataKey } from './publicDataCache'

interface PublicDataState<T> {
  key: PublicDataKey
  data: T | null
  error: boolean
  isLoading: boolean
  isRefreshing: boolean
}

export function usePublicData<T>(
  key: PublicDataKey,
  maxAgeMs: number,
  fetcher: () => Promise<T>,
) {
  const initialCache = readPublicDataCache<T>(key)
  const initialData = initialCache?.data ?? null
  const [state, setState] = useState<PublicDataState<T>>({
    key,
    data: initialData,
    error: false,
    isLoading: initialData === null,
    isRefreshing: initialData !== null && !isPublicDataCacheFresh(key, maxAgeMs),
  })
  const activeKey = useRef(key)
  const mounted = useRef(false)
  const stateRef = useRef(state)
  activeKey.current = key
  stateRef.current = state

  const refresh = useCallback(async (forceRefresh = false) => {
    const cachedData = readPublicDataCache<T>(key)?.data ?? null
    const currentState = stateRef.current
    const previousData = currentState.key === key ? currentState.data : cachedData
    setState({
      key,
      data: previousData,
      error: false,
      isLoading: previousData === null,
      isRefreshing: previousData !== null,
    })

    try {
      const data = await loadPublicData(key, maxAgeMs, fetcher, forceRefresh)
      if (mounted.current && activeKey.current === key) {
        setState({ key, data, error: false, isLoading: false, isRefreshing: false })
      }
    } catch {
      if (mounted.current && activeKey.current === key) {
        setState((current) => ({
          key,
          data: current.key === key ? current.data : cachedData,
          error: true,
          isLoading: false,
          isRefreshing: false,
        }))
      }
    }
  }, [fetcher, key, maxAgeMs])

  useEffect(() => {
    mounted.current = true
    return () => {
      mounted.current = false
    }
  }, [])

  useEffect(() => {
    void refresh()
  }, [refresh])

  const retry = useCallback(() => refresh(true), [refresh])
  const currentCache = state.key === key ? null : readPublicDataCache<T>(key)
  const currentState = state.key === key
    ? state
    : {
      key,
      data: currentCache?.data ?? null,
      error: false,
      isLoading: currentCache === null,
      isRefreshing: currentCache !== null && !isPublicDataCacheFresh(key, maxAgeMs),
    }

  return { ...currentState, retry }
}
