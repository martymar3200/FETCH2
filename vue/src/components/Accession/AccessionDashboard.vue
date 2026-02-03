<template>
  <div class="accession-dashboard">
    <div class="row q-mb-xs-xl q-mb-sm-none">
      <div class="col-grow q-mb-xs-md q-mb-sm-none">
        <EssentialTable
          ref="accessionTableRef"
          :table-columns="accessionTableColumns"
          :table-visible-columns="accessionTableVisibleColumns"
          :table-data="accessionJobList"
          :enable-table-reorder="false"
          :heading-row-class="'q-mb-xs-md q-mb-md-lg'"
          :enable-pagination="true"
          :pagination-total="accessionJobListTotal"
          :pagination-loading="appIsLoadingData"
          :hide-table-rearrange="true"
          @update-pagination="loadAccessionJobs($event)"
          @selected-table-row="loadAccessionJob($event.workflow_id)"
        >
          <template #heading-row>
            <div
              class="col-sm-5 col-md-12 col-lg-auto"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? '' : 'self-center'"
            >
              <h1 class="text-h4 text-bold">
                Accession Jobs
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
                color="accent"
                icon="add_circle"
                label="Start Accession"
                class="btn-no-wrap text-body1 btn-modern"
                :disabled="appIsOffline"
                @click="startAccessionProcess"
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

  <!-- start accession process modal -->
  <PopupModal
    v-if="showAccessionModal"
    ref="accessionJobModal"
    :show-actions="false"
    @reset="reset"
    aria-label="AccessionJobCreationModal"
  >
    <template #header-content="{ hideModal }">
      <q-card-section class="row items-center justify-between q-pb-none">
        <h2
          v-if="accessionJob.trayed == null"
          class="text-h6"
        >
          Start New Accession
        </h2>
        <q-btn
          v-else
          icon="chevron_left"
          name="back"
          label="Back"
          no-caps
          flat
          dense
          class="text-body1"
          @click="accessionJob.trayed = null"
        />

        <q-btn
          icon="close"
          flat
          round
          dense
          aria-label="Close"
          @click="hideModal"
        />
      </q-card-section>
    </template>

    <template #main-content>
      <!-- first step in accession job process -->
      <q-card-section
        v-if="accessionJob.trayed == null"
        class="column no-wrap items-center"
      >
        <q-btn
          outline
          no-caps
          padding="14px md"
          label="Non-Tray Accession"
          class="accession-modal-btn full-width text-body1 q-mb-md"
          @click="accessionJob.trayed = false"
        />

        <q-btn
          outline
          no-caps
          padding="14px md"
          label="Trayed Accession"
          class="accession-modal-btn full-width text-body1"
          @click="accessionJob.trayed = true"
        />
      </q-card-section>

      <!-- second step in accession job process -->
      <template v-else>
        <q-card-section class="column no-wrap items-center">
          <div class="form-group q-mb-md">
            <label class="form-group-label">
              Owner <span class="text-caption text-negative">(Required)</span>
            </label>
            <SelectInput
              v-model="accessionJob.owner"
              :options="owners"
              option-type="owners"
              option-value="id"
              option-label="name"
              :placeholder="'Select Owner'"
              aria-label="ownerSelect"
            />
          </div>

          <div
            v-if="!accessionJob.trayed"
            class="form-group q-mb-md"
          >
            <label class="form-group-label">
              Container Size <span class="text-caption">(Optional)</span>
            </label>
            <SelectInput
              v-model="accessionJob.size_class"
              :options="sizeClass"
              option-type="sizeClass"
              option-value="id"
              option-label="name"
              :placeholder="'Select Size Class'"
              aria-label="containerSizeSelect"
            />
          </div>

          <div class="form-group">
            <label class="form-group-label">
              Media Type <span class="text-caption">(Optional)</span>
            </label>
            <SelectInput
              v-model="accessionJob.media_type"
              :options="mediaTypes"
              option-type="mediaTypes"
              option-value="id"
              option-label="name"
              :placeholder="'Select Media Type'"
              aria-label="mediaTypeSelect"
            />
          </div>
        </q-card-section>
      </template>
    </template>

    <template #footer-content="{ hideModal }">
      <q-card-section
        v-if="accessionJob.trayed !== null"
        class="row no-wrap justify-between items-center q-pt-sm"
      >
        <q-btn
          no-caps
          unelevated
          color="accent"
          label="Submit"
          class="text-body1 full-width"
          :disable="!canSubmitAccessionJob"
          :loading="appActionIsLoadingData"
          @click="submitAccessionJob()"
        />

        <q-space class="q-mx-xs" />

        <q-btn
          outline
          no-caps
          label="Cancel"
          class="accession-modal-btn text-body1 full-width"
          @click="hideModal"
        />
      </q-card-section>
    </template>
  </PopupModal>
</template>

<script setup>
import { onBeforeMount, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Notify } from 'quasar'
import { useUserStore } from '@/stores/user-store'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from 'src/stores/option-store'
import { useAccessionStore } from 'src/stores/accession-store'
import { storeToRefs } from 'pinia'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import EssentialTable from '@/components/EssentialTable.vue'
import SelectInput from '@/components/SelectInput.vue'
import PopupModal from '@/components/PopupModal.vue'

const router = useRouter()

// Composables
const { currentScreenSize } = useCurrentScreenSize()

// Store Data
const {
  appIsLoadingData,
  appActionIsLoadingData,
  appIsOffline
} = storeToRefs(useGlobalStore())
const {
  resetAccessionStore,
  postAccessionJob,
  getAccessionJobList,
  getAccessionJob
} = useAccessionStore()
const {
  accessionJob,
  accessionJobList,
  accessionJobListTotal
} = storeToRefs(useAccessionStore())
const {
  owners,
  mediaTypes,
  sizeClass
} = storeToRefs(useOptionStore())
const { userData } = storeToRefs(useUserStore())


// Local Data
const accessionJobModal = ref(null)
const accessionTableRef = ref(null)
const showFilterRow = ref(false)  // Toggle visibility of filter row

// Column filter state for server-side filtering
const columnFilters = ref({
  workflow_id: null,
  trayed: null,
  status: [
    'Created',
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

const accessionTableVisibleColumns = ref([
  'id',
  'trayed',
  'status'
])
const accessionTableColumns = ref([
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
const showAccessionModal = ref(false)
const canSubmitAccessionJob = computed(() => {
  if (accessionJob.value.owner !== null) {
    return true
  } else {
    return false
  }
})

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

// Logic


onBeforeMount(() => {
  resetAccessionStore()
  loadAccessionJobs()

  if (currentScreenSize.value == 'xs') {
    accessionTableVisibleColumns.value = [
      'id',
      'trayed',
      'status'
    ]
  }
})

const reset = () => {
  resetAccessionStore()
  showAccessionModal.value = false
}
const startAccessionProcess = () => {
  resetAccessionStore()
  showAccessionModal.value = !showAccessionModal.value
}

const loadAccessionJobs = async (qParams) => {
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

    await getAccessionJobList(filterParams)
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
  if (accessionTableRef.value) {
    accessionTableRef.value.resetTablePagination()
  }
  loadAccessionJobs()
}

// Clear all column filters
const clearColumnFilters = () => {
  columnFilters.value = {
    workflow_id: null,
    trayed: null,
    status: [
      'Created',
      'Paused',
      'Running'
    ]  // Reset to default active statuses
  }
  applyColumnFilters()
}
const loadAccessionJob = async (workflowId) => {
  try {
    appIsLoadingData.value = true
    await getAccessionJob(workflowId)

    router.push({
      name: 'accession',
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
const submitAccessionJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      size_class_id: accessionJob.trayed ? undefined : accessionJob.value.size_class,
      media_type_id: accessionJob.value.media_type,
      owner_id: accessionJob.value.owner,
      status: 'Running',
      trayed: accessionJob.value.trayed,
      created_by_id: userData.value.user_id
    }

    await postAccessionJob(payload)

    router.push({
      name: 'accession',
      params: {
        jobId: accessionJob.value.workflow_id
      }
    })

    Notify.create({
      type: 'positive',
      message: 'An Accession Job has successfully been created.'
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
  } finally {
    appActionIsLoadingData.value = false
    accessionJobModal.value.hideModal()
  }
}

</script>
<style lang="scss" scoped>
</style>
