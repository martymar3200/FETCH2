
<template>
  <PopupModal
    ref="clearBinModal"
    :show-actions="false"
    :modal-width="'500px'"
    @reset="resetModal"
    aria-label="clearBinModal"
  >
    <template #header-content="{ hideModal }">
      <q-card-section class="row items-center q-pb-none">
        <h2 class="text-h6 text-bold">
          Clear Shipping Bin
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
      <q-card-section>
        <div class="text-body1 q-mb-md">
          Scan a bin barcode to clear it (mark as empty/returned).
        </div>

        <div class="form-group">
          <label class="form-group-label">
            Bin Barcode
          </label>
          <TextInput
            ref="barcodeInput"
            v-model="barcode"
            placeholder="Scan Bin Barcode"
            :autofocus="true"
            @keydown.enter="submitClearBin"
          />
        </div>

        <div
          v-if="lastClearedBin"
          class="q-mt-md text-positive flex items-center"
        >
          <q-icon
            name="check_circle"
            size="sm"
            class="q-mr-sm"
          />
          <span>Success: Bin {{ lastClearedBin }} cleared!</span>
        </div>
      </q-card-section>
    </template>

    <template #footer-content>
      <q-card-section class="row justify-end q-pt-none">
        <BaseButton
          no-caps
          unelevated
          color="accent"
          label="Clear Bin"
          class="text-body1"
          :loading="loading"
          :disabled="!barcode"
          @click="submitClearBin"
        />
      </q-card-section>
    </template>
  </PopupModal>
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
import { ref, nextTick } from 'vue'
import { notify } from '@/utils/notify'
import { useShippingStore } from '@/stores/shipping-store'
import PopupModal from '@/components/PopupModal.vue'
import TextInput from '@/components/TextInput.vue'

const { clearBin } = useShippingStore()

const barcode = ref('')
const loading = ref(false)
const lastClearedBin = ref(null)
const barcodeInput = ref(null)

const resetModal = () => {
  barcode.value = ''
  lastClearedBin.value = null
  loading.value = false
}

const submitClearBin = async () => {
  if (!barcode.value) {
    return
  }

  loading.value = true
  lastClearedBin.value = null

  try {
    await clearBin(barcode.value)

    notify({
      type: 'positive',
      message: `Bin ${barcode.value} cleared successfully`
    })

    lastClearedBin.value = barcode.value
    barcode.value = ''

    // Updates
    // Keep modal open for continuous scanning? Yes.
    // Refocus input
    nextTick(() => {
      // focus logic if TextInput exposes it or if using native input
      // existing TextInput component usually supports v-model
    })

  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || 'Failed to clear bin'
    })
  } finally {
    loading.value = false
  }
}
</script>
