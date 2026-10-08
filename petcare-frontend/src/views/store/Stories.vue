<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api'
import { parseTime } from '../../utils/datetime'

const stories = ref<any[]>([])
const visible = ref(false)
const form = ref({ title: '', content: '' })
const likedSet = ref<Set<number>>(new Set())

async function load() {
  stories.value = await api.stories()
}
onMounted(load)

async function publish() {
  if (!form.value.title.trim() || !form.value.content.trim()) return ElMessage.warning('请填写标题和内容')
  await api.publishStory(form.value)
  ElMessage.success('✨ 分享成功')
  visible.value = false
  form.value = { title: '', content: '' }
  stories.value = await api.stories()
}

async function like(s: any) {
  if (likedSet.value.has(s.id)) return
  await api.likeStory(s.id)
  s.likes = (s.likes || 0) + 1
  likedSet.value.add(s.id)
}

const ICONS = ['🐶', '🐱', '🐰', '🦊', '🐹', '🐢']
const iconOf = (id: number) => ICONS[id % ICONS.length]

const timeAgo = (t: string) => {
  const d = parseTime(t)
  if (!d) return ''
  const day = Math.floor((Date.now() - d.getTime()) / 86400000)
  if (day <= 0) return '今天'
  if (day < 30) return `${day} 天前`
  return `${d.getMonth() + 1}月${d.getDate()}日`
}
</script>

<template>
  <div class="story-page">
    <!-- 页头 -->
    <div class="page-head-row">
      <div>
        <h2 class="page-title">主人故事墙</h2>
        <p class="page-desc">毛孩子们在这里的快乐时光，都值得被记录</p>
      </div>
      <el-button type="primary" round @click="visible = true">+ 分享故事</el-button>
    </div>

    <!-- 信息流卡片列表 -->
    <div class="story-list">
      <div v-for="s in stories" :key="s.id" class="story-item">
        <!-- 左：emoji 徽标 -->
        <div class="story-badge">{{ iconOf(s.id) }}</div>
        <!-- 右：内容 -->
        <div class="story-main">
          <div class="story-head">
            <span class="story-author">{{ s.author }}</span>
            <span class="dot">·</span>
            <span class="sub">{{ timeAgo(s.createTime) }}</span>
          </div>
          <div class="story-title">{{ s.title }}</div>
          <p class="story-content">{{ s.content }}</p>
          <div class="story-actions">
            <button class="like-btn" :class="{ liked: likedSet.has(s.id) }" @click="like(s)">
              <span>{{ likedSet.has(s.id) ? '❤️' : '🤍' }}</span> 赞 {{ s.likes || 0 }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="!stories.length" class="story-empty">
      <p>还没有故事，来分享第一份快乐吧～</p>
      <el-button type="primary" round @click="visible = true">写第一篇故事</el-button>
    </div>

    <!-- 发布弹窗 -->
    <el-dialog v-model="visible" title="分享寄养故事" width="520px">
      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="50" show-word-limit placeholder="如：豆豆的快乐寄养周记" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="6" maxlength="500" show-word-limit
                    placeholder="毛孩子玩了什么、吃得好不好、有没有变胖…" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" round @click="publish">发布故事</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.story-page { max-width: 860px; margin: 0 auto; }
.page-head-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; }

/* 信息流卡 */
.story-list { display: flex; flex-direction: column; gap: 12px; }
.story-item {
  display: flex; gap: 16px; background: #fff; border-radius: 16px; padding: 20px 22px;
  box-shadow: 0 2px 12px -6px rgba(47,70,57,.12);
  transition: box-shadow .15s;
}
.story-item:hover { box-shadow: 0 8px 22px -10px rgba(47,70,57,.2); }
.story-badge {
  width: 46px; height: 46px; border-radius: 50%; flex-shrink: 0;
  background: linear-gradient(135deg, #EAF2ED, #D4E6DC);
  display: flex; align-items: center; justify-content: center; font-size: 24px;
}
.story-main { flex: 1; min-width: 0; }
.story-head { display: flex; align-items: center; gap: 6px; margin-bottom: 6px; }
.story-author { font-weight: 700; font-size: 14px; }
.dot { color: #C4BCB2; }
.story-title { font-size: 17px; font-weight: 800; margin-bottom: 6px; }
.story-content { font-size: 14px; line-height: 1.85; color: #5C6B63; margin: 0 0 12px;
  white-space: pre-wrap; }
.story-actions { display: flex; gap: 8px; }
.like-btn {
  display: inline-flex; align-items: center; gap: 6px; border: 1px solid #E7DFE2; background: #FBF7F8;
  color: #8A7280; font-size: 13px; border-radius: 999px; padding: 6px 16px; cursor: pointer;
  transition: all .15s;
}
.like-btn:hover { border-color: #E8A0B4; color: #C77A8E; }
.like-btn.liked { background: #FDEDF2; border-color: #F3C1CF; color: #C77A8E; }

.story-empty { background: #fff; border-radius: 16px; padding: 60px; text-align: center; }
.story-empty p { font-size: 15px; font-weight: 700; margin: 0 0 14px; color: var(--pc-ink-2); }
</style>
