<template>
  <q-select
    v-bind="$attrs"
    class="base-select"
    :outlined="outlined"
    :dense="dense"
    :options-dense="optionsDense"
  >
    <template
      v-for="(_, slot) in $slots"
      #[slot]="scope"
    >
      <slot
        :name="slot"
        v-bind="scope || {}"
      />
    </template>
  </q-select>
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
  },
  optionsDense: {
    type: Boolean,
    default: false
  }
})
</script>

<style lang="scss" scoped>
.base-select {
  /* Future standardized styling can go here */
}
</style>
