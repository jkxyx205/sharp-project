import terser from '@rollup/plugin-terser';

export default {
  input: ['js/tab-b.js'], // 入口文件
  output: {
    dir: 'js/dist',
    entryFileNames: '[name].js',
    format: 'iife', // 也可以是 'esm', 'cjs', 'umd' 等
    sourcemap: false,
  },
  watch: {
    include: 'js/**',  // 监听整个 src 目录
    exclude: 'node_modules/**' // 忽略 node_modules
  },
  plugins: [
    terser({
      mangle: {
        toplevel: true // 混淆顶层变量和函数名
      },
      compress: {
        drop_console: false,   // 删除 console.log
        drop_debugger: false   // 删除 debugger
      },
      format: {
        comments: true       // 删除注释
      }
    })
  ]
};
