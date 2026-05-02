<template>
  <q-linear-progress
    :value="progressValue"
    :color="color"
    size="12px"
    class="q-mb-lg rounded-borders"
  >
    <template v-if="showLabel">
      <div class="absolute-full flex flex-center">
        <q-badge
          color="white"
          text-color="dark"
          :label="`${completed}/${total}`"
        />
      </div>
    </template>
  </q-linear-progress>
</template>

<script setup>
/**
 * JobProgressBar - Visual progress indicator for job completion
 *
 * Usage:
 * <JobProgressBar :completed="5" :total="10" />
 * <JobProgressBar :completed="shelvedCount" :total="totalCount" show-label />
 */
import { computed } from 'vue'

const props = defineProps({
  completed: {
    type: Number,
    required: true
  },
  total: {
    type: Number,
    required: true
  },
  color: {
    type: String,
    default: 'accent'
  },
  showLabel: {
    type: Boolean,
    default: false
  }
})

const progressValue = computed(() => {
  if (props.total === 0) {
    return 0
  }
  return props.completed / props.total
})
</script>
