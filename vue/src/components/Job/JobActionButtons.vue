<template>
  <div class="row q-gutter-sm">
    <!-- Start button -->
    <q-btn
      v-if="showStart"
      no-caps
      unelevated
      color="accent"
      label="Start Job"
      class="btn-modern"
      :loading="loading"
      @click="$emit('start')"
    />

    <!-- Pause button -->
    <q-btn
      v-if="showPause"
      no-caps
      flat
      color="warning"
      label="Pause"
      :loading="loading"
      @click="$emit('pause')"
    />

    <!-- Resume button -->
    <q-btn
      v-if="showResume"
      no-caps
      unelevated
      color="accent"
      label="Resume"
      class="btn-modern"
      :loading="loading"
      @click="$emit('resume')"
    />

    <!-- Complete button -->
    <q-btn
      v-if="showComplete"
      no-caps
      unelevated
      color="positive"
      label="Complete Job"
      class="btn-modern"
      :loading="loading"
      @click="$emit('complete')"
    />

    <!-- Custom slot for additional buttons -->
    <slot />
  </div>
</template>

<script setup>
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
