<template>
  <span :class="badgeClass">{{ statusText }}</span>
</template>

<script>
export default {
  name: 'StatusBadge',
  props: {
    status: {
      type: String,
      required: true
    },
    startTime: {
      type: String,
      default: ''
    },
    endTime: {
      type: String,
      default: ''
    }
  },
  computed: {
    badgeClass() {
      const timeStatus = this.getTimeStatus();
      switch (timeStatus) {
        case 'notStarted':
          return 'bg-blue-100 text-blue-800 text-xs font-semibold px-2.5 py-0.5 rounded-full';
        case 'inProgress':
          return 'bg-green-100 text-green-800 text-xs font-semibold px-2.5 py-0.5 rounded-full';
        case 'ended':
          return 'bg-gray-100 text-gray-800 text-xs font-semibold px-2.5 py-0.5 rounded-full';
        case 'pending':
          return 'bg-amber-100 text-amber-800 text-xs font-semibold px-2.5 py-0.5 rounded-full';
        case 'approved':
          return 'bg-emerald-100 text-emerald-800 text-xs font-semibold px-2.5 py-0.5 rounded-full';
        case 'rejected':
          return 'bg-rose-100 text-rose-800 text-xs font-semibold px-2.5 py-0.5 rounded-full';
        default:
          return 'bg-gray-100 text-gray-800 text-xs font-semibold px-2.5 py-0.5 rounded-full';
      }
    },
    statusText() {
      const timeStatus = this.getTimeStatus();
      switch (timeStatus) {
        case 'notStarted':
          return '未开始';
        case 'inProgress':
          return '进行中';
        case 'ended':
          return '已结束';
        case 'pending':
          return '待审核';
        case 'approved':
          return '已通过';
        case 'rejected':
          return '已拒绝';
        default:
          return '未知';
      }
    }
  },
  methods: {
    getTimeStatus() {
      // 如果没有开始时间和结束时间，则按照原来的状态显示
      if (!this.startTime || !this.endTime) {
        return this.status;
      }

      // 如果活动未通过审核，直接返回审核状态
      if (this.status !== 'approved') {
        return this.status;
      }

      const now = new Date();
      const start = new Date(this.startTime);
      const end = new Date(this.endTime);

      // 判断活动状态
      if (now < start) {
        return 'notStarted';
      } else if (now >= start && now <= end) {
        return 'inProgress';
      } else {
        return 'ended';
      }
    }
  }
}
</script>