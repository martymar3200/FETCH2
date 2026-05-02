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
  min-height: 48px;
  border-radius: 8px; /* rounded-borders equivalent */
  margin: 4px 8px; /* space between links */
  transition: background-color 0.2s ease, opacity 0.2s ease;

  :deep(div.q-item__section--avatar) {
    min-width: initial;
  }

  /* Active state styling handled by parent or router-link-active class */
  &.q-router-link--active,
  &.nav-active {
    background-color: rgba(255, 255, 255, 0.1);
    font-weight: 500;
  }

  &:hover:not(.disabled) {
    background-color: rgba(255, 255, 255, 0.05);
  }
}
</style>
