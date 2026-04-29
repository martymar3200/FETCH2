<template>
  <PopupModal
    @reset="emit('hide')"
    aria-label="ItemDetailsScannerModal"
  >
    <template #header-content="{ hideModal }">
      <q-card-section class="row items-center q-pb-none">
        <h2 class="text-h6 text-bold">
          Item Lookup
        </h2>
        <BaseButton
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
            :barcode="scannedItemDetails.barcode?.value ?? 'Please Scan Item Barcode'"
            :min-height="'5rem'"
          />
        </div>
      </q-card-section>

      <q-card-section class="row q-pb-none">
        <div class="col-12">
          <div class="row full-width q-pb-sm">
            <label class="text-body1 text-bold full-width q-mr-sm">
              Full Location:
            </label>
            <p class="text-h4 text-accent text-bold">
              {{ itemLocation }}
            </p>
          </div>
        </div>
        <div class="col-6">
          <div class="row full-width q-pb-sm">
            <label class="text-body1 text-bold full-width q-mr-sm">
              Owner:
            </label>
            <p class="text-body1">
              {{ scannedItemDetails.owner?.name }}
            </p>
          </div>
        </div>
        <div class="col-6">
          <div class="row full-width q-pb-sm">
            <label class="text-body1 text-bold full-width q-mr-sm">
              Media Type:
            </label>
            <p class="text-body1">
              {{ scannedItemDetails.media_type?.name }}
            </p>
          </div>
        </div>
        <div class="col-6">
          <div class="row full-width q-pb-sm">
            <label class="text-body1 text-bold full-width q-mr-sm">
              Status:
            </label>
            <p
              v-if="scannedItemDetails.status"
              class="text-body1"
            >
              <span
                class="outline text-nowrap q-pa-xs"
                :class="scannedItemDetails.status == 'In' ? 'text-highlight' : scannedItemDetails.status == 'Out' ? 'text-highlight-negative' : 'text-highlight-warning'"
              >
                {{ scannedItemDetails.status }}
              </span>
            </p>
          </div>
        </div>
      </q-card-section>
    </template>

    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <BaseButton
          variant="primary"
          label="Done"
          class="text-body1 full-width"
          @click="hideModal"
        />
      </q-card-section>
    </template>
  </PopupModal>
</template>

<script setup>
import { watch, computed, inject } from 'vue'
import { notify } from '@/utils/notify'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store'
import { useRecordManagementStore } from '@/stores/record-management-store'
import { useBarcodeStore } from '@/stores/barcode-store' // Import the barcode store
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import PopupModal from '@/components/PopupModal.vue'
import BarcodeBox from '@/components/BarcodeBox.vue'
import BaseButton from '@/components/Base/BaseButton.vue'

// Emits
const emit = defineEmits(['hide'])

// Composables
const { compiledBarCode } = useBarcodeScanHandler()

// Store Data
const { appActionIsLoadingData } = storeToRefs(useGlobalStore())
const recordManagementStore = useRecordManagementStore()
const { getItemDetails } = recordManagementStore
const { itemDetails: scannedItemDetails } = storeToRefs(recordManagementStore)
// ======================================================
// ========= START: BARCODE STORE SETUP =================
// ======================================================
const barcodeStore = useBarcodeStore()
const { getBarcodeDetails } = barcodeStore
const { barcodeDetails } = storeToRefs(barcodeStore)
// ======================================================
// ========== END: BARCODE STORE SETUP ==================
// ======================================================

// Logic

const getItemLocation = inject('get-item-location')

const itemLocation = computed(() => {
  if (scannedItemDetails.value?.id) {
    return getItemLocation(scannedItemDetails.value) || 'Not Shelved'
  }
  return 'Scan an item...'
})

watch(compiledBarCode, (barcode) => {
  if (barcode !== '') {
    fetchItemLocation(barcode)
  }
})

const fetchItemLocation = async (barcodeValue) => {
  try {
    appActionIsLoadingData.value = true
    // Clear previous details first
    recordManagementStore.itemDetails = { id: null }

    // Step 1: Check if the barcode exists at all. This will throw an error if not found.
    await getBarcodeDetails(barcodeValue)

    // Step 2: Check if the barcode is actually for an "Item".
    if (barcodeDetails.value.id && barcodeDetails.value.type.name !== 'Item') {
      notify({
        type: 'negative',
        message: 'The scanned barcode is not an "Item Barcode"! Please try again.'
      })
      // We must throw an error here to stop execution and prevent getItemDetails from running.
      throw new Error('Not an item barcode')
    }

    // If the above checks pass, then we can safely get the full item details.
    await getItemDetails(barcodeValue)

  } catch (error) {
    // This block will now catch errors from getBarcodeDetails OR if we throw our own.
    // We check error.message to avoid showing our internal 'Not an item barcode' message.
    if (error.message !== 'Not an item barcode') {
      notify({
        type: 'negative',
        message: `Barcode ${barcodeValue} not in FETCH`,
        timeout: 0,
        actions: [
          {
            icon: 'close',
            color: 'white'
          }
        ]
      })
    }
  } finally {
    appActionIsLoadingData.value = false
  }
}
</script>