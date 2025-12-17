<template>
  <q-item
    :clickable="!disabled"
    tag="a"
    role="link"
    :to="link"
    class="essential-link"
    :dense="dense"
    :class="disabled ? 'disabled' : ''"
    :aria-disabled="disabled"
    @click="emit('click')"
  >
    <q-item-section
      v-if="icon"
      avatar
      :style="{'padding': iconPadding}"
    >
      <q-icon
        :name="icon"
        :size="iconSize"
        role="img"
        :aria-label="`${icon}-icon`"
      />
    </q-item-section>

    <q-item-section>
      <q-item-label>
        <span>{{ title }}</span>
      </q-item-label>
      <q-item-label
        v-if="caption !== ''"
        caption
      >
        {{ caption }}
      </q-item-label>
    </q-item-section>

    <q-item-section
      v-if="iconRight"
      avatar
      :style="{'padding': iconRightPadding}"
    >
      <q-icon
        :name="iconRight"
        :size="iconRightSize"
        role="img"
        :aria-label="`${iconRight}-icon`"
      />
    </q-item-section>
  </q-item>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  caption: {
    type: String,
    default: ''
  },
  link: {
    type: String,
    default: null
  },
  icon: {
    type: String,
    default: ''
  },
  iconSize: {
    type: String,
    default: '24px'
  },
  iconPadding: {
    type: String,
    default: '0px 16px 0px 0px'
  },
  iconRight: {
    type: String,
    default: ''
  },
  iconRightSize: {
    type: String,
    default: '24px'
  },
  iconRightPadding: {
    type: String,
    default: '0px 0px 0px 16px'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  dense: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])
</script>

<style lang="scss" scoped>
.essential-link {
  :deep(div.q-item__section--avatar) {
    min-width: initial;
  }
}
</style>
