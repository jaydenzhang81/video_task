const path = require('path')

module.exports = {
  // 路径别名配置
  alias: {
    '@': path.resolve(__dirname, './')
  },
  
  // 开发服务器配置
  devServer: {
    port: 8081,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: {
          '^/api': '/api'
        }
      }
    }
  },
  
  // 构建配置
  build: {
    // 生产环境配置
    productionSourceMap: false,
    
    // CSS配置
    css: {
      loaderOptions: {
        scss: {
          additionalData: `@import "@/uni.scss";`
        }
      }
    }
  }
}
