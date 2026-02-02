<template>
  <div class="shelving-direct-execute">
    <!-- Header using shared component -->
    <JobPageHeader
      title="Direct Shelving Job"
      :job-id="job?.id"
      :status="job?.status"
      :status-color="getStatusColor(job?.status)"
      :subtitle="subtitle"
      :menu-options="headerMenuOptions"
    >
      <template #actions>
        <JobActionButtons
          v-if="job?.status !== 'Completed'"
          :status="job?.status || 'Created'"
          :can-complete="allContainersShelved"
          :loading="actionLoading"
          @start="startJob"
          @complete="showCompleteDialog = true"
        />
      </template>
    </JobPageHeader>

    <!-- Progress Bar -->
    <JobProgressBar
      :completed="shelvedCount"
      :total="totalCount"
    />

    <!-- Quick User Assign Card -->
    <q-card
      v-if="editJob"
      flat
      bordered
      class="q-mb-lg user-assign-card"
    >
      <q-card-section class="row items-center q-pb-none">
        <div class="text-h6 text-bold">
          Assign User
        </div>
        <q-space />
        <q-btn
          flat
          round
          dense
          icon="close"
          @click="editJob = false"
        />
      </q-card-section>

      <q-card-section class="row q-col-gutter-md items-end">
        <div class="col-12 col-md-4">
          <label class="form-group-label">Select User</label>
          <SelectInput
            v-model="job.assigned_user_id"
            :options="users"
            option-type="users"
            option-value="id"
            option-label="name"
            placeholder="Select a user"
          />
        </div>
        <div class="col-auto">
          <q-btn
            no-caps
            unelevated
            color="accent"
            label="Save Assignment"
            class="btn-modern"
            :loading="actionLoading"
            @click="updateUserAssignment"
          />
        </div>
      </q-card-section>
    </q-card>

    <!-- Not Started Message -->
    <q-card
      v-if="job?.status === 'Created'"
      class="q-mb-lg"
    >
      <q-card-section class="text-center q-pa-lg">
        <q-icon
          name="play_circle"
          size="64px"
          color="accent"
          class="q-mb-md"
        />
        <div class="text-h6 q-mb-sm">
          Ready to Start
        </div>
        <p class="text-grey-7">
          Click "Start Job" to begin scanning shelves and containers.
        </p>
      </q-card-section>
    </q-card>

    <!-- Current Shelf Card (only when job is Running) -->
    <q-card
      v-if="job?.status === 'Running'"
      class="q-mb-lg"
    >
      <q-card-section>
        <div class="row q-col-gutter-md">
          <!-- Shelf Scan Section -->
          <div class="col-12 col-md-6">
            <label class="form-group-label">Current Shelf</label>
            <div
              v-if="currentShelf"
              class="text-h5 text-weight-bold text-accent"
            >
              {{ currentShelf }}
            </div>
            <q-input
              v-else
              v-model="shelfBarcodeInput"
              outlined
              dense
              placeholder="Scan shelf barcode"
              @keyup.enter="scanShelf"
              ref="shelfInput"
              autofocus
            >
              <template #append>
                <q-icon name="shelves" />
              </template>
            </q-input>
          </div>

          <!-- Shelf Details -->
          <div
            v-if="currentShelf"
            class="col-12 col-md-6"
          >
            <div class="row q-gutter-md">
              <div class="col">
                <div class="text-caption text-grey-6">
                  Owner
                </div>
                <div class="text-body1">
                  {{ job?.owner?.name || '-' }}
                </div>
              </div>
              <div class="col">
                <div class="text-caption text-grey-6">
                  Size Class
                </div>
                <div class="text-body1">
                  {{ job?.size_class?.name || '-' }}
                </div>
              </div>
              <div class="col-auto">
                <q-btn
                  flat
                  no-caps
                  color="grey-7"
                  label="Change Shelf"
                  icon="refresh"
                  @click="clearShelf"
                />
              </div>
            </div>
          </div>
        </div>
      </q-card-section>
    </q-card>

    <!-- Container Scan Section (only when shelf is selected) -->
    <q-card
      v-if="currentShelf && job?.status === 'Running'"
      class="q-mb-lg bg-accent-1"
    >
      <q-card-section>
        <div class="row q-col-gutter-md items-end">
          <div class="col-12 col-md-6">
            <label class="form-group-label">Scan Container Barcode</label>
            <q-input
              v-model="containerBarcodeInput"
              outlined
              dense
              placeholder="Scan container barcode"
              @keyup.enter="scanContainer"
              ref="containerInput"
            >
              <template #append>
                <q-icon name="qr_code_scanner" />
              </template>
            </q-input>
          </div>
          <div class="col-12 col-md-3">
            <label class="form-group-label">Position #</label>
            <q-input
              v-model.number="positionNumber"
              type="number"
              outlined
              dense
              min="1"
              :placeholder="nextPosition ? `Next: ${nextPosition}` : 'Position'"
            />
          </div>
          <div class="col-12 col-md-3">
            <q-btn
              no-caps
              unelevated
              color="accent"
              label="Shelve"
              class="btn-modern full-width"
              :loading="scanning"
              :disable="!containerBarcodeInput || !positionNumber"
              @click="shelveContainer"
            />
          </div>
        </div>

        <!-- Scan Error -->
        <div
          v-if="scanError"
          class="text-negative q-mt-sm"
        >
          {{ scanError }}
        </div>
      </q-card-section>
    </q-card>

    <!-- Container List -->
    <q-card>
      <q-card-section>
        <div class="row items-center q-mb-md">
          <div class="col">
            <div class="text-h6">
              Containers Shelved
            </div>
          </div>
          <div class="col-auto">
            <q-badge
              color="accent"
              :label="`${shelvedCount}/${totalCount}`"
            />
          </div>
        </div>

        <q-table
          :rows="containers"
          :columns="containerColumns"
          row-key="id"
          flat
          dense
          :pagination="{ rowsPerPage: 15 }"
          class="essential-table"
        >
          <template #body-cell-barcode="props">
            <q-td :props="props">
              <span class="text-weight-medium">{{ props.row.barcode?.value || '-' }}</span>
            </q-td>
          </template>
          <template #body-cell-status="props">
            <q-td :props="props">
              <q-badge
                :color="props.row.scanned_for_shelving ? 'positive' : 'grey'"
                :label="props.row.scanned_for_shelving ? 'Shelved' : 'Pending'"
              />
            </q-td>
          </template>
        </q-table>
      </q-card-section>
    </q-card>

    <!-- Complete Job Dialog -->
    <JobConfirmDialog
      v-model="showCompleteDialog"
      title="Complete Job?"
      message="Are you sure you want to complete this shelving job?"
      confirm-label="Complete"
      confirm-color="positive"
      :loading="completing"
      @confirm="completeJob"
    />

    <!-- Cancel Job Dialog -->
    <JobConfirmDialog
      v-model="showCancelDialog"
      title="Cancel Job?"
      message="Are you sure you want to cancel this shelving job?"
      warning="Any containers added will need to be re-shelved."
      confirm-label="Yes, Cancel"
      confirm-color="negative"
      :loading="cancelling"
      @confirm="cancelJob"
    />

    <!-- Audit Trail Modal -->
    <AuditTrail
      v-if="showAuditTrailModal"
      ref="historyModal"
      @reset="showAuditTrailModal = false"
      job-type="shelving_jobs"
      :job-id="job?.id"
    />

    <!-- Print Component -->
    <ShelvingBatchSheet
      ref="batchSheetComponent"
      :shelving-job-details="job"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useShelvingStore } from '@/stores/shelving-store'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { storeToRefs } from 'pinia'
import { useQuasar } from 'quasar'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import { useIndexDbHandler } from '@/composables/useIndexDbHandler.js'

// Shared Job Components
import JobPageHeader from '@/components/Job/JobPageHeader.vue'
import JobProgressBar from '@/components/Job/JobProgressBar.vue'
import JobActionButtons from '@/components/Job/JobActionButtons.vue'
import JobConfirmDialog from '@/components/Job/JobConfirmDialog.vue'
import AuditTrail from '@/components/AuditTrail.vue'
import SelectInput from '@/components/SelectInput.vue'
import ShelvingBatchSheet from '@/components/Shelving/ShelvingBatchSheet.vue'

const router = useRouter()
const shelvingStore = useShelvingStore()
const $q = useQuasar()

// Composables
const { compiledBarCode } = useBarcodeScanHandler()
const { checkUserPermission } = usePermissionHandler()
const { addDataToIndexDb, getDataInIndexDb, deleteDataInIndexDb } = useIndexDbHandler()

// Store refs
const { appIsLoadingData, appIsOffline, appPendingSync } = storeToRefs(useGlobalStore())
const { users } = storeToRefs(useOptionStore())
const { shelvingJob, shelvingJobContainers, allContainersShelved } = storeToRefs(shelvingStore)
const { getShelfByBarcode, patchShelvingJob, postShelvingJobContainer, resetShelvingJobContainer } = shelvingStore

// Injected helpers
const currentIsoDate = inject('current-iso-date')
const handleAlert = inject('handle-alert')
const getItemLocation = inject('get-item-location')

// Local State
const job = computed(() => shelvingJob.value)
const containers = computed(() => shelvingJobContainers.value)
const shelfBarcodeInput = ref('')
const containerBarcodeInput = ref('')
const positionNumber = ref(null)
const scanning = ref(false)
const actionLoading = ref(false)
const completing = ref(false)
const cancelling = ref(false)
const scanError = ref('')
const showCompleteDialog = ref(false)
const showCancelDialog = ref(false)
const showAuditTrailModal = ref(false)
const batchSheetComponent = ref(null)
const editJob = ref(false)

// Input refs
const shelfInput = ref(null)
const containerInput = ref(null)

// Computed
const currentShelf = computed(() => job.value?.shelf_barcode?.value || '')
const nextPosition = computed(() => job.value?.nextAvailablePosition || null)
const totalCount = computed(() => containers.value.length)
const shelvedCount = computed(() => containers.value.filter(c => c.scanned_for_shelving).length)
const subtitle = computed(() => {
  const parts = []
  if (job.value?.owner?.name) {
    parts.push(job.value.owner.name)
  }
  if (job.value?.size_class?.name) {
    parts.push(job.value.size_class.name)
  }
  parts.push(`${shelvedCount.value}/${totalCount.value} shelved`)
  const user = job.value?.assigned_user?.name || 'Unassigned'
  parts.push(user)
  return parts.join(' • ')
})

const headerMenuOptions = computed(() => {
  const options = []

  // Assign User
  options.push({
    label: 'Assign User',
    icon: 'person_add',
    hidden: !checkUserPermission('can_assign_and_reassign_shelving_job'),
    disabled: editJob.value || job.value?.status === 'Completed',
    action: () => {
      editJob.value = true
    }
  })

  // View History - always available
  options.push({
    label: 'View History',
    icon: 'history',
    color: 'grey',
    action: () => viewHistory()
  })

  // Print Job - always available
  options.push({
    label: 'Print Job',
    icon: 'print',
    color: 'grey',
    action: () => printJob()
  })

  // Cancel Job - only when Created (not started)
  if (job.value?.status === 'Created') {
    options.push({
      label: 'Cancel Job',
      icon: 'cancel',
      color: 'negative',
      action: () => {
        showCancelDialog.value = true
      }
    })
  }

  return options
})

const updateUserAssignment = async () => {
  actionLoading.value = true
  try {
    await patchShelvingJob({
      id: job.value.id,
      assigned_user_id: job.value.assigned_user_id
    })
    $q.notify({
      type: 'positive',
      message: 'User assignment updated'
    })
    editJob.value = false
  } catch (error) {
    $q.notify({
      type: 'negative',
      message: 'Failed to update user assignment'
    })
  } finally {
    actionLoading.value = false
  }
}

const containerColumns = [
  {
    name: 'barcode',
    label: 'Barcode',
    field: row => row.barcode?.value,
    align: 'left',
    sortable: true
  },
  {
    name: 'owner',
    label: 'Owner',
    field: row => row.owner?.name || '-',
    align: 'left'
  },
  {
    name: 'size_class',
    label: 'Size Class',
    field: row => row.size_class?.name || '-',
    align: 'left'
  },
  {
    name: 'location',
    label: 'Location',
    field: row => getItemLocation(row),
    align: 'left'
  },
  {
    name: 'status',
    label: 'Status',
    field: 'scanned_for_shelving',
    align: 'center'
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
    await patchShelvingJob({
      id: job.value.id,
      status: 'Running'
    })
    $q.notify({
      type: 'positive',
      message: 'Job started!'
    })
    addDataToIndexDb('shelvingStore', 'shelvingJob', JSON.parse(JSON.stringify(shelvingJob.value)))
  } catch (error) {
    $q.notify({
      type: 'negative',
      message: 'Failed to start job'
    })
  } finally {
    actionLoading.value = false
  }
}

const viewHistory = () => {
  showAuditTrailModal.value = true
}

const printJob = () => {
  batchSheetComponent.value?.printBatchReport()
}

const cancelJob = async () => {
  cancelling.value = true
  try {
    await patchShelvingJob({
      id: job.value.id,
      status: 'Cancelled'
    })
    showCancelDialog.value = false
    $q.notify({
      type: 'info',
      message: 'Job cancelled'
    })
    deleteDataInIndexDb('shelvingStore', 'shelvingJob')
    router.push({ name: 'shelving' })
  } catch (error) {
    $q.notify({
      type: 'negative',
      message: 'Failed to cancel job'
    })
  } finally {
    cancelling.value = false
  }
}

const scanShelf = async () => {
  if (!shelfBarcodeInput.value) {
    return
  }

  if (!checkUserPermission('can_create_and_execute_direct_shelving_job')) {
    handleAlert({
      type: 'error',
      text: 'Permission denied',
      autoClose: true
    })
    return
  }

  try {
    appIsLoadingData.value = true
    await getShelfByBarcode(shelfBarcodeInput.value)
    shelfBarcodeInput.value = ''

    // Auto-focus container input
    nextTick(() => {
      containerInput.value?.focus()
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

const clearShelf = () => {
  shelvingJob.value.shelf_barcode.value = ''
  shelvingJob.value.owner.id = null
  shelvingJob.value.owner.name = ''
  shelvingJob.value.size_class_id = null
  shelvingJob.value.size_class.name = ''
  addDataToIndexDb('shelvingStore', 'shelvingJob', JSON.parse(JSON.stringify(shelvingJob.value)))
}

const scanContainer = () => {
  if (!containerBarcodeInput.value) {
    return
  }
  processContainerScan(containerBarcodeInput.value)
}

const processContainerScan = (barcode) => {
  // Check if already shelved
  if (containers.value.some(c => c.barcode?.value === barcode && c.scanned_for_shelving)) {
    scanError.value = 'Container already shelved'
    return
  }

  // Check shelf capacity
  if (!appIsOffline.value && !nextPosition.value) {
    scanError.value = 'Shelf is full'
    return
  }

  // Auto-populate position
  if (!positionNumber.value && nextPosition.value) {
    positionNumber.value = nextPosition.value
  }

  scanError.value = ''
}

const shelveContainer = async () => {
  if (!containerBarcodeInput.value || !positionNumber.value) {
    return
  }

  scanning.value = true
  scanError.value = ''

  try {
    const payload = {
      job_id: job.value.id,
      container_barcode_value: containerBarcodeInput.value,
      shelf_barcode_value: currentShelf.value,
      shelf_position_number: parseInt(positionNumber.value),
      shelved_dt: currentIsoDate(),
      scanned_for_shelving: true
    }
    await postShelvingJobContainer(payload)

    addDataToIndexDb('shelvingStore', 'shelvingJob', JSON.parse(JSON.stringify(shelvingJob.value)))

    $q.notify({
      type: 'positive',
      message: 'Container shelved!'
    })
    containerBarcodeInput.value = ''
    positionNumber.value = nextPosition.value

    nextTick(() => {
      containerInput.value?.focus()
    })
  } catch (error) {
    scanError.value = error.response?.data?.detail || 'Failed to shelve container'
  } finally {
    scanning.value = false
    resetShelvingJobContainer()
  }
}

const completeJob = async () => {
  completing.value = true
  try {
    await patchShelvingJob({
      id: job.value.id,
      status: 'Completed',
      run_timestamp: currentIsoDate()
    })

    showCompleteDialog.value = false
    $q.notify({
      type: 'positive',
      message: 'Job completed!'
    })
    deleteDataInIndexDb('shelvingStore', 'shelvingJob')
    router.push({ name: 'shelving' })
  } catch (error) {
    $q.notify({
      type: 'negative',
      message: 'Failed to complete job'
    })
  } finally {
    completing.value = false
  }
}

// Watch for barcode scans
watch(compiledBarCode, (barcode) => {
  if (!barcode || job.value?.status === 'Completed') {
    return
  }

  if (!currentShelf.value) {
    // Scan as shelf
    shelfBarcodeInput.value = barcode
    scanShelf()
  } else {
    // Scan as container
    containerBarcodeInput.value = barcode
    processContainerScan(barcode)
  }
})

// Initialize
onMounted(async () => {
  if (!appIsOffline.value && !appPendingSync.value) {
    await nextTick()
    addDataToIndexDb('shelvingStore', 'shelvingJob', JSON.parse(JSON.stringify(shelvingJob.value)))
  } else {
    const res = await getDataInIndexDb('shelvingStore')
    if (res?.data?.shelvingJob) {
      shelvingJob.value = res.data.shelvingJob
    }
  }

  // Auto-focus appropriate input
  nextTick(() => {
    if (!currentShelf.value) {
      shelfInput.value?.focus()
    } else {
      containerInput.value?.focus()
    }
  })
})
</script>

<style scoped lang="scss">
.shelving-direct-execute {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
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

.bg-accent-1 {
  background: linear-gradient(135deg, rgba(var(--q-accent), 0.1) 0%, rgba(var(--q-accent), 0.05) 100%);
}

.user-assign-card {
  border-radius: 12px;
  background: white;
}

.btn-modern {
  border-radius: 8px;
  padding: 8px 24px;
  font-weight: 600;
}
</style>
