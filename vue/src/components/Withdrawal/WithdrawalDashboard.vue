<template>
  <div class="withdrawal-dashboard">
    <div class="row q-mb-xs-xl q-mb-sm-none">
      <div class="col-grow q-mb-xs-md q-mb-sm-none">
        <EssentialTable
          ref="withdrawTableComponent"
          :table-columns="withdrawTableColumns"
          :table-visible-columns="withdrawTableVisibleColumns"
          :table-data="withdrawJobList"
          :enable-table-reorder="false"
          :heading-row-class="'q-mb-xs-md q-mb-md-lg'"
          :enable-pagination="true"
          :pagination-total="withdrawJobListTotal"
          :pagination-loading="appIsLoadingData"
          :hide-table-rearrange="true"
          @update-pagination="loadWithdrawJobs($event)"
          @selected-table-row="loadWithdrawJob($event.id)"
        >
          <template #heading-row>
            <div
              class="col-sm-5 col-md-auto q-mb-md-sm"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? '' : 'self-center'"
            >
              <h1 class="text-h4 text-bold">
                Withdraw Jobs
              </h1>
            </div>

            <div class="col-grow" />

            <!-- Filter buttons and Create - right justified -->
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
              <q-btn
                no-caps
                unelevated
                color="accent"
                label="Create Withdraw Job"
                class="btn-no-wrap text-body1 btn-modern"
                :disabled="appIsOffline"
                @click="createWithdrawJob"
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
                <!-- Job ID filter -->
                <q-input
                  v-if="col.name === 'id'"
                  v-model="columnFilters.id"
                  dense
                  outlined
                  clearable
                  placeholder="Search..."
                  class="column-filter-input"
                  @keyup.enter="applyColumnFilters"
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
                  options-dense
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
              v-else-if="colName == 'item_count'"
              class="text-nowrap"
            >
              {{ value }} Items
            </span>
            <span v-else-if="colName == 'create_dt'">
              {{ formatDateTime(value).date }}
            </span>
            <span v-else-if="colName == 'last_transition'">
              {{ formatDateTime(value).date }}
            </span>
          </template>
        </EssentialTable>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onBeforeMount, ref, inject, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Notify } from 'quasar'
import { useGlobalStore } from '@/stores/global-store'
import { useWithdrawalStore } from '@/stores/withdrawal-store'
import { useUserStore } from '@/stores/user-store'
import { storeToRefs } from 'pinia'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import EssentialTable from '@/components/EssentialTable.vue'

const router = useRouter()

// Composables
const { currentScreenSize } = useCurrentScreenSize()

// Store Data
const {
  appIsLoadingData,
  appIsOffline
} = storeToRefs(useGlobalStore())
const {
  withdrawJobList,
  withdrawJobListTotal,
  withdrawJob
} = storeToRefs(useWithdrawalStore())
const {
  resetWithdrawStore,
  getWithdrawJobList,
  getWithdrawJob,
  postWithdrawJob
} = useWithdrawalStore()
const { userData } = storeToRefs(useUserStore())

// Local Data
const withdrawTableComponent = ref(null)
const showFilterRow = ref(false)

// Column filters
const columnFilters = ref({
  id: null,
  status: [
    'Created',
    'Paused',
    'Running'
  ]
})

const withdrawTableVisibleColumns = ref([
  'id',
  'item_count',
  'status',
  'create_dt'
])
const withdrawTableColumns = ref([
  {
    name: 'id',
    field: 'id',
    label: 'Job ID #',
    align: 'left',
    sortable: true,
    style: 'min-width: 120px'
  },
  {
    name: 'item_count',
    field: row => row.item_count + row.non_tray_item_count,
    label: '# of Items',
    align: 'left',
    sortable: true,
    style: 'min-width: 120px'
  },
  {
    name: 'status',
    field: 'status',
    label: 'Status',
    align: 'left',
    sortable: true,
    style: 'min-width: 150px'
  },
  {
    name: 'create_dt',
    field: 'create_dt',
    label: 'Date Created',
    align: 'left',
    sortable: true,
    style: 'min-width: 150px'
  }
])

// Dynamic dropdown options
const statusOptions = computed(() => {
  const dynamicStatuses = new Set()
  if (withdrawJobList.value && withdrawJobList.value.length > 0) {
    withdrawJobList.value.forEach(job => {
      if (job.status) {
        dynamicStatuses.add(job.status)
      }
    })
  }
  // Add all possible statuses
  const allStatuses = [
    'Created',
    'Running',
    'Paused',
    'Completed',
    'Canceled'
  ]
  allStatuses.forEach(s => dynamicStatuses.add(s))
  return Array.from(dynamicStatuses).sort().map(s => ({
    label: s,
    value: s
  }))
})

// Status badge helpers
const getStatusBadgeClass = (status) => {
  const statusMap = {
    Created: 'status-badge--created',
    Running: 'status-badge--running',
    Paused: 'status-badge--paused',
    Completed: 'status-badge--completed',
    Canceled: 'status-badge--failed'
  }
  return statusMap[status] || ''
}

const getStatusIcon = (status) => {
  const iconMap = {
    Created: 'schedule',
    Running: 'play_arrow',
    Paused: 'pause',
    Completed: 'check_circle',
    Canceled: 'cancel'
  }
  return iconMap[status] || 'help'
}

// Logic

const formatDateTime = inject('format-date-time')

onBeforeMount(() => {
  resetWithdrawStore()
  loadWithdrawJobs()

  if (currentScreenSize.value == 'xs') {
    withdrawTableVisibleColumns.value = [
      'id',
      'item_count',
      'create_dt'
    ]
  }
})

const loadWithdrawJobs = async (qParams) => {
  try {
    appIsLoadingData.value = true

    // Build filter params
    const filterParams = { ...qParams }

    // Apply column filters
    if (columnFilters.value.id) {
      filterParams.workflow_id = columnFilters.value.id
    }
    if (columnFilters.value.status && columnFilters.value.status.length > 0) {
      filterParams.status = columnFilters.value.status
    }

    await getWithdrawJobList(filterParams)
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to load withdraw jobs'
    })
  } finally {
    appIsLoadingData.value = false
  }
}

// Apply column filters
const applyColumnFilters = () => {
  if (withdrawTableComponent.value) {
    withdrawTableComponent.value.resetTablePagination()
  }
  loadWithdrawJobs()
}

// Clear all column filters
const clearColumnFilters = () => {
  columnFilters.value = {
    id: null,
    status: []
  }
  applyColumnFilters()
}

const loadWithdrawJob = async (jobId) => {
  try {
    appIsLoadingData.value = true

    await getWithdrawJob(jobId)
    router.push({
      name: 'withdrawal',
      params: {
        jobId
      }
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to load withdraw job'
    })
  } finally {
    appIsLoadingData.value = false
  }
}
const createWithdrawJob = async () => {
  try {
    appIsLoadingData.value = true
    const payload = {
      created_by_id: userData.value.user_id
    }
    await postWithdrawJob(payload)

    // route the user to the withdrawal job detail page
    router.push({
      name: 'withdrawal',
      params: {
        jobId: withdrawJob.value.id
      }
    })

    Notify.create({
      type: 'positive',
      message: 'A Withdraw Job has been successfully created.'
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to create withdraw job'
    })
  } finally {
    appIsLoadingData.value = false
  }
}

</script>
<style lang="scss" scoped>
.btn-modern {
  border-radius: 8px;
  font-weight: 500;
  padding: 8px 16px;
  transition: all 0.2s ease;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

:deep(.filter-row) {
  background: rgba(0, 0, 0, 0.02);

  .filter-cell {
    padding: 8px 12px;
    min-width: 120px;
  }
}

:deep(.column-filter-input) {
  min-width: 100px;

  .q-field__control {
    min-height: 32px;
    height: 32px;
  }

  .q-field__native {
    padding: 0 8px;
    font-size: 13px;
  }

  .q-field__marginal {
    height: 32px;
  }
}

:deep(.table-component-table) {
  min-width: 600px;

  th {
    min-width: 100px;
  }
}

:deep(.q-table__container) {
  overflow-x: auto;
}
</style>
