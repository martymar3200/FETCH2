<template>
  <BaseButton
    flat
    icon="more_vert"
    class="more-menu"
    aria-label="optionsMenu"
    aria-haspopup="menu"
    :aria-expanded="optionMenuState"
  >
    <q-menu
      @show="optionMenuState = true"
      @hide="optionMenuState = false"
      aria-label="optionsMenuList"
    >
      <q-list class="more-menu-list">
        <q-item
          v-for="(opt, i) in options.filter(opt => !opt.hidden)"
          :key="i"
          :clickable="!opt.disabled"
          v-close-popup="!opt.disabled"
          @click="!opt.disabled ? emit('click', opt) : null"
          :class="[opt.disabled ? 'disabled' : '', opt.optionClass ?? '']"
          role="menuitem"
        >
          <q-item-section>
            {{ opt.text }}
          </q-item-section>
          <q-tooltip v-if="opt.tooltip">
            {{ opt.tooltip }}
          </q-tooltip>
        </q-item>
      </q-list>
    </q-menu>
  </BaseButton>
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
import { ref } from 'vue'

defineProps({
  options: {
    type: Array,
    required: true
  }
})

const emit = defineEmits(['click'])

const optionMenuState = ref(false)
</script>

<style lang="scss" scoped>
.more-menu {
  padding: 0;

  &-list {
    min-width: 100px;
  }
}
</style>