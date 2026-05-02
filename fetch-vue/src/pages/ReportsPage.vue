<template>
  <q-page
    :style-fn="handlePageOffset"
    padding
    class="reports"
  >
    <LoadingOverlay />

    <ReportsDashboard />
  </q-page>
</template>

<script setup>
import { inject } from 'vue'
import LoadingOverlay from '@/components/LoadingOverlay.vue'
import ReportsDashboard from '@/components/Reports/ReportsDashboard.vue'

// Logic
const handlePageOffset = inject('handle-page-offset')
</script>

<style lang="scss" scoped>
</style>
