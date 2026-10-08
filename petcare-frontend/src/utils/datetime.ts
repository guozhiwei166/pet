/**
 * 后端时间字段解析：JacksonConfig 把 LocalDateTime 序列化为 "yyyy-MM-dd HH:mm:ss"
 * （空格分隔，非 ISO）。Safari 等 WebKit 内核解析该格式会得到 Invalid Date，
 * 因此统一替换为 'T' 再交给 Date 解析。
 */
export function parseTime(s?: string | null): Date | null {
  if (!s) return null
  const d = new Date(s.trim().replace(' ', 'T'))
  return Number.isNaN(d.getTime()) ? null : d
}

/** yyyy-MM-dd HH:mm:ss → MM-dd HH:mm；无法解析时退化为字符串截取，避免显示 NaN */
export function formatTime(s?: string | null): string {
  if (!s) return ''
  const d = parseTime(s)
  const p = (x: number) => String(x).padStart(2, '0')
  if (!d) return s.slice(5, 16)
  return `${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

/** 是否与今天同一天（用于消息中心"今天/更早"分组） */
export function isToday(s?: string | null): boolean {
  const d = parseTime(s)
  return d ? d.toDateString() === new Date().toDateString() : false
}
