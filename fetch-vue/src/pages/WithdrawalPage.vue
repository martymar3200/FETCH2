<template>
  <q-page
    :style-fn="handlePageOffset"
    :padding="!route.params.jobId"
    class="withdrawal"
  >
    <LoadingOverlay />

    <template v-if="!pageInitLoading">
      <WithdrawalDashboard v-if="!route.params.jobId" />
      <WithdrawalJobDetail v-if="route.params.jobId" />
    </template>
  </q-page>
</template>

<script setup>
import { inject, onMounted, onBeforeMount } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useWithdrawalStore } from '@/stores/withdrawal-store'
import { useGlobalStore } from '@/stores/global-store'
import LoadingOverlay from '@/components/LoadingOverlay.vue'
import WithdrawalDashboard from '@/components/Withdrawal/WithdrawalDashboard.vue'
import WithdrawalJobDetail from '@/components/Withdrawal/WithdrawalJobDetail.vue'

const route = useRoute()

// Store Data
const { getWithdrawJob } = useWithdrawalStore()
const { pageInitLoading } = storeToRefs(useGlobalStore())

// Logic
const handlePageOffset = inject('handle-page-offset')

onBeforeMount(() => {
  pageInitLoading.value = true
})

onMounted( async () => {
  // if there is an id in the url we need to load that withdrawal job directly
  if (route.params.jobId) {
    await getWithdrawJob(route.params.jobId)
  }
  pageInitLoading.value = false
})
</script>
<style lang="scss" scoped>
</style>
