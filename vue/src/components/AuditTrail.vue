<template>
  <PopupModal
    ref="auditTrailModal"
    :show-actions="false"
    @reset="emit('hide')"
    aria-label="auditTrailModal"
  >
    <template #header-content="{ hideModal }">
      <q-card-section class="row items-center q-pb-none">
        <h2 class="text-h6 text-bold">
          View History
        </h2>

        <q-btn
          icon="close"
          flat
          round
          dense
          class="q-ml-auto"
          @click="hideModal"
          aria-label="closeModal"
        />
      </q-card-section>
    </template>
    <template #main-content>
      <q-card-section class="q-pb-none">
        <div class="col-auto flex justify-end">
          <DownloadExcel
            class="link text-body2 text-accent"
            :data="auditTrailData"
            type="csv"
            :name="`audit-trail-export-${mainProps.jobType}-job-${mainProps.jobId}.csv`"
            :worksheet="`Audit Trail - ${mainProps.jobType} Job ${mainProps.jobId}`"
            :escape-csv="false"
            aria-label="downloadAuditTrailLink"
          >
            Export Audit
          </DownloadExcel>
        </div>
        <EssentialTable
          :table-columns="tableColumns"
          :table-visible-columns="tableVisibleColumns"
          :table-data="auditTrailData"
          :hide-table-rearrange="true"
          :heading-row-class="'q-mb-xs-md q-mb-md-lg'"
          :heading-filter-class="currentScreenSize == 'xs' ? 'col-xs-6 q-mr-auto' : 'q-ml-auto'"
          :pagination-loading="appIsLoadingData"
          @selected-table-row="null"
        >
          <template #table-td="{ colName, value }">
            <span
              v-if="colName === 'updated_at'"
            >{{ formatDateTime(value).dateTime }}</span>
          </template>
        </EssentialTable>
      </q-card-section>
    </template>
    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <q-btn
          outline
          no-caps
          label="Close"
          class="text-body1 full-width"
          @click="hideModal"
        />
      </q-card-section>
    </template>
  </PopupModal>
</template>

<script setup>

// Imports
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import PopupModal from '@/components/PopupModal.vue'
import EssentialTable from '@/components/EssentialTable.vue'
import { useGlobalStore } from '@/stores/global-store'
import { useReportsStore } from '@/stores/reports-store'
import { ref, onMounted, inject } from 'vue'
import { storeToRefs } from 'pinia'

// Props
const mainProps = defineProps({
  jobType: {
    type: String,
    default: ''
  },
  jobId: {
    type: Number,
    default: null
  }
})

// Emits
const emit = defineEmits(['hide'])

// Composables
const { currentScreenSize } = useCurrentScreenSize()

// Store Data
const { appIsLoadingData } = storeToRefs(useGlobalStore())
const { auditTrailData } = storeToRefs(useReportsStore())
const { getAuditTrailData } = useReportsStore()

// Local Data
const tableColumns = ref([
  {
    name: 'updated_by',
    field: 'updated_by',
    label: 'User Updated',
    align: 'left',
    sortable: true
  },
  {
    name: 'last_action',
    field: 'last_action',
    label: 'Last Action',
    align: 'left',
    sortable: true
  },
  {
    name: 'updated_at',
    field: 'updated_at',
    label: 'Last Activity',
    align: 'left',
    sortable: true
  }
])
const tableVisibleColumns = ref([
  'updated_by',
  'last_action',
  'updated_at'
])

// Logic
const formatDateTime = inject('format-date-time')
onMounted( async () => {
  if (mainProps.jobType && mainProps.jobId) {
    appIsLoadingData.value = true
    await getAuditTrailData(mainProps.jobType, mainProps.jobId)
    appIsLoadingData.value = false
  }
})
</script>

<style lang="scss" scoped></style>
