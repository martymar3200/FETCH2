<template>
  <div class="refile-execute">
    <!-- Header using shared component -->
    <JobPageHeader
      title="Refile Job"
      :job-id="jobId"
      :status="job?.status"
      :status-color="getStatusColor(job?.status)"
      :subtitle="subtitle"
      :menu-options="menuOptions"
    >
      <template #actions>
        <div
          v-if="editJobInfo || editItems"
          class="row q-gutter-x-sm"
        >
          <BaseButton
            no-caps
            unelevated
            :color="editJobInfo ? 'accent' : 'negative'"
            :label="editJobInfo ? 'Save Edits' : 'Revert Items to Queue'"
            :disable="editItems && !selectedItems.length"

            :loading="actionLoading"
            @click="editJobInfo ? updateRefileJob() : revertSelectedItemsToQueue()"
          />
          <BaseButton
            no-caps
            unelevated
            outline
            color="accent"
            label="Cancel"
            class="btn-modern-outline"
            @click="cancelRefileJobEdits"
          />
        </div>
        <JobActionButtons
          v-else-if="job?.status !== 'Completed'"
          :status="job?.status || 'Created'"
          :can-complete="allItemsRefiled"
          :loading="actionLoading"
          @start="startJob"
          @pause="pauseJob"
          @resume="resumeJob"
          @complete="showCompleteDialog = true"
        />
      </template>
    </JobPageHeader>

    <!-- Quick Edit Card (Only when editing user) -->
    <q-card
      v-if="editJobInfo"
      flat
      bordered
      class="details-card q-mb-lg q-mt-md"
    >
      <q-card-section class="q-pa-md">
        <div class="row q-col-gutter-md items-center">
          <div class="col-12 col-sm-6">
            <div class="detail-item">
              <label class="form-group-label">Assigned User</label>
              <SelectInput
                v-model="job.assigned_user_id"
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

    <!-- Progress Bar -->
    <JobProgressBar
      :completed="refiledCount"
      :total="totalCount"
    />

    <!-- Not Started Message -->
    <q-card
      v-if="job?.status === 'Created' || job?.status === 'Assigned'"
      class="q-mt-md bg-accent-1 text-center q-pa-xl"
    >
      <q-card-section>
        <q-icon
          name="play_circle_outline"
          size="4rem"
          color="accent"
        />
        <p class="text-h6 q-mt-md">
          Job Ready to Start
        </p>
        <p class="text-grey-7">
          Click "Start Job" to begin scanning items for refile.
        </p>
      </q-card-section>
    </q-card>

    <template v-else>
      <!-- Scanning Card (Top for ergonomics) -->
      <q-card
        v-if="job?.status === 'Running'"
        class="q-mt-lg q-mb-lg bg-accent-1"
      >
        <q-card-section>
          <div class="row q-col-gutter-md items-end">
            <div class="col-12 col-md-8">
              <label class="form-group-label">Scan Item Barcode</label>
              <q-input
                v-model="barcodeInput"
                outlined
                dense
                placeholder="Scan item barcode to refile"
                @keyup.enter="handleManualScan"
                ref="barcodeInputRef"
                autofocus
              >
                <template #append>
                  <q-icon name="qr_code_scanner" />
                </template>
              </q-input>
            </div>
          </div>
        </q-card-section>
      </q-card>

      <!-- Item Table -->
      <q-card class="q-mt-md">
        <q-card-section>
          <div class="row items-center q-mb-md">
            <div class="col">
              <div class="text-h6">
                Items to Refile
              </div>
              <div
                v-if="editItems"
                class="text-caption text-negative"
              >
                Select items to revert to the refile queue
              </div>
            </div>
            <div class="col-auto flex q-gutter-x-sm">
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
              <q-input
                v-model="tableFilter"
                dense
                outlined
                placeholder="Filter table..."
              >
                <template #append>
                  <q-icon name="search" />
                </template>
              </q-input>
            </div>
          </div>

          <q-table
            :rows="jobItems"
            :columns="columns"
            row-key="id"
            flat
            bordered
            :filter="tableFilter"
            :pagination="{ rowsPerPage: 0 }"
            hide-pagination
            :selection="editItems ? 'multiple' : 'none'"
            v-model:selected="selectedItems"
          >
            <!-- Custom Cell for Status -->
            <template #body-cell-status="cellProps">
              <q-td :props="cellProps">
                <q-chip
                  v-if="cellProps.row.status === 'In'"
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
              </q-td>
            </template>

            <!-- Custom Cell for Actions -->
            <template #body-cell-actions="cellProps">
              <q-td
                :props="cellProps"
                class="text-right"
              >
                <BaseButton
                  v-if="cellProps.row.status === 'Out'"
                  flat
                  round
                  dense
                  color="negative"
                  icon="undo"
                  @click="confirmRevert(cellProps.row)"
                >
                  <q-tooltip>Revert to Queue</q-tooltip>
                </BaseButton>
              </q-td>
            </template>
          </q-table>
        </q-card-section>
      </q-card>
    </template>

    <!-- Complete Dialog -->
    <JobConfirmDialog
      v-model="showCompleteDialog"
      title="Complete Refile Job?"
      message="This will mark all scanned items as refiled and complete the job. Proceed?"
      confirm-label="Complete"
      confirm-color="positive"
      :loading="actionLoading"
      @confirm="completeJob"
    />

    <!-- Revert Confirmation Dialog -->
    <q-dialog v-model="showRevertDialog">
      <q-card style="min-width: 350px">
        <q-card-section>
          <div class="text-h6">
            Revert Item?
          </div>
          <div class="text-body2 q-mt-sm">
            Are you sure you want to remove item <strong>{{ pendingRevertItem?.barcode?.value }}</strong> from this job and send it back to the refile queue?
          </div>
        </q-card-section>
        <q-card-actions align="right">
          <BaseButton
            flat
            label="Cancel"
            color="primary"
            v-close-popup
          />
          <BaseButton
            unelevated
            label="Revert"
            color="negative"
            @click="revertItem"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <!-- Delete Confirmation Modal -->
    <PopupModal
      v-if="showConfirmationModal === 'DeleteJob'"
      title="Delete"
      text="Are you sure you want to delete the job?"
      :show-actions="false"
      @reset="showConfirmationModal = null"
      aria-label="deleteConfirmationModal"
    >
      <template #footer-content="{ hideModal }">
        <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
          <BaseButton
            no-caps
            unelevated
            color="negative"
            label="Delete Job"
            class="text-body1 full-width"
            :loading="actionLoading"
            @click="cancelRefileJob(); hideModal();"
          />
          <q-space class="q-mx-xs" />
          <BaseButton
            outline
            no-caps
            label="Cancel"
            class="text-body1 full-width"
            @click="hideModal"
          />
        </q-card-section>
      </template>
    </PopupModal>

    <!-- Batch Sheet for Printing -->
    <RefileBatchSheet
      ref="batchSheetRef"
      :refile-job-details="job"
    />

    <!-- Verification Modal -->
    <RefileVerifyModal
      v-if="showVerifyModal"
      :item="pendingVerifyItem"
      :loading="actionLoading"
      @hide="closeVerifyModal"
      @verify="completeItemRefile"
    />

    <!-- audit trail modal -->
    <AuditTrail
      v-if="showAuditTrailModal"
      @reset="showAuditTrailModal = false"
      job-type="refile_jobs"
      :job-id="job?.id"
    />
  </div>
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
import { ref, computed, onMounted, watch, nextTick, inject, toRaw } from 'vue'
import { useRouter } from 'vue-router'
import { useRefileStore } from '@/stores/refile-store'
import { useGlobalStore } from '@/stores/global-store'
import { useUserStore } from '@/stores/user-store'
import { useOptionStore } from '@/stores/option-store'
import { storeToRefs } from 'pinia'
import { Notify } from 'quasar'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler'
import { useIndexDbHandler } from '@/composables/useIndexDbHandler'
import { usePermissionHandler } from '@/composables/usePermissionHandler'

// Components
import JobPageHeader from '@/components/Job/JobPageHeader.vue'
import JobProgressBar from '@/components/Job/JobProgressBar.vue'
import JobActionButtons from '@/components/Job/JobActionButtons.vue'
import JobConfirmDialog from '@/components/Job/JobConfirmDialog.vue'
import RefileBatchSheet from '@/components/Refile/RefileBatchSheet.vue'
import RefileVerifyModal from '@/components/Refile/RefileVerifyModal.vue'
import AuditTrail from '@/components/AuditTrail.vue'
import PopupModal from '@/components/PopupModal.vue'
import SelectInput from '@/components/SelectInput.vue'

const props = defineProps({
  jobId: {
    type: [
      Number,
      String
    ],
    required: true
  }
})


const router = useRouter()
const refileStore = useRefileStore()
const globalStore = useGlobalStore()
const userStore = useUserStore()
const optionStore = useOptionStore()

const { checkUserPermission } = usePermissionHandler()
const { users } = storeToRefs(optionStore)
const { refileJob: job, originalRefileJob, allItemsRefiled } = storeToRefs(refileStore)
const { userData } = storeToRefs(userStore)
const { addDataToIndexDb, getDataInIndexDb, deleteDataInIndexDb } = useIndexDbHandler()

// Editor State
const editJobInfo = ref(false)
const editItems = ref(false)
const selectedItems = ref([])
const showConfirmationModal = ref(null)

// Scanning logic
const barcodeInput = ref('')
const barcodeInputRef = ref(null)
const scanLock = ref(false)
const { compiledBarCode } = useBarcodeScanHandler()

// Table logic
const tableFilter = ref('')
const statusFilter = ref('all')

// Dialogs & Loading
const actionLoading = ref(false)
const showCompleteDialog = ref(false)
const showRevertDialog = ref(false)
const pendingRevertItem = ref(null)
const batchSheetRef = ref(null)

// Verification State
const showVerifyModal = ref(false)
const pendingVerifyItem = ref(null)

// History Validation
const showAuditTrailModal = ref(false)

// Injected helpers
const currentIsoDate = inject('current-iso-date')

// Computed
const subtitle = computed(() => {
  if (!job.value) {
    return ''
  }
  let building = '-'
  if (job.value.refile_job_items?.[0]) {
    const firstItem = job.value.refile_job_items[0]
    const loc = firstItem.tray ? firstItem.tray.shelf_position?.location : firstItem.shelf_position?.location
    if (loc) {
      building = loc.split('-')[0]
    }
  }
  const items = `${refiledCount.value} of ${totalCount.value} items`
  const user = job.value.assigned_user?.name || '-'
  return `${building} • ${items} • Assigned to: ${user}`
})

const jobItems = computed(() => {
  const items = job.value?.refile_job_items || []
  if (statusFilter.value === 'all') {
    return items
  }
  return items.filter(i => i.status === statusFilter.value)
})

const totalCount = computed(() => jobItems.value.length)
const refiledCount = computed(() => jobItems.value.filter(i => i.status === 'In').length)

const menuOptions = computed(() => [
  {
    label: 'Assign User',
    hidden: !checkUserPermission('can_assign_jobs'),
    disabled: globalStore.appIsOffline || editJobInfo.value || editItems.value || job.value?.status == 'Paused' || job.value?.status == 'Completed',
    action: () => {
      editJobInfo.value = true
    }
  },
  {
    label: 'Edit Items',
    disabled: globalStore.appIsOffline || editJobInfo.value || editItems.value || job.value?.status == 'Paused' || job.value?.status == 'Completed',
    action: () => {
      editItems.value = true
    }
  },
  {
    label: 'Delete Job',
    hidden: !checkUserPermission('can_delete_refile_job'),
    color: 'negative',
    disabled: globalStore.appIsOffline || editJobInfo.value || editItems.value || job.value?.status == 'Completed' || (jobItems.value && jobItems.value.some(itm => itm.status == 'In')),
    action: () => {
      showConfirmationModal.value = 'DeleteJob'
    }
  },
  {
    label: 'Print Job',
    action: () => batchSheetRef.value?.printBatchReport()
  },
  {
    label: 'View History',
    action: () => {
      showAuditTrailModal.value = true
    }
  }
])

const columns = [
  {
    name: 'location',
    label: 'Location',
    align: 'left',
    field: row => row.tray?.shelf_position?.location || row.shelf_position?.location || '-'
  },
  {
    name: 'tray_barcode',
    label: 'Tray Barcode',
    align: 'left',
    field: row => row.tray?.barcode?.value || '-'
  },
  {
    name: 'barcode',
    label: 'Item Barcode',
    align: 'left',
    field: row => row.barcode?.value || '-'
  },
  {
    name: 'owner',
    label: 'Owner',
    align: 'left',
    field: row => row.owner?.name || '-'
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

// Lifecycle
onMounted(async () => {
  if (globalStore.appIsOffline) {
    const res = await getDataInIndexDb('refileStore')
    if (res?.data?.refileJob) {
      refileStore.refileJob = res.data.refileJob
      refileStore.originalRefileJob = res.data.originalRefileJob
    }
  } else {
    await refileStore.getRefileJob(props.jobId)
    saveState()
  }
})

// Persistence
const saveState = () => {
  addDataToIndexDb('refileStore', 'refileJob', JSON.parse(JSON.stringify(job.value)))
  addDataToIndexDb('refileStore', 'originalRefileJob', JSON.parse(JSON.stringify(originalRefileJob.value)))
}

// Editor logic
const cancelRefileJobEdits = () => {
  refileStore.refileJob = { ...toRaw(originalRefileJob.value) }
  editJobInfo.value = false
  editItems.value = false
  selectedItems.value = []
}

const revertSelectedItemsToQueue = async () => {
  try {
    actionLoading.value = true
    const payload = {
      barcode_values: selectedItems.value.map(item => item.barcode?.value)
    }
    await refileStore.deleteRefileJobItems(payload)
    Notify.create({
      type: 'info',
      message: 'Items reverted to queue'
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to revert items'
    })
  } finally {
    actionLoading.value = false
    selectedItems.value = []
    editItems.value = false
  }
}

const updateRefileJob = async () => {
  try {
    actionLoading.value = true
    const payload = {
      id: job.value.id,
      assigned_user_id: job.value.assigned_user_id,
      run_timestamp: currentIsoDate()
    }
    await refileStore.patchRefileJob(payload)
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
    actionLoading.value = false
    editJobInfo.value = false
  }
}

const cancelRefileJob = async () => {
  try {
    actionLoading.value = true
    await refileStore.deleteRefileJob(job.value.id)
    Notify.create({
      type: 'positive',
      message: 'The Refile Job has been canceled.'
    })
    deleteDataInIndexDb('refileStore', 'refileJob')
    deleteDataInIndexDb('refileStore', 'originalRefileJob')
    router.push({ name: 'refile' })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to cancel job'
    })
  } finally {
    actionLoading.value = false
    showConfirmationModal.value = null
  }
}

// Watchers
watch(compiledBarCode, (newVal) => {
  if (newVal && job.value?.status === 'Running' && !scanLock.value) {
    handleBarcodeScanned(newVal)
  }
})

// Methods
const getStatusColor = (status) => {
  switch (status) {
    case 'Created': return 'blue'
    case 'Running': return 'accent'
    case 'Paused': return 'orange'
    case 'Completed': return 'positive'
    default: return 'grey'
  }
}

const handleManualScan = () => {
  if (barcodeInput.value) {
    handleBarcodeScanned(barcodeInput.value)
    barcodeInput.value = ''
  }
}

const handleBarcodeScanned = async (barcode) => {
  if (scanLock.value) {
    return
  }
  scanLock.value = true

  const item = jobItems.value.find(i => i.barcode?.value === barcode)

  if (!item) {
    Notify.create({
      type: 'negative',
      message: 'Item not found in this job'
    })
    scanLock.value = false
    return
  }

  if (item.status === 'In') {
    Notify.create({
      type: 'warning',
      message: 'Item already refiled'
    })
    scanLock.value = false
    return
  }

  // Instead of auto-refiling, we open the verification modal
  pendingVerifyItem.value = item
  showVerifyModal.value = true
  barcodeInput.value = ''
  scanLock.value = false // Release lock so modal can handle its own scanning
}

const closeVerifyModal = () => {
  showVerifyModal.value = false
  pendingVerifyItem.value = null
  nextTick(() => barcodeInputRef.value?.focus())
}

const completeItemRefile = async (item) => {
  actionLoading.value = true
  try {
    const payload = {
      job_id: job.value.id,
      run_timestamp: currentIsoDate()
    }

    if (item.tray) {
      payload.item_id = item.id
      await refileStore.patchRefileJobTrayItemScanned(payload)
    } else {
      payload.non_tray_item_id = item.id
      await refileStore.patchRefileJobNonTrayItemScanned(payload)
    }
    Notify.create({
      type: 'positive',
      message: 'Item refiled',
      timeout: 500
    })
    closeVerifyModal()
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: 'Failed to update item'
    })
  } finally {
    actionLoading.value = false
  }
}

const startJob = async () => {
  actionLoading.value = true
  try {
    const payload = {
      id: job.value.id,
      status: 'Running',
      assigned_user_id: userData.value.user_id,
      run_timestamp: currentIsoDate()
    }
    await refileStore.patchRefileJob(payload)
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
    await refileStore.patchRefileJob({
      id: job.value.id,
      status: 'Paused',
      run_timestamp: currentIsoDate()
    })
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
    await refileStore.patchRefileJob({
      id: job.value.id,
      status: 'Running',
      run_timestamp: currentIsoDate()
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: 'Failed to resume job'
    })
  } finally {
    actionLoading.value = false
  }
}

const completeJob = async () => {
  actionLoading.value = true
  try {
    await refileStore.patchRefileJob({
      id: job.value.id,
      status: 'Completed',
      run_timestamp: currentIsoDate()
    })
    deleteDataInIndexDb('refileStore', 'refileJob')
    router.push({ name: 'refile' })
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

const confirmRevert = (item) => {
  pendingRevertItem.value = item
  showRevertDialog.value = true
}

const revertItem = async () => {
  if (!pendingRevertItem.value) {
    return
  }
  actionLoading.value = true
  try {
    const payload = {
      barcode_values: [pendingRevertItem.value.barcode.value]
    }
    await refileStore.deleteRefileJobItems(payload)
    Notify.create({
      type: 'info',
      message: 'Item reverted to queue'
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: 'Failed to revert item'
    })
  } finally {
    actionLoading.value = false
    showRevertDialog.value = false
    pendingRevertItem.value = null
  }
}

</script>

<style lang="scss" scoped>
.refile-execute {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.bg-accent-1 {
  background-color: rgba($accent, 0.05);
}

.form-group-label {
  display: block;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  color: #666;
  margin-bottom: 4px;
}

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

.details-card {
  border-radius: 12px;
  background: white;
}

.detail-item {
  display: flex;
  flex-direction: column;
}


.btn-modern-outline {
  border-radius: 8px;
  padding: 8px 24px;
}
</style>
