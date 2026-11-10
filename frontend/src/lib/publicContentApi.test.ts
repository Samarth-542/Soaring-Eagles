// Runtime validation coverage for public snapshot reads.
import { afterEach, describe, expect, it, vi } from 'vitest'

afterEach(() => {
  vi.resetModules()
  vi.unstubAllEnvs()
  vi.unstubAllGlobals()
})

describe('public content snapshots', () => {
  it('fetches the immutable file selected by the public manifest without credentials', async () => {
    vi.stubEnv('VITE_PUBLIC_CONTENT_BASE_URL', 'https://storage.example/public-content')
    vi.stubGlobal('window', globalThis)
    const fetch = vi.fn()
      .mockResolvedValueOnce(new Response(JSON.stringify({
        version: 'v1',
        generatedAt: '2026-11-10T12:00:00Z',
        snapshots: {
          'team.json': { path: 'snapshots/v1/team.json', sha256: 'digest' },
        },
      }), { status: 200 }))
      .mockResolvedValueOnce(new Response(JSON.stringify({
        version: 'v1',
        generatedAt: '2026-11-10T12:00:00Z',
        data: { groups: [] },
      }), { status: 200 }))
    vi.stubGlobal('fetch', fetch)

    const { fetchPublicTeamSnapshot } = await import('./publicContentApi')
    const snapshot = await fetchPublicTeamSnapshot()

    expect(snapshot.data.groups).toEqual([])
    expect(fetch).toHaveBeenNthCalledWith(
      1,
      'https://storage.example/public-content/manifest.json',
      expect.objectContaining({ credentials: 'omit', cache: 'no-cache' }),
    )
    expect(fetch).toHaveBeenNthCalledWith(
      2,
      'https://storage.example/public-content/snapshots/v1/team.json',
      expect.objectContaining({ credentials: 'omit', cache: 'force-cache' }),
    )
  })

  it('rejects manifest paths that do not match the announced snapshot version', async () => {
    vi.stubEnv('VITE_PUBLIC_CONTENT_BASE_URL', 'https://storage.example/public-content')
    vi.stubGlobal('window', globalThis)
    const fetch = vi.fn().mockResolvedValueOnce(new Response(JSON.stringify({
      version: 'v1',
      generatedAt: '2026-11-10T12:00:00Z',
      snapshots: {
        'team.json': { path: 'snapshots/v2/team.json', sha256: 'digest' },
      },
    }), { status: 200 }))
    vi.stubGlobal('fetch', fetch)

    const { fetchPublicTeamSnapshot } = await import('./publicContentApi')

    await expect(fetchPublicTeamSnapshot())
      .rejects.toThrow('does not contain a valid team.json snapshot')
    expect(fetch).toHaveBeenCalledTimes(1)
  })

  it('rejects a team snapshot without the directory groups array', async () => {
    vi.stubEnv('VITE_PUBLIC_CONTENT_BASE_URL', 'https://storage.example/public-content')
    vi.stubGlobal('window', globalThis)
    const fetch = vi.fn()
      .mockResolvedValueOnce(new Response(JSON.stringify({
        version: 'v1',
        generatedAt: '2026-11-10T12:00:00Z',
        snapshots: {
          'team.json': { path: 'snapshots/v1/team.json', sha256: 'digest' },
        },
      }), { status: 200 }))
      .mockResolvedValueOnce(new Response(JSON.stringify({
        version: 'v1',
        generatedAt: '2026-11-10T12:00:00Z',
        data: [],
      }), { status: 200 }))
    vi.stubGlobal('fetch', fetch)

    const { fetchPublicTeamSnapshot } = await import('./publicContentApi')

    await expect(fetchPublicTeamSnapshot())
      .rejects.toThrow('invalid directory format')
  })
})
