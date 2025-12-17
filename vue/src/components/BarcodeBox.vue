<template>
  <div
    class="barcode text-h4 text-center"
    :style="[ minHeight ? `min-height:${minHeight};` : null]"
  >
    {{ barcode }}
  </div>
</template>

<script setup>
defineProps({
  barcode: {
    type: String,
    default: ''
  },
  minHeight: {
    type: String,
    default: ''
  }
})
</script>

<style lang="scss" scoped>
.barcode {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  min-height: 18rem;
  background-color: $secondary;
  color: $color-white;
  border-radius: 3px;

  @media (max-width: $breakpoint-sm-min) {
    min-height: 5rem;
  }
}
</style>