<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex flex-col items-center justify-center p-4">
    <div class="max-w-4xl w-full bg-white rounded-2xl shadow-xl p-6 md:p-8">
      <div class="text-center mb-8">
        <h1 class="text-3xl md:text-4xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-indigo-600 to-purple-600 mb-3">
          校园活动报名系统
        </h1>
        <p class="text-gray-600">
          欢迎使用现代化的校园活动管理系统
        </p>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-3 gap-5 mb-8">
        <div class="bg-gradient-to-br from-blue-50 to-blue-100 p-5 rounded-xl border border-blue-200 hover:shadow-md transition-shadow">
          <div class="flex items-center justify-center w-12 h-12 bg-blue-500 rounded-lg mb-4 mx-auto">
            <i class="fas fa-calendar-check text-white text-xl"></i>
          </div>
          <h2 class="text-xl font-semibold text-blue-800 mb-2 text-center">活动列表</h2>
          <p class="text-gray-600 text-center">查看所有可用的校园活动</p>
        </div>
        
        <div class="bg-gradient-to-br from-green-50 to-green-100 p-5 rounded-xl border border-green-200 hover:shadow-md transition-shadow">
          <div class="flex items-center justify-center w-12 h-12 bg-green-500 rounded-lg mb-4 mx-auto">
            <i class="fas fa-user-check text-white text-xl"></i>
          </div>
          <h2 class="text-xl font-semibold text-green-800 mb-2 text-center">我的报名</h2>
          <p class="text-gray-600 text-center">管理您已报名的活动</p>
        </div>
        
        <div class="bg-gradient-to-br from-amber-50 to-amber-100 p-5 rounded-xl border border-amber-200 hover:shadow-md transition-shadow">
          <div class="flex items-center justify-center w-12 h-12 bg-amber-500 rounded-lg mb-4 mx-auto">
            <i class="fas fa-bullhorn text-white text-xl"></i>
          </div>
          <h2 class="text-xl font-semibold text-amber-800 mb-2 text-center">发布活动</h2>
          <p class="text-gray-600 text-center">创建新的校园活动</p>
        </div>
      </div>
      
      <div class="text-center">
        <button 
          class="bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 text-white font-bold py-3 px-8 rounded-xl transition duration-300 transform hover:scale-105 shadow-lg"
          @click="handleStart"
        >
          <i class="fas fa-rocket mr-2"></i>
          开始使用
        </button>
      </div>
    </div>
    
    <div class="mt-8 text-center text-gray-600 text-sm">
      <p>© 2023 校园活动报名系统 - 让校园活动更简单</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'App',
  methods: {
    handleStart() {
      alert('开始使用校园活动报名系统！');
    }
  }
}
</script>

<style scoped>
/* 添加一些额外的样式来增强视觉效果 */
.bg-gradient-to-br {
  background-size: 200% 200%;
  animation: gradientAnimation 10s ease infinite;
}

@keyframes gradientAnimation {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
</style>