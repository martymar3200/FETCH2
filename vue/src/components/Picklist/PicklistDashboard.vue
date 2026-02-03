<template>
  <div class="picklist-dashboard">
    <div class="row q-mb-xs-xl q-mb-sm-none">
      <div class="col-grow q-mb-xs-md q-mb-sm-none">
        <EssentialTable
          ref="picklistTableComponent"
          :table-columns="picklistTableColumns"
          :table-visible-columns="picklistTableVisibleColumns"
          :table-data="picklistJobList"
          :enable-table-reorder="false"
          :enable-selection="false"
          :heading-row-class="'q-mb-xs-md q-mb-md-xl'"
          :enable-pagination="true"
          :pagination-total="picklistJobListTotal"
          :pagination-loading="appIsLoadingData"
          :hide-table-rearrange="true"
          @update-pagination="loadPicklistJobs($event)"
          @selected-table-row="loadPicklistJob($event.id)"
        >
          <template #heading-row>
            <div
              class="col-sm-5 col-md-12 col-lg-auto"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? '' : 'self-center'"
            >
              <h1 class="text-h4 text-bold">
                Pick List Jobs
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
                  v-model="columnFilters.id"
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

                <!-- Building filter -->
                <q-select
                  v-else-if="col.name === 'building_name'"
                  v-model="columnFilters.building_name"
                  dense
                  outlined
                  clearable
                  multiple
                  emit-value
                  map-options
                  :options="buildingOptionsFromData.length > 0 ? buildingOptionsFromData : buildingOptions"
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
                  :options="statusOptionsFromData.length > 0 ? statusOptionsFromData : picklistStatusOptions"
                  placeholder="All"
                  class="column-filter-input"
                  @update:model-value="applyColumnFilters"
                  @click.stop
                />

                <!-- Assigned User filter -->
                <q-select
                  v-else-if="col.name === 'user_id'"
                  v-model="columnFilters.assigned_user"
                  dense
                  outlined
                  clearable
                  multiple
                  emit-value
                  map-options
                  :options="userOptionsFromData.length > 0 ? userOptionsFromData : userOptions"
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
              v-if="colName == 'request_count'"
              class="outline text-nowrap"
            >
              {{ value }} Items
            </span>
            <span
              v-else-if="colName == 'status'"
              class="status-badge"
              :class="getStatusBadgeClass(value)"
            >
              <q-icon
                :name="getStatusIcon(value)"
                size="16px"
              />
              {{ value }}
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
import { useOptionStore } from '@/stores/option-store'
import { usePicklistStore } from '@/stores/picklist-store'
import { useUserStore } from '@/stores/user-store'
import { storeToRefs } from 'pinia'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import EssentialTable from '@/components/EssentialTable.vue'

const router = useRouter()

// Composables
const { currentScreenSize } = useCurrentScreenSize()
const { checkUserPermission } = usePermissionHandler()

// Store Data
const { appIsLoadingData } = storeToRefs(useGlobalStore())
const {
  buildings,
  users
} = storeToRefs(useOptionStore())
const {
  resetPicklistStore,
  getPicklistJobList,
  getPicklistJob
} = usePicklistStore()
const { picklistJobList, picklistJobListTotal } = storeToRefs(usePicklistStore())
const { userData } = storeToRefs(useUserStore())

// Filter State
const showFilterRow = ref(false)
const picklistTableComponent = ref(null)

// Column filter state
const columnFilters = ref({
  id: null,
  building_name: [],
  status: [
    'Created',
    'Paused',
    'Running'
  ], // Default to show active jobs
  assigned_user: []
})

// Static filter dropdown options
const picklistStatusOptions = [
  {
    label: 'Created',
    value: 'Created'
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

// Dynamic filter options from current table data
const buildingOptionsFromData = computed(() => {
  const buildingSet = new Set()
  picklistJobList.value.forEach(row => {
    if (row.building?.id && row.building?.name) {
      buildingSet.add(JSON.stringify({
        id: row.building.id,
        name: row.building.name
      }))
    }
  })
  return Array.from(buildingSet).map(b => {
    const parsed = JSON.parse(b)
    return {
      label: parsed.name,
      value: parsed.name
    }
  }).sort((a, b) => a.label.localeCompare(b.label))
})

const statusOptionsFromData = computed(() => {
  const statuses = new Set()
  picklistJobList.value.forEach(row => {
    if (row.status) {
      statuses.add(row.status)
    }
  })
  return Array.from(statuses).sort().map(s => ({
    label: s,
    value: s
  }))
})

const userOptionsFromData = computed(() => {
  const userSet = new Set()
  picklistJobList.value.forEach(row => {
    if (row.user?.name) {
      userSet.add(row.user.name)
    }
  })
  return Array.from(userSet).sort().map(u => ({
    label: u,
    value: u
  }))
})

// Fallback options from store data
const buildingOptions = computed(() =>
  buildings.value.map(b => ({
    label: b.name,
    value: b.name
  }))
)

const userOptions = computed(() =>
  users.value.map(u => ({
    label: u.name,
    value: u.name
  }))
)

// Status badge helper functions
const getStatusIcon = (status) => {
  switch (status) {
    case 'Created':
      return 'mdi-plus-circle'
    case 'Running':
      return 'mdi-progress-clock'
    case 'Paused':
      return 'mdi-pause-circle'
    case 'Completed':
      return 'mdi-check-circle'
    default:
      return 'mdi-help-circle'
  }
}

const getStatusBadgeClass = (status) => {
  switch (status) {
    case 'Created':
      return 'status-badge--created'
    case 'Running':
      return 'status-badge--running'
    case 'Paused':
      return 'status-badge--paused'
    case 'Completed':
      return 'status-badge--completed'
    default:
      return ''
  }
}

// Local Data
const picklistTableVisibleColumns = ref([
  'id',
  'building_name',
  'request_count',
  'status',
  'user_id',
  'create_dt',
  'last_transition'
])
const picklistTableColumns = ref([
  {
    name: 'id',
    field: 'id',
    label: 'Job Number',
    align: 'left',
    sortable: true
  },
  {
    name: 'building_name',
    field: row => row.building?.name,
    label: 'Building',
    align: 'left',
    sortable: true
  },
  {
    name: 'request_count',
    field: 'request_count',
    label: '# of Items in Job',
    align: 'left',
    sortable: true
  },
  {
    name: 'status',
    field: 'status',
    label: 'Status',
    align: 'left',
    sortable: true
  },
  {
    name: 'user_id',
    field: row => row.user ? row.user.name : '',
    label: 'Assigned User',
    align: 'left',
    sortable: true
  },
  {
    name: 'create_dt',
    field: 'create_dt',
    label: 'Date Added',
    align: 'left',
    sortable: true
  },
  {
    name: 'last_transition',
    field: 'last_transition',
    label: 'Last Updated',
    align: 'left',
    sortable: true
  }
])

// Logic

const formatDateTime = inject('format-date-time')

onBeforeMount(() => {
  resetPicklistStore()
  loadPicklistJobs()

  if (currentScreenSize.value == 'xs') {
    picklistTableVisibleColumns.value = [
      'id',
      'status',
      'user_id',
      'create_dt'
    ]
  }
})

const loadPicklistJobs = async (qParams) => {
  try {
    appIsLoadingData.value = true

    // Build filter params
    const filterParams = {
      ...qParams,
      user_id: checkUserPermission('can_view_all_picklist_jobs') ? null : userData.value.user_id
    }

    // Apply column filters
    if (columnFilters.value.id) {
      filterParams.id = columnFilters.value.id
    }
    if (columnFilters.value.building_name && columnFilters.value.building_name.length > 0) {
      filterParams.building_name = columnFilters.value.building_name
    }
    if (columnFilters.value.status && columnFilters.value.status.length > 0) {
      filterParams.status = columnFilters.value.status
    }
    if (columnFilters.value.assigned_user && columnFilters.value.assigned_user.length > 0) {
      filterParams.assigned_user = columnFilters.value.assigned_user
    }

    await getPicklistJobList(filterParams)
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to load picklist jobs'
    })
  } finally {
    appIsLoadingData.value = false
  }
}

// Apply column filters - triggers server-side filtering
const applyColumnFilters = () => {
  if (picklistTableComponent.value) {
    picklistTableComponent.value.resetTablePagination()
  }
  loadPicklistJobs()
}

// Clear all column filters
const clearColumnFilters = () => {
  columnFilters.value = {
    id: null,
    building_name: [],
    status: [],
    assigned_user: []
  }
  applyColumnFilters()
}

const loadPicklistJob = async (id) => {
  try {
    appIsLoadingData.value = true
    await getPicklistJob(id)
    router.push({
      name: 'picklist',
      params: {
        jobId: id
      }
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to load picklist job'
    })
  } finally {
    appIsLoadingData.value = false
  }
}
</script>

<style lang="scss" scoped>
// Table horizontal scroll and minimum column widths
:deep(.q-table__container) {
  overflow-x: auto;
}

:deep(.q-table) {
  min-width: 900px;
}

:deep(.filter-row) {
  .filter-cell {
    min-width: 120px;
    padding: 4px 8px;
  }

  .column-filter-input {
    min-width: 100px;
    width: 100%;
  }
}

:deep(.q-table th) {
  min-width: 100px;
  white-space: nowrap;
}
</style>
