<template>
  <q-page
    :style-fn="handlePageOffset"
    :padding="!route.params.jobId && !route.params.type"
    class="shelving"
  >
    <LoadingOverlay />

    <template v-if="!pageInitLoading">
      <ShelvingDashboard v-if="route.name == 'shelving' && !route.params.jobId" />
      <ShelvingJobDetails v-else-if="route.name == 'shelving' && route.params.jobId" />
      <ShelvingJobDirectToShelf v-else-if="route.name == 'shelving-dts' && route.params.jobId" />
      <ShelvingMove v-else-if="route.name == 'shelving-move' && route.params.type" />
    </template>
  </q-page>
</template>

<script setup>
import { inject, onBeforeMount, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useShelvingStore } from '@/stores/shelving-store'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import LoadingOverlay from '@/components/LoadingOverlay.vue'
import ShelvingDashboard from '@/components/Shelving/ShelvingDashboard.vue'
import ShelvingJobDetails from '@/components/Shelving/ShelvingJobDetails.vue'
import ShelvingJobDirectToShelf from '@/components/Shelving/ShelvingJobDirectToShelf.vue'
import ShelvingMove from '@/components/Shelving/ShelvingMove.vue'

const route = useRoute()

// Store Data
const { getShelvingJob, getDirectShelvingJob } = useShelvingStore()
const { pageInitLoading } = storeToRefs(useGlobalStore())
const { getOptions } = useOptionStore()

// Logic
const handlePageOffset = inject('handle-page-offset')

onBeforeMount(() => {
  pageInitLoading.value = true
})

onMounted( async () => {
  // load any options info that will be needed on the shelving page
  if (!route.params.jobId) {
    await Promise.all([getOptions('users', { sort_by: 'name' })])
  }

  // if there is an id in the url we need to load that shelving job
  if (route.name == 'shelving' && route.params.jobId) {
    await getShelvingJob(route.params.jobId)
  } else if (route.name == 'shelving-dts' && route.params.jobId) {
    // only load the direct shelving job page after dts data is retrieved since the store data is slightly different from whats returned from api
    await getDirectShelvingJob(route.params.jobId)
  }
  pageInitLoading.value = false
})
</script>
<style lang="scss" scoped>
</style>
