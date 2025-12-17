<template>
  <PrintTemplate ref="printTemplate">
    <template #print-html>
      <article>
        <section>
          <header>
            <h1 class="text-h4 text-bold q-mb-sm">
              Job: {{ accessionJobDetails.workflow_id }}
            </h1>
          </header>
        </section>

        <q-space class="divider q-my-md" />

        <section>
          <p class="text-bold q-mb-sm">
            Accession Job Completed Date:
            {{ accessionJobDetails.status == 'Completed' ? formatDateTime(accessionJobDetails.last_transition).date : '' }}
          </p>
          <p class="text-bold">
            Accession Job User:
            {{
              accessionJobDetails.user
                ? accessionJobDetails.user.name
                : "No Assignee"
            }}
          </p>
        </section>

        <q-space class="divider q-my-md" />

        <section>
          <p class="text-h5 text-bold q-mb-sm">
            Total Trays:
            {{
              accessionJobDetails.trays ? accessionJobDetails.trays.length : 0
            }}
          </p>
          <p class="text-h5 text-bold q-mb-sm">
            Total Items: {{ renderTotalItems }}
          </p>
          <p class="text-h5 text-bold">
            Owner: {{ accessionJobDetails.owner?.name }}
          </p>
        </section>

        <q-space class="divider q-my-md" />

        <section>
          <p class="text-h4 text-bold q-mb-sm">
            Manifest:
          </p>
          <table
            v-if="accessionJobDetails.trayed == false"
            class="table-borderless"
          >
            <thead>
              <tr>
                <th>Barcode</th>
                <th>Size Class</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="item in accessionJobDetails.non_tray_items"
                :key="item.id"
              >
                <td>{{ renderItemBarcodeDisplay(item) }}</td>
                <td>{{ item.size_class?.name }}</td>
              </tr>
            </tbody>
          </table>
          <table
            v-else
            class="table-borderless"
          >
            <thead>
              <tr>
                <th>Barcode</th>
                <th>Size Class</th>
                <th># of Items</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="tray in accessionJobDetails.trays"
                :key="tray.id"
              >
                <td>{{ renderItemBarcodeDisplay(tray) }}</td>
                <td>{{ tray.size_class?.name }}</td>
                <td>{{ renderTrayItems(tray) }}</td>
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
  accessionJobDetails: {
    type: Object,
    required: true
  }
})

// Local Data
const printTemplate = ref(null)
const renderTotalItems = computed(() => {
  if (mainProps.accessionJobDetails.trayed) {
    return mainProps.accessionJobDetails.items.length
  } else {
    return mainProps.accessionJobDetails.non_tray_items.length
  }
})

// Logic
const formatDateTime = inject('format-date-time')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')

const printBatchReport = () => {
  printTemplate.value.print()
}
const renderTrayItems = (trayData) => {
  let trayItemsById = []
  if (
    mainProps.accessionJobDetails.items.some(
      (itm) => itm.tray_id == trayData.id
    )
  ) {
    trayItemsById = mainProps.accessionJobDetails.items.filter(
      (itm) => itm.tray_id == trayData.id
    )
  }
  return trayItemsById.length
}

defineExpose({ printBatchReport })
</script>
