<template>
  <div
    class="barcode-box"
    :class="[compact ? 'barcode-box--compact' : '']"
  >
    <span class="barcode-box__label">{{ barcode }}</span>
  </div>
</template>

<script setup>
defineProps({
  barcode: {
    type: String,
    default: ''
  },
  compact: {
    type: Boolean,
    default: false
  },
  minHeight: {
    type: String,
    default: ''
  }
})
</script>

<style lang="scss" scoped>
.barcode-box {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  padding: 1rem 1.5rem;
  background: linear-gradient(135deg, $secondary 0%, darken($secondary, 8%) 100%);
  color: $color-white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  min-height: 4rem;

  &__label {
    font-size: 1.25rem;
    font-weight: 600;
    letter-spacing: 0.5px;
  }

  &--compact {
    padding: 0.75rem 1rem;
    min-height: 3rem;

    .barcode-box__label {
      font-size: 1rem;
    }
  }

  @media (max-width: $breakpoint-sm-min) {
    min-height: 3rem;
    padding: 0.75rem 1rem;

    &__label {
      font-size: 1rem;
    }
  }
}
</style>