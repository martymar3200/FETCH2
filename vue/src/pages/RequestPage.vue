<template>
  <q-page
    :style-fn="handlePageOffset"
    :padding="route.name !== 'request-batch'"
    class="request"
  >
    <LoadingOverlay />

    <template v-if="!pageInitLoading">
      <RequestDashboard v-if="route.name == 'request'" />
      <RequestItemDetails v-if="route.name == 'request-details'" />
      <RequestBatchJobDetails v-if="route.name == 'request-batch'" />
    </template>
  </q-page>
</template>

<script setup>
import { inject, onBeforeMount, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useRequestStore } from '@/stores/request-store'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import LoadingOverlay from '@/components/LoadingOverlay.vue'
import RequestDashboard from '@/components/Request/RequestDashboard.vue'
import RequestItemDetails from '@/components/Request/RequestItemDetails.vue'
import RequestBatchJobDetails from '@/components/Request/RequestBatchJobDetails.vue'

const route = useRoute()

// Store Data
const { getRequestBatchJob, getRequestJob } = useRequestStore()
const { pageInitLoading } = storeToRefs(useGlobalStore())
const { getOptions } = useOptionStore()

// Logic
const handlePageOffset = inject('handle-page-offset')

onBeforeMount(() => {
  pageInitLoading.value = true
})

onMounted( async () => {
  // load any options info that will be needed on the request page
  if (!route.params.jobId) {
    await Promise.all([
      getOptions('buildings', { sort_by: 'name' }),
      getOptions('requestsPriorities', { sort_by: 'value' }),
      getOptions('requestsLocations', { sort_by: 'name' }),
      getOptions('requestsTypes', { sort_by: 'type' }),
      getOptions('mediaTypes', { sort_by: 'name' })
    ])
  }

  // if there is an id in the url we need to load that request job
  if (route.name == 'request-batch') {
    await getRequestBatchJob(route.params.jobId)
  }

  if (route.name == 'request-details') {
    await getRequestJob(route.params.jobId)
  }
  pageInitLoading.value = false
})
</script>

<style lang="scss" scoped>
</style>
