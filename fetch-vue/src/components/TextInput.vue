<template>
  <q-input
    :id="id"
    ref="inputRef"
    :dense="currentScreenSize == 'xs'"
    outlined
    :model-value="modelValue"
    @update:model-value="updateModelValue"
    :placeholder="placeholder"
    :disable="disabled"
    :inputmode="scanMode ? (keyboardEnabled ? 'numeric' : 'none') : inputmode"
    class="custom-text full-width"
    @click="scanMode && handleInputClick()"
    @blur="scanMode && handleInputBlur()"
  >
    <template #append>
      <slot name="append" />
    </template>
  </q-input>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'

// Props
defineProps({
  id: {
    type: String,
    default: undefined
  },
  modelValue: undefined,
  placeholder: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  inputmode: {
    type: String,
    default: 'text'
  },
  scanMode: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['update:modelValue'])

// Compasables
const { currentScreenSize } = useCurrentScreenSize()

// Logic
const updateModelValue = (value) => {
  emit('update:modelValue', value)
}

const inputRef = ref(null)
const keyboardEnabled = ref(false)

const handleInputClick = () => {
  if (!keyboardEnabled.value) {
    keyboardEnabled.value = true
    nextTick(() => {
      inputRef.value?.focus()
    })
  }
}

const handleInputBlur = () => {
  keyboardEnabled.value = false
}

const focus = () => {
  inputRef.value?.focus()
}

defineExpose({
  focus
})
</script>

<style lang="scss" scoped>
.custom-text {
  :deep(.q-field__control) {
    &::before {
      border-color: $color-black;
    }
  }

  &.q-field--disabled {
    :deep(.q-field__control) {
      &::before {
        border-color: rgba($color-black, .25);
      }
    }
  }
}
</style>