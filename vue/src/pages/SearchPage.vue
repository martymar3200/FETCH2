<template>
  <q-page
    :style-fn="handlePageOffset"
    padding
    class="search"
  >
    <LoadingOverlay />

    <SearchAdvancedResults />
  </q-page>
</template>

<script setup>
import { inject } from 'vue'
import LoadingOverlay from '@/components/LoadingOverlay.vue'
import SearchAdvancedResults from '@/components/Search/SearchAdvancedResults.vue'

// Logic
const handlePageOffset = inject('handle-page-offset')
</script>

<style lang="scss" scoped>
</style>
