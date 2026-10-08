import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5190,
    strictPort: true,
    proxy: {
      '/api': 'http://localhost:8082',
      '/upload': 'http://localhost:8082'
    }
  }
})
