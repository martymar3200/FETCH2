<template>
  <q-page
    :style-fn="handlePageOffset"
    padding
    class="flex flex-center column"
  >
    <h1 v-if="userData.user_id">
      Welcome, {{ userData.first_name }} {{ userData.last_name }}!
    </h1>
  </q-page>
</template>

<script setup>
import { inject } from 'vue'
import { storeToRefs } from 'pinia'
import { useUserStore } from '@/stores/user-store'

// Store Data
const { userData } = storeToRefs(useUserStore())

// Logic
const handlePageOffset = inject('handle-page-offset')

</script>

<style lang="scss" scoped>
h1 {
  font-size: 30px;
  color: $primary;
}

.demo {
  display: inline-block;
  width: max-content;
  min-width: 300px;
  list-style: none;
  background-color: $color-gray;
  border-radius: 4px;
  padding: 0.5rem;
}
</style>
