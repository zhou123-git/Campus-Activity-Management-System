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
    }
  },
  computed: {
    badgeClass() {
      switch (this.status) {
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
      switch (this.status) {
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
  }
}
</script>