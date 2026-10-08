import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 前端默认 5173,代理 /api 与 /uploads 到后端 8080
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
      '/uploads': { target: 'http://localhost:8080', changeOrigin: true }
    }
  }
})
