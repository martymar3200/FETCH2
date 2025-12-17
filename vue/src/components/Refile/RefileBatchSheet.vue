<template>
  <PrintTemplate ref="printTemplate">
    <template #print-html>
      <article>
        <section>
          <header>
            <h1 class="text-h4 text-bold q-mb-sm">
              Job: {{ refileJobDetails.id }}
            </h1>
          </header>
        </section>

        <q-space class="divider q-my-md" />

        <section>
          <p class="text-bold q-mb-sm">
            Refile Job Created Date: {{ formatDateTime(refileJobDetails.create_dt).date }}
          </p>
          <p class="text-bold">
            Refile Job User:
            {{
              refileJobDetails.assigned_user
                ? refileJobDetails.assigned_user.name
                : "No Assignee"
            }}
          </p>
        </section>

        <q-space class="divider q-my-md" />

        <section>
          <p class="text-h4 text-bold q-mb-sm">
            Manifest:
          </p>
          <table
            class="table-borderless"
          >
            <thead>
              <tr>
                <th>Item Location</th>
                <th>Tray Barcode</th>
                <th>Barcode</th>
                <th>Owner</th>
                <th>Size Class</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="item in mainProps.refileJobDetails.refile_job_items"
                :key="item.id"
              >
                <td>{{ item.tray ? item.tray.shelf_position?.location : item.shelf_position?.location }}</td>
                <td>{{ item.tray ? renderItemBarcodeDisplay(item.tray) : '' }}</td>
                <td>{{ renderItemBarcodeDisplay(item) }}</td>
                <td>{{ item.owner?.name }}</td>
                <td>{{ item.size_class?.name }}</td>
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
const mainProps = defineProps({
  refileJobDetails: {
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
