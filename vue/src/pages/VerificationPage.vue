<template>
  <q-page
    :style-fn="handlePageOffset"
    :padding="!route.params.jobId"
    class="verification column no-wrap"
  >
    <LoadingOverlay />

    <template v-if="!pageInitLoading">
      <VerificationDashboard v-if="!route.params.jobId" />
      <VerificationContainerDisplay
        v-if="route.params.jobId"
        ref="verificationContainerComponent"
      />
    </template>
  </q-page>
</template>

<script setup>
import { onBeforeMount, onMounted, inject, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Notify } from 'quasar'
import { storeToRefs } from 'pinia'
import { useVerificationStore } from 'src/stores/verification-store'
import { useGlobalStore } from '@/stores/global-store'
import LoadingOverlay from '@/components/LoadingOverlay.vue'
import VerificationDashboard from '@/components/Verification/VerificationDashboard.vue'
import VerificationContainerDisplay from '@/components/Verification/VerificationContainerDisplay.vue'

const route = useRoute()
const router = useRouter() // Initialize router

// Store Data
const {
  getVerificationJob,
  getVerificationTray,
  getVerificationNonTrayItem
} = useVerificationStore()
const { verificationJob } = storeToRefs(useVerificationStore())
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

// NEW: Watch for changes in the containerId URL parameter to re-fetch data
watch(() => route.params.containerId, async (newId, oldId) => {
  // Only run if the ID has actually changed and exists
  if (newId && newId !== oldId) {
    // Re-fetch the data for the newly selected tray
    if (verificationJob.value.trayed) {
      await getVerificationTray(newId).catch(error => {
        Notify.create({
          type: 'negative',
          message: error.response?.data?.detail || error
        })
        router.push({
          name: 'verification',
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
  // if there is an id in the url we need to load that job
  if (route.params.jobId) {
    await getVerificationJob(route.params.jobId)
  }

  if (route.params.containerId) {
    if (verificationJob.value.trayed) {
      await getVerificationTray(route.params.containerId)
    } else {
      await getVerificationNonTrayItem(route.params.containerId)
    }
  }
  pageInitLoading.value = false
})
</script>