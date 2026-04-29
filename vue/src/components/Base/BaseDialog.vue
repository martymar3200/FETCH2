<template>
  <q-dialog
    v-bind="$attrs"
    class="base-dialog"
  >
    <q-card class="base-dialog-card">
      <q-card-section
        v-if="title"
        class="row items-center q-pb-none"
      >
        <div class="text-h6 text-weight-bold">
          {{ title }}
        </div>
        <q-space />
        <q-btn
          v-close-popup
          icon="mdi-close"
          flat
          round
          dense
        />
      </q-card-section>

      <q-card-section class="q-pt-md">
        <slot />
      </q-card-section>

      <q-card-actions
        v-if="$slots.actions"
        align="right"
        class="q-px-md q-pb-md"
      >
        <slot name="actions" />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  }
})
</script>

<style lang="scss" scoped>
.base-dialog-card {
  min-width: 400px;
  max-width: 90vw;
  border-radius: 8px;
}
</style>
