<template>
  <div class="mobile-actions">
    <q-btn
      no-caps
      unelevated
      :icon="buttonOneIcon"
      :color="buttonOneColor"
      :label="buttonOneLabel"
      class="btn-no-wrap text-body1 full-width"
      :class="buttonOneIcon == 'none' ? 'btn-hide-icon' : null"
      :outline="buttonOneOutline"
      :disabled="buttonOneDisabled"
      :loading="buttonOneLoading"
      @click="emit('buttonOneClick', buttonOneLabel)"
    />

    <q-space class="q-mx-xs" />

    <q-btn
      no-caps
      unelevated
      :icon="buttonTwoIcon"
      :color="buttonTwoColor"
      :label="buttonTwoLabel"
      class="btn-no-wrap text-body1 full-width"
      :class="buttonTwoIcon == 'none' ? 'btn-hide-icon' : null"
      :outline="buttonTwoOutline"
      :disabled="buttonTwoDisabled"
      :loading="buttonTwoLoading"
      @click="emit('buttonTwoClick', buttonTwoLabel)"
    />
  </div>
</template>

<script setup>
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
  padding: 8px;
  background-color: $color-white;
  z-index: 2000;

  &::before {
    content: "";
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    box-shadow: 0 0 10px 2px rgba(0, 0, 0, 0.1), 0 0px 10px rgba(0, 0, 0, 0.12);
  }
}
</style>