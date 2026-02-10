<template>
  <div class="verification-dashboard">
    <div class="row q-mb-xs-xl q-mb-sm-none">
      <div class="col-grow q-mb-xs-md q-mb-sm-none">
        <EssentialTable
          ref="verificationTableRef"
          :table-columns="verificationTableColumns"
          :table-visible-columns="verificationTableVisibleColumns"
          :table-data="verificationJobList"
          :enable-table-reorder="false"
          :heading-row-class="'q-mb-xs-md q-mb-md-lg'"
          :enable-pagination="true"
          :pagination-total="verificationJobListTotal"
          :pagination-loading="appIsLoadingData"
          :hide-table-rearrange="true"
          @update-pagination="loadVerificationJobs($event)"
          @selected-table-row="loadVerificationJob($event.workflow_id)"
        >
          <template #heading-row>
            <div
              class="col-sm-5 col-md-12 col-lg-auto"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? '' : 'self-center'"
            >
              <h1 class="text-h4 text-bold">
                Verification Jobs
              </h1>
            </div>

            <div class="col-grow" />

            <div
              class="col-auto flex items-center"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? 'justify-end q-mb-md' : ''"
            >
              <q-btn
                flat
                dense
                no-caps
                :color="showFilterRow ? 'accent' : 'grey-7'"
                :label="showFilterRow ? 'Hide Filters' : 'Show Filters'"
                :icon="showFilterRow ? 'filter_alt' : 'filter_alt_off'"
                class="q-mr-sm"
                @click="showFilterRow = !showFilterRow"
              />
              <q-btn
                v-if="showFilterRow"
                flat
                dense
                no-caps
                color="grey-7"
                label="Clear"
                icon="clear_all"
                class="q-mr-md"
                @click="clearColumnFilters"
              />
            </div>
          </template>

          <!-- Filter row inside table header -->
          <template #header-filter-row="{ cols }">
            <q-tr
              v-if="showFilterRow"
              class="filter-row"
            >
              <q-th
                v-for="col in cols"
                :key="col.name"
                class="filter-cell"
              >
                <!-- Job Number filter -->
                <q-input
                  v-if="col.name === 'id'"
                  v-model="columnFilters.workflow_id"
                  dense
                  outlined
                  clearable
                  placeholder="Search..."
                  class="column-filter-input"
                  debounce="400"
                  @update:model-value="applyColumnFilters"
                  @click.stop
                >
                  <template #prepend>
                    <q-icon
                      name="search"
                      size="16px"
                      color="grey-6"
                    />
                  </template>
                </q-input>

                <!-- Job Type filter -->
                <q-select
                  v-else-if="col.name === 'trayed'"
                  v-model="columnFilters.trayed"
                  dense
                  outlined
                  clearable
                  emit-value
                  map-options
                  :options="jobTypeOptions"
                  placeholder="All"
                  class="column-filter-input"
                  @update:model-value="applyColumnFilters"
                  @click.stop
                />

                <!-- Status filter -->
                <q-select
                  v-else-if="col.name === 'status'"
                  v-model="columnFilters.status"
                  dense
                  outlined
                  clearable
                  multiple
                  emit-value
                  map-options
                  :options="statusOptions"
                  placeholder="All"
                  class="column-filter-input"
                  @update:model-value="applyColumnFilters"
                  @click.stop
                />
              </q-th>
            </q-tr>
          </template>

          <template #table-td="{ colName, value }">
            <span
              v-if="colName == 'status'"
              class="status-badge"
              :class="getStatusBadgeClass(value)"
            >
              <q-icon
                :name="getStatusIcon(value)"
                size="16px"
              />
              {{ value }}
            </span>
            <span
              v-else-if="colName == 'trayed'"
              class="text-secondary"
            >
              {{ value == true ? 'Trayed' : 'Non-Trayed' }}
            </span>
          </template>
        </EssentialTable>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onBeforeMount } from 'vue'
import { Notify } from 'quasar'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { useGlobalStore } from '@/stores/global-store'
import { useVerificationStore } from 'src/stores/verification-store'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import EssentialTable from '@/components/EssentialTable.vue'

const router = useRouter()

// Composables
const { currentScreenSize } = useCurrentScreenSize()

// Store Data
const { appIsLoadingData } = storeToRefs(useGlobalStore())
const {
  resetVerificationStore,
  getVerificationJobList,
  getVerificationJob
} = useVerificationStore()
const { verificationJobList, verificationJobListTotal } = storeToRefs(useVerificationStore())


// Local Data
const verificationTableRef = ref(null)
const showFilterRow = ref(false)  // Toggle visibility of filter row

// Column filter state for server-side filtering
const columnFilters = ref({
  workflow_id: null,
  trayed: null,
  status: [
    'Created',
    'Assigned',
    'Paused',
    'Running'
  ]  // Default to showing active jobs
})

// Filter dropdown options
const jobTypeOptions = [
  {
    label: 'Trayed',
    value: true
  },
  {
    label: 'Non-Trayed',
    value: false
  }
]
const statusOptions = [
  {
    label: 'Created',
    value: 'Created'
  },
  {
    label: 'Assigned',
    value: 'Assigned'
  },
  {
    label: 'Paused',
    value: 'Paused'
  },
  {
    label: 'Running',
    value: 'Running'
  },
  {
    label: 'Completed',
    value: 'Completed'
  }
]

const verificationTableVisibleColumns = ref([
  'id',
  'trayed',
  'status'
])
const verificationTableColumns = ref([
  {
    name: 'id',
    field: 'workflow_id',
    label: 'Job Number',
    align: 'left',
    sortable: true
  },
  {
    name: 'trayed',
    field: 'trayed',
    label: 'Job Type',
    align: 'left',
    sortable: true
  },
  {
    name: 'status',
    field: 'status',
    label: 'Status',
    align: 'left',
    sortable: true
  }
])

// Status badge helper functions
const getStatusIcon = (status) => {
  switch (status) {
    case 'Created':
      return 'mdi-plus-circle'
    case 'Assigned':
      return 'mdi-account-check'
    case 'Running':
      return 'mdi-play-circle'
    case 'Paused':
      return 'mdi-pause-circle'
    case 'Completed':
      return 'mdi-check-circle'
    default:
      return 'mdi-alert-circle'
  }
}

const getStatusBadgeClass = (status) => {
  switch (status) {
    case 'Created':
      return 'status-badge--created'
    case 'Assigned':
      return 'status-badge--assigned'
    case 'Running':
      return 'status-badge--running'
    case 'Paused':
      return 'status-badge--paused'
    case 'Completed':
      return 'status-badge--completed'
    default:
      return 'status-badge--error'
  }
}

// Logic


onBeforeMount(() => {
  resetVerificationStore()
  loadVerificationJobs()

  if (currentScreenSize.value == 'xs') {
    verificationTableVisibleColumns.value = [
      'id',
      'trayed',
      'status'
    ]
  }
})

const loadVerificationJobs = async (qParams) => {
  try {
    appIsLoadingData.value = true

    // Build filter params from column filters
    const filterParams = {
      ...qParams
    }

    // Add workflow_id search if provided
    if (columnFilters.value.workflow_id) {
      filterParams.workflow_id = columnFilters.value.workflow_id
    }

    // Add trayed filter if selected
    if (columnFilters.value.trayed !== null) {
      filterParams.trayed = columnFilters.value.trayed
    }

    // Add status filter
    if (columnFilters.value.status && columnFilters.value.status.length > 0) {
      filterParams.status = columnFilters.value.status
    }

    await getVerificationJobList(filterParams)
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
  } finally {
    appIsLoadingData.value = false
  }
}

// Apply column filters - triggers server-side filtering
const applyColumnFilters = () => {
  // Reset pagination to page 1 when filters change
  if (verificationTableRef.value) {
    verificationTableRef.value.resetTablePagination()
  }
  loadVerificationJobs()
}

// Clear all column filters
const clearColumnFilters = () => {
  columnFilters.value = {
    workflow_id: null,
    trayed: null,
    status: [
      'Created',
      'Assigned',
      'Paused',
      'Running'
    ]  // Reset to default active statuses
  }
  applyColumnFilters()
}

const loadVerificationJob = async (workflowId) => {
  try {
    appIsLoadingData.value = true
    await getVerificationJob(workflowId)

    router.push({
      name: 'verification',
      params: {
        jobId: workflowId
      }
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
  } finally {
    appIsLoadingData.value = false
  }
}
</script>
<style lang="scss" scoped>
</style>