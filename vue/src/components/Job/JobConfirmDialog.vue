<template>
  <q-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <q-card style="min-width: 350px">
      <q-card-section>
        <div class="text-h6">
          {{ title }}
        </div>
      </q-card-section>
      <q-card-section class="q-pt-none">
        <p v-if="message">
          {{ message }}
        </p>
        <p
          v-if="warning"
          class="text-negative"
        >
          {{ warning }}
        </p>
        <slot />
      </q-card-section>
      <q-card-actions
        v-if="completeJobMode"
        class="row justify-evenly q-gutter-sm q-pt-sm"
      >
        <BaseButton
          no-caps
          unelevated
          color="positive"
          label="Complete"
          class="col-grow text-body1"
          :loading="loading"
          :disable="loading"
          @click="$emit('confirm', false)"
        />
        <BaseButton
          no-caps
          unelevated
          color="positive"
          label="Complete & Print"
          class="col-grow text-body1"
          :loading="loading"
          :disable="loading"
          @click="$emit('confirm', true)"
        />
        <BaseButton
          outline
          no-caps
          :label="cancelLabel"
          class="col-grow text-body1"
          v-close-popup
        />
      </q-card-actions>
      <q-card-actions
        v-else
        align="right"
      >
        <BaseButton
          flat
          :label="cancelLabel"
          color="grey"
          v-close-popup
        />
        <BaseButton
          unelevated
          :label="confirmLabel"
          :color="confirmColor"
          :loading="loading"
          :disable="loading"
          @click="$emit('confirm')"
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
/**
 * JobConfirmDialog - Reusable confirmation dialog for job actions
 *
 * Usage:
 * <JobConfirmDialog
 *   v-model="showCancelDialog"
 *   title="Cancel Job?"
 *   message="Are you sure you want to cancel this job?"
 *   warning="All progress will be lost."
 *   confirm-label="Yes, Cancel"
 *   confirm-color="negative"
 *   :loading="cancelling"
 *   @confirm="confirmCancel"
 * />
 */
defineProps({
  modelValue: {
    type: Boolean,
    required: true
  },
  title: {
    type: String,
    required: true
  },
  message: {
    type: String,
    default: ''
  },
  warning: {
    type: String,
    default: ''
  },
  confirmLabel: {
    type: String,
    default: 'Confirm'
  },
  confirmColor: {
    type: String,
    default: 'positive'
  },
  cancelLabel: {
    type: String,
    default: 'Cancel'
  },
  loading: {
    type: Boolean,
    default: false
  },
  completeJobMode: {
    type: Boolean,
    default: false
  }
})

defineEmits([
  'update:modelValue',
  'confirm'
])
</script>
