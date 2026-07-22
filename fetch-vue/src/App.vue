<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { useQuasar, LocalStorage } from 'quasar'

const $q = useQuasar()

onMounted(() => {
  const savedTheme = LocalStorage.getItem('themePreference') || 'auto'
  if (savedTheme === 'dark') {
    $q.dark.set(true)
  } else if (savedTheme === 'light') {
    $q.dark.set(false)
  } else {
    $q.dark.set('auto')
  }
})
</script>