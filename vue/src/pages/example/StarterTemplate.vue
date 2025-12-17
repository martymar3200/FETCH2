<template>
  <q-page
    :style-fn="handleHeaderOffset"
    padding
  >
    starting page/component, please copy and replace as needed
  </q-page>
</template>

<script setup>
import { ref, inject } from 'vue'
// Props
const mainProps = defineProps({
  templateProp: {
    type: String,
    default: ''
  }
})

// Emits
const emit = defineEmits(['click'])

// Compasables
const { TemplateComposable } = useTemplateComposable()

// Store Data
const { templateAction } = useTemplateStore()
const { templateState } = storeToRefs(useTemplateStore())

// Local Data
const templateData = ref(null)

// Logic
const handleHeaderOffset = inject('handle-offset')
const templateFunction = (value) => {
  return value
}
</script>

<style lang="scss" scoped>
</style>
