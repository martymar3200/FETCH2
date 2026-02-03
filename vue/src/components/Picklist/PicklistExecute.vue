<template>
  <div class="picklist-execute">
    <!-- Header using shared component -->
    <JobPageHeader
      title="Pick List Job"
      :job-id="jobId"
      :status="job?.status"
      :status-color="getStatusColor(job?.status)"
      :subtitle="subtitle"
      :menu-options="menuOptions"
    >
      <template #actions>
        <JobActionButtons
          v-if="job?.status !== 'Completed'"
          :status="job?.status || 'Created'"
          :can-complete="allItemsRetrieved"
          :loading="actionLoading"
          @start="startJob"
          @pause="pauseJob"
          @resume="resumeJob"
          @complete="showCompleteDialog = true"
        />
      </template>
    </JobPageHeader>

    <!-- Progress Bar -->
    <JobProgressBar
      :completed="retrievedCount"
      :total="totalCount"
    />

    <!-- Scan Section -->
    <q-card
      v-if="job?.status === 'Running'"
      class="q-mb-lg bg-accent-1"
    >
      <q-card-section>
        <div class="row q-col-gutter-md items-end">
          <div class="col-grow">
            <label class="form-group-label">Scan Item Barcode to Retrieve</label>
            <q-input
              v-model="barcodeInput"
              outlined
              dense
              placeholder="Scan item barcode"
              @keyup.enter="handleManualScan"
              ref="scanInput"
              autofocus
            >
              <template #append>
                <q-icon name="qr_code_scanner" />
              </template>
            </q-input>
          </div>
          <div class="col-auto">
            <q-btn
              no-caps
              unelevated
              color="accent"
              label="Retrieve"
              class="btn-modern"
              :loading="scanning"
              @click="handleManualScan"
            />
          </div>
        </div>
      </q-card-section>
    </q-card>

    <!-- Item List -->
    <q-card>
      <q-card-section>
        <div class="row items-center q-mb-md">
          <div class="col">
            <div class="text-h6">
              Items to Pick
            </div>
          </div>
          <div class="col-auto">
            <q-btn-toggle
              v-model="filter"
              no-caps
              rounded
              unelevated
              toggle-color="accent"
              :options="[
                { label: 'All', value: 'all' },
                { label: 'Pending', value: 'PickList' },
                { label: 'Retrieved', value: 'Out' }
              ]"
            />
          </div>
        </div>

        <q-table
          :rows="filteredItems"
          :columns="columns"
          row-key="id"
          flat
          dense
          :pagination="{ rowsPerPage: 15 }"
          class="essential-table"
        >
          <template #body-cell-barcode="props">
            <q-td :props="props">
              <span
                class="text-weight-medium cursor-pointer text-primary"
                @click="viewItemDetails(props.row)"
              >
                {{ renderItemBarcodeDisplay(props.row.item || props.row.non_tray_item) }}
              </span>
            </q-td>
          </template>

          <template #body-cell-status="props">
            <q-td
              :props="props"
              class="text-center"
            >
              <q-badge
                :color="props.row.status === 'PickList' ? 'grey' : 'positive'"
                :label="props.row.status === 'PickList' ? 'Pending' : 'Retrieved'"
              />
              <q-icon
                v-if="props.row.status !== 'PickList'"
                name="check_circle"
                color="positive"
                size="xs"
                class="q-ml-xs"
              />
            </q-td>
          </template>

          <template #body-cell-actions="props">
            <q-td
              :props="props"
              class="text-right"
            >
              <q-btn
                v-if="props.row.status === 'PickList' && job?.status !== 'Completed'"
                flat
                round
                dense
                size="sm"
                icon="undo"
                color="negative"
                @click="revertItem(props.row)"
              >
                <q-tooltip>Revert Item to Queue</q-tooltip>
              </q-btn>
            </q-td>
          </template>
        </q-table>
      </q-card-section>
    </q-card>

    <!-- Dialogs -->
    <JobConfirmDialog
      v-model="showCompleteDialog"
      title="Complete Job?"
      message="Are you sure you want to complete this pick list job?"
      confirm-label="Complete & Print"
      confirm-color="positive"
      :loading="actionLoading"
      @confirm="completeJob(true)"
    >
      <template #extra-actions>
        <q-btn
          flat
          no-caps
          label="Complete Only"
          color="positive"
          @click="completeJob(false)"
        />
      </template>
    </JobConfirmDialog>

    <JobConfirmDialog
      v-model="showCancelDialog"
      title="Cancel Job?"
      message="Are you sure you want to cancel this pick list job?"
      warning="This will return all items to the pick list queue."
      confirm-label="Yes, Cancel"
      confirm-color="negative"
      :loading="actionLoading"
      @confirm="cancelJob"
    />

    <!-- Shared Components -->
    <AuditTrail
      v-if="showAuditTrailModal"
      ref="historyModal"
      @reset="showAuditTrailModal = false"
      job-type="pick_lists"
      :job-id="jobId"
    />

    <PicklistBatchSheet
      ref="batchSheetComponent"
      :picklist-job-details="job"
      :picklist-job-items="picklistItems"
    />

    <PicklistItemDetailModal
      v-if="showItemDetailModal"
      @hide="showItemDetailModal = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePicklistStore } from '@/stores/picklist-store'
import { useUserStore } from '@/stores/user-store'
import { storeToRefs } from 'pinia'
import { Notify } from 'quasar'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import { useIndexDbHandler } from '@/composables/useIndexDbHandler.js'

// Shared Components
import JobPageHeader from '@/components/Job/JobPageHeader.vue'
import JobProgressBar from '@/components/Job/JobProgressBar.vue'
import JobActionButtons from '@/components/Job/JobActionButtons.vue'
import JobConfirmDialog from '@/components/Job/JobConfirmDialog.vue'
import AuditTrail from '@/components/AuditTrail.vue'
import PicklistBatchSheet from '@/components/Picklist/PicklistBatchSheet.vue'
import PicklistItemDetailModal from '@/components/Picklist/PicklistItemDetailModal.vue'

const route = useRoute()
const router = useRouter()
const picklistStore = usePicklistStore()


// Composables
const { compiledBarCode } = useBarcodeScanHandler()
const { addDataToIndexDb, deleteDataInIndexDb } = useIndexDbHandler()

// Store Refs
const { picklistJob, picklistItems, allItemsRetrieved } = storeToRefs(picklistStore)
const { userData } = storeToRefs(useUserStore())

// Local State
const jobId = computed(() => route.params.id || route.params.jobId)
const job = computed(() => picklistJob.value)
const filter = ref('all')
const barcodeInput = ref('')
const actionLoading = ref(false)
const scanning = ref(false)
const showCompleteDialog = ref(false)
const showCancelDialog = ref(false)
const showAuditTrailModal = ref(false)
const showItemDetailModal = ref(false)
const batchSheetComponent = ref(null)
const scanInput = ref(null)

// Injected helpers
const currentIsoDate = inject('current-iso-date')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')
const getItemLocation = inject('get-item-location')

// Computed
const totalCount = computed(() => picklistItems.value.length)
const retrievedCount = computed(() => picklistItems.value.filter(i => i.status !== 'PickList').length)
const subtitle = computed(() => {
  const parts = []
  if (job.value?.building?.name) {
    parts.push(job.value.building.name)
  }
  parts.push(`${retrievedCount.value}/${totalCount.value} retrieved`)
  return parts.join(' • ')
})

const filteredItems = computed(() => {
  if (filter.value === 'all') {
    return picklistItems.value
  }
  return picklistItems.value.filter(i => i.status === filter.value)
})

const menuOptions = computed(() => [
  {
    label: 'View History',
    icon: 'history',
    color: 'grey',
    action: () => {
      showAuditTrailModal.value = true
    }
  },
  {
    label: 'Print Job',
    icon: 'print',
    color: 'grey',
    action: () => {
      batchSheetComponent.value?.printBatchReport()
    }
  },
  {
    label: 'Cancel Job',
    icon: 'delete',
    color: 'negative',
    action: () => {
      showCancelDialog.value = true
    },
    hidden: retrievedCount.value > 0 // Only allow cancel if no items picked
  }
])

const columns = [
  {
    name: 'barcode',
    label: 'Barcode',
    align: 'left',
    field: 'barcode',
    sortable: true
  },
  {
    name: 'tray_barcode',
    label: 'Tray Barcode',
    align: 'left',
    field: row => row.item?.tray?.barcode?.value || '-'
  },
  {
    name: 'location',
    label: 'Location',
    align: 'left',
    field: row => row.item ? getItemLocation(row.item.tray) : getItemLocation(row.non_tray_item)
  },
  {
    name: 'owner',
    label: 'Owner',
    align: 'left',
    field: row => row.item?.owner?.name || row.non_tray_item?.owner?.name
  },
  {
    name: 'status',
    label: 'Status',
    align: 'center',
    field: 'status'
  },
  {
    name: 'actions',
    label: '',
    align: 'right',
    field: 'actions'
  }
]

// Methods
const getStatusColor = (status) => {
  const colors = {
    Created: 'grey',
    Running: 'info',
    Paused: 'warning',
    Completed: 'positive'
  }
  return colors[status] || 'grey'
}

const startJob = async () => {
  actionLoading.value = true
  try {
    const payload = {
      id: jobId.value,
      status: 'Running',
      user_id: userData.value.user_id,
      run_timestamp: currentIsoDate()
    }
    await picklistStore.patchPicklistJob(payload)
    saveState()
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: 'Failed to start job'
    })
  } finally {
    actionLoading.value = false
  }
}

const pauseJob = async () => {
  actionLoading.value = true
  try {
    const payload = {
      id: jobId.value,
      status: 'Paused',
      run_timestamp: currentIsoDate()
    }
    await picklistStore.patchPicklistJob(payload)
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: 'Failed to pause job'
    })
  } finally {
    actionLoading.value = false
  }
}

const resumeJob = async () => {
  actionLoading.value = true
  try {
    const payload = {
      id: jobId.value,
      status: 'Running',
      run_timestamp: currentIsoDate()
    }
    await picklistStore.patchPicklistJob(payload)
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: 'Failed to resume job'
    })
  } finally {
    actionLoading.value = false
  }
}

const handleManualScan = () => {
  if (!barcodeInput.value) {
    return
  }
  processScan(barcodeInput.value)
}

const processScan = async (barcode) => {
  if (!barcode || job.value?.status !== 'Running' || scanning.value) {
    return
  }

  scanning.value = true
  const item = picklistItems.value.find(itm =>
    (itm.item?.barcode?.value === barcode) || (itm.non_tray_item?.barcode?.value === barcode)
  )

  if (!item) {
    Notify.create({
      type: 'negative',
      message: 'Item not in this pick list'
    })
    barcodeInput.value = ''
    scanning.value = false
    return
  }

  if (item.status !== 'PickList') {
    Notify.create({
      type: 'warning',
      message: 'Item already retrieved'
    })
    barcodeInput.value = ''
    scanning.value = false
    return
  }

  try {
    await picklistStore.patchPicklistJobItemScanned({
      id: jobId.value,
      request_id: item.id,
      run_timestamp: currentIsoDate(),
      status: 'Out'
    })

    // Update local state for immediate feedback
    item.status = 'Out'
    barcodeInput.value = ''
    saveState()

    Notify.create({
      type: 'positive',
      message: 'Item retrieved',
      position: 'top',
      timeout: 1000
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: 'Failed to update item'
    })
  } finally {
    scanning.value = false
    nextTick(() => scanInput.value?.focus())
  }
}

const revertItem = async (row) => {
  try {
    await picklistStore.deletePicklistJobItem(row.id)
    Notify.create({
      type: 'info',
      message: 'Item reverted to queue'
    })
    saveState()
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: 'Failed to revert item'
    })
  }
}

const completeJob = async (print) => {
  actionLoading.value = true
  try {
    await picklistStore.patchPicklistJob({
      id: jobId.value,
      status: 'Completed',
      run_timestamp: currentIsoDate()
    })
    if (print) {
      batchSheetComponent.value?.printBatchReport()
    }
    deleteDataInIndexDb('picklistStore', 'activeJob')
    router.push({ name: 'picklist' })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: 'Failed to complete job'
    })
  } finally {
    actionLoading.value = false
    showCompleteDialog.value = false
  }
}

const cancelJob = async () => {
  actionLoading.value = true
  try {
    await picklistStore.deletePicklistJob(jobId.value)
    deleteDataInIndexDb('picklistStore', 'activeJob')
    router.push({ name: 'picklist' })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: 'Failed to cancel job'
    })
  } finally {
    actionLoading.value = false
    showCancelDialog.value = false
  }
}

const viewItemDetails = async (row) => {
  const barcode = renderItemBarcodeDisplay(row.item || row.non_tray_item)
  await picklistStore.getPicklistJobItem(barcode)
  showItemDetailModal.value = true
}

const saveState = () => {
  addDataToIndexDb('picklistStore', 'activeJob', {
    jobId: jobId.value,
    timestamp: Date.now()
  })
}

// Listen for hardware scans
watch(compiledBarCode, (barcode) => {
  if (barcode) {
    processScan(barcode)
  }
})

onMounted(async () => {
  if (!job.value || job.value.id != jobId.value) {
    await picklistStore.getPicklistJob(jobId.value)
  }

  nextTick(() => {
    if (job.value?.status === 'Running') {
      scanInput.value?.focus()
    }
  })
})
</script>

<style scoped lang="scss">
.picklist-execute {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.bg-accent-1 {
  background: linear-gradient(135deg, rgba(var(--q-accent), 0.1) 0%, rgba(var(--q-accent), 0.05) 100%);
}

.form-group-label {
  display: block;
  font-weight: 500;
  margin-bottom: 4px;
  color: #555;
}

.essential-table {
  :deep(.q-table__container) {
    border-radius: 8px;
  }
}
</style>
