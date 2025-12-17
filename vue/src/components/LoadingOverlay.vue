<template>
  <div
    v-if="appIsLoadingData || pageInitLoading || propLoading"
    class="overlay"
  >
    <q-spinner-bars
      color="primary"
      size="2rem"
      class="overlay-loading"
    />
  </div>
</template>

<script setup>
import { useGlobalStore } from '@/stores/global-store'
import { storeToRefs } from 'pinia'
// Props
defineProps({
  // used for unit test or forced loading scenarios
  propLoading: {
    type: Boolean,
    default: false
  }
})
// Store Data
const { appIsLoadingData, pageInitLoading } = storeToRefs(useGlobalStore())
</script>

<style lang="scss" scoped>
.overlay-loading {
  z-index: 1;
}
</style>