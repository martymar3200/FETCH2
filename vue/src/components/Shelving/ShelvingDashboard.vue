<template>
  <div class="shelving-dashboard">
    <div class="row q-mb-xs-xl q-mb-sm-none">
      <div class="col-grow q-mb-xs-md q-mb-sm-none">
        <EssentialTable
          ref="shelvingTableRef"
          :table-columns="shelfTableColumns"
          :table-visible-columns="shelfTableVisibleColumns"
          :table-data="shelvingJobList"
          :enable-table-reorder="false"
          :heading-row-class="'q-mb-xs-md q-mb-md-lg'"
          :enable-pagination="true"
          :pagination-total="shelvingJobListTotal"
          :pagination-loading="appIsLoadingData"
          :hide-table-rearrange="true"
          @update-pagination="loadShelvingJobs($event)"
          @selected-table-row="loadShelvingJob($event.id, $event.origin)"
        >
          <template #heading-row>
            <div
              class="col-sm-5 col-md-12 col-lg-auto"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? '' : 'self-center'"
            >
              <h1 class="text-h4 text-bold">
                Shelving Jobs
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
              <q-btn
                no-caps
                unelevated
                icon-right="arrow_drop_down"
                color="accent"
                label="Create Shelving Job"
                class="text-body1 btn-modern"
                :disabled="appIsOffline"
                aria-label="createShelvingJobMenu"
                aria-haspopup="menu"
                :aria-expanded="shelvingJobMenuState"
              >
                <q-menu
                  @show="shelvingJobMenuState = true"
                  @hide="shelvingJobMenuState = false"
                  aria-label="requestJobMenuList"
                >
                  <q-list>
                    <q-item
                      v-if="checkUserPermission('can_create_and_execute_direct_shelving_job')"
                      clickable
                      v-close-popup
                      @click="showShelvingJobModal = 'Direct'"
                      role="menuitem"
                    >
                      <q-item-section>
                        <q-item-label>
                          <span>
                            Direct To Shelf
                          </span>
                        </q-item-label>
                      </q-item-section>
                    </q-item>
                    <q-item
                      v-if="checkUserPermission('can_create_and_execute_shelving_job')"
                      clickable
                      v-close-popup
                      @click="$router.push({ name: 'ShelveByListCreate' })"
                      role="menuitem"
                    >
                      <q-item-section>
                        <q-item-label>
                          <span>
                            Shelve by List
                          </span>
                        </q-item-label>
                      </q-item-section>
                    </q-item>
                    <q-item
                      v-if="checkUserPermission('can_move_trays_and_items_shelving_locations')"
                      clickable
                      v-close-popup
                      @click="submitShelvingMove('tray-item')"
                      role="menuitem"
                    >
                      <q-item-section>
                        <q-item-label>
                          <span>
                            Move Tray Item
                          </span>
                        </q-item-label>
                      </q-item-section>
                    </q-item>
                    <q-item
                      v-if="checkUserPermission('can_move_trays_and_items_shelving_locations')"
                      clickable
                      v-close-popup
                      @click="submitShelvingMove('tray-non-tray')"
                      role="menuitem"
                    >
                      <q-item-section>
                        <q-item-label>
                          <span class="text-no-wrap">
                            Move Tray / Non-Tray
                          </span>
                        </q-item-label>
                      </q-item-section>
                    </q-item>
                  </q-list>
                </q-menu>
              </q-btn>
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

                <!-- Origin filter -->
                <q-select
                  v-else-if="col.name === 'origin'"
                  v-model="columnFilters.origin"
                  dense
                  outlined
                  clearable
                  :options="['Direct', 'List', 'Verification', 'Move']"
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
              v-else-if="colName == 'origin'"
              class="text-weight-medium"
            >
              {{ value }}
            </span>
            <span
              v-else-if="colName == 'container_count'"
              class="outline text-nowrap"
            >
              {{ value }} Containers
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

    <!-- Create Shelving Job Modal -->
    <PopupModal
      v-if="showShelvingJobModal"
      ref="createShelvingJobModal"
      :show-actions="false"
      :modal-width="'600px'"
      @reset="resetCreateShelfJobModal"
      aria-label="createShelvingJobModal"
    >
      <template #header-content="{ hideModal }">
        <q-card-section class="row items-center q-pb-none">
          <h2
            class="text-h6 text-bold"
          >
            {{ showShelvingJobModal == 'Direct' ? 'Create Direct Shelving Job' : 'Create Shelving Job' }}
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
        <q-card-section v-if="showShelvingJobModal == 'Verification'">
          <div class="row q-mb-md">
            <div class="col-xs-12 col-sm-8 flex items-center">
              <p :class="currentScreenSize !== 'xs' ? 'text-h6' : 'text-body1 q-mb-xs'">
                Assign Shelving Location?
              </p>
            </div>
            <div class="col-xs-12 col-sm-4">
              <div class="form-group">
                <ToggleButtonInput
                  v-model="shelvingJob.assignLocation"
                  :options="[
                    {label: 'Yes', value: true},
                    {label: 'No', value: false}
                  ]"
                />
              </div>
            </div>
          </div>

          <div class="row">
            <div class="col-12 q-mb-sm">
              <h3 class="text-h6 text-bold">
                Verification Job List:
              </h3>
            </div>
            <div class="col-12">
              <div class="form-group">
                <label class="form-group-label">
                  Please Select Verification Job(s)
                </label>
                <SelectInput
                  v-model="shelvingJob.verification_jobs"
                  :multiple="true"
                  :use-chips="true"
                  :hide-selected="false"
                  :force-option-type-reload="true"
                  :options="verificationJobsDropdown"
                  option-type="verificationJobsDropdown"
                  :option-query="{ unshelved: true }"
                  option-value="id"
                  option-label="workflow_id"
                  :placeholder="'Select Verification Job(s) by Number'"
                  aria-label="verificationJobSelect"
                >
                  <template #option="{ itemProps, opt, selected, toggleOption }">
                    <q-item v-bind="itemProps">
                      <q-item-section>
                        <q-item-label class="text-body1">
                          <span>Job #: {{ opt.workflow_id }}</span>
                          <span class="text-secondary"> - {{ opt.trayed ? 'Trayed' : 'Non-Tray' }} ({{ opt.trayed ? `${opt.tray_count} containers, ${opt.item_count} items` : `${opt.non_tray_item_count} items` }})</span>
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
            </div>
          </div>

          <div class="row q-mt-md">
            <div class="col-12 q-mb-sm">
              <h3 class="text-h6 text-bold">
                Please Select Shelving Locations:
              </h3>
            </div>
            <div class="col-12">
              <div
                class="form-group q-mb-md"
              >
                <label class="form-group-label">
                  Building
                </label>
                <SelectInput
                  v-model="shelvingJob.building_id"
                  :options="buildings"
                  option-type="buildings"
                  option-value="id"
                  option-label="name"
                  :clearable="false"
                  :placeholder="'Select Building'"
                  @update:model-value="handleShelvingJobFormChange('Building')"
                  aria-label="buildingSelect"
                />
              </div>

              <template v-if="shelvingJob.assignLocation">
                <div
                  class="form-group q-mb-md"
                >
                  <label class="form-group-label">
                    Module
                  </label>
                  <SelectInput
                    v-model="shelvingJob.module_id"
                    :options="modules"
                    option-type="modules"
                    :option-query="{
                      building_id: shelvingJob.building_id
                    }"
                    option-value="id"
                    option-label="module_number"
                    :placeholder="'Select Module'"
                    :disabled="!shelvingJob.building_id"
                    :clearable="false"
                    @update:model-value="handleShelvingJobFormChange('Module')"
                    aria-label="moduleSelect"
                  />
                </div>

                <div class="row">
                  <div
                    class="col-xs-12 col-sm-6 q-pr-sm-xs q-mb-md"
                  >
                    <div class="form-group">
                      <label class="form-group-label">
                        Aisle
                      </label>
                      <SelectInput
                        v-model="shelvingJob.aisle_id"
                        :options="aisles"
                        option-type="aisles"
                        :option-query="{
                          building_id: shelvingJob.building_id,
                          module_id: shelvingJob.module_id,
                          sort_by: 'aisle_number'
                        }"
                        option-value="id"
                        :option-label="opt => opt.aisle_number.number"
                        :placeholder="'Select Aisle'"
                        :disabled="!shelvingJob.module_id"
                        :clearable="false"
                        @update:model-value="handleShelvingJobFormChange('Aisle')"
                        aria-label="aisleSelect"
                      />
                    </div>
                  </div>
                  <div
                    class="col-xs-12 col-sm-6 q-pl-sm-xs q-mb-xs-md q-mb-sm-none"
                  >
                    <div class="form-group">
                      <label class="form-group-label">
                        Side
                      </label>
                      <ToggleButtonInput
                        v-model="shelvingJob.side_id"
                        :options="sides"
                        option-value="id"
                        option-label="side_orientation.name"
                        :disabled="!shelvingJob.aisle_id"
                        @update:model-value="handleShelvingJobFormChange('Side')"
                      />
                    </div>
                  </div>
                </div>

                <div
                  class="form-group"
                >
                  <label class="form-group-label">
                    Ladder
                  </label>
                  <SelectInput
                    v-model="shelvingJob.ladder_id"
                    :options="ladders"
                    option-type="ladders"
                    :option-query="{
                      building_id: shelvingJob.building_id,
                      module_id: shelvingJob.module_id,
                      aisle_id: shelvingJob.aisle_id,
                      side_id: shelvingJob.side_id,
                      sort_by: 'ladder_number'
                    }"
                    option-value="id"
                    :option-label="opt => opt.ladder_number.number"
                    :placeholder="'Select Ladder'"
                    :disabled="!shelvingJob.side_id"
                    :clearable="false"
                    @update:model-value="handleShelvingJobFormChange('Ladder')"
                    aria-label="ladderSelect"
                  />
                </div>
              </template>
            </div>
          </div>
        </q-card-section>
        <q-card-section v-else-if="showShelvingJobModal == 'Direct'">
          <div class="row">
            <div class="col-12 q-mb-sm">
              <h3 class="text-h6 text-bold">
                Please Select Shelving Location:
              </h3>
            </div>
            <div class="col-12">
              <div
                class="form-group"
              >
                <label class="form-group-label">
                  Building
                </label>
                <SelectInput
                  v-model="shelvingJob.building_id"
                  :options="buildings"
                  option-type="buildings"
                  option-value="id"
                  option-label="name"
                  :clearable="false"
                  :placeholder="'Select Building'"
                  @update:model-value="handleShelvingJobFormChange('Building')"
                  aria-label="buildingSelect"
                />
              </div>
            </div>
          </div>
        </q-card-section>
      </template>

      <template #footer-content="{ hideModal }">
        <q-card-section
          class="row no-wrap justify-between items-center q-pt-sm"
        >
          <q-btn
            v-if="showShelvingJobModal == 'Direct'"
            no-caps
            unelevated
            color="accent"
            label="Submit"
            class="text-body1 full-width"
            :disabled="!isCreateShelvingJobFormValid"
            @click="submitDirectToShelfJob(); hideModal();"
          />
          <q-btn
            v-else
            no-caps
            unelevated
            color="accent"
            label="Submit"
            class="text-body1 full-width"
            :disabled="!isCreateShelvingJobFormValid"
            @click="submitShelvingJob(); hideModal();"
          />

          <q-space class="q-mx-xs" />

          <q-btn
            outline
            no-caps
            label="Cancel"
            class="shelving-modal-btn text-body1 full-width"
            @click="hideModal"
          />
        </q-card-section>
      </template>
    </PopupModal>
  </div>
</template>

<script setup>
import { onBeforeMount, ref, inject, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useGlobalStore } from '@/stores/global-store'
import { useUserStore } from '@/stores/user-store'
import { useOptionStore } from '@/stores/option-store'
import { useBuildingStore } from '@/stores/building-store'
import { useShelvingStore } from '@/stores/shelving-store'
import { storeToRefs } from 'pinia'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import EssentialTable from '@/components/EssentialTable.vue'
import SelectInput from '@/components/SelectInput.vue'
import ToggleButtonInput from '@/components/ToggleButtonInput.vue'
import PopupModal from '@/components/PopupModal.vue'

const router = useRouter()

// Composables
const { currentScreenSize } = useCurrentScreenSize()
const { checkUserPermission } = usePermissionHandler()

// Store Data
const {
  appIsLoadingData,
  appIsOffline
} = storeToRefs(useGlobalStore())
const {
  buildings,
  modules,
  aisles,
  ladders,
  verificationJobsDropdown
} = storeToRefs(useOptionStore())
const {
  getBuildingDetails,
  getModuleDetails,
  getAisleDetails,
  getSideDetails,
  getSideList,
  resetBuildingStore,
  resetBuildingChildren,
  resetModuleChildren,
  resetAisleChildren,
  resetSideChildren
} = useBuildingStore()
const { sides } = storeToRefs(useBuildingStore())
const {
  shelvingJobList,
  shelvingJob,
  shelvingJobListTotal
} = storeToRefs(useShelvingStore())
const {
  resetShelvingStore,
  resetShelvingJob,
  getShelvingJobList,
  postShelvingJob,
  getShelvingJob,
  createShelvingJob
} = useShelvingStore()
const { userData } = storeToRefs(useUserStore())

// Local Data
const createShelvingJobModal = ref(null)
const shelvingTableRef = ref(null)
const showFilterRow = ref(false)  // Toggle visibility of filter row

// Column filter state for server-side filtering
const columnFilters = ref({
  id: null,
  status: [
    'Created',
    'Paused',
    'Running'
  ]  // Default to showing active jobs
})

// Filter dropdown options
const statusOptions = [
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

// Status badge helper functions
const getStatusIcon = (status) => {
  switch (status) {
    case 'Created':
      return 'mdi-plus-circle'
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

const shelfTableVisibleColumns = ref([
  'id',
  'origin',
  'container_count',
  'status',
  'user_id',
  'create_dt',
  'last_transition'
])
const shelfTableColumns = ref([
  {
    name: 'id',
    field: 'id',
    label: 'Job Number',
    align: 'left',
    sortable: true,
    order: 0
  },
  {
    name: 'origin',
    field: 'origin',
    label: 'Job Type',
    align: 'left',
    sortable: true,
    order: 1
  },
  {
    name: 'container_count',
    field: row => (row.tray_count + row.non_tray_item_count),
    label: '# of Containers in Job',
    align: 'left',
    sortable: true,
    order: 2
  },
  {
    name: 'status',
    field: 'status',
    label: 'Status',
    align: 'left',
    sortable: true,
    order: 3
  },
  {
    name: 'user_id',
    field: row => row.user ? row.user.name : '',
    label: 'Assigned User',
    align: 'left',
    sortable: true,
    order: 4
  },
  {
    name: 'create_dt',
    field: 'create_dt',
    label: 'Date Added',
    align: 'left',
    sortable: true,
    order: 5
  },
  {
    name: 'last_transition',
    field: 'last_transition',
    label: 'Last Updated',
    align: 'left',
    sortable: true,
    order: 6
  }
])
const shelvingJobMenuState = ref(false)
const showShelvingJobModal = ref(null)
const isCreateShelvingJobFormValid = computed(() => {
  if (showShelvingJobModal.value == 'Verification' && (shelvingJob.value.verification_jobs.length == 0 || !shelvingJob.value.building_id)) {
    return false
  } else if (showShelvingJobModal.value == 'Direct' && !shelvingJob.value.building_id) {
    return false
  } else {
    return true
  }
})

// Logic
const handleAlert = inject('handle-alert')
const formatDateTime = inject('format-date-time')

onBeforeMount(() => {
  resetShelvingStore()
  loadShelvingJobs()

  if (currentScreenSize.value == 'xs') {
    shelfTableVisibleColumns.value = [
      'id',
      'status',
      'user_id',
      'create_dt'
    ]
  }
})

const resetCreateShelfJobModal = () => {
  resetShelvingJob()
  resetBuildingStore()
  showShelvingJobModal.value = null
}
const handleShelvingJobFormChange = async (valueType) => {
  // reset the form depending on the edited form field type
  switch (valueType) {
    case 'Building':
      resetBuildingChildren()
      await getBuildingDetails(shelvingJob.value.building_id)
      shelvingJob.value.module_id = null
      shelvingJob.value.aisle_id = null
      shelvingJob.value.side_id = null
      shelvingJob.value.ladder_id = null
      return
    case 'Module':
      // clear state for aisle options downward since user needs to select an aisle next to populate the rest of the data
      resetModuleChildren()
      await getModuleDetails(shelvingJob.value.module_id)
      shelvingJob.value.aisle_id = null
      shelvingJob.value.side_id = null
      shelvingJob.value.ladder_id = null
      return
    case 'Aisle':
      resetAisleChildren()
      await getAisleDetails(shelvingJob.value.aisle_id)
      // also get sides since sides are buttons and not dynamically loaded from a options select input
      await getSideList({
        building_id: shelvingJob.value.building_id,
        module_id: shelvingJob.value.module_id,
        aisle_id: shelvingJob.value.aisle_id
      })
      shelvingJob.value.side_id = null
      shelvingJob.value.ladder_id = null
      return
    case 'Side':
      resetSideChildren()
      await getSideDetails(shelvingJob.value.side_id)
      shelvingJob.value.ladder_id = null
      return
    case 'Ladder':
      return
  }
}

const loadShelvingJobs = async (qParams) => {
  try {
    appIsLoadingData.value = true

    // Build filter params from column filters
    const filterParams = {
      ...qParams
    }

    // Add id search if provided
    if (columnFilters.value.id) {
      filterParams.id = columnFilters.value.id
    }

    // Add status filter
    if (columnFilters.value.status && columnFilters.value.status.length > 0) {
      filterParams.status = columnFilters.value.status
    }

    // Add origin filter
    if (columnFilters.value.origin) {
      filterParams.origin = columnFilters.value.origin
    }

    // Add user filter based on permission
    filterParams.user_id = checkUserPermission('can_view_all_shelving_jobs') ? null : userData.value.user_id

    await getShelvingJobList(filterParams)
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

// Apply column filters - triggers server-side filtering
const applyColumnFilters = () => {
  // Reset pagination to page 1 when filters change
  if (shelvingTableRef.value) {
    shelvingTableRef.value.resetTablePagination()
  }
  loadShelvingJobs()
}

// Clear all column filters
const clearColumnFilters = () => {
  columnFilters.value = {
    id: null,
    status: [
      'Created',
      'Paused',
      'Running'
    ], // Reset to default active statuses
    origin: null
  }
  applyColumnFilters()
}
const loadShelvingJob = async (jobId, type) => {
  try {
    appIsLoadingData.value = true
    await getShelvingJob(jobId)

    // Route based on job origin/type
    // Note: 'type' param comes from table row 'origin' field
    const origin = type || shelvingJob.value.origin

    if (origin === 'Direct') {
      router.push({
        name: 'shelving-dts',
        params: {
          jobId
        }
      })
    } else if (origin === 'List') {
      router.push({
        name: 'ShelveByListExecute',
        params: {
          id: jobId
        }
      })
    } else if (origin === 'Move') {
      let moveType = 'tray-item'
      if (shelvingJob.value.mode === 'MoveShelf') {
        moveType = 'tray-non-tray'
      }
      router.push({
        name: 'shelving-move',
        params: {
          type: moveType,
          jobId
        }
      })
    } else {
      // Default / Verification
      router.push({
        name: 'shelving',
        params: {
          jobId
        }
      })
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
const submitShelvingJob = async () => {
  try {
    appIsLoadingData.value = true
    const params = {
      module_id: shelvingJob.value.module_id,
      aisle_id: shelvingJob.value.aisle_id,
      side_id: shelvingJob.value.side_id,
      ladder_id: shelvingJob.value.ladder_id
    }
    const payload = {
      status: 'Created',
      building_id: shelvingJob.value.building_id,
      verification_jobs: shelvingJob.value.verification_jobs,
      origin: 'Verification',
      created_by_id: userData.value.user_id
    }
    await postShelvingJob(payload, params)

    // route the user to the shelving job detail page
    router.push({
      name: 'shelving',
      params: {
        jobId: shelvingJob.value.id
      }
    })

    handleAlert({
      type: 'success',
      text: 'A Shelving Job has been successfully created.',
      autoClose: true
    })
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appIsLoadingData.value = false
    createShelvingJobModal.value.hideModal()
  }
}
const submitDirectToShelfJob = async () => {
  try {
    appIsLoadingData.value = true
    const payload = {
      status: 'Created',
      building_id: shelvingJob.value.building_id,
      user_id: userData.value.user_id,
      origin: 'Direct',
      created_by_id: userData.value.user_id
    }
    await createShelvingJob(payload)
    router.push({
      name: 'shelving-dts',
      params: {
        jobId: shelvingJob.value.id
      }
    })

    handleAlert({
      type: 'success',
      text: 'A Direct Shelving Job has been successfully created.',
      autoClose: true
    })
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appIsLoadingData.value = false
    createShelvingJobModal.value.hideModal()
  }
}
const submitShelvingMove = async (moveType) => {
  try {
    appIsLoadingData.value = true
    const mode = moveType === 'tray-item' ? 'MoveTrayItem' : 'MoveShelf'
    const payload = {
      status: 'Created',
      origin: 'Move',
      mode,
      building_id: userData.value.building_id || 1,
      created_by_id: userData.value.user_id,
      user_id: userData.value.user_id
    }
    const res = await createShelvingJob(payload)
    router.push({
      name: 'shelving-move',
      params: {
        type: moveType,
        jobId: res.id
      }
    })
  } catch (e) {
    handleAlert({
      type: 'error',
      text: e,
      autoClose: true
    })
  } finally {
    appIsLoadingData.value = false
  }
}

</script>
<style lang="scss" scoped>
.shelving {
  &-details {
    position: relative;
    display: flex;
    align-items: center;
    flex-wrap: nowrap;

    @media (max-width: $breakpoint-sm-min) {
      flex-direction: column;
      align-items: flex-start;
    }
  }
}
</style>
