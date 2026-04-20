<template>
  <PrintTemplate ref="printTemplate">
    <template #print-html>
      <article>
        <section>
          <header>
            <h1 class="text-h4 text-bold q-mb-sm">
              Shipping Manifest: #{{ manifestData.id }}
            </h1>
          </header>
        </section>

        <q-space class="divider q-my-md" />

        <section>
          <div class="row q-col-gutter-md">
            <div class="col-6">
              <p class="text-bold q-mb-sm">
                Completed Date:
                {{ manifestData.completed_dt ? formatDateTime(manifestData.completed_dt).dateTime : '-' }}
              </p>
            </div>
            <div class="col-6 text-right">
              <p class="text-h6 text-bold">
                Total Items: {{ totalItems }}
              </p>
            </div>
          </div>
        </section>

        <q-space class="divider q-my-md" />

        <section>
          <table class="table-borderless full-width">
            <thead>
              <tr>
                <th class="text-left">
                  Bin
                </th>
                <th class="text-left">
                  Destination
                </th>
                <th class="text-left">
                  Items
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="bin in manifestData.bins"
                :key="bin.id"
              >
                <td style="vertical-align: top; width: 20%;">
                  <strong>{{ bin.barcode }}</strong>
                </td>
                <td style="vertical-align: top; width: 25%;">
                  {{ bin.delivery_location?.name || '-' }}
                </td>
                <td style="vertical-align: top;">
                  <div
                    v-for="item in bin.items"
                    :key="item.id"
                    class="q-mb-xs"
                  >
                    ({{ item.barcode?.value }})
                  </div>
                </td>
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
const props = defineProps({
  manifestData: {
    type: Object,
    required: true
  }
})

// Local Data
const printTemplate = ref(null)

// Logic
const formatDateTime = inject('format-date-time')

const totalItems = computed(() => {
  return props.manifestData.bins?.reduce((sum, bin) => sum + (bin.items?.length || 0), 0) || 0
})

const printManifest = () => {
  printTemplate.value.print()
}

defineExpose({ printManifest })
</script>

<style lang="scss" scoped>
.table-borderless {
  width: 100%;
  border-collapse: collapse;

  th {
      text-align: left;
      border-bottom: 2px solid #000;
      padding: 8px 4px;
  }

  td {
      padding: 8px 4px;
      border-bottom: 1px solid #eee;
  }
}
.divider {
    border-bottom: 1px solid #000;
    margin: 1rem 0;
}
</style>
