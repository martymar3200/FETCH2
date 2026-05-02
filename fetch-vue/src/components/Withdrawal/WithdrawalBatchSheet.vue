<template>
  <PrintTemplate ref="printTemplate">
    <template #print-html>
      <article>
        <section>
          <header>
            <h1 class="text-h4 text-bold q-mb-sm">
              Job: {{ withdrawalJobDetails.id }}
            </h1>
          </header>
        </section>

        <q-space class="divider q-my-md" />

        <section>
          <p class="text-bold q-mb-sm">
            Withdrawal Job Completed Date: {{ withdrawalJobDetails.status === 'Completed' ? formatDateTime(withdrawalJobDetails.last_transition).date : '' }}
          </p>
          <p class="text-bold">
            Withdrawal Job User:
            {{
              withdrawalJobDetails.assigned_user
                ? withdrawalJobDetails.assigned_user.name
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
                <th>Item Barcode</th>
                <th>Item Status</th>
                <th>Tray Barcode</th>
                <th>Shelf Barcode</th>
                <th>Location</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="item in mainProps.withdrawalJobDetails.items"
                :key="item.id"
              >
                <td>{{ renderItemDetails(item, 'barcode') }}</td>
                <td>{{ renderItemDetails(item, 'status') }}</td>
                <td>{{ renderItemDetails(item, 'tray barcode') }}</td>
                <td>{{ renderItemDetails(item, 'shelf barcode') }}</td>
                <td>{{ renderItemDetails(item, 'location') }}</td>
              </tr>
              <tr
                v-for="item in mainProps.withdrawalJobDetails.non_tray_items"
                :key="item.id"
              >
                <td>{{ renderItemDetails(item, 'barcode') }}</td>
                <td>{{ renderItemDetails(item, 'status') }}</td>
                <td /> <!-- No tray barcode on non tray items -->
                <td>{{ renderItemDetails(item, 'shelf barcode') }}</td>
                <td>{{ renderItemDetails(item, 'location') }}</td>
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
  withdrawalJobDetails: {
    type: Object,
    required: true
  }
})

// Local Data
const printTemplate = ref(null)

// Logic
const formatDateTime = inject('format-date-time')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')
const renderWithdrawnTrayBarcode = inject('render-withdrawn-tray-barcode')
const renderWithdrawnShelfBarcode = inject('render-withdrawn-shelf-barcode')
const renderWithdrawnItemLocation = inject('render-withdrawn-item-location')

const printBatchReport = () => {
  printTemplate.value.print()
}

const renderItemDetails = (item, details) => {
  switch (details) {
    case 'barcode':
      return renderItemBarcodeDisplay(item)
    case 'status':
      return item?.status
    case 'tray barcode':
      return item.status === 'Withdrawn' ? renderWithdrawnTrayBarcode(item) : renderItemBarcodeDisplay(item?.tray)
    case 'shelf barcode':
      return item.status === 'Withdrawn' ? renderWithdrawnShelfBarcode(item) : renderItemBarcodeDisplay(item.tray ? item?.tray?.shelf_position?.shelf : item?.shelf_position?.shelf)
    case 'location':
      return renderWithdrawnItemLocation(item)
    default:
      return ''
  }
}

defineExpose({ printBatchReport })
</script>
