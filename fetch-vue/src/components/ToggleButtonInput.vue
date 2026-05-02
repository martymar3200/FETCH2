<template>
  <q-btn-toggle
    :model-value="modelValue"
    @update:model-value="updateModelValue"
    spread
    no-caps
    unelevated
    :toggle-color="toggleColor"
    :color="activeTextColor"
    :text-color="textColor"
    class="custom-toggle"
    :style="[ currentScreenSize == 'xs' ? 'height:40px;' : 'height:56px;' ]"
    :options="localOptions"
    :disable="disabled"
    aria-label="toggleOptionsGroup"
  >
    <template #left>
      <slot name="left" />
    </template>
    <template #right>
      <slot name="right" />
    </template>
  </q-btn-toggle>
</template>

<script setup>
import { computed, inject } from 'vue'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'

// Props
const mainProps = defineProps({
  modelValue: undefined,
  options: {
    type: Array,
    default () {
      return [
        {
          label: 'Yes',
          value: true
        },
        {
          label: 'No',
          value: false
        }
      ]
    }
  },
  optionValue: {
    // option can be either 'value' or 'value.value2.value3'
    type: String,
    default: ''
  },
  optionLabel: {
    // option can be either 'label' or 'label.label2.label3'
    type: null,
    default: ''
  },
  toggleColor: {
    type: String,
    default: 'accent'
  },
  activeTextColor: {
    type: String,
    default: 'white'
  },
  textColor: {
    type: String,
    default: 'black'
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['update:modelValue'])

// Compasables
const { currentScreenSize } = useCurrentScreenSize()

// Local Data
const localOptions = computed(() => {
  let formattedOptions = []
  // if we pass in a custom optionValue or optionLabel prop
  // then we know the options are not in the needed value, label format and has to be formatted
  if (mainProps.optionLabel !== '' || mainProps.optionValue !== '') {
    formattedOptions = mainProps.options.map(opt => {
      return {
        label: mainProps.optionLabel.includes('.') ?  getNestedKeyPath(opt, mainProps.optionLabel) : opt[mainProps.optionLabel],
        value: mainProps.optionValue.includes('.') ?  getNestedKeyPath(opt, mainProps.optionValue) : opt[mainProps.optionValue]
      }
    })
  } else {
    formattedOptions = mainProps.options
  }

  return formattedOptions
})

// Logic
const getNestedKeyPath = inject('get-nested-key-path')

const updateModelValue = (value) => {
  emit('update:modelValue', value)
}
</script>

<style lang="scss" scoped>
.custom-toggle {
  width: 100%;

  :deep(.q-btn) {
    border: 1px solid $accent;

    &:first-child {
      border-right-width: 0px;
    }
  }

  button[aria-pressed=true] {
    border-radius: 0;
  }
}
</style>
