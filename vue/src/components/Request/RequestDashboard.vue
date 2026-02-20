<template>
  <div class="request-dashboard">
    <div class="row q-mb-xs-xl q-mb-sm-none">
      <div class="col-grow q-mb-xs-md q-mb-sm-none">
        <EssentialTable
          ref="requestTableComponent"
          :table-columns="requestDisplayType == 'request_view' ? requestTableColumns : requestBatchTableColumns"
          :table-visible-columns="requestDisplayType == 'request_view' ? requestTableVisibleColumns : requestBatchTableVisibleColumns"
          :table-data="requestJobList"
          :enable-table-reorder="false"
          :enable-selection="showCreatePickList || showAddPickList"
          :heading-row-class="'q-mb-xs-md q-mb-md-xl'"
          :enable-pagination="true"
          :pagination-total="requestJobListTotal"
          :pagination-loading="appIsLoadingData"
          :rows-per-page-options="[25, 50, 75, 100, 250, 500]"
          :hide-table-rearrange="true"
          @update-pagination="loadRequestJobs($event)"
          @selected-table-row="loadRequestJob($event.id)"
          @selected-data="selectedRequestItems = $event"
        >
          <template #heading-row>
            <div
              class="col-sm-5 col-md-auto q-mb-md-sm"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? '' : 'self-center'"
            >
              <h1 class="text-h4 text-bold">
                {{ requestDisplayType == 'request_view' ? 'Requests' : 'Batch Requests' }}
              </h1>
            </div>

            <!-- View toggle - left justified after title -->
            <div
              class="col-auto q-ml-md self-center"
              :class="currentScreenSize == 'xs' ? 'col-12 q-mb-md' : ''"
            >
              <ToggleButtonInput
                v-model="requestDisplayType"
                :options="[
                  {label: 'Request View', value: 'request_view'},
                  {label: 'Batch View', value: 'batch_view'}
                ]"
                @update:model-value="clearTableSelection(); requestTableComponent.resetTablePagination(); loadRequestJobs();"
                class="text-no-wrap toggle-modern"
              />
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
                icon-right="arrow_drop_down"
                color="accent"
                label="Create"
                class="text-body1 btn-modern"
                :disabled="showCreatePickList || showAddPickList"
                aria-label="createRequestJobMenu"
                aria-haspopup="menu"
                :aria-expanded="requestJobMenuState"
              >
                <q-menu
                  @show="requestJobMenuState = true"
                  @hide="requestJobMenuState = false"
                  aria-label="requestJobMenuList"
                >
                  <q-list>
                    <q-item
                      v-if="checkUserPermission('can_add_to_picklist_job')"
                      clickable
                      v-close-popup
                      @click="showPickListModal = 'Add'"
                      role="menuitem"
                    >
                      <q-item-section>
                        <q-item-label>
                          <span>
                            Add to Pick List
                          </span>
                        </q-item-label>
                      </q-item-section>
                    </q-item>
                    <q-item
                      v-if="checkUserPermission('can_create_picklist_job')"
                      clickable
                      v-close-popup
                      @click="showPickListModal = 'Create'"
                      role="menuitem"
                    >
                      <q-item-section>
                        <q-item-label>
                          <span>
                            Create a Pick List
                          </span>
                        </q-item-label>
                      </q-item-section>
                    </q-item>
                    <q-item
                      v-if="checkUserPermission('can_create_and_submit_manual_requests')"
                      clickable
                      v-close-popup
                      @click="showCreateRequestByType = 'manual'"
                      role="menuitem"
                    >
                      <q-item-section>
                        <q-item-label>
                          <span>
                            Create Manual Requests
                          </span>
                        </q-item-label>
                      </q-item-section>
                    </q-item>
                    <q-item
                      v-if="checkUserPermission('can_create_and_submit_batch_requests')"
                      clickable
                      v-close-popup
                      @click="showCreateRequestByType = 'bulk'"
                      role="menuitem"
                    >
                      <q-item-section>
                        <q-item-label>
                          <span class="text-no-wrap">
                            Import Requests from File
                          </span>
                        </q-item-label>
                      </q-item-section>
                    </q-item>
                  </q-list>
                </q-menu>
              </q-btn>
            </div>

            <div
              v-if="(showCreatePickList || showAddPickList) && currentScreenSize !== 'xs'"
              class="col-12 order-2 flex"
            >
              <div class="request-dashboard-actions q-ml-auto q-mt-md">
                <q-btn
                  no-caps
                  unelevated
                  :color="showCreatePickList ? 'accent' : 'positive'"
                  :label="`(${selectedRequestItems.length}) ${showCreatePickList ? 'Create Pick List' : 'Add To Pick List'}`"
                  class="btn-no-wrap text-body1 q-mr-xs full-height"
                  :disabled="selectedRequestItems.length == 0"
                  :loading="appActionIsLoadingData"
                  @click="showCreatePickList ? createPickListJob() : updatePickListJob()"
                />
                <q-btn
                  no-caps
                  outline
                  label="Cancel"
                  class="btn-no-wrap text-body1 q-ml-xs full-height"
                  @click="resetPickListForm(); requestTableComponent.resetTablePagination(); loadRequestJobs();"
                />
              </div>
            </div>
            <MobileActionBar
              v-else-if="(showCreatePickList || showAddPickList) && currentScreenSize == 'xs'"
              :button-one-color="showCreatePickList ? 'accent' : 'positive'"
              :button-one-label="`(${selectedRequestItems.length}) ${showCreatePickList ? 'Create Pick List' : 'Add To Pick List'}`"
              :button-one-outline="false"
              :button-one-loading="appActionIsLoadingData"
              :button-one-disabled="selectedRequestItems.length == 0"
              @button-one-click="showCreatePickList ? createPickListJob() : updatePickListJob()"
              :button-two-color="'black'"
              :button-two-label="'Cancel'"
              :button-two-outline="true"
              @button-two-click="resetPickListForm(); requestTableComponent.resetTablePagination(); loadRequestJobs();"
            />
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
                <!-- Request View Filters -->
                <template v-if="requestDisplayType === 'request_view'">
                  <!-- Request ID filter -->
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

                  <!-- Request Type filter -->
                  <q-select
                    v-else-if="col.name === 'request_type'"
                    v-model="columnFilters.request_type"
                    dense
                    outlined
                    clearable
                    multiple
                    emit-value
                    map-options
                    :options="requestTypeOptionsFromData.length > 0 ? requestTypeOptionsFromData : requestTypeOptions"
                    placeholder="All"
                    class="column-filter-input"
                    @update:model-value="applyColumnFilters"
                    @click.stop
                  />

                  <!-- Barcode filter -->
                  <q-input
                    v-else-if="col.name === 'barcode_value'"
                    v-model="columnFilters.barcode"
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

                  <!-- External Request ID filter -->
                  <q-input
                    v-else-if="col.name === 'external_request_id'"
                    v-model="columnFilters.external_request_id"
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

                  <!-- Building filter -->
                  <q-select
                    v-else-if="col.name === 'building_name'"
                    v-model="columnFilters.building_id"
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

                  <!-- Requestor Name filter -->
                  <q-input
                    v-else-if="col.name === 'requestor_name'"
                    v-model="columnFilters.requestor_name"
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
                    :options="statusOptionsFromData.length > 0 ? statusOptionsFromData : requestStatusOptions"
                    placeholder="All"
                    class="column-filter-input"
                    @update:model-value="applyColumnFilters"
                    @click.stop
                  />

                  <!-- Priority filter -->
                  <q-select
                    v-else-if="col.name === 'priority'"
                    v-model="columnFilters.priority"
                    dense
                    outlined
                    clearable
                    multiple
                    emit-value
                    map-options
                    :options="priorityOptionsFromData.length > 0 ? priorityOptionsFromData : priorityOptions"
                    placeholder="All"
                    class="column-filter-input"
                    @update:model-value="applyColumnFilters"
                    @click.stop
                  />

                  <!-- Media Type filter -->
                  <q-select
                    v-else-if="col.name === 'media_type'"
                    v-model="columnFilters.media_type"
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

                  <!-- Item Location filter -->
                  <q-input
                    v-else-if="col.name === 'location'"
                    v-model="columnFilters.location"
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

                  <!-- Delivery Location filter -->
                  <q-select
                    v-else-if="col.name === 'delivery_location'"
                    v-model="columnFilters.delivery_location"
                    dense
                    outlined
                    clearable
                    multiple
                    emit-value
                    map-options
                    :options="deliveryLocationOptionsFromData.length > 0 ? deliveryLocationOptionsFromData : deliveryLocationOptions"
                    placeholder="All"
                    class="column-filter-input"
                    @update:model-value="applyColumnFilters"
                    @click.stop
                  />
                </template>

                <!-- Batch View Filters -->
                <template v-else>
                  <!-- Import Source filter -->
                  <q-input
                    v-if="col.name === 'file_type'"
                    v-model="batchColumnFilters.file_type"
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

                  <!-- Status filter (Batch) -->
                  <q-select
                    v-else-if="col.name === 'status'"
                    v-model="batchColumnFilters.status"
                    dense
                    outlined
                    clearable
                    multiple
                    emit-value
                    map-options
                    :options="batchStatusOptions"
                    placeholder="All"
                    class="column-filter-input"
                    @update:model-value="applyColumnFilters"
                    @click.stop
                  />

                  <!-- Uploaded By filter -->
                  <q-input
                    v-else-if="col.name === 'user_id'"
                    v-model="batchColumnFilters.user_name"
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
                </template>
              </q-th>
            </q-tr>
          </template>

          <template #table-td="{ colName, value }">
            <span
              v-if="colName == 'request_type'"
              class="text-nowrap"
              :class="value == '' ? 'text-highlight outline' : ''"
            >
              {{ value }}
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
            <span
              v-else-if="colName == 'media_type'"
              class="outline text-nowrap"
              :class="'text-highlight'"
            >
              {{ value }}
            </span>
            <span v-else-if="colName == 'create_dt'">
              {{ formatDateTime(value).date }}
            </span>
          </template>
        </EssentialTable>
      </div>
    </div>

    <!-- Request Creation Modal -->
    <RequestCreateEditModal
      v-if="showCreateRequestByType"
      :type="showCreateRequestByType"
      :request-data="requestJob"
      @change-display="requestDisplayType = $event"
      @hide="showCreateRequestByType = null"
    />

    <!-- Create/Add To Picklist Modal -->
    <PopupModal
      v-if="showPickListModal"
      ref="picklistModalComponent"
      :show-actions="false"
      @reset="showPickListModal = null"
      aria-label="picklistJobModal"
    >
      <template #header-content="{ hideModal }">
        <q-card-section class="row items-center justify-between q-pb-none">
          <h2 class="text-h6">
            {{ showPickListModal == 'Create' ? 'Filter Requests By Building' : 'Filter Requests & Select Pick List' }}
          </h2>

          <q-btn
            icon="close"
            flat
            round
            dense
            aria-label="Close"
            @click="filterRequestsByBuilding = null; addToPickListJob = null; hideModal();"
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
              v-model="filterRequestsByBuilding"
              :options="buildings"
              option-type="buildings"
              option-value="id"
              option-label="name"
              :placeholder="'Select Building'"
              aria-label="buildingSelect"
            />
          </div>

          <div
            v-if="showPickListModal == 'Add'"
            class="form-group q-mt-md"
          >
            <label class="form-group-label">
              Add To Pick List
            </label>
            <SelectInput
              v-model="addToPickListJob"
              :options="picklists"
              option-type="picklists"
              :option-query="{status: [
                'Created',
                'Paused'
              ]}"
              option-value="id"
              option-label="id"
              :force-option-type-reload="true"
              :placeholder="'Select Pick List Job'"
              aria-label="picklistJobSelect"
            >
              <template #option="{ itemProps, opt, selected, toggleOption }">
                <q-item v-bind="itemProps">
                  <q-item-section>
                    <q-item-label class="text-body1">
                      <span>Job #: {{ opt.id }}</span>
                      <span class="text-secondary"> - {{ opt.request_count }} Items ({{ formatDateTime(opt.create_dt).date }})</span>
                    </q-item-label>
                  </q-item-section>
                  <q-item-section side>
                    <q-checkbox
                      :model-value="selected"
                      @update:model-value="toggleOption(opt)"
                    />
                  </q-item-section>
                </q-item>
              </template>
            </SelectInput>
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
            :disabled="showPickListModal == 'Create' ? !filterRequestsByBuilding : (!filterRequestsByBuilding || !addToPickListJob)"
            :loading="appActionIsLoadingData"
            @click="loadRequestJobsByBuilding()"
          />

          <q-space class="q-mx-xs" />

          <q-btn
            outline
            no-caps
            label="Cancel"
            class="text-body1 full-width"
            @click="filterRequestsByBuilding = null; addToPickListJob = null; hideModal();"
          />
        </q-card-section>
      </template>
    </PopupModal>
  </div>
</template>

<script setup>
import { onBeforeMount, ref, inject, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Notify } from 'quasar'
import { useGlobalStore } from '@/stores/global-store'
import { useUserStore } from '@/stores/user-store'
import { useOptionStore } from '@/stores/option-store'
import { useRequestStore } from '@/stores/request-store'
import { usePicklistStore } from '@/stores/picklist-store'
import { storeToRefs } from 'pinia'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import EssentialTable from '@/components/EssentialTable.vue'
import ToggleButtonInput from '@/components/ToggleButtonInput.vue'
import MobileActionBar from '@/components/MobileActionBar.vue'
import RequestCreateEditModal from '@/components/Request/RequestCreateEditModal.vue'
import PopupModal from '@/components/PopupModal.vue'
import SelectInput from '@/components/SelectInput.vue'

const router = useRouter()

// Composables
const { currentScreenSize } = useCurrentScreenSize()
const { checkUserPermission } = usePermissionHandler()

// Store Data
const { appIsLoadingData, appActionIsLoadingData } = storeToRefs(useGlobalStore())
const {
  buildings,
  picklists,
  mediaTypes,
  requestsPriorities,
  requestsLocations
} = storeToRefs(useOptionStore())
const {
  getRequestJobList,
  getRequestJob,
  getRequestBatchJobList,
  getRequestBatchJob
} = useRequestStore()
const {
  requestJobList,
  requestJobListTotal,
  requestJob
} = storeToRefs(useRequestStore())
const { postPicklistJob, patchPicklistJobItem } = usePicklistStore()
const { picklistJob } = storeToRefs(usePicklistStore())
const { userData } = storeToRefs(useUserStore())

// Filter State
const showFilterRow = ref(false)

// Column filter state for Request View (server-side filtering)
const columnFilters = ref({
  id: null,
  request_type: [],
  barcode: null,
  external_request_id: null,
  building_id: [],
  requestor_name: null,
  status: [],
  priority: [],
  media_type: [],
  location: null,
  delivery_location: []
})

// Column filter state for Batch View
const batchColumnFilters = ref({
  file_type: null,
  status: [],
  user_name: null
})

// Static filter dropdown options
const requestTypeOptions = [
  {
    label: 'General Delivery',
    value: 'General Delivery'
  },
  {
    label: 'ILL (InterLibrary Loan)',
    value: 'ILL (InterLibrary Loan)'
  },
  {
    label: 'Scan and Deliver',
    value: 'Scan and Deliver'
  }
]

const requestStatusOptions = [
  {
    label: 'New',
    value: 'New'
  },
  {
    label: 'PickList',
    value: 'PickList'
  },
  {
    label: 'Retrieved',
    value: 'Retrieved'
  },
  {
    label: 'Completed',
    value: 'Completed'
  }
]

const batchStatusOptions = [
  {
    label: 'New',
    value: 'New'
  },
  {
    label: 'Processing',
    value: 'Processing'
  },
  {
    label: 'Failed',
    value: 'Failed'
  },
  {
    label: 'Cancelled',
    value: 'Cancelled'
  },
  {
    label: 'Completed',
    value: 'Completed'
  }
]

// Dynamic filter options from current table data
const requestTypeOptionsFromData = computed(() => {
  const types = new Set()
  requestJobList.value.forEach(row => {
    if (row.request_type?.type) {
      types.add(row.request_type.type)
    }
  })
  return Array.from(types).sort().map(t => ({
    label: t,
    value: t
  }))
})

const buildingOptionsFromData = computed(() => {
  const buildingSet = new Set()
  requestJobList.value.forEach(row => {
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
      value: parsed.id
    }
  }).sort((a, b) => a.label.localeCompare(b.label))
})

const statusOptionsFromData = computed(() => {
  const statuses = new Set()
  requestJobList.value.forEach(row => {
    if (row.status) {
      statuses.add(row.status)
    }
  })
  return Array.from(statuses).sort().map(s => ({
    label: s,
    value: s
  }))
})

const priorityOptionsFromData = computed(() => {
  const priorities = new Set()
  requestJobList.value.forEach(row => {
    if (row.priority?.value) {
      priorities.add(row.priority.value)
    }
  })
  return Array.from(priorities).sort().map(p => ({
    label: p,
    value: p
  }))
})

const mediaTypeOptionsFromData = computed(() => {
  const types = new Set()
  requestJobList.value.forEach(row => {
    const type = row.item?.media_type?.name || row.non_tray_item?.media_type?.name
    if (type) {
      types.add(type)
    }
  })
  return Array.from(types).sort().map(t => ({
    label: t,
    value: t
  }))
})

const deliveryLocationOptionsFromData = computed(() => {
  const locations = new Set()
  requestJobList.value.forEach(row => {
    if (row.delivery_location?.name) {
      locations.add(row.delivery_location.name)
    }
  })
  return Array.from(locations).sort().map(l => ({
    label: l,
    value: l
  }))
})

// Fallback options from store data
const buildingOptions = computed(() =>
  buildings.value.map(b => ({
    label: b.name,
    value: b.id
  }))
)

const priorityOptions = computed(() =>
  requestsPriorities.value.map(p => ({
    label: p.value,
    value: p.value
  }))
)

const mediaTypeOptions = computed(() =>
  mediaTypes.value.map(m => ({
    label: m.name,
    value: m.name
  }))
)

const deliveryLocationOptions = computed(() =>
  requestsLocations.value.map(dl => ({
    label: dl.name,
    value: dl.name
  }))
)

// Status badge helper functions
const getStatusIcon = (status) => {
  switch (status) {
    case 'New':
      return 'mdi-plus-circle'
    case 'PickList':
      return 'mdi-format-list-checks'
    case 'Retrieved':
      return 'mdi-truck-delivery'
    case 'Processing':
      return 'mdi-progress-clock'
    case 'Completed':
      return 'mdi-check-circle'
    case 'Failed':
      return 'mdi-alert-circle'
    case 'Cancelled':
      return 'mdi-cancel'
    default:
      return 'mdi-help-circle'
  }
}

const getStatusBadgeClass = (status) => {
  switch (status) {
    case 'New':
      return 'status-new'
    case 'PickList':
    case 'Processing':
    case 'Retrieved':
      return 'status-in-progress'
    case 'Completed':
      return 'status-completed'
    case 'Failed':
      return 'status-failed'
    case 'Cancelled':
      return 'status-cancelled'
    default:
      return ''
  }
}

// Local Data
const requestJobMenuState = ref(false)
const requestTableComponent = ref(null)
const requestTableVisibleColumns = ref([
  'id',
  'request_type',
  'barcode_value',
  'external_request_id',
  'building_name',
  'requestor_name',
  'status',
  'priority',
  'media_type',
  'location',
  'delivery_location',
  'create_dt'
])
const requestTableColumns = ref([
  {
    name: 'id',
    field: 'id',
    label: 'Request ID #',
    align: 'left',
    sortable: true
  },
  {
    name: 'request_type',
    field: row => row.request_type?.type,
    label: 'Request Type',
    align: 'left',
    sortable: true
  },
  {
    name: 'barcode_value',
    field: row => row.item ? renderItemBarcodeDisplay(row.item) : renderItemBarcodeDisplay(row.non_tray_item),
    label: 'Barcode',
    align: 'left',
    sortable: true,
    style: 'min-width: 150px'
  },
  {
    name: 'external_request_id',
    field: 'external_request_id',
    label: 'External Request ID',
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
    name: 'requestor_name',
    field: 'requestor_name',
    label: 'Requestor Name',
    align: 'left',
    sortable: true
  },
  {
    name: 'status',
    field: 'status',
    label: 'Request Status',
    align: 'left',
    sortable: true
  },
  {
    name: 'priority',
    field: row => row.priority?.value,
    label: 'Priority',
    align: 'left',
    sortable: true
  },
  {
    name: 'media_type',
    field: row => row.item ? row.item?.media_type?.name : row.non_tray_item?.media_type?.name,
    label: 'Media Type',
    align: 'left',
    sortable: true
  },
  {
    name: 'location',
    field: row => row.item ? getItemLocation(row.item.tray) : getItemLocation(row.non_tray_item),
    label: 'Item Location',
    align: 'left',
    sortable: true
  },
  {
    name: 'delivery_location',
    field: row => row.delivery_location?.name,
    label: 'Delivery Location',
    align: 'left',
    sortable: true
  },
  {
    name: 'create_dt',
    field: 'create_dt',
    label: 'Date Created',
    align: 'left',
    sortable: true
  }
])
const requestBatchTableVisibleColumns = ref([
  'file_type',
  'request_count',
  'status',
  'user_id',
  'create_dt'
])
const requestBatchTableColumns = ref([
  {
    name: 'file_type',
    field: 'file_type',
    label: 'Import Source',
    align: 'left',
    sortable: true
  },
  {
    name: 'request_count',
    field: row => row.requests?.length,
    label: '# of Requests',
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
    field: row => row.user?.name,
    label: 'Uploaded By',
    align: 'left',
    sortable: true
  },
  {
    name: 'create_dt',
    field: 'create_dt',
    label: 'Date Imported',
    align: 'left',
    sortable: true
  }
])
const requestDisplayType = ref('request_view')
const picklistModalComponent = ref(null)
const showCreatePickList = ref(false)
const showAddPickList = ref(false)
const showPickListModal = ref(null)
const addToPickListJob = ref(null)
const selectedRequestItems = ref([])
const showCreateRequestByType = ref(null)
const filterRequestsByBuilding = ref(null)
const activePickListBuildingFilter = ref(null)

// Logic

const formatDateTime = inject('format-date-time')
const getItemLocation = inject('get-item-location')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')

onBeforeMount(() => {
  loadRequestJobs()

  if (currentScreenSize.value == 'xs') {
    requestTableVisibleColumns.value = [
      'id',
      'request_type',
      'barcode_value',
      'requestor_name'
    ]
    requestBatchTableVisibleColumns.value = [
      'file_type',
      'request_count',
      'status',
      'create_dt'
    ]
  }
})

const clearTableSelection = () => {
  requestTableComponent.value.clearSelectedData()
  selectedRequestItems.value = []
}
const resetPickListForm = () => {
  showCreatePickList.value = false
  showAddPickList.value = false
  showPickListModal.value = null
  filterRequestsByBuilding.value = null
  activePickListBuildingFilter.value = null
  addToPickListJob.value = null
  clearTableSelection()
}

const loadRequestJobs = async (qParams) => {
  try {
    appIsLoadingData.value = true
    if (requestDisplayType.value == 'request_view') {
      const isPickListMode = showCreatePickList.value || showAddPickList.value

      // Build filter params from column filters
      const filterParams = {
        ...qParams,
        queue: true,
        unassociated_pick_list: isPickListMode ? true : false,
        // Persist building filter during pick list mode for sorting/pagination
        ...(isPickListMode && activePickListBuildingFilter.value ? { building_id: activePickListBuildingFilter.value } : {})
      }

      // Apply column filters (only when not in pick list mode)
      if (!isPickListMode) {
        if (columnFilters.value.id) {
          filterParams.id = columnFilters.value.id
        }
        if (columnFilters.value.request_type && columnFilters.value.request_type.length > 0) {
          filterParams.request_type = columnFilters.value.request_type
        }
        if (columnFilters.value.barcode) {
          filterParams.barcode_value = columnFilters.value.barcode
        }
        if (columnFilters.value.external_request_id) {
          filterParams.external_request_id = columnFilters.value.external_request_id
        }
        if (columnFilters.value.building_id && columnFilters.value.building_id.length > 0) {
          filterParams.building_id = columnFilters.value.building_id
        }
        if (columnFilters.value.requestor_name) {
          filterParams.requestor_name = columnFilters.value.requestor_name
        }
        if (columnFilters.value.status && columnFilters.value.status.length > 0) {
          filterParams.status = columnFilters.value.status
        }
        if (columnFilters.value.priority && columnFilters.value.priority.length > 0) {
          filterParams.priority = columnFilters.value.priority
        }
        if (columnFilters.value.media_type && columnFilters.value.media_type.length > 0) {
          filterParams.media_type = columnFilters.value.media_type
        }
        if (columnFilters.value.location) {
          // Convert displayed location format to database format
          // Display uses "R" and "L", database stores "Right" and "Left"
          let locationSearch = columnFilters.value.location
          // Replace standalone -R- with -Right- and -L- with -Left-
          locationSearch = locationSearch.replace(/-R-/gi, '-Right-')
          locationSearch = locationSearch.replace(/-L-/gi, '-Left-')
          // Handle if R or L is at the end (e.g., "M1-A2-R")
          locationSearch = locationSearch.replace(/-R$/gi, '-Right')
          locationSearch = locationSearch.replace(/-L$/gi, '-Left')
          filterParams.item_location = locationSearch
        }
        if (columnFilters.value.delivery_location && columnFilters.value.delivery_location.length > 0) {
          filterParams.delivery_location = columnFilters.value.delivery_location
        }
      }

      await getRequestJobList(filterParams)
    } else {
      // Batch View filters
      const batchFilterParams = { ...qParams }

      if (batchColumnFilters.value.file_type) {
        batchFilterParams.file_type = batchColumnFilters.value.file_type
      }
      if (batchColumnFilters.value.status && batchColumnFilters.value.status.length > 0) {
        batchFilterParams.status = batchColumnFilters.value.status
      }
      if (batchColumnFilters.value.user_name) {
        batchFilterParams.user_name = batchColumnFilters.value.user_name
      }

      await getRequestBatchJobList(batchFilterParams)
    }
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to load requests'
    })
  } finally {
    appIsLoadingData.value = false
  }
}

// Apply column filters - triggers server-side filtering
const applyColumnFilters = () => {
  // Reset pagination to page 1 when filters change
  if (requestTableComponent.value) {
    requestTableComponent.value.resetTablePagination()
  }
  loadRequestJobs()
}

// Clear all column filters
const clearColumnFilters = () => {
  if (requestDisplayType.value === 'request_view') {
    columnFilters.value = {
      id: null,
      request_type: [],
      barcode: null,
      external_request_id: null,
      building_id: [],
      requestor_name: null,
      status: [],
      priority: [],
      media_type: [],
      location: null,
      delivery_location: []
    }
  } else {
    batchColumnFilters.value = {
      file_type: null,
      status: [],
      user_name: null
    }
  }
  applyColumnFilters()
}
const loadRequestJobsByBuilding = async () => {
  try {
    appActionIsLoadingData.value = true
    // change table view back to request view and clear out any pagination settings
    requestDisplayType.value = 'request_view'
    requestTableComponent.value.resetTablePagination()
    // Store the selected building filter so it persists during sorting/pagination
    activePickListBuildingFilter.value = filterRequestsByBuilding.value

    await getRequestJobList({
      building_id: filterRequestsByBuilding.value,
      unassociated_pick_list: true
    })

    // display next step in picklist creation
    if (showPickListModal.value == 'Create') {
      showCreatePickList.value = true
    } else {
      showAddPickList.value = true
    }
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to filter requests'
    })
  } finally {
    appActionIsLoadingData.value = false
    picklistModalComponent.value.hideModal()
  }
}
const loadRequestJob = async (id) => {
  try {
    appIsLoadingData.value = true

    if (requestDisplayType.value == 'batch_view') {
      await getRequestBatchJob(id)
      router.push({
        name: 'request-batch',
        params: {
          jobId: id
        }
      })
    } else {
      await getRequestJob(id)
      router.push({
        name: 'request-details',
        params: {
          jobId: id
        }
      })
    }
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to load request job'
    })
  } finally {
    appIsLoadingData.value = false
  }
}
const createPickListJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      request_ids: selectedRequestItems.value.map(item => item.id),
      created_by_id: userData.value.user_id
    }
    await postPicklistJob(payload)

    // display an alert with the created picklist job id so you can click that and link directly to the new job if needed
    Notify.create({
      type: 'positive',
      message: `Successfully created Pick List #: <a href='/picklist/${picklistJob.value.id}' style='color: white; text-decoration: underline;'>${picklistJob.value.id}</a>`,
      html: true,
      timeout: 0,
      actions: [
        {
          icon: 'close',
          color: 'white'
        }
      ]
    })
    requestTableComponent.value.resetTablePagination()
    loadRequestJobs()
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to create picklist'
    })
  } finally {
    appActionIsLoadingData.value = false
    resetPickListForm()
  }
}
const updatePickListJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: addToPickListJob.value,
      request_ids: selectedRequestItems.value.map(item => item.id)
    }
    await patchPicklistJobItem(payload)

    // display an alert with the updated picklist job id so you can click that and link directly to the job if needed
    Notify.create({
      type: 'positive',
      message: `Successfully added items to Pick List #: <a href='/picklist/${picklistJob.value.id}' style='color: white; text-decoration: underline;'>${picklistJob.value.id}</a>`,
      html: true,
      timeout: 0,
      actions: [
        {
          icon: 'close',
          color: 'white'
        }
      ]
    })
    requestTableComponent.value.resetTablePagination()
    loadRequestJobs()
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to update picklist'
    })
  } finally {
    appActionIsLoadingData.value = false
    resetPickListForm()
  }
}
</script>

<style lang="scss" scoped>
.toggle-modern {
  :deep(.q-btn-group) {
    border-radius: 10px;
    overflow: hidden;
    box-shadow:
      0 2px 4px rgba(0, 0, 0, 0.1),
      0 4px 8px rgba(0, 0, 0, 0.08);
  }

  :deep(.q-btn) {
    border-radius: 0;
    font-weight: 600;
    letter-spacing: 0.02em;
    transition: all 0.2s ease-in-out;

    &:first-child {
      border-top-left-radius: 10px;
      border-bottom-left-radius: 10px;
    }

    &:last-child {
      border-top-right-radius: 10px;
      border-bottom-right-radius: 10px;
    }
  }
}

// Table horizontal scroll and minimum column widths
:deep(.q-table__container) {
  overflow-x: auto;
}

:deep(.q-table) {
  min-width: 1400px; // Ensure table has minimum width to prevent cramping
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

// Ensure table header cells also have minimum widths
:deep(.q-table th) {
  min-width: 100px;
  white-space: nowrap;
}
</style>
