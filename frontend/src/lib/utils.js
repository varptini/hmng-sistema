import { clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'
import { format, formatDistanceToNow, isAfter, addDays } from 'date-fns'
import { es } from 'date-fns/locale'

export function cn(...inputs) {
  return twMerge(clsx(inputs))
}

export function formatDate(date) {
  if (!date) return '—'
  return format(new Date(date), 'dd/MM/yyyy', { locale: es })
}

export function formatDateTime(date) {
  if (!date) return '—'
  return format(new Date(date), 'dd/MM/yyyy HH:mm', { locale: es })
}

export function formatRelative(date) {
  if (!date) return '—'
  return formatDistanceToNow(new Date(date), { addSuffix: true, locale: es })
}

export function getEstadoCaducidadClass(estado) {
  const map = {
    vigente: 'badge-vigente',
    por_caducar: 'badge-por-caducar',
    caducado: 'badge-caducado',
  }
  return map[estado] || ''
}

export function getEstadoStockClass(estado) {
  const map = {
    normal: 'badge-normal',
    stock_bajo: 'badge-stock-bajo',
    agotado: 'badge-agotado',
  }
  return map[estado] || ''
}

export function getEstadoPedidoClass(estado) {
  const map = {
    pendiente: 'badge-pendiente',
    atendido: 'badge-atendido',
    cancelado: 'badge-cancelado',
    enviado: 'badge-enviado',
    recibido: 'badge-recibido',
  }
  return map[estado] || ''
}

export function formatNumber(n, decimals = 0) {
  if (n == null) return '0'
  return parseFloat(n).toLocaleString('es-MX', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  })
}
