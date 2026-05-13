import { useState, useCallback } from 'react'

let listeners = []
let toasts = []

function dispatch(toast) {
  toasts = [...toasts, toast]
  listeners.forEach(l => l(toasts))
}

export function toast({ title, description, variant = 'default', duration = 4000 }) {
  const id = Date.now().toString()
  const dismiss = () => {
    toasts = toasts.filter(t => t.id !== id)
    listeners.forEach(l => l(toasts))
  }
  dispatch({ id, title, description, variant, dismiss })
  setTimeout(dismiss, duration)
}

export function useToast() {
  const [t, setT] = useState(toasts)
  const listen = useCallback((fn) => {
    listeners.push(fn)
    return () => { listeners = listeners.filter(l => l !== fn) }
  }, [])

  useState(() => {
    const unsub = listen(setT)
    return unsub
  })

  return { toasts: t, toast }
}
