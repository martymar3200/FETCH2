<template>
  <div class="row q-gutter-sm">
    <!-- Start button -->
    <BaseButton
      v-if="showStart"
      no-caps
      unelevated
      color="accent"
      label="Start Job"

      :loading="loading"
      @click="$emit('start')"
    />

    <!-- Pause button -->
    <BaseButton
      v-if="showPause"
      no-caps
      flat
      color="warning"
      label="Pause"
      :loading="loading"
      @click="$emit('pause')"
    />

    <!-- Resume button -->
    <BaseButton
      v-if="showResume"
      no-caps
      unelevated
      color="accent"
      label="Resume"

      :loading="loading"
      @click="$emit('resume')"
    />

    <!-- Complete button -->
    <BaseButton
      v-if="showComplete"
      no-caps
      unelevated
      color="positive"
      label="Complete Job"

      :loading="loading"
      @click="$emit('complete')"
    />

    <!-- Custom slot for additional buttons -->
    <slot />
  </div>
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
/**
 * JobActionButtons - Standard action buttons for job lifecycle
 *
 * Usage:
 * <JobActionButtons
 *   :status="job.status"
 *   :can-complete="allItemsProcessed"
 *   @start="startJob"
 *   @pause="pauseJob"
 *   @resume="resumeJob"
 *   @complete="completeJob"
 * />
 */
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: String,
    required: true
  },
  canComplete: {
    type: Boolean,
    default: true
  },
  loading: {
    type: Boolean,
    default: false
  }
})

defineEmits([
  'start',
  'pause',
  'resume',
  'complete'
])

const showStart = computed(() => props.status === 'Created' || props.status === 'Assigned')
const showPause = computed(() => props.status === 'Running')
const showResume = computed(() => props.status === 'Paused')
const showComplete = computed(() => props.status === 'Running' && props.canComplete)
</script>
