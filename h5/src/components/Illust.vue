<script setup>
/**
 * 品牌插画组件：统一扁平萌系风格的 SVG 场景
 * scene: hero(猫狗草原) / house(犬舍) / tower(猫屋) / shop(店面) / sleep(安睡) / box(笼位)
 */
defineProps({
  scene: { type: String, default: 'box' },
})

/* 依据字符串生成稳定柔和的背景色（用于封面轮换） */
const palette = [
  ['#FFE9D6', '#FFD3B3'],
  ['#DFF2E8', '#BCE5D0'],
  ['#E3EDFD', '#C7DBF9'],
  ['#FFF1CE', '#FFE3A0'],
  ['#FDE7F0', '#F9CFDF'],
]
function bgTones(seed) {
  let n = 0
  for (const c of String(seed || '')) n = (n * 31 + c.charCodeAt(0)) % 997
  return palette[n % palette.length]
}
defineExpose({ bgTones })
</script>

<template>
  <!-- 场景一：草原上的狗与猫（登录页 / 首页横幅） -->
  <svg v-if="scene === 'hero'" viewBox="0 0 360 230" class="ill" aria-hidden="true">
    <circle cx="304" cy="46" r="30" fill="#FFD37A" />
    <circle cx="304" cy="46" r="44" fill="#FFD37A" opacity=".28" />
    <g fill="#FFFFFF" opacity=".92">
      <ellipse cx="72" cy="46" rx="30" ry="13" />
      <ellipse cx="98" cy="38" rx="22" ry="11" />
      <ellipse cx="250" cy="86" rx="24" ry="10" />
    </g>
    <path d="M-10 168 Q 90 118 200 156 T 372 150 L 372 232 L -10 232 Z" fill="#B9E2C8" />
    <path d="M-10 196 Q 120 156 250 192 T 372 188 L 372 232 L -10 232 Z" fill="#96D1AC" />
    <g stroke="#63AE85" stroke-width="3" stroke-linecap="round">
      <path d="M46 200 q 2 -12 -4 -16 M52 200 q 0 -13 6 -17 M58 200 q 3 -9 10 -10" fill="none" />
      <path d="M300 208 q 2 -12 -4 -16 M306 208 q 0 -13 6 -17 M312 208 q 3 -9 10 -10" fill="none" />
    </g>
    <!-- 小狗 -->
    <g>
      <ellipse cx="118" cy="205" rx="52" ry="9" fill="#5B4636" opacity=".10" />
      <path d="M158 168 q 20 -14 14 -30" fill="none" stroke="#D9772F" stroke-width="9" stroke-linecap="round" />
      <ellipse cx="120" cy="172" rx="44" ry="31" fill="#F4A259" />
      <path d="M76 176 a 44 31 0 0 0 20 26 v -26 Z" fill="#E8944B" />
      <circle cx="120" cy="122" r="35" fill="#F4A259" />
      <path d="M92 96 q -14 4 -12 26 q 1 12 10 12 q 8 0 10 -12 q 2 -16 -8 -26 Z" fill="#D9772F" />
      <path d="M148 96 q 14 4 12 26 q -1 12 -10 12 q -8 0 -10 -12 q -2 -16 8 -26 Z" fill="#D9772F" />
      <ellipse cx="120" cy="136" rx="18" ry="13" fill="#FFE9D2" />
      <circle cx="106" cy="114" r="3.6" fill="#4A3B2F" />
      <circle cx="134" cy="114" r="3.6" fill="#4A3B2F" />
      <circle cx="107.4" cy="112.6" r="1.2" fill="#fff" />
      <circle cx="135.4" cy="112.6" r="1.2" fill="#fff" />
      <circle cx="120" cy="130" r="4.6" fill="#5B4636" />
      <path d="M120 134.6 q 0 6 6 7 M120 134.6 q 0 6 -6 7" fill="none" stroke="#5B4636" stroke-width="2.2" stroke-linecap="round" />
      <circle cx="94" cy="126" r="5.5" fill="#FFAF87" opacity=".6" />
      <circle cx="146" cy="126" r="5.5" fill="#FFAF87" opacity=".6" />
      <rect x="100" y="150" width="40" height="10" rx="5" fill="#E85B5B" />
      <circle cx="120" cy="160" r="4" fill="#FFD37A" />
      <ellipse cx="94" cy="200" rx="11" ry="7" fill="#FFE9D2" />
      <ellipse cx="146" cy="200" rx="11" ry="7" fill="#FFE9D2" />
    </g>
    <!-- 小猫 -->
    <g>
      <ellipse cx="248" cy="207" rx="46" ry="9" fill="#5B4636" opacity=".10" />
      <path d="M284 196 q 22 4 22 -18 q 0 -12 -12 -14" fill="none" stroke="#7E8CA0" stroke-width="10" stroke-linecap="round" />
      <path d="M216 200 q 0 -34 32 -34 q 32 0 32 34 Z" fill="#98A6B8" />
      <g stroke="#7E8CA0" stroke-width="4" stroke-linecap="round">
        <path d="M238 180 h 12 M236 190 h 14" />
      </g>
      <circle cx="248" cy="140" r="29" fill="#98A6B8" />
      <path d="M226 122 l -6 -20 l 22 8 Z" fill="#98A6B8" />
      <path d="M270 122 l 6 -20 l -22 8 Z" fill="#98A6B8" />
      <path d="M228 116 l -3 -9 l 10 4 Z" fill="#F5B8C4" />
      <path d="M268 116 l 3 -9 l -10 4 Z" fill="#F5B8C4" />
      <path d="M240 142 q 3 3 6 0 M252 142 q 3 3 6 0" fill="none" stroke="#4A3B2F" stroke-width="2.4" stroke-linecap="round" />
      <path d="M246 148 l 2.6 3.4 l 2.6 -3.4 Z" fill="#E88AA0" />
      <path d="M294 168 q 8 1 8 8" fill="none" stroke="#7E8CA0" stroke-width="10" stroke-linecap="round" opacity="0" />
      <ellipse cx="230" cy="204" rx="10" ry="6" fill="#E6ECF4" />
      <ellipse cx="266" cy="204" rx="10" ry="6" fill="#E6ECF4" />
    </g>
    <g fill="#FF9DB5">
      <path d="M172 62 c 3 -5 11 -3 11 3 c 0 5 -6 8 -11 11 c -5 -3 -11 -6 -11 -11 c 0 -6 8 -8 11 -3 Z" opacity=".9" />
      <path d="M292 108 c 2.4 -4 8.6 -2.4 8.6 2.4 c 0 4 -4.6 6.2 -8.6 8.6 c -4 -2.4 -8.6 -4.6 -8.6 -8.6 c 0 -4.8 6.2 -6.4 8.6 -2.4 Z" opacity=".75" />
    </g>
  </svg>

  <!-- 场景二：犬舍 -->
  <svg v-else-if="scene === 'house'" viewBox="0 0 160 120" class="ill" aria-hidden="true">
    <ellipse cx="80" cy="106" rx="56" ry="8" fill="#5B4636" opacity=".10" />
    <rect x="34" y="52" width="92" height="52" rx="10" fill="#FFF3E4" />
    <path d="M22 56 L 80 16 L 138 56 q 2 6 -6 6 L 28 62 q -8 0 -6 -6 Z" fill="#FF8A5C" />
    <path d="M62 104 v -22 a 18 18 0 0 1 36 0 v 22 Z" fill="#C97B4A" />
    <circle cx="80" cy="86" r="4.4" fill="#FFE3C2" />
    <circle cx="44" cy="42" r="12" fill="#FFE0C2" />
    <g fill="#C97B4A">
      <ellipse cx="40.5" cy="39" rx="2" ry="2.6" />
      <ellipse cx="47.5" cy="39" rx="2" ry="2.6" />
      <path d="M44 43 c 3.4 0 5.6 1.8 5.6 3.8 c 0 1.5 -1.3 2.4 -2.7 2.4 c -1 0 -2 -.4 -2.9 -.4 c -.9 0 -1.9 .4 -2.9 .4 c -1.4 0 -2.7 -.9 -2.7 -2.4 c 0 -2 2.2 -3.8 5.6 -3.8 Z" />
    </g>
    <g transform="rotate(-18 126 96)">
      <rect x="112" y="92" width="26" height="8" rx="4" fill="#fff" />
      <circle cx="112" cy="93.5" r="5" fill="#fff" />
      <circle cx="112" cy="98.5" r="5" fill="#fff" />
      <circle cx="138" cy="93.5" r="5" fill="#fff" />
      <circle cx="138" cy="98.5" r="5" fill="#fff" />
    </g>
    <path d="M140 104 q 1 -9 -5 -12 M144 104 q 3 -8 9 -9" fill="none" stroke="#63AE85" stroke-width="3" stroke-linecap="round" />
  </svg>

  <!-- 场景三：猫爬架 -->
  <svg v-else-if="scene === 'tower'" viewBox="0 0 160 120" class="ill" aria-hidden="true">
    <ellipse cx="80" cy="106" rx="56" ry="8" fill="#5B4636" opacity=".10" />
    <rect x="32" y="58" width="96" height="46" rx="11" fill="#EAF1FB" />
    <rect x="46" y="20" width="66" height="42" rx="11" fill="#DCE7F8" />
    <path d="M62 104 v -16 a 18 18 0 0 1 36 0 v 16 Z" fill="#B9CBE4" />
    <path d="M68 92 q 4 -6 9 -6 M84 88 q 5 0 8 5" fill="none" stroke="#98A6B8" stroke-width="2.4" stroke-linecap="round" />
    <circle cx="70" cy="90" r="2.2" fill="#4A3B2F" />
    <circle cx="88" cy="90" r="2.2" fill="#4A3B2F" />
    <path d="M74 40 h 10 M72 48 h 14" stroke="#C4D2E8" stroke-width="4" stroke-linecap="round" />
    <line x1="106" y1="20" x2="106" y2="8" stroke="#C4D2E8" stroke-width="2.5" />
    <circle cx="106" cy="14" r="0" />
    <circle cx="106" cy="16" r="0" />
    <g transform="rotate(12 112 26)">
      <line x1="112" y1="12" x2="112" y2="22" stroke="#C4D2E8" stroke-width="2.5" />
      <circle cx="112" cy="28" r="6.5" fill="#FFB637" />
    </g>
    <ellipse cx="26" cy="104" rx="10" ry="12" fill="#EAF1FB" />
    <path d="M20 96 q 6 -8 12 0" fill="none" stroke="#C4D2E8" stroke-width="3" stroke-linecap="round" />
  </svg>

  <!-- 场景四：店面 -->
  <svg v-else-if="scene === 'shop'" viewBox="0 0 160 120" class="ill" aria-hidden="true">
    <ellipse cx="80" cy="106" rx="58" ry="8" fill="#5B4636" opacity=".10" />
    <rect x="30" y="44" width="100" height="60" rx="10" fill="#FFF7EF" />
    <path d="M24 34 h 112 v 10 a 9 9 0 0 1 -18 0 a 9 9 0 0 1 -19 0 a 9 9 0 0 1 -19 0 a 9 9 0 0 1 -19 0 a 9 9 0 0 1 -19 0 a 9 9 0 0 1 -18 0 Z" fill="#FF8A5C" />
    <g fill="#FFE0C2">
      <path d="M43 34 h 19 v 10 a 9.5 9.5 0 0 1 -19 0 Z" />
      <path d="M81 34 h 19 v 10 a 9.5 9.5 0 0 1 -19 0 Z" opacity=".9" />
      <path d="M24 34 h 19 v 10 a 9 9 0 0 1 -18 0 Z" opacity=".8" />
    </g>
    <rect x="42" y="62" width="30" height="26" rx="7" fill="#BFE0EF" />
    <rect x="88" y="62" width="26" height="42" rx="7" fill="#F0D9C0" />
    <circle cx="107" cy="84" r="2.4" fill="#C97B4A" />
    <circle cx="57" cy="40" r="0" />
    <g transform="translate(93 8)">
      <circle cx="0" cy="12" r="13" fill="#FFE0C2" />
      <g fill="#C97B4A">
        <ellipse cx="-4.5" cy="8.5" rx="2.1" ry="2.7" />
        <ellipse cx="4.5" cy="8.5" rx="2.1" ry="2.7" />
        <ellipse cx="-8.5" cy="13.5" rx="1.9" ry="2.4" />
        <ellipse cx="8.5" cy="13.5" rx="1.9" ry="2.4" />
        <path d="M0 13 c 3.6 0 6 2 6 4.2 c 0 1.6 -1.4 2.6 -2.9 2.6 c -1.1 0 -2 -.5 -3.1 -.5 c -1.1 0 -2 .5 -3.1 .5 c -1.5 0 -2.9 -1 -2.9 -2.6 c 0 -2.2 2.4 -4.2 6 -4.2 Z" />
      </g>
    </g>
    <g fill="#63AE85">
      <path d="M34 104 q -2 -12 8 -16 q 2 10 -8 16 Z" />
      <path d="M38 104 q 6 -8 14 -6 q -4 8 -14 6 Z" />
    </g>
  </svg>

  <!-- 场景五：安睡（空状态） -->
  <svg v-else-if="scene === 'sleep'" viewBox="0 0 160 120" class="ill" aria-hidden="true">
    <circle cx="118" cy="34" r="20" fill="#FFD37A" />
    <path d="M112 18 a 20 20 0 1 0 22 28 a 16 16 0 1 1 -22 -28 Z" fill="#FFC24B" opacity="0" />
    <ellipse cx="78" cy="94" rx="50" ry="8" fill="#5B4636" opacity=".10" />
    <path d="M44 88 q -4 -26 34 -26 q 38 0 34 26 q -2 8 -12 8 l -44 0 q -10 0 -12 -8 Z" fill="#98A6B8" />
    <circle cx="112" cy="66" r="17" fill="#98A6B8" />
    <path d="M100 54 l -3 -12 l 12 5 Z" fill="#98A6B8" />
    <path d="M124 54 l 3 -12 l -12 5 Z" fill="#98A6B8" />
    <path d="M106 68 q 2.4 2.4 4.8 0 M118 68 q 2.4 2.4 4.8 0" fill="none" stroke="#4A3B2F" stroke-width="2" stroke-linecap="round" />
    <path d="M60 76 h 10 M58 84 h 12" stroke="#7E8CA0" stroke-width="4" stroke-linecap="round" />
    <g fill="#A39A91" font-family="sans-serif" font-weight="700">
      <text x="128" y="46" font-size="13">z</text>
      <text x="138" y="34" font-size="17">z</text>
      <text x="150" y="20" font-size="21">z</text>
    </g>
  </svg>

  <!-- 默认：笼位小场景 -->
  <svg v-else viewBox="0 0 160 120" class="ill" aria-hidden="true">
    <ellipse cx="80" cy="102" rx="50" ry="8" fill="#5B4636" opacity=".10" />
    <rect x="34" y="38" width="92" height="62" rx="12" fill="#FFF3E4" />
    <rect x="42" y="46" width="76" height="46" rx="8" fill="#FFE4CC" />
    <g stroke="#E8B98E" stroke-width="3.4" stroke-linecap="round">
      <path d="M42 56 h 76 M42 68 h 76 M42 80 h 76" />
    </g>
    <circle cx="80" cy="69" r="13" fill="#F4A259" />
    <circle cx="75" cy="66" r="2" fill="#4A3B2F" />
    <circle cx="85" cy="66" r="2" fill="#4A3B2F" />
    <ellipse cx="80" cy="73" rx="4.6" ry="3.4" fill="#FFE9D2" />
    <circle cx="80" cy="71.4" r="1.8" fill="#5B4636" />
  </svg>
</template>

<style scoped>
.ill { width: 100%; height: 100%; display: block; }
</style>
