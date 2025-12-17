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
            Total Trays: {{ shelvingJobDetails.trays ? shelvingJobDetails.trays.length : 0 }}
          </p>
          <p class="text-h5 text-bold q-mb-sm">
            Total Non-Trays: {{ shelvingJobDetails.non_tray_items ? shelvingJobDetails.non_tray_items.length : 0 }}
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
                v-for="containers in shelvingJobDetails.trays"
                :key="containers.id"
              >
                <td>{{ renderItemBarcodeDisplay(containers) }}</td>
                <td>{{ containers.shelf_position?.location?.split('-')[1] }}</td>
                <td>{{ containers.shelf_position?.location?.split('-')[2] }}</td>
                <td>{{ containers.shelf_position?.location?.split('-')[3] }}</td>
                <td>{{ containers.shelf_position?.location?.split('-')[4] }}</td>
                <td>{{ containers.shelf_position?.location?.split('-')[5] }}</td>
                <td>{{ containers.shelf_position?.location?.split('-')[6] }}</td>
              </tr>
              <tr
                v-for="containers in shelvingJobDetails.non_tray_items"
                :key="containers.id"
              >
                <td>{{ renderItemBarcodeDisplay(containers) }}</td>
                <td>{{ containers.shelf_position?.location?.split('-')[1] }}</td>
                <td>{{ containers.shelf_position?.location?.split('-')[2] }}</td>
                <td>{{ containers.shelf_position?.location?.split('-')[3] }}</td>
                <td>{{ containers.shelf_position?.location?.split('-')[4] }}</td>
                <td>{{ containers.shelf_position?.location?.split('-')[5] }}</td>
                <td>{{ containers.shelf_position?.location?.split('-')[6] }}</td>
              </tr>
            </tbody>
          </table>
        </section>
      </article>
    </template>
  </PrintTemplate>
</template>

<script setup>
import { ref, inject } from 'vue'
import PrintTemplate from '@/components/PrintTemplate.vue'
// Props
defineProps({
  shelvingJobDetails: {
    type: Object,
    required: true
  }
})

// Local Data
const printTemplate = ref(null)

// Logic
const formatDateTime = inject('format-date-time')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')

const printBatchReport = () => {
  printTemplate.value.print()
}

defineExpose({ printBatchReport })
</script>
