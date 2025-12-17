<template>
  <PopupModal
    @reset="resetRefileItem(); emit('hide');"
    aria-label="refileJobItemDetailModal"
  >
    <template #header-content="{ hideModal }">
      <q-card-section class="row items-center q-pb-none">
        <h2
          v-if="refileJob.status !== 'Running' || refileItem.status !== 'Out'"
          class="text-h6 text-bold"
        >
          Item Details
        </h2>
        <h2
          v-else
          class="text-h6 text-bold"
        >
          {{ refileItem.tray ? 'Place Tray Item' : 'Place Non-Tray Item' }}
        </h2>

        <q-btn
          icon="close"
          flat
          round
          dense
          class="q-ml-auto"
          @click="hideModal"
          aria-label="closeModal"
        />
      </q-card-section>
    </template>

    <template #main-content>
      <q-card-section class="row q-pb-sm">
        <div class="col-12">
          <BarcodeBox
            v-if="refileJob.status !== 'Running' || refileItem.status !== 'Out'"
            :barcode="renderItemBarcodeDisplay(refileItem)"
            :min-height="'5rem'"
          />
          <BarcodeBox
            v-else
            :barcode="refileItem.tray ? 'Please Scan Tray' : 'Please Scan Shelf'"
            :min-height="'5rem'"
          />
        </div>
      </q-card-section>

      <q-card-section
        class="row q-pb-none"
      >
        <div class="col-6">
          <div class="container-details">
            <label class="text-body1 text-bold">
              Item Barcode:
            </label>
            <p class="text-body1">
              {{ refileItem.barcode?.value }}
            </p>
          </div>
        </div>
        <div class="col-6">
          <div
            v-if="refileItem.tray"
            class="container-details"
          >
            <label class="text-body1 text-bold">
              Tray Barcode:
            </label>
            <p class="text-body1">
              {{ refileItem.tray?.barcode?.value }}
            </p>
          </div>
        </div>
        <div class="col-6">
          <div class="container-details">
            <label class="text-body1 text-bold">
              Module:
            </label>
            <p class="text-body1">
              {{ refileItem.tray ? refileItem.tray?.shelf_position?.location?.split('-')[1] : refileItem.shelf_position?.location?.split('-')[1] }}
            </p>
          </div>
        </div>
        <div class="col-6">
          <div class="container-details">
            <label class="text-body1 text-bold">
              Aisle:
            </label>
            <p class="text-body1">
              {{ refileItem.tray ? refileItem.tray?.shelf_position?.location?.split('-')[2] : refileItem.shelf_position?.location?.split('-')[2] }}
            </p>
          </div>
        </div>
        <div class="col-6">
          <div class="container-details">
            <label class="text-body1 text-bold">
              Side:
            </label>
            <p class="text-body1">
              {{ refileItem.tray ? refileItem.tray?.shelf_position?.location?.split('-')[3] : refileItem.shelf_position?.location?.split('-')[3] }}
            </p>
          </div>
        </div>
        <div class="col-6">
          <div class="container-details">
            <label class="text-body1 text-bold">
              Ladder:
            </label>
            <p class="text-body1">
              {{ refileItem.tray ? refileItem.tray?.shelf_position?.location?.split('-')[4] : refileItem.shelf_position?.location?.split('-')[4] }}
            </p>
          </div>
        </div>
        <div class="col-6">
          <div class="container-details">
            <label class="text-body1 text-bold">
              Shelf:
            </label>
            <p class="text-body1">
              {{ refileItem.tray ? refileItem.tray?.shelf_position?.location?.split('-')[5] : refileItem.shelf_position?.location?.split('-')[5] }}
            </p>
          </div>
        </div>
        <div class="col-6">
          <div class="container-details">
            <label class="text-body1 text-bold">
              Shelf Position:
            </label>
            <p class="text-body1">
              {{ refileItem.tray ? refileItem.tray?.shelf_position?.location?.split('-')[6] : refileItem.shelf_position?.location?.split('-')[6] }}
            </p>
          </div>
        </div>
      </q-card-section>
    </template>

    <template #footer-content="{ hideModal }">
      <q-card-section class="column items-center q-pt-sm">
        <q-btn
          no-caps
          unelevated
          outline
          color="accent"
          label="Cancel"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="hideModal"
        />
      </q-card-section>
    </template>
  </PopupModal>
</template>

<script setup>
import { inject, watch } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store'
import { useRefileStore } from '@/stores/refile-store'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import { useIndexDbHandler } from '@/composables/useIndexDbHandler.js'
import PopupModal from '@/components/PopupModal.vue'
import BarcodeBox from '@/components/BarcodeBox.vue'

const route = useRoute()

// Emits
const emit = defineEmits(['hide'])

// Compasables
const { compiledBarCode } = useBarcodeScanHandler()
const { addDataToIndexDb } = useIndexDbHandler()

// Store Data
const { appActionIsLoadingData } = storeToRefs(useGlobalStore())
const {
  patchRefileJobTrayItemScanned,
  patchRefileJobNonTrayItemScanned,
  resetRefileItem
} = useRefileStore()
const {
  refileItem,
  refileJob,
  originalRefileJob
} = storeToRefs(useRefileStore())

// Local Data

// Logic
const handleAlert = inject('handle-alert')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')

watch(compiledBarCode, (barcode) => {
  if (barcode !== '' && refileJob.value.status == 'Running' && refileItem.value.tray) {
    triggerTrayScan(barcode)
  } else if (barcode !== '' && refileJob.value.status == 'Running' && refileItem.value.shelf_position) {
    triggerShelfScan(barcode)
  }
})
const triggerTrayScan = (barcode_value) => {
  if (refileItem.value.status !== 'Out') {
    handleAlert({
      type: 'error',
      text: 'The scanned item has already been marked as refiled.',
      autoClose: true
    })
  } else if (barcode_value == refileItem.value.tray.barcode.value && !refileItem.value.status !== 'Out') {
    updateTrayItemAsRefiled()
  } else {
    handleAlert({
      type: 'error',
      text: `The scanned tray barcode does not match this items tray barcode (${refileItem.value.tray.barcode.value}). Please try again!`,
      autoClose: true
    })
  }
}
const triggerShelfScan = (barcode_value) => {
  if (refileItem.value.status !== 'Out') {
    handleAlert({
      type: 'error',
      text: 'The scanned item has already been marked as refiled.',
      autoClose: true
    })
  } else if (barcode_value == refileItem.value.shelf_position.shelf.barcode.value && !refileItem.value.status !== 'Out') {
    updateNonTrayItemAsRefiled()
  } else {
    handleAlert({
      type: 'error',
      text: `The scanned shelf barcode does not match the non tray items shelf barcode (${refileItem.value.shelf_position.shelf.barcode.value}). Please try again!`,
      autoClose: true
    })
  }
}

const updateTrayItemAsRefiled = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      job_id: route.params.jobId,
      item_id: refileItem.value.id,
      status: 'In'
    }
    await patchRefileJobTrayItemScanned(payload)

    // directly update the refile tray item in the refile job items
    refileJob.value.items[refileJob.value.items.findIndex(itm => itm.id == payload.item_id)].status = payload.status

    // also directly update the refile tray item in the refile_job_items array
    const refileJobItemIndex = refileJob.value.refile_job_items.findIndex(itm => itm.barcode.value == refileItem.value.barcode.value)
    const refileJobItemByIndex = refileJob.value.refile_job_items[refileJobItemIndex]
    refileJobItemByIndex.status = payload.status
    // move the item to bottom of the list
    refileJob.value.refile_job_items.splice(refileJobItemIndex, 1)
    refileJob.value.refile_job_items.push(refileJobItemByIndex)

    // update the stored refileJob since the container will get changed at the job requests level
    addDataToIndexDb('refileStore', 'refileJob', JSON.parse(JSON.stringify(refileJob.value)))
    addDataToIndexDb('refileStore', 'originalRefileJob', JSON.parse(JSON.stringify(originalRefileJob.value)))

    handleAlert({
      type: 'success',
      text: 'The tray item has been refiled.',
      autoClose: true
    })
    resetRefileItem()
    emit('hide')
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}
const updateNonTrayItemAsRefiled = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      job_id: route.params.jobId,
      non_tray_item_id: refileItem.value.id,
      status: 'In'
    }
    await patchRefileJobNonTrayItemScanned(payload)

    // directly update the refile non tray item in the refile job items
    refileJob.value.non_tray_items[refileJob.value.non_tray_items.findIndex(itm => itm.id == payload.non_tray_item_id)].status = payload.status

    // also directly update the refile non tray item in the refile_job_items array
    const refileJobItemIndex = refileJob.value.refile_job_items.findIndex(itm => itm.barcode.value == refileItem.value.barcode.value)
    const refileJobItemByIndex = refileJob.value.refile_job_items[refileJobItemIndex]
    refileJobItemByIndex.status = payload.status
    // move the item to bottom of the list
    refileJob.value.refile_job_items.splice(refileJobItemIndex, 1)
    refileJob.value.refile_job_items.push(refileJobItemByIndex)

    // update the stored refileJob since the container will get changed at the job requests level
    addDataToIndexDb('refileStore', 'refileJob', JSON.parse(JSON.stringify(refileJob.value)))
    addDataToIndexDb('refileStore', 'originalRefileJob', JSON.parse(JSON.stringify(originalRefileJob.value)))

    handleAlert({
      type: 'success',
      text: 'The non tray item has been refiled.',
      autoClose: true
    })
    resetRefileItem()
    emit('hide')
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}
</script>

<style lang="scss" scoped>
.container {
  &-details {
    display: flex;
    flex-flow: row wrap;
    width: 100%;
    padding-bottom: 8px;

    label {
      margin-right: .5rem;
    }
  }
}
</style>
