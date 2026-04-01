<template>
  <PopupModal
    @reset="handleClose"
    aria-label="AddRefileQueueItemModal"
    class="refile-add-queue-modal"
  >
    <template #header-content="{ hideModal }">
      <q-card-section class="row items-center q-pb-none">
        <div class="col">
          <h2 class="text-h6 text-bold q-ma-none">
            Add Item To Queue
          </h2>
          <div class="text-caption text-grey-7">
            Scan an item barcode to add it to the refile queue
          </div>
        </div>

        <BaseButton
          icon="close"
          flat
          round
          dense
          color="grey-7"
          class="q-ml-auto"
          @click="hideModal"
          aria-label="closeModal"
        />
      </q-card-section>
    </template>

    <template #main-content>
      <q-card-section class="q-pt-md">
        <!-- Physical/Manual Scan Input (Standardized pattern) -->
        <div class="scan-input-container q-mb-lg">
          <label class="form-group-label q-mb-xs">Scan or Enter Barcode</label>
          <q-input
            v-model="barcodeInput"
            outlined
            dense
            placeholder="Scan item barcode"
            @keyup.enter="handleManualScan"
            ref="scanInputRef"
            autofocus
            class="scan-input-modern"
          >
            <template #append>
              <q-icon
                name="qr_code_scanner"
                color="accent"
              />
            </template>
          </q-input>
        </div>

        <div class="scan-results-wrapper">
          <!-- Current Scan Section -->
          <div
            v-if="refileItem.id"
            class="current-scan-section q-mb-lg"
          >
            <div class="section-header row items-center q-mb-sm">
              <q-icon
                name="sensors"
                color="accent"
                size="xs"
                class="q-mr-xs"
              />
              <span class="text-overline text-accent text-bold">CURRENT SCAN</span>
            </div>

            <div class="item-info-card highlighted">
              <div class="item-info-grid">
                <div class="info-item">
                  <span class="info-label">Barcode</span>
                  <span class="info-value text-accent">{{ refileItem.barcode?.value || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Owner</span>
                  <span class="info-value">{{ refileItem.owner?.name || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Module</span>
                  <span class="info-value">{{ getModule(refileItem) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Aisle</span>
                  <span class="info-value">{{ getAisle(refileItem) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Last Scanned Section -->
          <div
            v-if="lastRefileItem.id"
            class="last-scanned-section"
          >
            <div class="section-header row items-center q-mb-sm">
              <q-icon
                name="history"
                color="grey-6"
                size="xs"
                class="q-mr-xs"
              />
              <span class="text-overline text-grey-7 text-bold">LAST SCANNED</span>
            </div>

            <div class="item-info-card secondary">
              <div class="item-info-grid">
                <div class="info-item">
                  <span class="info-label">Barcode</span>
                  <span class="info-value text-grey-8">{{ lastRefileItem.barcode?.value || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Owner</span>
                  <span class="info-value">{{ lastRefileItem.owner?.name || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Module</span>
                  <span class="info-value">{{ getModule(lastRefileItem) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">Aisle</span>
                  <span class="info-value">{{ getAisle(lastRefileItem) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div
            v-if="!refileItem.id && !lastRefileItem.id"
            class="text-center q-pa-xl empty-state"
          >
            <q-icon
              name="qr_code_scanner"
              size="4rem"
              color="grey-2"
            />
            <div class="text-grey-5 q-mt-md font-weight-bold">
              Waiting for initial scan...
            </div>
          </div>
        </div>
      </q-card-section>
    </template>

    <template #footer-content>
      <q-card-section class="row no-wrap justify-end items-center q-pt-none">
        <BaseButton
          flat
          no-caps
          label="Cancel"
          color="grey-8"
          class="btn-modern-flat q-mr-sm"
          @click="handleClose"
        />
        <BaseButton
          unelevated
          no-caps
          label="Done"
          color="accent"

          style="min-width: 120px;"
          @click="handleClose"
        />
      </q-card-section>
    </template>
  </PopupModal>
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
import { ref, watch, nextTick } from 'vue'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store'
import { useRefileStore } from '@/stores/refile-store'
import { useBarcodeStore } from '@/stores/barcode-store'
import { Notify } from 'quasar'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import PopupModal from '@/components/PopupModal.vue'

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
const barcodeInput = ref('')
const scanInputRef = ref(null)
const lastRefileItem = ref({})
const scanLock = ref(false)

// Logic
// const handleAlert = inject('handle-alert')

const handleClose = () => {
  resetRefileItem()
  emit('hide')
}

const getModule = (item) => {
  const loc = item?.tray ? item.tray.shelf_position?.location : item?.shelf_position?.location
  return loc ? loc.split('-')[1] : '-'
}

const getAisle = (item) => {
  const loc = item?.tray ? item.tray.shelf_position?.location : item?.shelf_position?.location
  return loc ? loc.split('-')[2] : '-'
}

watch(compiledBarCode, (barcode) => {
  if (barcode !== '' && !scanLock.value) {
    handleScan(barcode)
  }
})

const handleManualScan = () => {
  if (barcodeInput.value && !scanLock.value) {
    handleScan(barcodeInput.value)
    barcodeInput.value = ''
  }
}

const handleScan = (barcode_value) => {
  if (barcode_value !== refileItem.value?.barcode?.value) {
    addItemToQueue(barcode_value)
  } else {
    Notify.create({
      type: 'negative',
      message: 'The scanned item barcode was already added. Please try again!'
    })
  }
}

const addItemToQueue = async (barcode_value) => {
  try {
    scanLock.value = true
    appActionIsLoadingData.value = true
    // check if the scanned item barcode is in the system first
    await getBarcodeDetails(barcode_value)

    // next check if scanned barcode is an item type barcode
    if (barcodeDetails.value.id && barcodeDetails.value.type.name !== 'Item') {
      Notify.create({
        type: 'negative',
        message: 'The scanned barcode is not an "Item Barcode"! Please try again.'
      })
      return
    }

    const payload = {
      barcode_value
    }

    // Capture current as "Last Scanned" before updating
    if (refileItem.value.id) {
      lastRefileItem.value = { ...refileItem.value }
    }

    await postRefileQueueItem(payload)

    Notify.create({
      type: 'positive',
      message: 'Item added to queue',
      position: 'top',
      timeout: 1000
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to add item to queue'
    })
  } finally {
    appActionIsLoadingData.value = false
    scanLock.value = false
    barcodeInput.value = ''
    nextTick(() => {
      scanInputRef.value?.focus()
    })
  }
}
</script>

<style lang="scss" scoped>
.refile-add-queue-modal {
  :deep(.q-dialog__inner) {
    padding: 24px;
  }
}

.form-group-label {
  display: block;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  color: #a0aec0;
  letter-spacing: 0.05em;
}

.scan-input-modern {
  :deep(.q-field__control) {
    border-radius: 12px;
    background: #f8fafc;
    transition: all 0.2s ease;
    &:hover {
      background: #f1f5f9;
    }
  }
}

.item-info-card {
  padding: 16px;
  border-radius: 16px;
  border: 1px solid #edf2f7;
  background: white;
  transition: all 0.3s ease;

  &.highlighted {
    border-color: rgba($accent, 0.3);
    background: linear-gradient(to bottom right, #ffffff, rgba($accent, 0.02));
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  }

  &.secondary {
    background: #f8fafc;
    border-color: #e2e8f0;
    opacity: 0.85;
  }
}

.item-info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-label {
  font-size: 0.65rem;
  color: #a0aec0;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 2px;
}

.info-value {
  font-size: 0.95rem;
  color: #2d3748;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-state {
  border: 2px dashed #e2e8f0;
  border-radius: 20px;
}

.btn-modern-flat {
  padding: 8px 16px;
  border-radius: 10px;
  font-weight: 600;
}

</style>