<template>
  <q-input
    v-bind="$attrs"
    class="base-input"
    :outlined="outlined"
    :dense="dense"
  >
    <!-- Pass down all slots natively to the underlying q-input -->
    <template
      v-for="(_, slot) in $slots"
      #[slot]="scope"
    >
      <slot
        :name="slot"
        v-bind="scope || {}"
      />
    </template>
  </q-input>
</template>

<script setup>
defineProps({
  outlined: {
    type: Boolean,
    default: true
  },
  dense: {
    type: Boolean,
    default: false
  }
})
</script>

<style lang="scss" scoped>
.base-input {
  /* Future standardized styling can go here */
}
</style>
