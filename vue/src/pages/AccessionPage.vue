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
import { Notify } from 'quasar'
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


// Close the drawer when entering a job page
watch(() => route.params.jobId, (newJobId) => {
  if (newJobId) {
    setMainNavDrawerOpen(false)
  }
}, {
  immediate: true
})

watch(() => route.params.containerId, async (newId, oldId) => {
  if (newId && newId !== oldId) {
    if (accessionJob.value.trayed) {
      await getAccessionTray(newId).catch(error => {
        Notify.create({
          type: 'negative',
          message: error.response?.data?.detail || error
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
        Notify.create({
          type: 'negative',
          message: error.response?.data?.detail || error
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
        Notify.create({
          type: 'negative',
          message: error.response?.data?.detail || error
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