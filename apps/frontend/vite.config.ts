import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      
      '/admin-api': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      },
      '/api/showShare': {
        target: 'http://localhost:8093',
        changeOrigin: true,
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/fileup': {
        target: 'http://localhost:8090',
        changeOrigin: true,
      },
      '/file': {
        target: 'http://localhost:8099',
        changeOrigin: true,
      },
      // 优化 /api/showShare 代理，匹配所有 /api/showShare 及其子路径
      '/share/shareFile': {
        target: 'http://localhost:8093/api',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/share/, '/share'),
      },
      '/share/loadShareList': {
        target: 'http://localhost:8093/api',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/share/, '/share'),
      },
      '/share/cancelShare': {
        target: 'http://localhost:8093/api',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/share/, '/share'),
      },
      
    },
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
  },
})