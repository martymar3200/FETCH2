<template>
  <PrintTemplate ref="printTemplate">
    <template #print-html>
      <article>
        <section>
          <header>
            <h1 class="text-h4 text-bold q-mb-sm">
              Report: {{ reportDetails.type }}
            </h1>
          </header>
        </section>

        <q-space class="divider q-my-md" />

        <section>
          <table
            class="table-borderless"
          >
            <thead>
              <tr>
                <th
                  v-for="(data, i) in reportDetails.headers"
                  :key="i"
                >
                  {{ data.label }}
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(data, i) in reportDetails.data"
                :key="i"
              >
                <td
                  v-for="(header, j) in reportDetails.headers"
                  :key="j"
                >
                  {{ typeof header.field === 'function' ? header.field(data) : data[header.field] }}
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
import { ref } from 'vue'
import PrintTemplate from '@/components/PrintTemplate.vue'

// Props
defineProps({
  reportDetails: {
    type: Object,
    required: true
  }
})

// Local Data
const printTemplate = ref(null)

// Logic
const printReport = () => {
  printTemplate.value.print()
}

defineExpose({ printReport })
</script>
