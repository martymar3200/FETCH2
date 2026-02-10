<template>
  <div class="refile-job-details">
    <!-- Header using shared component -->
    <JobPageHeader
      title="Refile Job"
      :job-id="refileJob.id"
      :status="refileJob.status"
      :status-color="getStatusColor(refileJob.status)"
      :subtitle="headerSubtitle"
      :menu-options="headerMenuOptions"
    >
      <template #actions>
        <div
          v-if="editJobInfo || editItems"
          class="row q-gutter-x-sm"
        >
          <q-btn
            no-caps
            unelevated
            :color="editJobInfo ? 'accent' : 'negative'"
            :label="editJobInfo ? 'Save Edits' : 'Revert Items to Queue'"
            :disable="editItems && !selectedItems.length"
            class="btn-modern"
            :loading="appActionIsLoadingData"
            @click="editJobInfo ? updateRefileJob() : revertItemsToQueue()"
          />
          <q-btn
            no-caps
            unelevated
            outline
            color="accent"
            label="Cancel"
            class="btn-modern-outline"
            @click="cancelRefileJobEdits"
          />
        </div>
        <q-btn
          v-else-if="refileJob.status === 'Created' || refileJob.status === 'Assigned'"
          no-caps
          unelevated
          color="accent"
          label="Start Refile Job"
          class="btn-modern"
          :disabled="appIsOffline || appPendingSync || !checkUserPermission('can_execute_and_complete_refile_job')"
          :loading="appActionIsLoadingData"
          @click="executeRefileJob"
        />
      </template>
    </JobPageHeader>

    <!-- Quick Edit Card (Only when editing user) -->
    <q-card
      v-if="editJobInfo"
      flat
      bordered
      class="details-card q-mb-lg"
    >
      <q-card-section class="q-pa-md">
        <div class="row q-col-gutter-md items-center">
          <div class="col-12 col-sm-6">
            <div class="detail-item">
              <label class="form-group-label">Assigned User</label>
              <SelectInput
                v-model="refileJob.assigned_user_id"
                :options="users"
                option-type="users"
                option-value="id"
                option-label="name"
                class="q-mt-xs"
              />
            </div>
          </div>
        </div>
      </q-card-section>
    </q-card>

    <!-- Essential Table Section -->
    <q-card
      flat
      bordered
      class="table-card"
    >
      <q-card-section class="q-pa-none">
        <EssentialTable
          ref="refileItemsTableComponent"
          :table-columns="itemTableColumns"
          :table-visible-columns="itemTableVisibleColumns"
          :filter-options="itemTableFilters"
          :table-data="refileJobItems"
          :row-key="row => renderItemBarcodeDisplay(row)"
          :enable-table-reorder="false"
          :hide-table-rearrange="true"
          :enable-selection="editItems"
          :heading-row-class="'q-mb-md q-px-md q-pt-md'"
          :heading-filter-class="'q-ml-auto'"
          @selected-table-row="loadRefileItem(renderItemBarcodeDisplay($event))"
          @selected-data="selectedItems = $event"
        >
          <template #heading-row>
            <div class="col">
              <h2 class="text-h6 text-bold q-ma-none">
                Items in Job
              </h2>
              <div
                v-if="editItems"
                class="text-caption text-negative"
              >
                Select items to revert to the refile queue
              </div>
            </div>
            <div class="col-auto flex items-center">
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
              <q-btn-toggle
                v-model="statusFilter"
                no-caps
                rounded
                unelevated
                toggle-color="accent"
                color="white"
                text-color="grey-7"
                class="toggle-modern-rounded"
                :options="[
                  { label: 'All', value: 'all' },
                  { label: 'Pending', value: 'Out' },
                  { label: 'Refiled', value: 'In' }
                ]"
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
                <!-- Item Location filter -->
                <q-input
                  v-if="col.name === 'item_location'"
                  v-model="columnFilters.item_location"
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

                <!-- Tray Barcode filter -->
                <q-input
                  v-else-if="col.name === 'tray_barcode'"
                  v-model="columnFilters.tray_barcode"
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

                <!-- Barcode filter -->
                <q-input
                  v-else-if="col.name === 'barcode'"
                  v-model="columnFilters.barcode"
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

                <!-- Owner filter -->
                <q-input
                  v-else-if="col.name === 'owner'"
                  v-model="columnFilters.owner"
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

                <!-- Size Class filter -->
                <q-input
                  v-else-if="col.name === 'size_class'"
                  v-model="columnFilters.size_class"
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
              </q-th>
            </q-tr>
          </template>

          <template #table-td="{ colName, props: cellProps, value }">
            <span v-if="colName == 'actions'">
              <q-btn
                v-if="cellProps.row.status === 'Out' && refileJob.status !== 'Paused' && refileJob.status !== 'Completed' && checkUserPermission('can_edit_refile_job')"
                flat
                round
                dense
                size="sm"
                icon="undo"
                color="negative"
                @click="removeRefileItems([cellProps.row.barcode.value])"
              >
                <q-tooltip>Revert Item to Queue</q-tooltip>
              </q-btn>
            </span>
            <span v-else-if="colName == 'scanned_for_refile'">
              <q-chip
                v-if="value === 'In'"
                color="positive"
                text-color="white"
                icon="check_circle"
                label="Refiled"
                dense
              />
              <q-chip
                v-else
                color="grey-4"
                text-color="grey-9"
                label="Pending"
                dense
              />
            </span>
          </template>
        </EssentialTable>
      </q-card-section>
    </q-card>
  </div>

  <!-- confirmation modal -->
  <PopupModal
    v-if="showConfirmationModal"
    :title="showConfirmationModal == 'CompleteJob' ? 'Confirm' : 'Delete'"
    :text="showConfirmationModal == 'CompleteJob' ? 'Are you sure you want to complete the job?' : 'Are you sure you want to delete the job?'"
    :show-actions="false"
    @reset="showConfirmationModal = null"
    aria-label="confirmationModal"
  >
    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <q-btn
          v-if="showConfirmationModal == 'CompleteJob'"
          no-caps
          unelevated
          color="accent"
          label="Complete"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="completeRefileJob(); hideModal();"
        />
        <q-btn
          v-else
          no-caps
          unelevated
          color="negative"
          label="Delete Job"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="cancelRefileJob(); hideModal();"
        />
        <q-space class="q-mx-xs" />
        <q-btn
          outline
          no-caps
          label="Cancel"
          class="text-body1 full-width"
          @click="hideModal"
        />
      </q-card-section>
    </template>
  </PopupModal>

  <!-- refile item detail modal -->
  <RefileItemDetailModal
    v-if="showRefileItemDetailModal"
    @hide="showRefileItemDetailModal = false"
  />
  <RefileBatchSheet
    ref="batchSheetComponent"
    :refile-job-details="refileJob"
  />

  <!-- audit trail modal -->
  <AuditTrail
    v-if="showAuditTrailModal"
    ref="historyModal"
    @reset="showAuditTrailModal = null"
    :job-type="showAuditTrailModal"
    :job-id="refileJob.id"
  />
</template>

<script setup>
import { onBeforeMount, onMounted, ref, computed, inject, toRaw, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { useUserStore } from '@/stores/user-store'
import { useRefileStore } from '@/stores/refile-store'
import { storeToRefs } from 'pinia'
import { Notify } from 'quasar'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import { useIndexDbHandler } from '@/composables/useIndexDbHandler.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import JobPageHeader from '@/components/Job/JobPageHeader.vue'
import EssentialTable from '@/components/EssentialTable.vue'
import SelectInput from '@/components/SelectInput.vue'
import PopupModal from '@/components/PopupModal.vue'
import RefileItemDetailModal from '@/components/Refile/RefileItemDetailModal.vue'
import AuditTrail from '@/components/AuditTrail.vue'
import RefileBatchSheet from '@/components/Refile/RefileBatchSheet.vue'

const router = useRouter()

// Composables
const { currentScreenSize } = useCurrentScreenSize()
const { compiledBarCode } = useBarcodeScanHandler()
const {
  addDataToIndexDb,
  getDataInIndexDb,
  deleteDataInIndexDb
} = useIndexDbHandler()
const { checkUserPermission } = usePermissionHandler()

// Store Data
const {
  appIsLoadingData,
  appActionIsLoadingData,
  appPendingSync,
  appIsOffline
} = storeToRefs(useGlobalStore())
const { userData } = storeToRefs(useUserStore())
const { users } = storeToRefs(useOptionStore())
const {
  patchRefileJob,
  deleteRefileJob,
  deleteRefileJobItems,
  getRefileJobItem
} = useRefileStore()
const {
  refileJob,
  originalRefileJob,
  refileItem
} = storeToRefs(useRefileStore())

// Local Data
const refileItemsTableComponent = ref(null)
const batchSheetComponent = ref(null)
const editJobInfo = ref(false)
const editItems = ref(false)
const selectedItems = ref([])
const itemTableVisibleColumns = ref([
  'item_location',
  'tray_barcode',
  'barcode',
  'owner',
  'size_class',
  'scanned_for_refile',
  'actions'
])
const itemTableColumns = ref([
  {
    name: 'item_location',
    field: row => row.tray ? getItemLocation(row.tray) : getItemLocation(row),
    label: 'Item Location',
    align: 'left',
    sortable: true
  },
  {
    name: 'tray_barcode',
    field: row => renderItemBarcodeDisplay(row.tray),
    label: 'Tray Barcode',
    align: 'left',
    sortable: true
  },
  {
    name: 'barcode',
    field: row => renderItemBarcodeDisplay(row),
    label: 'Barcode',
    align: 'left',
    sortable: true
  },
  {
    name: 'owner',
    field: row => row.owner?.name,
    label: 'Owner',
    align: 'left',
    sortable: true
  },
  {
    name: 'size_class',
    field: row => row.size_class?.name,
    label: 'Size Class',
    align: 'left',
    sortable: true
  },
  {
    name: 'scanned_for_refile',
    field: 'status',
    label: '',
    align: 'center',
    sortable: false,
    required: true,
    headerStyle: 'max-width: 200px'
  },
  {
    name: 'actions',
    field: 'actions',
    label: '',
    align: 'center',
    sortable: false,
    required: true
  }
])
const totalItemsCount = computed(() => refileJob.value.refile_job_items ? refileJob.value.refile_job_items.length : 0)
const refiledItemsCount = computed(() => refileJob.value.refile_job_items ? refileJob.value.refile_job_items.filter(i => i.status === 'In').length : 0)

const headerSubtitle = computed(() => {
  let building = '-'
  if (refileJob.value.refile_job_items?.[0]) {
    const firstItem = refileJob.value.refile_job_items[0]
    const loc = firstItem.tray ? firstItem.tray.shelf_position?.location : firstItem.shelf_position?.location
    if (loc) {
      building = loc.split('-')[0]
    }
  }
  const items = `${refiledItemsCount.value} of ${totalItemsCount.value} items`
  const user = refileJob.value.assigned_user?.name || 'Unassigned'
  return `${building} • ${items} • ${user}`
})

const headerMenuOptions = computed(() => [
  {
    label: 'Assign User',
    hidden: !checkUserPermission('can_assign_jobs'),
    disabled: appIsOffline.value || editJobInfo.value || editItems.value || refileJob.value.status == 'Paused' || refileJob.value.status == 'Completed',
    action: () => {
      editJobInfo.value = true
    }
  },
  {
    label: 'Edit Items',
    disabled: appIsOffline.value || editJobInfo.value || editItems.value || refileJob.value.status == 'Paused' || refileJob.value.status == 'Completed',
    action: () => {
      editItems.value = true
    }
  },
  {
    label: 'Delete Job',
    hidden: !checkUserPermission('can_delete_refile_job'),
    color: 'negative',
    disabled: appIsOffline.value || editJobInfo.value || editItems.value || refileJob.value.status == 'Completed' || (refileJob.value.refile_job_items && refileJob.value.refile_job_items.some(itm => itm.status == 'In')),
    action: () => {
      showConfirmationModal.value = 'DeleteJob'
    }
  },
  {
    label: 'Print Job',
    action: () => batchSheetComponent.value.printBatchReport()
  },
  {
    label: 'View History',
    action: () => {
      showAuditTrailModal.value = 'refile_jobs'
    }
  }
])

const getStatusColor = (status) => {
  switch (status) {
    case 'Created': return 'blue'
    case 'Running': return 'accent'
    case 'Paused': return 'orange'
    case 'Completed': return 'positive'
    default: return 'grey'
  }
}

// Logic - injections need to be before computeds that use them

const currentIsoDate = inject('current-iso-date')
const getItemLocation = inject('get-item-location')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')

const statusFilter = ref('all')
const showFilterRow = ref(false)
const columnFilters = ref({
  item_location: '',
  tray_barcode: '',
  barcode: '',
  owner: '',
  size_class: ''
})

const clearColumnFilters = () => {
  columnFilters.value = {
    item_location: '',
    tray_barcode: '',
    barcode: '',
    owner: '',
    size_class: ''
  }
}

const applyColumnFilters = () => {
  // Filtering is handled reactively in the computed property
}

const refileJobItems = computed(() => {
  if (!refileJob.value.refile_job_items) {
    return []
  }
  let items = refileJob.value.refile_job_items

  // Apply status filter
  if (editItems.value) {
    items = items.filter(item => item.status == 'Out')
  } else if (statusFilter.value !== 'all') {
    items = items.filter(item => item.status === statusFilter.value)
  }

  // Apply column filters
  if (columnFilters.value.item_location) {
    const search = columnFilters.value.item_location.toLowerCase()
    items = items.filter(item => {
      const loc = item.tray ? getItemLocation(item.tray) : getItemLocation(item)
      return loc && loc.toLowerCase().includes(search)
    })
  }
  if (columnFilters.value.tray_barcode) {
    const search = columnFilters.value.tray_barcode.toLowerCase()
    items = items.filter(item => {
      const trayBc = renderItemBarcodeDisplay(item.tray)
      return trayBc && trayBc.toLowerCase().includes(search)
    })
  }
  if (columnFilters.value.barcode) {
    const search = columnFilters.value.barcode.toLowerCase()
    items = items.filter(item => {
      const bc = renderItemBarcodeDisplay(item)
      return bc && bc.toLowerCase().includes(search)
    })
  }
  if (columnFilters.value.owner) {
    const search = columnFilters.value.owner.toLowerCase()
    items = items.filter(item => {
      return item.owner?.name && item.owner.name.toLowerCase().includes(search)
    })
  }
  if (columnFilters.value.size_class) {
    const search = columnFilters.value.size_class.toLowerCase()
    items = items.filter(item => {
      return item.size_class?.name && item.size_class.name.toLowerCase().includes(search)
    })
  }

  return items
})
const itemTableFilters = []
const showConfirmationModal = ref(null)
const showRefileItemDetailModal = ref(false)
const historyModal = ref(null)
const showAuditTrailModal = ref(false)

onBeforeMount(() => {
  if (currentScreenSize.value == 'xs') {
    itemTableVisibleColumns.value = [
      'item_location',
      'tray_barcode',
      'barcode',
      'scanned_for_refile',
      'actions'
    ]
  }
})

onMounted(async () => {
  // when user is online and loads a job we store the current refile job data and original in indexdb for reference offline
  if (!appIsOffline.value) {
    addDataToIndexDb('refileStore', 'refileJob', JSON.parse(JSON.stringify(refileJob.value)))
    addDataToIndexDb('refileStore', 'originalRefileJob', JSON.parse(JSON.stringify(originalRefileJob.value)))
  } else {
    // get saved refile job data if were offline and page was reloaded/refreshed
    const res = await getDataInIndexDb('refileStore')
    refileJob.value = res.data.refileJob
    originalRefileJob.value = res.data.originalRefileJob
  }
})

watch(compiledBarCode, (barcode) => {
  if (barcode !== '' && refileJob.value.status == 'Running' && !refileItem.value.id) {
    // only allow scans if the job is in a running state and there is no active refileItem set in state
    triggerItemScan(barcode)
  }
})
const triggerItemScan = (barcode_value) => {
  // check if the scanned barcode is in the item data and that the barcode hasnt been refiled already
  if (!refileJob.value.refile_job_items?.some(itm => itm.barcode.value == barcode_value)) {
    Notify.create({
      type: 'negative',
      message: 'The scanned item does not exist in this refile job. Please try again.',
      timeout: 2000
    })
    return
  } else if (refileJob.value.refile_job_items?.some(itm => itm.barcode.value == barcode_value && itm.status !== 'Out')) {
    Notify.create({
      type: 'negative',
      message: 'The scanned item has already been marked as refiled.',
      timeout: 2000
    })
    return
  } else {
    // load the scanned request item by id of the scanned item barcode
    loadRefileItem(barcode_value)
  }
}


const cancelRefileJobEdits = () => {
  // Reset the refile job
  refileJob.value = { ...toRaw(originalRefileJob.value) }
  editJobInfo.value = false
  // Reset the items
  editItems.value = false
  refileItemsTableComponent.value.clearSelectedData()
}
const executeRefileJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: refileJob.value.id,
      status: 'Running',
      assigned_user_id: refileJob.value.assigned_user_id ? refileJob.value.assigned_user_id : userData.value.user_id,
      run_timestamp: currentIsoDate()
    }
    await patchRefileJob(payload)

    // store the current refile job data in indexdb for reference offline whenever job is executed
    addDataToIndexDb('refileStore', 'refileJob', JSON.parse(JSON.stringify(refileJob.value)))
    addDataToIndexDb('refileStore', 'originalRefileJob', JSON.parse(JSON.stringify(originalRefileJob.value)))

    Notify.create({
      type: 'positive',
      message: 'Refile Job Successfully Started'
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to start refile job'
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}
const revertItemsToQueue = async () => {
  try {
    appActionIsLoadingData.value = true
    await removeRefileItems(selectedItems.value.map(item => item.barcode.value))
    refileItemsTableComponent.value.clearSelectedData()
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to revert items'
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}
const updateRefileJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: refileJob.value.id,
      assigned_user_id: refileJob.value.assigned_user_id,
      run_timestamp: currentIsoDate()
    }
    await patchRefileJob(payload)

    Notify.create({
      type: 'positive',
      message: 'The job has been updated.'
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to update job'
    })
  } finally {
    appActionIsLoadingData.value = false
    editJobInfo.value = false
  }
}
const cancelRefileJob = async () => {
  try {
    appIsLoadingData.value = true
    await deleteRefileJob(refileJob.value.id)

    Notify.create({
      type: 'positive',
      message: 'The Refile Job has been canceled.'
    })

    router.push({
      name: 'refile',
      params: {
        jobId: null
      }
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to cancel job'
    })
  } finally {
    appIsLoadingData.value = false
    deleteDataInIndexDb('refileStore', 'refileJob')
    deleteDataInIndexDb('refileStore', 'originalRefileJob')
  }
}
const completeRefileJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: refileJob.value.id,
      status: 'Completed',
      run_timestamp: currentIsoDate()
    }
    await patchRefileJob(payload)

    Notify.create({
      type: 'positive',
      message: 'The Refile Job has been completed.'
    })

    router.push({
      name: 'refile',
      params: {
        jobId: null
      }
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to complete job'
    })
  } finally {
    appActionIsLoadingData.value = false
    deleteDataInIndexDb('refileStore', 'refileJob')
    deleteDataInIndexDb('refileStore', 'originalRefileJob')
  }
}
const loadRefileItem = (barcode_value) => {
  // since we already have all the items data we just need to set the refileItem from the refileJob items directly
  getRefileJobItem(barcode_value)
  showRefileItemDetailModal.value = true
}
const removeRefileItems = async (barcode_values) => {
  try {
    appIsLoadingData.value = true
    const payload = {
      barcode_values
    }
    await deleteRefileJobItems(payload)

    if (appIsOffline.value) {
      // when offline we remove the refile items directly by filtering out the matching barcodes in either items or nonTrayItems
      refileJob.value.items = refileJob.value.items.filter(itm => !barcode_values.includes(itm.barcode.value))
      refileJob.value.non_tray_items = refileJob.value.non_tray_items.filter(itm => !barcode_values.includes(itm.barcode.value))
      originalRefileJob.value.items = originalRefileJob.value.items.filter(itm => !barcode_values.includes(itm.barcode.value))
      originalRefileJob.value.non_tray_items = originalRefileJob.value.non_tray_items.filter(itm => !barcode_values.includes(itm.barcode.value))
    }

    // store the current refile job data in indexdb for reference offline whenever job is executed
    addDataToIndexDb('refileStore', 'refileJob', JSON.parse(JSON.stringify(refileJob.value)))
    addDataToIndexDb('refileStore', 'originalRefileJob', JSON.parse(JSON.stringify(originalRefileJob.value)))

    const alertMessage = (editItems.value && selectedItems.value.length) > 1 ? 'items have' : 'item has'
    Notify.create({
      type: 'positive',
      message: `The ${alertMessage} been sent back to the refile queue.`
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to remove items'
    })
  } finally {
    appIsLoadingData.value = false
  }
}
</script>

<style lang="scss" scoped>
.refile-job-details {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.details-card {
  border-radius: 12px;
  background: white;
}

.detail-item {
  display: flex;
  flex-direction: column;
}

.detail-label {
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  color: #a0aec0;
  letter-spacing: 0.05em;
  margin-bottom: 4px;
}

.detail-value {
  font-size: 1.1rem;
  color: #2d3748;
  font-weight: 600;
}

.table-card {
  border-radius: 12px;
  overflow: hidden;
}

.btn-modern {
  border-radius: 8px;
  padding: 8px 24px;
  font-weight: 600;
}

.btn-modern-outline {
  border-radius: 8px;
  padding: 8px 24px;
}
</style>
