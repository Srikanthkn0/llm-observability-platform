import React, { createContext, useContext, useMemo, useState } from 'react'
import { api } from '../api/client'
import type { AuthUser } from '../types'

interface AuthContextValue {
  user: AuthUser | null
  login: (email: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

function load(): AuthUser | null {
  const raw = localStorage.getItem('llmobs_auth')
  if (!raw) return null
  try { return JSON.parse(raw) as AuthUser } catch { return null }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => load())

  const value = useMemo<AuthContextValue>(() => ({
    user,
    async login(email, password) {
      const res = await api<AuthUser>('/api/v1/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      })
      localStorage.setItem('llmobs_auth', JSON.stringify(res))
      setUser(res)
    },
    logout() {
      localStorage.removeItem('llmobs_auth')
      setUser(null)
    },
  }), [user])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth outside provider')
  return ctx
}
