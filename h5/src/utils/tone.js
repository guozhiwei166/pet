/** 依据字符串生成稳定的柔和底色对（用于插画封面轮换） */
const PALETTES = [
  ['#FFE9D6', '#FFD3B3'],
  ['#DFF2E8', '#BCE5D0'],
  ['#E3EDFD', '#C7DBF9'],
  ['#FFF1CE', '#FFE3A0'],
  ['#FDE7F0', '#F9CFDF'],
]

export function toneOf(seed) {
  let n = 0
  for (const c of String(seed || '')) n = (n * 31 + c.charCodeAt(0)) % 997
  return PALETTES[n % PALETTES.length]
}

export function toneStyle(seed) {
  const [a, b] = toneOf(seed)
  return { background: `linear-gradient(160deg, ${a} 0%, ${b} 100%)` }
}
