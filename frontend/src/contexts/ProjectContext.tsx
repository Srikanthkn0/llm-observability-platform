import React, { createContext, useContext, useEffect, useState } from 'react'
import { api } from '../api/client'
import { useAuth } from './AuthContext'
import type { Project } from '../types'

interface ProjectContextValue {
  projects: Project[]
  project: Project | null
  setProjectId: (id: string) => void
  refresh: () => Promise<void>
}

const ProjectContext = createContext<ProjectContextValue | undefined>(undefined)

export function ProjectProvider({ children }: { children: React.ReactNode }) {
  const { user } = useAuth()
  const [projects, setProjects] = useState<Project[]>([])
  const [projectId, setProjectId] = useState<string | null>(localStorage.getItem('llmobs_project'))

  const refresh = async () => {
    if (!user) { setProjects([]); return }
    const list = await api<Project[]>('/api/v1/projects')
    setProjects(list)
    if (!projectId && list.length) {
      setProjectId(list[0].id)
      localStorage.setItem('llmobs_project', list[0].id)
    }
  }

  useEffect(() => { void refresh() }, [user])

  const project = projects.find(p => p.id === projectId) || projects[0] || null

  return (
    <ProjectContext.Provider value={{
      projects,
      project,
      setProjectId: (id) => {
        setProjectId(id)
        localStorage.setItem('llmobs_project', id)
      },
      refresh,
    }}>
      {children}
    </ProjectContext.Provider>
  )
}

export function useProject() {
  const ctx = useContext(ProjectContext)
  if (!ctx) throw new Error('useProject outside provider')
  return ctx
}
