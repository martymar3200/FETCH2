<template>
  <div class="withdrawal-job-detail q-pa-md">
    <!-- Header using shared component -->
    <JobPageHeader
      title="Withdraw Job"
      :job-id="withdrawJob.id"
      :status="withdrawJob.status"
      :status-color="getStatusColor(withdrawJob.status)"
      :subtitle="headerSubtitle"
      :menu-options="headerMenuOptions"
    >
      <template #actions>
        <div
          v-if="editJob"
          class="row q-gutter-x-sm"
        >
          <q-btn
            no-caps
            unelevated
            color="accent"
            label="Save Edits"
            class="btn-modern"
            :loading="appActionIsLoadingData"
            @click="updateWithdrawJob"
          />
          <q-btn
            no-caps
            unelevated
            outline
            color="accent"
            label="Cancel"
            class="btn-modern-outline"
            @click="cancelWithdrawJobEdits"
          />
        </div>
        <div
          v-else-if="withdrawJob.status !== 'Completed'"
          class="row q-gutter-x-sm"
        >
          <q-btn
            v-if="withdrawJobItems.some(itm => itm.status !== 'Out')"
            no-caps
            unelevated
            color="accent"
            :label="withdrawJob.pick_list_id ? 'Add To Pick List Job' : 'Create Pick List Job'"
            class="btn-modern"
            :disabled="withdrawJob.pick_list_id && !withdrawJobItems.some(itm => itm.status == 'In')"
            @click="withdrawJob.pick_list_id ? addToPicklistJob() : createPicklistJob()"
          />
          <q-btn
            no-caps
            unelevated
            color="positive"
            label="Withdraw Items"
            class="btn-modern"
            :disabled="withdrawJobItems.length == 0 || withdrawJobItems.some(itm => itm.status !== 'Out')"
            :loading="appActionIsLoadingData"
            @click="showConfirmationModal = 'CompleteJob'"
          />
        </div>
      </template>
    </JobPageHeader>

    <!-- Quick Edit Card (Only when editing user) -->
    <q-card
      v-if="editJob"
      flat
      bordered
      class="details-card q-mb-lg"
    >
      <q-card-section class="q-pa-md">
        <div class="row q-col-gutter-md items-center">
          <div class="col-12 col-sm-6">
            <div class="detail-item">
              <label class="detail-label">Assigned User</label>
              <SelectInput
                v-model="withdrawJob.assigned_user_id"
                :options="users"
                option-type="users"
                option-value="id"
                option-label="name"
                class="q-mt-xs"
              />
            </div>
          </div>
          <div class="col-12 col-sm-6">
            <div class="detail-item">
              <label class="detail-label">Date Created</label>
              <div class="detail-value">
                {{ formatDateTime(withdrawJob.create_dt).date }}
              </div>
            </div>
          </div>
        </div>
      </q-card-section>
    </q-card>

    <!-- Trays Table (if any) -->
    <q-card
      v-if="withdrawJob.trays && withdrawJob.trays.length > 0"
      flat
      bordered
      class="table-card q-mb-lg"
    >
      <q-card-section class="q-pa-none">
        <EssentialTable
          :table-columns="trayTableColumns"
          :table-visible-columns="trayTableVisibleColumns"
          :table-data="withdrawJob.trays"
          :row-key="'id'"
          :enable-table-reorder="false"
          :enable-selection="false"
          :hide-table-rearrange="true"
          :heading-row-class="'q-mb-md q-px-md q-pt-md'"
        >
          <template #heading-row>
            <div class="col">
              <h2 class="text-h6 text-bold q-ma-none">
                Trays in Job
              </h2>
            </div>
          </template>

          <template #table-td="{ colName, props }">
            <span v-if="colName == 'actions'">
              <q-btn
                flat
                round
                dense
                icon="undo"
                color="grey-7"
                :disable="withdrawJob.status == 'Completed'"
                @click="handleRemoveItem(props.row)"
              >
                <q-tooltip>Remove</q-tooltip>
              </q-btn>
            </span>
          </template>
        </EssentialTable>
      </q-card-section>
    </q-card>

    <!-- Items Table -->
    <q-card
      flat
      bordered
      class="table-card"
    >
      <q-card-section class="q-pa-none">
        <EssentialTable
          :table-columns="itemTableColumns"
          :table-visible-columns="itemTableVisibleColumns"
          :table-data="filteredWithdrawJobItems"
          :row-key="'id'"
          :enable-table-reorder="false"
          :enable-selection="false"
          :hide-table-rearrange="true"
          :heading-row-class="'q-mb-md q-px-md q-pt-md'"
          :highlight-row-class="'bg-color-green-light'"
          :highlight-row-key="'status'"
          :highlight-row-value="'Withdrawn'"
        >
          <template #heading-row>
            <div class="col">
              <h2 class="text-h6 text-bold q-ma-none">
                Items in Job
              </h2>
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
              <q-input
                ref="scanInputRef"
                v-model="scanInput"
                dense
                outlined
                placeholder="Scan or enter barcode..."
                class="scan-input q-mr-sm"
                style="min-width: 200px;"
                :disable="withdrawJob.status == 'Completed'"
                @keydown.enter="handleScanInput"
              >
                <template #append>
                  <q-icon
                    name="qr_code_scanner"
                    color="grey-6"
                  />
                </template>
              </q-input>
              <q-btn
                no-caps
                unelevated
                icon="upload_file"
                color="accent"
                label="Bulk Upload"
                class="text-body1 btn-modern"
                :disabled="withdrawJob.status == 'Completed'"
                @click="showAddItemModal = 'Bulk'"
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
                <!-- Shelf Barcode filter -->
                <q-input
                  v-if="col.name === 'shelf_barcode'"
                  v-model="columnFilters.shelf_barcode"
                  dense
                  outlined
                  placeholder="Filter..."
                  class="column-filter-input"
                  @click.stop
                />
                <!-- Tray Barcode filter -->
                <q-input
                  v-else-if="col.name === 'tray_barcode'"
                  v-model="columnFilters.tray_barcode"
                  dense
                  outlined
                  placeholder="Filter..."
                  class="column-filter-input"
                  @click.stop
                />
                <!-- Barcode filter -->
                <q-input
                  v-else-if="col.name === 'barcode'"
                  v-model="columnFilters.barcode"
                  dense
                  outlined
                  placeholder="Filter..."
                  class="column-filter-input"
                  @click.stop
                />
                <!-- Owner filter -->
                <q-input
                  v-else-if="col.name === 'owner'"
                  v-model="columnFilters.owner"
                  dense
                  outlined
                  placeholder="Filter..."
                  class="column-filter-input"
                  @click.stop
                />
                <!-- Status filter -->
                <q-select
                  v-else-if="col.name === 'status'"
                  v-model="columnFilters.status"
                  :options="['Out', 'In', 'Withdrawn']"
                  dense
                  outlined
                  clearable
                  placeholder="All"
                  class="column-filter-input"
                  @click.stop
                />
              </q-th>
            </q-tr>
          </template>

          <template #table-td="{ colName, props, value }">
            <span v-if="colName == 'actions'">
              <q-btn
                flat
                round
                dense
                icon="undo"
                color="grey-7"
                :disable="props.row.status == 'Withdrawn' || withdrawJob.status == 'Completed'"
                @click="handleRemoveItem(props.row)"
              >
                <q-tooltip>Remove</q-tooltip>
              </q-btn>
            </span>
            <span
              v-else-if="colName == 'status'"
              class="text-nowrap"
              :class="value == 'Withdrawn' ? 'text-positive' : 'text-highlight-negative outline'"
            >
              {{ value == 'Withdrawn' ? 'Withdrawn' : value }}
              <q-icon
                v-if="value == 'Withdrawn'"
                name="mdi-check-circle"
                color="positive"
                size="20px"
                class="q-ml-xs"
              />
            </span>
          </template>
        </EssentialTable>
      </q-card-section>
    </q-card>

    <!-- confirmation modal -->
    <PopupModal
      v-if="showConfirmationModal"
      :title="showConfirmationModal == 'CompleteJob' ? 'Confirm' : 'Delete'"
      :text="showConfirmationModal == 'CompleteJob' ? 'Are you sure you want to withdraw these items from the system?' : 'Are you sure you want to delete the job?'"
      :show-actions="false"
      @reset="showConfirmationModal = null"
    >
      <template #footer-content="{ hideModal }">
        <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
          <q-btn
            v-if="showConfirmationModal == 'CompleteJob'"
            no-caps
            unelevated
            color="accent"
            label="Withdraw & Print"
            class="btn-modern full-width"
            :loading="appActionIsLoadingData"
            @click="completeWithdrawJob('withdrawAndPrint'); hideModal();"
          />
          <q-space class="q-mx-xs" />
          <q-btn
            v-if="showConfirmationModal == 'CompleteJob'"
            no-caps
            unelevated
            color="accent"
            label="Withdraw Items"
            class="btn-modern full-width"
            :loading="appActionIsLoadingData"
            @click="completeWithdrawJob('withdraw'); hideModal();"
          />
          <q-btn
            v-else
            no-caps
            unelevated
            color="negative"
            label="Delete Job"
            class="btn-modern full-width"
            :loading="appActionIsLoadingData"
            @click="cancelWithdrawJob(); hideModal();"
          />
          <q-space class="q-mx-xs" />
          <q-btn
            outline
            no-caps
            label="Cancel"
            class="btn-modern-outline full-width"
            @click="hideModal"
          />
        </q-card-section>
      </template>
    </PopupModal>

    <!-- add item modal -->
    <WithdrawalJobAddItemModal
      v-if="showAddItemModal"
      :entry-type="showAddItemModal"
      @hide="showAddItemModal = null"
    />

    <!-- Print detail -->
    <WithdrawalBatchSheet
      ref="batchSheetComponent"
      :withdrawal-job-details="withdrawJob"
    />

    <!-- audit trail modal -->
    <AuditTrail
      v-if="showAuditTrailModal"
      ref="historyModal"
      @reset="showAuditTrailModal = null"
      :job-type="showAuditTrailModal"
      :job-id="withdrawJob.id"
    />
  </div>
</template>

<script setup>
import { onBeforeMount, ref, computed, inject, toRaw } from 'vue'
import { useRouter } from 'vue-router'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { useUserStore } from '@/stores/user-store'
import { useWithdrawalStore } from '@/stores/withdrawal-store'

import { storeToRefs } from 'pinia'
import { Notify } from 'quasar'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import JobPageHeader from '@/components/Job/JobPageHeader.vue'
import EssentialTable from '@/components/EssentialTable.vue'
import SelectInput from '@/components/SelectInput.vue'
import PopupModal from '@/components/PopupModal.vue'
import WithdrawalJobAddItemModal from '@/components/Withdrawal/WithdrawalJobAddItemModal.vue'
import AuditTrail from '@/components/AuditTrail.vue'
import WithdrawalBatchSheet from '@/components/Withdrawal/WithdrawalBatchSheet.vue'

const router = useRouter()

// Composables
const { currentScreenSize } = useCurrentScreenSize()

// Store Data
const {
  appIsLoadingData,
  appActionIsLoadingData
} = storeToRefs(useGlobalStore())
const { userData } = storeToRefs(useUserStore())
const { users } = storeToRefs(useOptionStore())
const {
  patchWithdrawJob,
  deleteWithdrawJob,
  deleteWithdrawJobItems,
  postWithdrawJobItem
} = useWithdrawalStore()
const {
  withdrawJob,
  originalWithdrawJob,
  withdrawJobItems
} = storeToRefs(useWithdrawalStore())

// Local Data
const batchSheetComponent = ref(null)
const scanInputRef = ref(null)
const scanInput = ref('')
const editJob = ref(false)
const showFilterRow = ref(false)
const showConfirmationModal = ref(null)
const showAddItemModal = ref(null)
const showAuditTrailModal = ref(false)

// Column filters
const columnFilters = ref({
  shelf_barcode: '',
  tray_barcode: '',
  barcode: '',
  owner: '',
  status: null
})

const clearColumnFilters = () => {
  columnFilters.value = {
    shelf_barcode: '',
    tray_barcode: '',
    barcode: '',
    owner: '',
    status: null
  }
}

// Logic - injections

const currentIsoDate = inject('current-iso-date')
const formatDateTime = inject('format-date-time')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')
const renderWithdrawnTrayBarcode = inject('render-withdrawn-tray-barcode')
const renderWithdrawnShelfBarcode = inject('render-withdrawn-shelf-barcode')
const renderWithdrawnItemLocation = inject('render-withdrawn-item-location')

// Computed
const headerSubtitle = computed(() => {
  const withdrawn = withdrawJobItems.value.filter(i => i.status === 'Withdrawn').length
  const total = withdrawJobItems.value.length
  const user = withdrawJob.value.assigned_user?.name || 'Unassigned'
  return `${withdrawn} of ${total} withdrawn • ${user}`
})

const headerMenuOptions = computed(() => [
  {
    label: 'Assign User',
    icon: 'person',
    disabled: editJob.value || withdrawJob.value.status === 'Completed',
    action: () => {
      editJob.value = true
    }
  },
  {
    label: 'Print Job',
    icon: 'print',
    action: () => batchSheetComponent.value?.printBatchReport()
  },
  {
    label: 'View History',
    icon: 'history',
    action: () => {
      showAuditTrailModal.value = 'withdraw_jobs'
    }
  },
  {
    label: 'Delete Job',
    icon: 'delete',
    color: 'negative',
    disabled: editJob.value || withdrawJob.value.status === 'Completed' || withdrawJobItems.value.some(itm => itm.status === 'Withdrawn'),
    action: () => {
      showConfirmationModal.value = 'DeleteJob'
    }
  }
])

const getStatusColor = (status) => {
  switch (status) {
    case 'Completed': return 'positive'
    case 'Running': return 'info'
    case 'Paused': return 'warning'
    case 'Created': return 'grey'
    default: return 'grey'
  }
}

// Filtered items with column filters applied
const filteredWithdrawJobItems = computed(() => {
  let items = withdrawJobItems.value

  // Apply column filters
  if (columnFilters.value.shelf_barcode) {
    const filter = columnFilters.value.shelf_barcode.toLowerCase()
    items = items.filter(item => {
      const shelfBarcode = item.status === 'Withdrawn'
        ? renderWithdrawnShelfBarcode(item)
        : (item.tray ? item.tray?.shelf_position?.shelf?.barcode?.value : item.shelf_position?.shelf?.barcode?.value)
      return shelfBarcode?.toLowerCase().includes(filter)
    })
  }

  if (columnFilters.value.tray_barcode) {
    const filter = columnFilters.value.tray_barcode.toLowerCase()
    items = items.filter(item => {
      const trayBarcode = item.status === 'Withdrawn'
        ? renderWithdrawnTrayBarcode(item)
        : renderItemBarcodeDisplay(item.tray)
      return trayBarcode?.toLowerCase().includes(filter)
    })
  }

  if (columnFilters.value.barcode) {
    const filter = columnFilters.value.barcode.toLowerCase()
    items = items.filter(item => {
      const barcode = renderItemBarcodeDisplay(item)
      return barcode?.toLowerCase().includes(filter)
    })
  }

  if (columnFilters.value.owner) {
    const filter = columnFilters.value.owner.toLowerCase()
    items = items.filter(item => item.owner?.name?.toLowerCase().includes(filter))
  }

  if (columnFilters.value.status) {
    items = items.filter(item => item.status === columnFilters.value.status)
  }

  return items
})

// Table columns - actions moved to last position
const itemTableVisibleColumns = ref([
  'shelf_barcode',
  'tray_barcode',
  'barcode',
  'owner',
  'status',
  'withdrawn_location',
  'actions'
])

const itemTableColumns = ref([
  {
    name: 'shelf_barcode',
    field: row => row.status === 'Withdrawn' ? renderWithdrawnShelfBarcode(row) : (row.tray ? row.tray?.shelf_position?.shelf?.barcode?.value : row.shelf_position?.shelf?.barcode?.value),
    label: 'Shelf Barcode',
    align: 'left',
    sortable: true
  },
  {
    name: 'tray_barcode',
    field: row => row.status === 'Withdrawn' ? renderWithdrawnTrayBarcode(row) : renderItemBarcodeDisplay(row.tray),
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
    name: 'status',
    field: 'status',
    label: 'Item Status',
    align: 'left',
    sortable: false
  },
  {
    name: 'withdrawn_location',
    field: row => renderWithdrawnItemLocation(row),
    label: 'Location',
    align: 'left',
    sortable: true
  },
  {
    name: 'actions',
    field: 'actions',
    label: '',
    align: 'right',
    sortable: false
  }
])

// Tray table columns - actions moved to last position
const trayTableVisibleColumns = ref([
  'shelf_barcode',
  'tray_barcode',
  'owner',
  'actions'
])

const trayTableColumns = ref([
  {
    name: 'shelf_barcode',
    field: row => row.status === 'Withdrawn' ? renderWithdrawnShelfBarcode(row) : row.shelf_position?.shelf?.barcode?.value,
    label: 'Shelf Barcode',
    align: 'left',
    sortable: true
  },
  {
    name: 'tray_barcode',
    field: row => row.status === 'Withdrawn' ? renderWithdrawnTrayBarcode(row) : renderItemBarcodeDisplay(row),
    label: 'Tray Barcode',
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
    name: 'actions',
    field: 'actions',
    label: '',
    align: 'right',
    sortable: false
  }
])

// Lifecycle
onBeforeMount(() => {
  if (currentScreenSize.value == 'xs') {
    itemTableVisibleColumns.value = [
      'shelf_barcode',
      'tray_barcode',
      'barcode',
      'status',
      'actions'
    ]
  }
})

// Methods
const handleRemoveItem = (row) => {
  removeWithdrawItems([row.barcode.value])
}

const handleScanInput = async () => {
  if (!scanInput.value.trim()) {
    return
  }

  const barcode = scanInput.value.trim()
  scanInput.value = ''

  try {
    appIsLoadingData.value = true
    const payload = {
      barcode_value: barcode
    }
    const errors = await postWithdrawJobItem(payload)

    if (errors && errors.length > 0) {
      Notify.create({
        type: 'negative',
        message: errors.join(', ')
      })
    } else {
      Notify.create({
        type: 'positive',
        message: `Item ${barcode} added to job.`,
        timeout: 1000
      })
    }
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to add item'
    })
  } finally {
    appIsLoadingData.value = false
    // Keep focus on input for continuous scanning
    scanInputRef.value?.focus()
  }
}

const cancelWithdrawJobEdits = () => {
  withdrawJob.value = { ...toRaw(originalWithdrawJob.value) }
  editJob.value = false
}

const updateWithdrawJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: withdrawJob.value.id,
      assigned_user_id: withdrawJob.value.assigned_user_id,
      run_timestamp: currentIsoDate()
    }
    await patchWithdrawJob(payload)

    await patchWithdrawJob(payload)

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
    editJob.value = false
  }
}

const cancelWithdrawJob = async () => {
  try {
    appIsLoadingData.value = true
    await deleteWithdrawJob(withdrawJob.value.id)

    await deleteWithdrawJob(withdrawJob.value.id)

    Notify.create({
      type: 'positive',
      message: 'The Withdraw Job has been canceled.'
    })

    router.push({
      name: 'withdrawal',
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
  }
}

const completeWithdrawJob = async (withdrawType) => {
  try {
    // check if an associated picklist exists and make sure it is completed
    if (withdrawJob.value.pick_list && withdrawJob.value.pick_list.status !== 'Completed') {
      Notify.create({
        type: 'negative',
        message: `A Pick list job # <a href='/picklist/${withdrawJob.value.pick_list_id}' style='color: white; text-decoration: underline;'>${withdrawJob.value.pick_list_id}</a> was generated for withdrawal but not completed yet, please complete the picklist job inorder to complete withdrawal process.`,
        html: true,
        timeout: 0,
        actions: [
          {
            icon: 'close',
            color: 'white'
          }
        ]
      })
      return
    }

    appActionIsLoadingData.value = true
    const payload = {
      id: withdrawJob.value.id,
      status: 'Completed',
      assigned_user_id: withdrawJob.value.assigned_user_id ? withdrawJob.value.assigned_user_id : userData.value.user_id,
      run_timestamp: currentIsoDate()
    }
    await patchWithdrawJob(payload)

    await patchWithdrawJob(payload)

    Notify.create({
      type: 'positive',
      message: 'All items have been successfully withdrawn, the job has been completed.'
    })

    // If the user has selected complete and print, let's print!
    if (withdrawType && withdrawType === 'withdrawAndPrint') {
      batchSheetComponent.value.printBatchReport()
    }
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to complete job'
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}

const removeWithdrawItems = async (barcode_values) => {
  try {
    appIsLoadingData.value = true
    const payload = {
      barcode_value: barcode_values[0]
    }
    await deleteWithdrawJobItems(payload)

    await deleteWithdrawJobItems(payload)

    Notify.create({
      type: 'positive',
      message: 'The item has been removed from the job.'
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to remove item'
    })
  } finally {
    appIsLoadingData.value = false
  }
}

const createPicklistJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: withdrawJob.value.id,
      create_pick_list: true
    }
    await patchWithdrawJob(payload)

    await patchWithdrawJob(payload)

    Notify.create({
      type: 'positive',
      message: `Successfully created Pick List #: <a href='/picklist/${withdrawJob.value.pick_list_id}' style='color: white; text-decoration: underline;'>${withdrawJob.value.pick_list_id}</a>`,
      html: true,
      timeout: 0,
      actions: [
        {
          icon: 'close',
          color: 'white'
        }
      ]
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to create picklist job'
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}

const addToPicklistJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: withdrawJob.value.id,
      add_to_picklist: true
    }
    await patchWithdrawJob(payload)

    await patchWithdrawJob(payload)

    Notify.create({
      type: 'positive',
      message: `Successfully updated Pick List #: <a href='/picklist/${withdrawJob.value.pick_list_id}' style='color: white; text-decoration: underline;'>${withdrawJob.value.pick_list_id}</a>`,
      html: true,
      timeout: 0,
      actions: [
        {
          icon: 'close',
          color: 'white'
        }
      ]
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to update picklist job'
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}
</script>

<style lang="scss" scoped>
.withdrawal-job-detail {
  .details-card {
    .detail-item {
      .detail-label {
        display: block;
        font-size: 0.75rem;
        font-weight: 600;
        color: var(--q-grey-7);
        text-transform: uppercase;
        letter-spacing: 0.5px;
        margin-bottom: 4px;
      }

      .detail-value {
        font-size: 1rem;
        color: var(--q-dark);
      }
    }
  }
}
</style>
