<template>
  <q-page
    :style-fn="handlePageOffset"
    padding
  >
    <LoadingOverlay />

    <template v-if="!pageInitLoading">
      <TrayDisplay
        v-if="$route.name == 'record-management-tray' && trayDetails.id"
      />
      <ShelfDisplay
        v-else-if="$route.name == 'record-management-shelf' && shelfDetails.id"
      />
      <ItemDisplay
        v-else-if="$route.name == 'record-management-items' && itemDetails.id"
      />
    </template>
  </q-page>
</template>

<script setup>
import { inject, onMounted, onBeforeMount, watch } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useRecordManagementStore } from '@/stores/record-management-store'
import { useGlobalStore } from '@/stores/global-store'
import LoadingOverlay from '@/components/LoadingOverlay.vue'
import TrayDisplay from '@/components/RecordManagement/TrayDisplay.vue'
import ShelfDisplay from '@/components/RecordManagement/ShelfDisplay.vue'
import ItemDisplay from '@/components/RecordManagement/ItemDisplay.vue'

const route = useRoute()

// Store Data
const {
  itemDetails,
  trayDetails,
  shelfDetails
} = storeToRefs(useRecordManagementStore())
const {
  getItemDetails,
  getTrayDetails,
  getShelfDetails
} = useRecordManagementStore()
const { pageInitLoading } = storeToRefs(useGlobalStore())

// Local Data

// Logic
const handlePageOffset = inject('handle-page-offset')

onBeforeMount(() => {
  pageInitLoading.value = true
})

onMounted( async () => {
  if (route.name == 'record-management-items' && route.params.barcode) {
    await getItemDetails(route.params.barcode)
  } else if (route.name == 'record-management-tray' && route.params.barcode) {
    await getTrayDetails(route.params.barcode)
  } else if (route.name == 'record-management-shelf' && route.params.barcode) {
    await getShelfDetails(route.params.barcode)
  }
  pageInitLoading.value = false
})

watch(() => route.params.barcode, async () => {
  if (route.name == 'record-management-items' && route.params.barcode) {
    await getItemDetails(route.params.barcode)
  } else if (route.name == 'record-management-tray' && route.params.barcode) {
    await getTrayDetails(route.params.barcode)
  } else if (route.name == 'record-management-shelf' && route.params.barcode) {
    await getShelfDetails(route.params.barcode)
  }
})
</script>
