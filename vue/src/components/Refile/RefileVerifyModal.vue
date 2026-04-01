<template>
  <PopupModal
    @reset="handleClose"
    aria-label="RefileVerifyModal"
    class="refile-verify-modal"
  >
    <template #header-content="{ hideModal }">
      <q-card-section class="row items-center q-pb-none">
        <div class="col">
          <h2 class="text-h6 text-bold q-ma-none">
            Verify Refile Location
          </h2>
          <div class="text-caption text-grey-7">
            Scan the target barcode to verify and complete refile
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
        <!-- Item Progress/Details -->
        <div class="item-verification-card q-mb-lg highlighted">
          <div class="section-header row items-center q-mb-sm">
            <q-icon
              name="inventory_2"
              color="accent"
              size="xs"
              class="q-mr-xs"
            />
            <span class="text-overline text-accent text-bold">ITEM TO REFILE</span>
          </div>

          <div class="item-info-grid">
            <div class="info-item">
              <span class="info-label">Item Barcode</span>
              <span class="info-value">{{ item?.barcode?.value || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Target Location</span>
              <span class="info-value text-bold text-accent">{{ targetLocation }}</span>
            </div>
            <div
              v-if="targetTrayBarcode"
              class="info-item"
            >
              <span class="info-label">Target Tray</span>
              <span class="info-value">{{ targetTrayBarcode }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Owner</span>
              <span class="info-value">{{ item?.owner?.name || '-' }}</span>
            </div>
          </div>
        </div>

        <!-- Verification Input (Standardized pattern) -->
        <div class="scan-input-container">
          <label class="form-group-label q-mb-xs">
            Scan {{ targetTrayBarcode ? 'Tray' : 'Shelf' }} Barcode
          </label>
          <q-input
            v-model="verificationInput"
            outlined
            dense
            :placeholder="`Scan ${targetTrayBarcode ? 'tray' : 'shelf'} barcode`"
            @keyup.enter="handleManualScan"
            ref="scanInputRef"
            autofocus
            class="scan-input-modern"
            :error="!!error"
            :error-message="error"
          >
            <template #append>
              <q-icon
                name="qr_code_scanner"
                :color="error ? 'negative' : 'accent'"
              />
            </template>
          </q-input>
        </div>

        <div
          v-if="loading"
          class="row justify-center q-mt-md"
        >
          <q-spinner-dots
            color="accent"
            size="40px"
          />
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
      </q-card-section>
    </template>
  </PopupModal>
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import PopupModal from '@/components/PopupModal.vue'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'hide',
  'verify'
])

// Composables
const { compiledBarCode } = useBarcodeScanHandler()

// Local State
const verificationInput = ref('')
const scanInputRef = ref(null)
const error = ref('')

// Computed
const targetTrayBarcode = computed(() => props.item?.tray?.barcode?.value || null)
const targetLocation = computed(() => {
  return props.item?.tray?.shelf_position?.location || props.item?.shelf_position?.location || '-'
})

const targetVerifyBarcode = computed(() => {
  // If it's a tray item, we verify against the tray barcode.
  // Otherwise, we verify against the shelf barcode.
  if (targetTrayBarcode.value) {
    return targetTrayBarcode.value
  }
  return props.item?.shelf_position?.shelf?.barcode?.value || null
})

// Lifecycle
onMounted(() => {
  nextTick(() => {
    scanInputRef.value?.focus()
  })
})

// Logic
const handleClose = () => {
  emit('hide')
}

const handleManualScan = () => {
  if (verificationInput.value) {
    processScan(verificationInput.value)
  }
}

watch(compiledBarCode, (barcode) => {
  if (barcode !== '') {
    processScan(barcode)
  }
})

const processScan = (scannedBarcode) => {
  error.value = ''

  if (scannedBarcode === targetVerifyBarcode.value) {
    emit('verify', props.item)
    verificationInput.value = ''
  } else {
    error.value = `Incorrect ${targetTrayBarcode.value ? 'tray' : 'shelf'} barcode. Expected: ${targetVerifyBarcode.value}`
    verificationInput.value = ''
    nextTick(() => {
      scanInputRef.value?.focus()
    })
  }
}
</script>

<style lang="scss" scoped>
.refile-verify-modal {
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

.item-verification-card {
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

.btn-modern-flat {
  padding: 8px 16px;
  border-radius: 10px;
  font-weight: 600;
}
</style>
