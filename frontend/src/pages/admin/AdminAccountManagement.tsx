import { useCallback, useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import {
  createAdminAccount,
  fetchAdminAccounts,
  setAdminAccountEnabled,
} from '../../lib/adminAccountsApi'
import type { AdminAccount } from '../../lib/adminAccountsApi'
import './AdminAccountManagement.css'

function AdminAccountManagement({ currentEmail }: { currentEmail: string }) {
  const [accounts, setAccounts] = useState<AdminAccount[]>([])
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isCreating, setIsCreating] = useState(false)
  const [busyAccountId, setBusyAccountId] = useState('')
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  const loadAccounts = useCallback(async () => {
    setError('')
    try {
      setAccounts(await fetchAdminAccounts())
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : 'Admin accounts could not be loaded.')
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    void loadAccounts()
  }, [loadAccounts])

  const addAccount = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setNotice('')
    setIsCreating(true)
    try {
      const created = await createAdminAccount(email.trim(), password)
      setAccounts((current) => [...current, created].sort((left, right) =>
        Number(right.enabled) - Number(left.enabled) || left.email.localeCompare(right.email)))
      setEmail('')
      setPassword('')
      setNotice(`Admin account created for ${created.email}. Share its password securely.`)
    } catch (createError) {
      setError(createError instanceof Error ? createError.message : 'Admin account could not be created.')
    } finally {
      setIsCreating(false)
    }
  }

  const toggleAccount = async (account: AdminAccount) => {
    setError('')
    setNotice('')
    setBusyAccountId(account.id)
    try {
      const updated = await setAdminAccountEnabled(account.id, !account.enabled)
      setAccounts((current) => current.map((item) => item.id === updated.id ? updated : item)
        .sort((left, right) => Number(right.enabled) - Number(left.enabled) || left.email.localeCompare(right.email)))
      setNotice(`${updated.email} ${updated.enabled ? 'activated' : 'deactivated'}.`)
    } catch (updateError) {
      setError(updateError instanceof Error ? updateError.message : 'Admin account status could not be changed.')
    } finally {
      setBusyAccountId('')
    }
  }

  return (
    <section className="admin-module-view admin-accounts-module" aria-labelledby="admin-page-title">
      <div className="admin-page-heading">
        <div>
          <p className="admin-eyebrow">ACCESS / MANAGEMENT</p>
          <h1 id="admin-page-title">Admin accounts<span>.</span></h1>
          <p>Manage admins with equal permissions and safeguards against deactivating the last active admin.</p>
        </div>
        <div className="admin-system-state"><span aria-hidden="true" /> DATABASE CONNECTED</div>
      </div>

      {(error || notice) && (
        <p className={error ? 'admin-account-feedback is-error' : 'admin-account-feedback'} role={error ? 'alert' : 'status'}>
          {error || notice}
        </p>
      )}

      <section className="admin-content-panel admin-account-form-panel" aria-labelledby="admin-account-form-title">
        <div className="admin-event-panel-heading">
          <div>
            <p className="admin-eyebrow">NEW ADMIN</p>
            <h2 id="admin-account-form-title">Add an account</h2>
          </div>
        </div>
        <form className="admin-account-form" onSubmit={(event) => void addAccount(event)}>
          <label>
            Admin email
            <input
              type="email"
              autoComplete="email"
              maxLength={254}
              required
              value={email}
              onChange={(event) => setEmail(event.target.value)}
            />
          </label>
          <label>
            Temporary password
            <input
              type="password"
              autoComplete="new-password"
              minLength={12}
              maxLength={72}
              required
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />
          </label>
          <div className="admin-account-form-actions">
            <button type="submit" disabled={isCreating}>
              {isCreating ? 'CREATING…' : 'CREATE ADMIN'}
            </button>
            <span>Use at least 12 characters. Passwords are stored as BCrypt hashes.</span>
          </div>
        </form>
      </section>

      <section className="admin-content-panel admin-account-list-panel" aria-labelledby="admin-account-list-title">
        <div className="admin-event-panel-heading">
          <div>
            <p className="admin-eyebrow">AUTHORIZED USERS</p>
            <h2 id="admin-account-list-title">{isLoading ? 'Loading…' : `${accounts.length} accounts`}</h2>
          </div>
        </div>
        {isLoading ? (
          <p role="status">Loading administrator accounts…</p>
        ) : accounts.length === 0 ? (
          <p className="admin-content-empty">No admin accounts are configured.</p>
        ) : (
          <ul className="admin-account-list">
            {accounts.map((account) => {
              const isCurrent = account.email.toLowerCase() === currentEmail.toLowerCase()
              return (
                <li key={account.id}>
                  <div className="admin-account-details">
                    <strong>{account.email}{isCurrent ? ' (you)' : ''}</strong>
                    <span>
                      {account.enabled ? 'Active' : 'Inactive'} · Added {new Intl.DateTimeFormat('en-IN', {
                        dateStyle: 'medium',
                        timeZone: 'Asia/Kolkata',
                      }).format(new Date(account.createdAt))}
                    </span>
                  </div>
                  <button
                    className={account.enabled ? 'is-danger' : 'is-activate'}
                    type="button"
                    disabled={busyAccountId === account.id || isCurrent}
                    title={isCurrent ? 'Sign in as another admin to deactivate this account.' : undefined}
                    onClick={() => void toggleAccount(account)}
                  >
                    {busyAccountId === account.id ? '…' : account.enabled ? 'DEACTIVATE' : 'ACTIVATE'}
                  </button>
                </li>
              )
            })}
          </ul>
        )}
      </section>
    </section>
  )
}

export default AdminAccountManagement
