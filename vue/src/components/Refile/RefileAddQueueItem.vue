<template>
  <PopupModal
    @reset="emit('hide')"
    aria-label="AddRefileQueueItemModal"
  >
    <template #header-content="{ hideModal }">
      <q-card-section class="row items-center q-pb-none">
        <h2
          class="text-h6 text-bold"
        >
          Add Item To Queue
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
            :barcode="refileItem.barcode?.value ?? 'Please Scan Barcode'"
            :min-height="'5rem'"
          />
        </div>
        <div
          v-if="showSuccessMesssage"
          class="col-12"
        >
          <p class="text-body2">
            Successfly added to queue. Scan another item barcode when ready!
          </p>
        </div>
      </q-card-section>

      <q-card-section class="row q-pb-none">
        <div class="col-6">
          <div class="container-details">
            <label class="text-body1 text-bold full-width">
              Item Barcode:
            </label>
            <p class="text-body1">
              {{ refileItem.barcode?.value }}
            </p>
          </div>
        </div>
        <div class="col-6">
          <div class="container-details">
            <label class="text-body1 text-bold full-width">
              Owner:
            </label>
            <p class="text-body1">
              {{ refileItem.owner?.name }}
            </p>
          </div>
        </div>
        <div class="col-6">
          <div class="container-details">
            <label class="text-body1 text-bold">
              Module:
            </label>
            <p class="text-body1">
              {{ refileItem.tray ? refileItem.tray.shelf_position?.location?.split('-')[1] : refileItem.shelf_position?.location?.split('-')[1] }}
            </p>
          </div>
        </div>
        <div class="col-6">
          <div class="container-details">
            <label class="text-body1 text-bold">
              Aisle:
            </label>
            <p class="text-body1">
              {{ refileItem.tray ? refileItem.tray.shelf_position?.location?.split('-')[2] : refileItem.shelf_position?.location?.split('-')[2] }}
            </p>
          </div>
        </div>
      </q-card-section>
    </template>

    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <q-btn
          no-caps
          unelevated
          color="accent"
          label="Done"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="resetRefileItem(); hideModal();"
        />

        <q-space class="q-mx-xs" />

        <q-btn
          outline
          no-caps
          label="Cancel"
          class="text-body1 full-width"
          @click="resetRefileItem(); hideModal();"
        />
      </q-card-section>
    </template>
  </PopupModal>
</template>

<script setup>
import { ref, inject, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store'
import { useRefileStore } from '@/stores/refile-store'
import { useBarcodeStore } from '@/stores/barcode-store'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import PopupModal from '@/components/PopupModal.vue'
import BarcodeBox from '@/components/BarcodeBox.vue'

// Emits
const emit = defineEmits(['hide'])

// Compasables
const { compiledBarCode } = useBarcodeScanHandler()

// Store Data
const { appActionIsLoadingData } = storeToRefs(useGlobalStore())
const { getBarcodeDetails } = useBarcodeStore()
const { barcodeDetails } = storeToRefs(useBarcodeStore())
const { postRefileQueueItem, resetRefileItem } = useRefileStore()
const { refileItem } = storeToRefs(useRefileStore())

// Local Data
const showSuccessMesssage = ref(false)

// Logic
const handleAlert = inject('handle-alert')

watch(compiledBarCode, (barcode) => {
  if (barcode !== '') {
    triggerRefileItemScan(barcode)
  }
})
const triggerRefileItemScan = (barcode_value) => {
  if (barcode_value !== refileItem.value.barcode.value) {
    addItemToQueue(barcode_value)
  } else {
    handleAlert({
      type: 'error',
      text: 'The scanned item barcode was already added. Please try again!',
      autoClose: true
    })
  }
}

const addItemToQueue = async (barcode_value) => {
  try {
    appActionIsLoadingData.value = true
    // check if the scanned item barcode is in the system first
    await getBarcodeDetails(barcode_value)

    // next check if scanned barcode is an item type barcode
    if (barcodeDetails.value.id && barcodeDetails.value.type.name !== 'Item') {
      handleAlert({
        type: 'error',
        text: 'The scanned barcode is not an "Item Barcode"! Please try again.',
        autoClose: true
      })
      return
    }

    const payload = {
      barcode_value
    }
    await postRefileQueueItem(payload)

    handleAlert({
      type: 'success',
      text: 'Successfully added an item to the Refile Queue! Scan another item when ready.',
      autoClose: true
    })
    showSuccessMesssage.value = true
    setTimeout(() => {
      showSuccessMesssage.value = false
    }, 2500)
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