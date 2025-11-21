<template>
  <div class="activity-card ant-card-hoverable rounded-xl overflow-hidden">
    <div class="p-5 h-full flex flex-col bg-white">
      <h5 class="text-indigo-600 font-bold text-lg mb-3">{{ activity.activityName }}</h5>
      <div class="flex-grow">
        <p class="text-gray-500 text-sm mb-2">
          <i class="far fa-calendar-alt mr-2"></i>
          时间：{{ activity.startTime?.replace('T', ' ') }} ~ {{ activity.endTime?.replace('T', ' ') }}
        </p>
        <p class="text-gray-500 text-sm mb-2">
          <i class="fas fa-users mr-2"></i>
          人数：{{ activity.count || 0 }}/{{ activity.maxNum || 0 }}
        </p>
        <p class="text-gray-500 text-sm mb-2">
          <i class="fas fa-user mr-2"></i>
          发布者：{{ activity.publisherName || activity.publisherId }}
        </p>
        <p class="text-gray-500 text-sm mb-3">
          <i class="fas fa-info-circle mr-2"></i>
          状态：<StatusBadge :status="activity.status" />
        </p>
      </div>
      <div class="flex justify-between mt-4 space-x-2">
        <button 
          class="flex-1 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 font-medium py-2 px-3 rounded-lg transition duration-200 ease-in-out transform hover:-translate-y-0.5"
          @click="$emit('view-details', activity)"
        >
          查看详情
        </button>
        <button 
          class="flex-1 bg-gradient-to-r from-green-500 to-emerald-500 hover:from-green-600 hover:to-emerald-600 text-white font-medium py-2 px-3 rounded-lg transition duration-200 ease-in-out transform hover:-translate-y-0.5 disabled:opacity-50 disabled:cursor-not-allowed"
          @click="$emit('apply', activity.activityId, activity.maxNum, activity.count)"
          :disabled="activity.status !== 'approved'"
        >
          报名
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import StatusBadge from './StatusBadge.vue';

export default {
  name: 'ActivityCard',
  components: {
    StatusBadge
  },
  props: {
    activity: {
      type: Object,
      required: true
    }
  },
  emits: ['apply', 'view-details']
}
</script>

<style scoped>
.activity-card {
  border-radius: 0.75rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  background: #ffffff;
  border: 1px solid #e5e7eb;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.activity-card:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  border-color: #d1d5db;
  transform: translateY(-2px);
}
</style>