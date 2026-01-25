<template>
  <div class="row q-mb-lg items-center">
    <div class="col">
      <div class="row items-center">
        <!-- Three-dot menu -->
        <q-btn
          v-if="menuOptions.length > 0"
          flat
          round
          dense
          icon="more_vert"
          class="q-mr-sm"
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
        </q-btn>

        <!-- Title + Badge -->
        <h1 class="text-h4 text-bold q-mb-none">
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
    <div class="col-auto">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup>
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
 *     <q-btn label="Start" color="accent" @click="startJob" />
 *   </template>
 * </JobPageHeader>
 */

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
