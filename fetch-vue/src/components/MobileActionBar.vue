<template>
  <div class="mobile-actions">
    <BaseButton
      v-if="buttonOneLabel"
      no-caps
      unelevated
      :icon="buttonOneIcon"
      :color="buttonOneColor"
      :label="buttonOneLabel"
      class="btn-no-wrap text-body1 full-width text-weight-bold"
      :class="buttonOneIcon == 'none' ? 'btn-hide-icon' : null"
      :outline="buttonOneOutline"
      :disabled="buttonOneDisabled"
      :loading="buttonOneLoading"
      @click="emit('buttonOneClick', buttonOneLabel)"
    />

    <BaseButton
      v-if="buttonTwoLabel"
      no-caps
      unelevated
      :icon="buttonTwoIcon"
      :color="buttonTwoColor"
      :label="buttonTwoLabel"
      class="btn-no-wrap text-body1 full-width text-weight-bold"
      :class="buttonTwoIcon == 'none' ? 'btn-hide-icon' : null"
      :outline="buttonTwoOutline"
      :disabled="buttonTwoDisabled"
      :loading="buttonTwoLoading"
      @click="emit('buttonTwoClick', buttonTwoLabel)"
    />
  </div>
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
//Props
defineProps({
  buttonOneIcon: {
    type: String,
    default: 'none'
  },
  buttonOneColor: {
    type: String,
    default: 'accent'
  },
  buttonOneLabel: {
    type: String,
    default: 'cancel'
  },
  buttonOneOutline: {
    type: Boolean,
    default: true
  },
  buttonOneDisabled: {
    type: Boolean,
    default: false
  },
  buttonOneLoading: {
    type: Boolean,
    default: false
  },
  buttonTwoIcon: {
    type: String,
    default: 'none'
  },
  buttonTwoColor: {
    type: String,
    default: 'accent'
  },
  buttonTwoLabel: {
    type: String,
    default: 'submit'
  },
  buttonTwoOutline: {
    type: Boolean,
    default: false
  },
  buttonTwoDisabled: {
    type: Boolean,
    default: false
  },
  buttonTwoLoading: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits([
  'buttonOneClick',
  'buttonTwoClick'
])
</script>

<style lang="scss" scoped>
.mobile-actions {
  position: fixed;
  bottom: 0;
  right: 0;
  left: 0;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 16px;
  background-color: white;
  z-index: 2000;
  border-top: 1px solid #e2e8f0;
}
</style>