<template>
  <PrintTemplate ref="printTemplate">
    <template #print-html>
      <article>
        <section>
          <header>
            <h1 class="text-h4 text-bold q-mb-sm">
              Job: {{ picklistJobDetails.id }}
            </h1>
          </header>
        </section>

        <q-space class="divider q-my-md" />

        <section>
          <p class="text-bold q-mb-sm">
            Building:
            {{ picklistJobDetails.building?.name }}
          </p>
          <p class="text-bold q-mb-sm">
            Picklist Job Completed Date:
            {{ picklistJobDetails.status == 'Completed' ? formatDateTime(picklistJobDetails.last_transition).date : '' }}
          </p>
          <p class="text-bold">
            Assigned User:
            {{
              picklistJobDetails.user
                ? picklistJobDetails.user.name
                : "No Assignee"
            }}
          </p>
        </section>

        <q-space class="divider q-my-md" />

        <section>
          <p class="text-h5 text-bold">
            Total Items: {{ renderTotalItems }}
          </p>
        </section>

        <q-space class="divider q-my-md" />

        <section>
          <p class="text-h4 text-bold q-mb-sm">
            Items in Job:
          </p>
          <table
            class="table-borderless"
          >
            <thead>
              <tr>
                <th>Barcode</th>
                <th>Tray Barcode</th>
                <th>Owner</th>
                <th>Size Class</th>
                <th>Item Location</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="rowItem in picklistJobItems"
                :key="rowItem.id"
              >
                <td>{{ rowItem.item ? renderItemBarcodeDisplay(rowItem.item) : renderItemBarcodeDisplay(rowItem.non_tray_item) }}</td>
                <td>{{ rowItem.item ? renderItemBarcodeDisplay(rowItem.item.tray) : '' }}</td>
                <td>{{ rowItem.item ? rowItem.item?.owner?.name : rowItem.non_tray_item?.owner?.name }}</td>
                <td>{{ rowItem.item ? rowItem.item?.size_class?.name : rowItem.non_tray_item?.size_class?.name }}</td>
                <td>{{ rowItem.item ? getItemLocation(rowItem.item.tray) : getItemLocation(rowItem.non_tray_item) }}</td>
              </tr>
            </tbody>
          </table>
        </section>
      </article>
    </template>
  </PrintTemplate>
</template>

<script setup>
import { ref, computed, inject } from 'vue'
import PrintTemplate from '@/components/PrintTemplate.vue'

// Props
const mainProps = defineProps({
  picklistJobDetails: {
    type: Object,
    required: true
  },
  picklistJobItems : {
    type: Array,
    required: true
  }
})

// Local Data
const printTemplate = ref(null)
const renderTotalItems = computed(() => {
  return mainProps.picklistJobItems.length
})

// Logic
const formatDateTime = inject('format-date-time')
const getItemLocation = inject('get-item-location')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')

const printBatchReport = () => {
  printTemplate.value.print()
}

defineExpose({ printBatchReport })
</script>
