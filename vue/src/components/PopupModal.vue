<template>
  <q-dialog
    v-model="showPopupModal"
    persistent
    @hide="emit('reset')"
  >
    <q-card
      class="popup-modal"
      :style="{ width: modalWidth, maxWidth: '90vw' }"
    >
      <!-- header section -->
      <slot
        name="header-content"
        :hide-modal="hideModal"
      >
        <q-card-section class="row items-center justify-between q-pb-none">
          <h2 class="text-h6">
            {{ title }}
          </h2>

          <BaseButton
            icon="close"
            flat
            round
            dense
            @click="showPopupModal = false"
            aria-label="closeModal"
          />
        </q-card-section>
      </slot>

      <!-- main content -->
      <slot
        name="main-content"
        :hide-modal="hideModal"
      >
        <q-card-section>
          <p>{{ text }}</p>
        </q-card-section>
      </slot>

      <!-- footer section -->
      <slot
        name="footer-content"
        :hide-modal="hideModal"
      >
        <q-card-section
          v-if="showActions"
          class="row no-wrap justify-between items-center"
        >
          <BaseButton
            variant="primary"
            label="Confirm"
            class="text-body1 full-width"
            :loading="appActionIsLoadingData"
            @click="emit('confirm'); showPopupModal = false;"
            aria-label="confirmButton"
          />

          <q-space class="q-mx-xs" />

          <BaseButton
            variant="outline"
            label="Cancel"
            class="text-body1 full-width"
            @click="showPopupModal = false"
            aria-label="cancelButton"
          />
        </q-card-section>
      </slot>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store'
import BaseButton from '@/components/Base/BaseButton.vue'

// Props
defineProps({
  title: {
    type: String,
    default: ''
  },
  text: {
    type: String,
    default: ''
  },
  showActions: {
    type: Boolean,
    default: true
  },
  modalWidth: {
    type: String,
    default: '500px'
  }
})

// Emits
const emit = defineEmits([
  'reset',
  'confirm'
])

// Store Data
const { appActionIsLoadingData } = storeToRefs(useGlobalStore())

// Local Data
const showPopupModal = ref(true)

// Logic
const hideModal = () => {
  showPopupModal.value = false
}
defineExpose({ hideModal })
</script>