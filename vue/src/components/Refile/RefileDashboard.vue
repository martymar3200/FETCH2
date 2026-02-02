<template>
  <div class="refile-dashboard">
    <div class="row q-mb-xs-xl q-mb-sm-none">
      <div class="col-grow q-mb-xs-md q-mb-sm-none">
        <EssentialTable
          ref="refileTableComponent"
          :table-columns="refileDisplayType == 'refile_job' ? refileTableColumns : queueTableColumns"
          :table-visible-columns="refileDisplayType == 'refile_job' ? refileTableVisibleColumns : queueTableVisibleColumns"
          :table-data="refileDisplayType == 'refile_job' ? refileJobList : refileQueueList"
          :row-key="refileDisplayType == 'refile_job' ? 'id' : 'barcode_value'"
          :enable-table-reorder="false"
          :enable-selection="showCreateRefileJob || showAddRefileJob"
          :heading-row-class="'q-mb-xs-md q-mb-md-xl'"
          :enable-pagination="true"
          :pagination-total="refileDisplayType == 'refile_job' ? refileJobListTotal : refileQueueListTotal"
          :pagination-loading="appIsLoadingData"
          :hide-table-rearrange="true"
          @update-pagination="loadRefileJobs($event)"
          @selected-table-row="refileDisplayType == 'refile_job' ? loadRefileJob($event.id) : null"
          @selected-data="selectedRefileItems = $event"
        >
          <template #heading-row>
            <div
              class="col-sm-5 col-md-auto q-mb-md-sm"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? '' : 'self-center'"
            >
              <h1 class="text-h4 text-bold">
                {{ refileDisplayType == 'refile_job' ? 'Refile Jobs' : 'Refile Queue' }}
              </h1>
            </div>

            <!-- View toggle - centered on desktop -->
            <div
              class="col-grow flex justify-center"
              :class="currentScreenSize == 'xs' ? 'col-12 q-mb-md' : ''"
            >
              <q-btn-toggle
                v-model="refileDisplayType"
                no-caps
                rounded
                unelevated
                toggle-color="accent"
                color="white"
                text-color="grey-7"
                class="toggle-modern-rounded"
                :options="dashboardToggleOptions"
                @update:model-value="clearTableSelection(); refileTableComponent.resetTablePagination(); loadRefileJobs();"
              />
            </div>

            <div class="col-grow" />

            <!-- Filter buttons and Create - right justified -->
            <div
              class="col-auto flex items-center q-gutter-x-sm"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? 'justify-end q-mb-md' : ''"
            >
              <q-btn
                flat
                dense
                no-caps
                :color="showFilterRow ? 'accent' : 'grey-7'"
                :label="showFilterRow ? 'Hide Filters' : 'Show Filters'"
                :icon="showFilterRow ? 'filter_alt' : 'filter_alt_off'"
                class="btn-modern-flat"
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
                class="btn-modern-flat"
                @click="clearColumnFilters"
              />
              <q-btn
                no-caps
                unelevated
                icon-right="arrow_drop_down"
                color="accent"
                label="Create"
                class="text-body1 btn-modern q-ml-sm"
                aria-label="CreateRefileJobMenu"
                aria-haspopup="menu"
                :aria-expanded="refileJobMenuState"
              >
                <q-menu
                  @show="refileJobMenuState = true"
                  @hide="refileJobMenuState = false"
                  aria-label="refileJobMenuList"
                >
                  <q-list>
                    <q-item
                      v-if="checkUserPermission('can_add_refile_item_to_queue')"
                      clickable
                      v-close-popup
                      @click="handleOptionMenu('Queue')"
                      role="menuitem"
                    >
                      <q-item-section>
                        <q-item-label>
                          <span class="text-nowrap">
                            Add Item to Queue
                          </span>
                        </q-item-label>
                      </q-item-section>
                    </q-item>
                    <q-item
                      v-if="checkUserPermission('can_add_to_refile_job')"
                      clickable
                      v-close-popup
                      @click="handleOptionMenu('Add')"
                      role="menuitem"
                    >
                      <q-item-section>
                        <q-item-label>
                          <span class="text-nowrap">
                            Add Item to Refile Job
                          </span>
                        </q-item-label>
                      </q-item-section>
                    </q-item>
                    <q-item
                      v-if="checkUserPermission('can_create_refile_job')"
                      clickable
                      v-close-popup
                      @click="handleOptionMenu('Create')"
                      role="menuitem"
                    >
                      <q-item-section>
                        <q-item-label>
                          <span class="text-nowrap">
                            Create Refile Job
                          </span>
                        </q-item-label>
                      </q-item-section>
                    </q-item>
                  </q-list>
                </q-menu>
              </q-btn>
            </div>

            <div
              v-if="(showCreateRefileJob || showAddRefileJob) && currentScreenSize !== 'xs'"
              class="col-12 order-2 flex"
            >
              <div class="q-ml-auto q-mt-md">
                <q-btn
                  no-caps
                  unelevated
                  :color="showCreateRefileJob ? 'accent' : 'positive'"
                  :label="`(${selectedRefileItems.length}) ${showCreateRefileJob ? 'Create Refile Job' : 'Add To Refile Job'}`"
                  class="btn-no-wrap text-body1 q-mr-xs full-height"
                  :disabled="selectedRefileItems.length == 0"
                  :loading="appActionIsLoadingData"
                  @click="showCreateRefileJob ? createRefileJob() : updateRefileJob()"
                />
                <q-btn
                  no-caps
                  outline
                  label="Cancel"
                  class="btn-no-wrap text-body1 q-ml-xs full-height"
                  @click="resetRefileJobForm(); getRefileQueueList();"
                />
              </div>
            </div>
            <MobileActionBar
              v-else-if="(showCreateRefileJob || showAddRefileJob) && currentScreenSize == 'xs'"
              :button-one-color="showCreateRefileJob ? 'accent' : 'positive'"
              :button-one-label="`(${selectedRefileItems.length}) ${showCreateRefileJob ? 'Create Refile Job' : 'Add To Refile Job'}`"
              :button-one-outline="false"
              :button-one-loading="appActionIsLoadingData"
              :button-one-disabled="selectedRefileItems.length == 0"
              @button-one-click="showCreateRefileJob ? createRefileJob() : updateRefileJob()"
              :button-two-color="'black'"
              :button-two-label="'Cancel'"
              :button-two-outline="true"
              @button-two-click="resetRefileJobForm(); loadRefileJobs();"
            />
          </template>

          <!-- Filter row inside table header -->
          <template #header-filter-row="{ cols }">
            <q-tr
              v-if="showFilterRow"
              class="filter-row"
            >
              <!-- Empty cell for checkbox column when selection is enabled -->
              <q-th
                v-if="showCreateRefileJob || showAddRefileJob"
                class="filter-cell"
                style="width: 50px;"
              />
              <q-th
                v-for="col in cols"
                :key="col.name"
                class="filter-cell"
              >
                <!-- Refile Job View Filters -->
                <template v-if="refileDisplayType === 'refile_job'">
                  <!-- Job ID filter -->
                  <q-input
                    v-if="col.name === 'id'"
                    v-model="jobColumnFilters.id"
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
                    v-model="jobColumnFilters.status"
                    dense
                    outlined
                    clearable
                    multiple
                    emit-value
                    map-options
                    :options="jobStatusOptions"
                    placeholder="All"
                    class="column-filter-input"
                    @update:model-value="applyColumnFilters"
                    @click.stop
                  />

                  <!-- Assigned User filter -->
                  <q-select
                    v-else-if="col.name === 'assigned_user_id'"
                    v-model="jobColumnFilters.assigned_user"
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
                </template>

                <!-- Refile Queue View Filters -->
                <template v-else>
                  <!-- Location filter -->
                  <q-input
                    v-if="col.name === 'location'"
                    v-model="queueColumnFilters.location"
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

                  <!-- Container Type filter -->
                  <q-select
                    v-else-if="col.name === 'container_type'"
                    v-model="queueColumnFilters.container_type"
                    dense
                    outlined
                    clearable
                    multiple
                    emit-value
                    map-options
                    :options="containerTypeOptions"
                    placeholder="All"
                    class="column-filter-input"
                    @update:model-value="applyColumnFilters"
                    @click.stop
                  />

                  <!-- Media Type filter -->
                  <q-select
                    v-else-if="col.name === 'media_type'"
                    v-model="queueColumnFilters.media_type"
                    dense
                    outlined
                    clearable
                    multiple
                    emit-value
                    map-options
                    :options="mediaTypeOptionsFromData.length > 0 ? mediaTypeOptionsFromData : mediaTypeOptions"
                    placeholder="All"
                    class="column-filter-input"
                    @update:model-value="applyColumnFilters"
                    @click.stop
                  />

                  <!-- Barcode filter -->
                  <q-input
                    v-else-if="col.name === 'barcode_value'"
                    v-model="queueColumnFilters.barcode_value"
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

                  <!-- Owner filter -->
                  <q-select
                    v-else-if="col.name === 'owner'"
                    v-model="queueColumnFilters.owner"
                    dense
                    outlined
                    clearable
                    multiple
                    emit-value
                    map-options
                    :options="ownerOptionsFromData.length > 0 ? ownerOptionsFromData : ownerOptions"
                    placeholder="All"
                    class="column-filter-input"
                    @update:model-value="applyColumnFilters"
                    @click.stop
                  />

                  <!-- Size Class filter -->
                  <q-select
                    v-else-if="col.name === 'size_class'"
                    v-model="queueColumnFilters.size_class"
                    dense
                    outlined
                    clearable
                    multiple
                    emit-value
                    map-options
                    :options="sizeClassOptionsFromData.length > 0 ? sizeClassOptionsFromData : sizeClassOptions"
                    placeholder="All"
                    class="column-filter-input"
                    @update:model-value="applyColumnFilters"
                    @click.stop
                  />
                </template>
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
            <span v-else-if="colName == 'create_dt'">
              {{ formatDateTime(value).date }}
            </span>
            <span v-else-if="colName == 'last_transition'">
              {{ formatDateTime(value).date }}
            </span>
            <span v-else-if="colName == 'scanned_for_refile_queue_dt'">
              {{ formatDateTime(value).date }}
            </span>
          </template>
        </EssentialTable>
      </div>
    </div>
  </div>

  <!-- Add Item to Refile Queue Modal-->
  <RefileAddQueueItem
    v-if="showAddItemToQueue"
    @hide="showAddItemToQueue = false; loadRefileJobs();"
  />

  <!-- Create/Add To Refile Modal -->
  <PopupModal
    v-if="showRefileJobModal"
    ref="refileJobModalComponent"
    :show-actions="false"
    @reset="showRefileJobModal = null"
    aria-label="refileJobModal"
  >
    <template #header-content="{ hideModal }">
      <q-card-section class="row items-center justify-between q-pb-none no-wrap">
        <h2 class="text-h6">
          {{ showRefileJobModal == 'Create' ? 'Filter Queue By Building' : 'Filter Queue & Select Refile Job' }}
        </h2>

        <q-btn
          icon="close"
          flat
          round
          dense
          aria-label="Close"
          @click="filterRefileByBuilding = null; addToRefileJob = null; hideModal();"
        />
      </q-card-section>
    </template>

    <template #main-content>
      <q-card-section class="column no-wrap items-center">
        <div class="form-group">
          <label class="form-group-label">
            Building
          </label>
          <SelectInput
            v-model="filterRefileByBuilding"
            :options="buildings"
            option-type="buildings"
            option-value="id"
            option-label="name"
            :placeholder="'Select Building'"
            aria-label="buildingSelect"
          />
        </div>

        <div
          v-if="showRefileJobModal == 'Add'"
          class="form-group q-mt-md"
        >
          <label class="form-group-label">
            Add To Refile Job
          </label>
          <SelectInput
            v-model="addToRefileJob"
            :options="refileJobs"
            option-type="refileJobs"
            :option-query="{status: [
              'Created',
              'Paused'
            ]}"
            option-value="id"
            option-label="id"
            :force-option-type-reload="true"
            :placeholder="'Select Refile Job'"
            aria-label="refileJobSelect"
          />
        </div>
      </q-card-section>
    </template>

    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <q-btn
          no-caps
          unelevated
          color="accent"
          label="Submit"
          class="text-body1 full-width text-nowrap"
          :disabled="showRefileJobModal == 'Create' ? !filterRefileByBuilding : (!filterRefileByBuilding || !addToRefileJob)"
          :loading="appActionIsLoadingData"
          @click="loadRefileQueueByBuilding()"
        />

        <q-space class="q-mx-xs" />

        <q-btn
          outline
          no-caps
          label="Cancel"
          class="text-body1 full-width"
          @click="filterRefileByBuilding = null; addToRefileJob = null; hideModal();"
        />
      </q-card-section>
    </template>
  </PopupModal>
</template>

<script setup>
import { onBeforeMount, ref, computed, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { useRefileStore } from '@/stores/refile-store'
import { useUserStore } from '@/stores/user-store'
import { storeToRefs } from 'pinia'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import RefileAddQueueItem from '@/components/Refile/RefileAddQueueItem.vue'
import EssentialTable from '@/components/EssentialTable.vue'
import PopupModal from '@/components/PopupModal.vue'
import SelectInput from '@/components/SelectInput.vue'
import MobileActionBar from '@/components/MobileActionBar.vue'

const router = useRouter()

// Composables
const { currentScreenSize } = useCurrentScreenSize()
const { checkUserPermission } = usePermissionHandler()

// Store Data
const { appIsLoadingData, appActionIsLoadingData } = storeToRefs(useGlobalStore())
const {
  buildings,
  refileJobs,
  users,
  mediaTypes,
  owners,
  sizeClass
} = storeToRefs(useOptionStore())
const {
  resetRefileStore,
  getRefileJobList,
  getRefileQueueList,
  getRefileJob,
  postRefileJob,
  postRefileJobItem
} = useRefileStore()
const {
  refileJobList,
  refileJobListTotal,
  refileQueueList,
  refileQueueListTotal,
  refileJob
} = storeToRefs(useRefileStore())
const { userData } = storeToRefs(useUserStore())

// Filter State
const showFilterRow = ref(false)

// Column filter state for Refile Job View
const jobColumnFilters = ref({
  id: null,
  status: [
    'Created',
    'Paused',
    'Running'
  ], // Default to active jobs
  assigned_user: []
})

// Column filter state for Refile Queue View
const queueColumnFilters = ref({
  location: null,
  container_type: [],
  media_type: [],
  barcode_value: null,
  owner: [],
  size_class: []
})

// Static filter dropdown options
const jobStatusOptions = [
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

const containerTypeOptions = [
  {
    label: 'Tray',
    value: 'Tray'
  },
  {
    label: 'Non-Tray',
    value: 'Non-Tray'
  }
]

// Dynamic filter options from current table data
const userOptionsFromData = computed(() => {
  const userSet = new Set()
  refileJobList.value.forEach(row => {
    if (row.assigned_user?.name) {
      userSet.add(row.assigned_user.name)
    }
  })
  return Array.from(userSet).sort().map(u => ({
    label: u,
    value: u
  }))
})

const mediaTypeOptionsFromData = computed(() => {
  const types = new Set()
  refileQueueList.value.forEach(row => {
    if (row.media_type) {
      types.add(row.media_type)
    }
  })
  return Array.from(types).sort().map(t => ({
    label: t,
    value: t
  }))
})

const ownerOptionsFromData = computed(() => {
  const ownerSet = new Set()
  refileQueueList.value.forEach(row => {
    if (row.owner) {
      ownerSet.add(row.owner)
    }
  })
  return Array.from(ownerSet).sort().map(o => ({
    label: o,
    value: o
  }))
})

const sizeClassOptionsFromData = computed(() => {
  const sizes = new Set()
  refileQueueList.value.forEach(row => {
    if (row.size_class) {
      sizes.add(row.size_class)
    }
  })
  return Array.from(sizes).sort().map(s => ({
    label: s,
    value: s
  }))
})

// Fallback options from store data
const userOptions = computed(() =>
  users.value.map(u => ({
    label: u.name,
    value: u.name
  }))
)

const mediaTypeOptions = computed(() =>
  mediaTypes.value.map(m => ({
    label: m.name,
    value: m.name
  }))
)

const ownerOptions = computed(() =>
  owners.value.map(o => ({
    label: o.name,
    value: o.name
  }))
)

const sizeClassOptions = computed(() =>
  sizeClass.value.map(s => ({
    label: s.name,
    value: s.name
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
const refileJobModalComponent = ref(null)
const refileJobMenuState = ref(false)
const refileTableComponent = ref(null)
const refileTableVisibleColumns = ref([
  'id',
  'item_count',
  'shelved_count',
  'status',
  'assigned_user_id',
  'create_dt',
  'last_transition'
])
const refileTableColumns = ref([
  {
    name: 'id',
    field: 'id',
    label: 'Job ID #',
    align: 'left',
    sortable: true
  },
  {
    name: 'item_count',
    field: row => (row.item_count + row.non_tray_item_count),
    label: '# of Items',
    align: 'left',
    sortable: true
  },
  {
    name: 'shelved_count',
    field: 'container_shelved_refiled_count',
    label: '# of Items Shelved',
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
    name: 'assigned_user_id',
    field: row => row.assigned_user ? row.assigned_user.name : '',
    label: 'Assigned User',
    align: 'left',
    sortable: true
  },
  {
    name: 'create_dt',
    field: 'create_dt',
    label: 'Date Created',
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
const queueTableVisibleColumns = ref([
  'location',
  'container_type',
  'media_type',
  'barcode_value',
  'owner',
  'size_class',
  'scanned_for_refile_queue_dt'
])
const queueTableColumns = ref([
  {
    name: 'location',
    field: row => `${row.module_number}-${row.aisle_number}-${row.side_orientation == 'Right' ? 'R' : row.side_orientation == 'Left' ? 'L' : row.side_orientation}-${row.ladder_number}-${row.shelf_number}-${row.shelf_position_number}`,
    label: 'Item Location',
    align: 'left',
    sortable: true,
    style: 'min-width: 150px'
  },
  {
    name: 'container_type',
    field: 'container_type',
    label: 'Container Type',
    align: 'left',
    sortable: true
  },
  {
    name: 'media_type',
    field: row => row.media_type,
    label: 'Media Type',
    align: 'left',
    sortable: true
  },
  {
    name: 'barcode_value',
    field: row => row.barcode_value,
    label: 'Item Barcode',
    align: 'left',
    sortable: true,
    style: 'min-width: 150px'
  },
  {
    name: 'owner',
    field: row => row.owner,
    label: 'Owner',
    align: 'left',
    sortable: true
  },
  {
    name: 'size_class',
    field: row => row.size_class,
    label: 'Container Size',
    align: 'left',
    sortable: true
  },
  {
    name: 'scanned_for_refile_queue_dt',
    field: 'scanned_for_refile_queue_dt',
    label: 'Date Added',
    align: 'left',
    sortable: true
  }
])
const refileDisplayType = ref('refile_job')
const showAddItemToQueue = ref(false)
const showCreateRefileJob = ref(false)
const showAddRefileJob = ref(false)
const showRefileJobModal = ref(null)
const selectedRefileItems = ref([])
const filterRefileByBuilding = ref(null)
const addToRefileJob = ref(null)
const formattedRefileQueueCount = computed(() => {
  return refileQueueListTotal.value.toLocaleString()
})

const dashboardToggleOptions = computed(() => [
  {
    label: 'Refile Jobs',
    value: 'refile_job'
  },
  {
    label: `Refile Queue (${formattedRefileQueueCount.value})`,
    value: 'refile_queue'
  }
])

// Logic
const handleAlert = inject('handle-alert')
const formatDateTime = inject('format-date-time')

onBeforeMount(() => {
  resetRefileStore()
  loadRefileJobs()
  getRefileQueueList({ size: 1 })
  if (currentScreenSize.value == 'xs') {
    refileTableVisibleColumns.value = [
      'id',
      'item_count',
      'shelved_count',
      'assigned_user_id'
    ]
    queueTableVisibleColumns.value = [
      'location',
      'barcode_value'
    ]
  }
})

const handleOptionMenu = (opt) => {
  if (opt == 'Queue') {
    showAddItemToQueue.value = true
    if (refileDisplayType.value !== 'refile_queue') {
      refileDisplayType.value = 'refile_queue'
      loadRefileJobs()
    }
  } else if (opt == 'Add') {
    showRefileJobModal.value = 'Add'
  } else if (opt == 'Create') {
    showRefileJobModal.value = 'Create'
  }
}
const clearTableSelection = () => {
  refileTableComponent.value.clearSelectedData()
  selectedRefileItems.value = []
}
const resetRefileJobForm = () => {
  showCreateRefileJob.value = false
  showAddRefileJob.value = false
  showRefileJobModal.value = null
  filterRefileByBuilding.value = null
  addToRefileJob.value = null
  clearTableSelection()
}

const loadRefileJobs = async (qParams) => {
  try {
    appIsLoadingData.value = true
    if (refileDisplayType.value == 'refile_job') {
      // Build filter params
      const filterParams = {
        ...qParams,
        user_id: checkUserPermission('can_view_all_refile_jobs') ? null : userData.value.user_id
      }

      // Apply column filters
      if (jobColumnFilters.value.id) {
        filterParams.id = jobColumnFilters.value.id
      }
      if (jobColumnFilters.value.status && jobColumnFilters.value.status.length > 0) {
        filterParams.status = jobColumnFilters.value.status
      }
      if (jobColumnFilters.value.assigned_user && jobColumnFilters.value.assigned_user.length > 0) {
        filterParams.assigned_user = jobColumnFilters.value.assigned_user
      }

      await getRefileJobList(filterParams)
    } else {
      // Build queue filter params
      const isRefileJobMode = showCreateRefileJob.value || showAddRefileJob.value
      const queueFilterParams = {
        ...qParams,
        // Persist building filter during refile job creation mode
        ...(isRefileJobMode && filterRefileByBuilding.value ? { building_id: filterRefileByBuilding.value } : {})
      }

      if (queueColumnFilters.value.location) {
        // Convert displayed location format to database format
        let locationSearch = queueColumnFilters.value.location
        locationSearch = locationSearch.replace(/-R-/gi, '-Right-')
        locationSearch = locationSearch.replace(/-L-/gi, '-Left-')
        locationSearch = locationSearch.replace(/-R$/gi, '-Right')
        locationSearch = locationSearch.replace(/-L$/gi, '-Left')
        queueFilterParams.item_location = locationSearch
      }
      if (queueColumnFilters.value.container_type && queueColumnFilters.value.container_type.length > 0) {
        queueFilterParams.container_type = queueColumnFilters.value.container_type
      }
      if (queueColumnFilters.value.media_type && queueColumnFilters.value.media_type.length > 0) {
        queueFilterParams.media_type = queueColumnFilters.value.media_type
      }
      if (queueColumnFilters.value.barcode_value) {
        queueFilterParams.barcode_value = queueColumnFilters.value.barcode_value
      }
      if (queueColumnFilters.value.owner && queueColumnFilters.value.owner.length > 0) {
        queueFilterParams.owner = queueColumnFilters.value.owner
      }
      if (queueColumnFilters.value.size_class && queueColumnFilters.value.size_class.length > 0) {
        queueFilterParams.size_class = queueColumnFilters.value.size_class
      }

      await getRefileQueueList(queueFilterParams)
    }
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appIsLoadingData.value = false
  }
}

// Apply column filters
const applyColumnFilters = () => {
  if (refileTableComponent.value) {
    refileTableComponent.value.resetTablePagination()
  }
  loadRefileJobs()
}

// Clear all column filters
const clearColumnFilters = () => {
  if (refileDisplayType.value === 'refile_job') {
    jobColumnFilters.value = {
      id: null,
      status: [],
      assigned_user: []
    }
  } else {
    queueColumnFilters.value = {
      location: null,
      container_type: [],
      media_type: [],
      barcode_value: null,
      owner: [],
      size_class: []
    }
  }
  applyColumnFilters()
}

const loadRefileQueueByBuilding = async () => {
  // this function only gets called during the creation/add refile job process
  try {
    refileDisplayType.value = 'refile_queue'
    appActionIsLoadingData.value = true
    await getRefileQueueList({ building_id: filterRefileByBuilding.value })

    // display next step in refile job creation
    if (showRefileJobModal.value == 'Create') {
      showCreateRefileJob.value = true
    } else {
      showAddRefileJob.value = true
    }
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
    refileJobModalComponent.value.hideModal()
  }
}
const loadRefileJob = async (id) => {
  try {
    appIsLoadingData.value = true
    await getRefileJob(id)

    router.push({
      name: 'refile',
      params: {
        jobId: id
      }
    })
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appIsLoadingData.value = false
  }
}
const createRefileJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      barcode_values: selectedRefileItems.value.map(item => item.barcode_value),
      created_by_id: userData.value.user_id
    }
    await postRefileJob(payload)

    // display an alert with the created refile job id so you can click that and link directly to the new job if needed
    handleAlert({
      type: 'success',
      text: `Successfully created Refile Job #: <a href='/refile/${refileJob.value.id}' tabindex='0'>${refileJob.value.id}</a>`,
      autoClose: false
    })
    loadRefileJobs()
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
    resetRefileJobForm()
  }
}
const updateRefileJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: addToRefileJob.value,
      barcode_values: selectedRefileItems.value.map(item => item.barcode_value)
    }
    await postRefileJobItem(payload)

    // display an alert with the updated refilet job id so you can click that and link directly to the job if needed
    handleAlert({
      type: 'success',
      text: `Successfully added items to Refile Job #: <a href='/refile/${refileJob.value.id}' tabindex='0'>${refileJob.value.id}</a>`,
      autoClose: false
    })
    loadRefileJobs()
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
    resetRefileJobForm()
  }
}
</script>

<style lang="scss" scoped>
.toggle-modern-rounded {
  border: 1px solid rgba(0, 0, 0, 0.05);
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);

  :deep(.q-btn) {
    padding: 0 24px;
    font-weight: 600;
    font-size: 0.9rem;
    min-height: 40px;

    &.q-btn--active {
      box-shadow: 0 4px 12px rgba(var(--q-accent), 0.3);
    }
  }
}

.btn-modern-flat {
  padding: 4px 12px;
  border-radius: 8px;
  &:hover {
    background: rgba(0, 0, 0, 0.05);
  }
}

// Table horizontal scroll and minimum column widths
:deep(.q-table__container) {
  overflow-x: auto;
}

:deep(.q-table) {
  min-width: 1000px;
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
