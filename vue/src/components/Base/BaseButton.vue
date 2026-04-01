<template>
  <q-btn
    v-bind="$attrs"
    :class="computedClasses"
    :unelevated="!raised"
    :no-caps="noCaps"
    :color="computedColor"
    class="base-button"
  >
    <!-- Pass down all slots natively to the underlying q-btn -->
    <template
      v-for="(_, slot) in $slots"
      #[slot]="scope"
    >
      <slot
        :name="slot"
        v-bind="scope || {}"
      />
    </template>
  </q-btn>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // Defines the visual variant of the button
  variant: {
    type: String,
    default: 'primary' // can be primary, secondary, outline, ghost, danger
  },
  // Whether to apply standard Quasar elevation
  raised: {
    type: Boolean,
    default: false
  },
  // Disable automatic uppercase text
  noCaps: {
    type: Boolean,
    default: true
  }
})

// Automatically map semantic variant intents to Quasar standard colors
const computedColor = computed(() => {
  // If a color is manually passed in attrs, it takes precedence natively over this prop,
  // but if we need to enforce it, we can set it here.
  switch (props.variant) {
    case 'primary': return 'accent' // using the standard blue from quasar.variables
    case 'secondary': return 'secondary' // Slate 700
    case 'danger': return 'negative' // Red 700
    case 'ghost': return 'transparent'
    case 'outline': return 'transparent'
    default: return 'accent'
  }
})

// Compute additional classes, enforcing the custom modern box-shadow where appropriate
const computedClasses = computed(() => {
  return {
    'btn-modern': props.variant === 'primary' || props.variant === 'danger',
    'text-primary': props.variant === 'ghost' || props.variant === 'outline',
    'btn-outline': props.variant === 'outline'
  }
})
</script>

<style lang="scss" scoped>
/* Any highly specific base button CSS modifiers that shouldn't pollute global scopes */
.base-button {
  &.btn-outline {
    border: 1px solid var(--q-primary);
  }
}
</style>
