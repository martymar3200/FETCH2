<template>
  <PrintTemplate ref="printTemplate">
    <template #print-html>
      <article>
        <section>
          <header>
            <h1 class="text-h4 text-bold q-mb-sm">
              Job: {{ shelvingJobDetails.id }}
            </h1>
          </header>
        </section>

        <q-space class="divider q-my-md" />

        <section>
          <p class="text-bold q-mb-sm">
            Shelving Job Completed Date/Time: {{ shelvingJobDetails.status == 'Completed' ? formatDateTime(shelvingJobDetails.last_transition).dateTime : '' }}
          </p>
          <p class="text-bold q-mb-sm">
            Shelving Job Created Date/Time: {{ formatDateTime(shelvingJobDetails.create_dt).dateTime }}
          </p>
          <p class="text-bold">
            Shelving Job User: {{ shelvingJobDetails.user ? shelvingJobDetails.user.name : 'No Assignee' }}
          </p>
        </section>

        <q-space class="divider q-my-md" />

        <section>
          <p class="text-h5 text-bold q-mb-sm">
            Total Trays: {{ trayCount }}
          </p>
          <p class="text-h5 text-bold q-mb-sm">
            Total Non-Trays: {{ nonTrayCount }}
          </p>
          <p class="text-h5 text-bold">
            Building: {{ shelvingJobDetails.building?.name }}
          </p>
        </section>

        <q-space class="divider q-my-md" />

        <section>
          <p class="text-h4 text-bold q-mb-sm">
            Shelving List:
          </p>
          <table
            class="table-borderless"
          >
            <thead>
              <tr>
                <th>Barcode</th>
                <th>Module</th>
                <th>Aisle</th>
                <th>Side</th>
                <th>Ladder</th>
                <th>Shelf #</th>
                <th>Shelf Position</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="container in allContainers"
                :key="container.id"
              >
                <td>{{ renderItemBarcodeDisplay(container) }}</td>
                <td>{{ getLocation(container, 1) }}</td>
                <td>{{ getLocation(container, 2) }}</td>
                <td>{{ getLocation(container, 3) }}</td>
                <td>{{ getLocation(container, 4) }}</td>
                <td>{{ getLocation(container, 5) }}</td>
                <td>{{ getLocation(container, 6) }}</td>
              </tr>
            </tbody>
          </table>
        </section>
      </article>
    </template>
  </PrintTemplate>
</template>

<script setup>
import { ref, inject, computed } from 'vue'
import PrintTemplate from '@/components/PrintTemplate.vue'
// Props
const props = defineProps({
  shelvingJobDetails: {
    type: Object,
    required: true
  },
  // For Shelve by List jobs, containers are tracked separately
  containers: {
    type: Array,
    default: () => []
  }
})

// Local Data
const printTemplate = ref(null)

// Logic
const formatDateTime = inject('format-date-time')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')

// Computed: Support both data structures
// Direct to Shelf: uses shelvingJobDetails.trays and shelvingJobDetails.non_tray_items
// Shelve by List: uses containers prop
const allContainers = computed(() => {
  // If containers prop is provided and has data, use it (Shelve by List)
  if (props.containers && props.containers.length > 0) {
    return props.containers
  }
  // Otherwise fall back to trays + non_tray_items (Direct to Shelf)
  const trays = props.shelvingJobDetails?.trays || []
  const nonTrayItems = props.shelvingJobDetails?.non_tray_items || []
  return [
    ...trays,
    ...nonTrayItems
  ]
})

const trayCount = computed(() => {
  if (props.containers && props.containers.length > 0) {
    return props.containers.filter(c => c.container_type === 'Tray').length
  }
  return props.shelvingJobDetails?.trays?.length || 0
})

const nonTrayCount = computed(() => {
  if (props.containers && props.containers.length > 0) {
    return props.containers.filter(c => c.container_type === 'NonTrayItem').length
  }
  return props.shelvingJobDetails?.non_tray_items?.length || 0
})

// Helper function to get location parts from different data structures
const getLocation = (container, index) => {
  // Shelve by List style - uses actual_location or proposed_location
  if (container.actual_location) {
    return container.actual_location.split('-')[index] || ''
  }
  if (container.proposed_location) {
    return container.proposed_location.split('-')[index] || ''
  }
  // Direct to Shelf style - uses shelf_position.location
  if (container.shelf_position?.location) {
    return container.shelf_position.location.split('-')[index] || ''
  }
  return ''
}

const printBatchReport = () => {
  printTemplate.value.print()
}

defineExpose({ printBatchReport })
</script>
