<template>
  <PrintTemplate ref="printTemplate">
    <template #print-html>
      <article>
        <section>
          <header>
            <h1 class="text-h4 text-bold q-mb-sm">
              Job: {{ verificationJobDetails.workflow_id }}
            </h1>
          </header>
        </section>

        <q-space class="divider q-my-md" />

        <section>
          <p class="text-bold q-mb-sm">
            Verification Job Completed Date: {{ verificationJobDetails.status == 'Completed' ? formatDateTime(verificationJobDetails.last_transition).date : '' }}
          </p>
          <p class="text-bold">
            Verification Job User:
            {{
              verificationJobDetails.user
                ? verificationJobDetails.user.name
                : "No Assignee"
            }}
          </p>
        </section>

        <q-space class="divider q-my-md" />

        <section>
          <p class="text-h5 text-bold q-mb-sm">
            Total Trays: {{ verificationJobDetails.trays ? verificationJobDetails.trays.length : 0 }}
          </p>
          <p class="text-h5 text-bold q-mb-sm">
            Total Items: {{ renderTotalItems }}
          </p>
          <p class="text-h5 text-bold">
            Owner: {{ verificationJobDetails.owner?.name }}
          </p>
        </section>

        <q-space class="divider q-my-md" />

        <section>
          <p class="text-h4 text-bold q-mb-sm">
            Manifest:
          </p>
          <table
            v-if="verificationJobDetails.trayed == false"
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
                v-for="item in verificationJobDetails.non_tray_items"
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
                v-for="tray in verificationJobDetails.trays"
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
  verificationJobDetails: {
    type: Object,
    required: true
  }
})

// Local Data
const printTemplate = ref(null)
const renderTotalItems = computed(() => {
  if (mainProps.verificationJobDetails.trayed) {
    return mainProps.verificationJobDetails.items.length
  } else {
    return mainProps.verificationJobDetails.non_tray_items.length
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
    mainProps.verificationJobDetails.items.some(
      (itm) => itm.tray_id == trayData.id
    )
  ) {
    trayItemsById = mainProps.verificationJobDetails.items.filter(
      (itm) => itm.tray_id == trayData.id
    )
  }
  return trayItemsById.length
}

defineExpose({ printBatchReport })
</script>