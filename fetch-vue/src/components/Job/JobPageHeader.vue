<template>
  <!-- Mobile Collapsible Header Card -->
  <q-card
    v-if="isMobile"
    flat
    bordered
    class="q-mb-md header-card-mobile bg-white"
  >
    <q-card-section class="q-pa-sm row items-center justify-between no-wrap">
      <div class="row items-center q-gutter-x-sm">
        <q-icon
          name="assignment"
          color="accent"
          size="sm"
        />
        <span class="text-weight-bold text-primary font-mono">
          Job <template v-if="jobId">#{{ jobId }}</template>
        </span>
        <q-badge
          v-if="status && isCollapsed"
          :color="statusColor"
          :label="status"
          size="xs"
        />
      </div>
      <div class="row items-center q-gutter-x-xs">
        <!-- Inline Menu on Mobile -->
        <BaseButton
          v-if="menuOptions.length > 0"
          flat
          round
          dense
          size="xs"
          icon="more_vert"
          color="grey-7"
        >
          <q-menu>
            <q-list style="min-width: 150px">
              <q-item
                v-for="opt in menuOptions"
                :key="opt.label"
                clickable
                v-close-popup
                :disable="opt.disabled"
                @click="opt.action"
              >
                <q-item-section
                  v-if="opt.icon"
                  avatar
                >
                  <q-icon
                    :name="opt.icon"
                    :color="opt.color || 'grey'"
                  />
                </q-item-section>
                <q-item-section :class="opt.disabled ? 'text-grey' : ''">
                  {{ opt.label }}
                </q-item-section>
              </q-item>
            </q-list>
          </q-menu>
        </BaseButton>
        <q-btn
          flat
          round
          dense
          size="sm"
          :icon="isCollapsed ? 'expand_more' : 'expand_less'"
          color="grey-7"
          @click="isCollapsed = !isCollapsed"
        />
      </div>
    </q-card-section>

    <!-- Expanded content on mobile -->
    <q-slide-transition>
      <div v-show="!isCollapsed">
        <q-separator />
        <q-card-section class="q-pa-md q-gutter-y-sm">
          <div class="row items-center justify-between no-wrap">
            <h2 class="text-h6 text-bold q-mb-none text-primary">
              {{ title }}
            </h2>
            <q-badge
              v-if="status"
              :color="statusColor"
              :label="status"
            />
          </div>
          <p
            v-if="subtitle"
            class="text-caption text-grey-7 q-mb-none"
          >
            {{ subtitle }}
          </p>
          <div
            v-if="$slots.actions"
            class="q-mt-sm row justify-end"
          >
            <slot name="actions" />
          </div>
        </q-card-section>
      </div>
    </q-slide-transition>
  </q-card>

  <!-- Desktop Header (Always Expanded) -->
  <div
    v-else
    class="job-header row q-mb-lg items-center"
  >
    <div class="col">
      <div class="row items-center">
        <!-- Three-dot menu -->
        <BaseButton
          v-if="menuOptions.length > 0"
          flat
          round
          dense
          icon="more_vert"
          class="job-header__menu q-mr-sm"
        >
          <q-menu>
            <q-list style="min-width: 150px">
              <q-item
                v-for="opt in menuOptions"
                :key="opt.label"
                clickable
                v-close-popup
                :disable="opt.disabled"
                @click="opt.action"
              >
                <q-item-section
                  v-if="opt.icon"
                  avatar
                >
                  <q-icon
                    :name="opt.icon"
                    :color="opt.color || 'grey'"
                  />
                </q-item-section>
                <q-item-section :class="opt.disabled ? 'text-grey' : ''">
                  {{ opt.label }}
                </q-item-section>
              </q-item>
            </q-list>
          </q-menu>
        </BaseButton>

        <!-- Title + Badge -->
        <h1 class="job-header__title text-h4 text-bold q-mb-none">
          {{ title }}
          <template v-if="jobId">
            #{{ jobId }}
          </template>
          <q-badge
            v-if="status"
            :color="statusColor"
            :label="status"
            class="q-ml-sm"
          />
        </h1>
      </div>
      <p
        v-if="subtitle"
        class="text-grey-7 q-mb-none"
      >
        {{ subtitle }}
      </p>
    </div>
    <div class="job-header__actions col-auto">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import BaseButton from '@/components/Base/BaseButton.vue'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
/**
 * JobPageHeader - Reusable header component for all job pages
 *
 * Usage:
 * <JobPageHeader
 *   title="Shelving Job"
 *   :job-id="123"
 *   status="Running"
 *   status-color="info"
 *   subtitle="5/10 shelved • PreAssigned Mode"
 *   :menu-options="[{ label: 'Cancel', icon: 'cancel', color: 'negative', action: cancelJob }]"
 * >
 *   <template #actions>
 *     <BaseButton label="Start" color="accent" @click="startJob" />
 *   </template>
 * </JobPageHeader>
 */

const { currentScreenSize } = useCurrentScreenSize()
const isMobile = computed(() => currentScreenSize.value === 'xs')
const isCollapsed = ref(true)

defineProps({
  title: {
    type: String,
    required: true
  },
  jobId: {
    type: [
      Number,
      String
    ],
    default: null
  },
  status: {
    type: String,
    default: ''
  },
  statusColor: {
    type: String,
    default: 'grey'
  },
  subtitle: {
    type: String,
    default: ''
  },
  menuOptions: {
    type: Array,
    default: () => []
    // Each option: { label: string, icon?: string, color?: string, action: function, disabled?: boolean }
  }
})
</script>

<style lang="scss" scoped>
.job-header {
  @media (max-width: 599px) {
    flex-direction: column;
    align-items: flex-start;
  }

  &__title {
    @media (max-width: 599px) {
      font-size: 1.25rem;
      line-height: 1.4;
    }
  }

  &__actions {
    @media (max-width: 599px) {
      width: 100%;
      margin-top: 8px;
    }
  }

  &__menu {
    @media (max-width: 599px) {
      padding: 4px;
      min-height: unset;
      font-size: 10px;

      :deep(.q-icon) {
        font-size: 20px;
      }
    }
  }
}
</style>

