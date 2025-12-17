<template>
  <q-page
    :style-fn="handlePageOffset"
    :padding="!route.params.jobId"
    class="accession column no-wrap"
  >
    <LoadingOverlay />

    <template v-if="!pageInitLoading">
      <AccessionDashboard v-if="!route.params.jobId" />
      <AccessionContainerDisplay v-if="route.params.jobId" />
    </template>
  </q-page>
</template>

<script setup>
import { onBeforeMount, onMounted, inject, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAccessionStore } from 'src/stores/accession-store'
import { useGlobalStore } from '@/stores/global-store'
import LoadingOverlay from '@/components/LoadingOverlay.vue'
import AccessionDashboard from '@/components/Accession/AccessionDashboard.vue'
import AccessionContainerDisplay from '@/components/Accession/AccessionContainerDisplay.vue'

const route = useRoute()
const router = useRouter()

// Store Data
const {
  getAccessionJob,
  getAccessionTray,
  getAccessionNonTrayItem
} = useAccessionStore()
const { accessionJob } = storeToRefs(useAccessionStore())
const globalStore = useGlobalStore()
const { pageInitLoading } = storeToRefs(globalStore)
const { setMainNavDrawerOpen } = globalStore

// Logic
const handlePageOffset = inject('handle-page-offset')
const handleAlert = inject('handle-alert')

// NEW: Watch for changes in the jobId to control the drawer state
watch(() => route.params.jobId, (newJobId) => {
  if (newJobId) {
    // If a jobId exists, we are on a job page, so close the drawer.
    setMainNavDrawerOpen(false)
  } else {
    // If no jobId exists, we are on the dashboard, so open the drawer.
    setMainNavDrawerOpen(true)
  }
}, {
  // This makes the watcher run immediately on component load, just like onMounted.
  immediate: true
})

watch(() => route.params.containerId, async (newId, oldId) => {
  if (newId && newId !== oldId) {
    if (accessionJob.value.trayed) {
      await getAccessionTray(newId).catch(error => {
        handleAlert({
          type: 'error',
          text: error,
          autoClose: true
        })
        router.push({
          name: 'accession',
          params: {
            jobId: route.params.jobId
          }
        })
      })
    }
  }
})

onBeforeMount(() => {
  pageInitLoading.value = true
})

onMounted( async () => {
  // REMOVED: The logic to close the drawer has been moved to the new watcher.
  if (route.params.jobId) {
    await getAccessionJob(route.params.jobId)
  }

  if (route.params.containerId) {
    if (accessionJob.value.trayed) {
      await getAccessionTray(route.params.containerId).catch(error => {
        handleAlert({
          type: 'error',
          text: error,
          autoClose: true
        })
        router.push({
          name: 'accession',
          params: {
            jobId: route.params.jobId
          }
        })
      })
    } else {
      await getAccessionNonTrayItem(route.params.containerId).catch(error => {
        handleAlert({
          type: 'error',
          text: error,
          autoClose: true
        })
        router.push({
          name: 'accession',
          params: {
            jobId: route.params.jobId
          }
        })
      })
    }
  }
  pageInitLoading.value = false
})
</script>