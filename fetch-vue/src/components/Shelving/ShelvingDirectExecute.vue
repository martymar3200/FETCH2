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
          v-if="job?.status !== 'Completed' && currentScreenSize !== 'xs'"
          :status="job?.status || 'Created'"
          :can-complete="allContainersShelved"
          :loading="actionLoading"
          @start="startJob"
          @complete="showCompleteDialog = true"
        />
      </template>
    </JobPageHeader>

    <!-- Minimal Progress Bar -->
    <div
      v-if="job"
      class="q-mb-md row items-center no-wrap bg-white border-all rounded-borders q-px-sm q-py-xs shadow-1"
    >
      <div
        class="text-grey-7 text-weight-bold text-uppercase q-mr-sm"
        style="font-size: 0.65rem; letter-spacing: 0.05em; min-width: max-content;"
      >
        PROGRESS
      </div>
      <div
        class="text-body2 text-weight-bold text-primary q-mr-sm"
        style="font-family: 'JetBrains Mono', monospace;"
      >
        {{ shelvedCount }}/{{ totalCount }}
      </div>
      <div class="col">
        <q-linear-progress
          :value="totalCount > 0 ? shelvedCount / totalCount : 0"
          color="accent"
          track-color="grey-3"
          rounded
          size="8px"
        />
      </div>
      <div
        class="text-caption text-weight-bold text-accent q-ml-sm"
        style="font-size: 0.75rem;"
      >
        {{ totalCount > 0 ? Math.round((shelvedCount / totalCount) * 100) : 0 }}%
      </div>
    </div>

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
        <BaseButton
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
          <BaseButton
            no-caps
            unelevated
            color="accent"
            label="Save Assignment"

            :loading="actionLoading"
            @click="updateUserAssignment"
          />
        </div>
      </q-card-section>
    </q-card>

    <!-- Not Started Message -->
    <q-card
      v-if="job?.status === 'Created' || job?.status === 'Assigned'"
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

    <!-- Scanning & Targeting Section -->
    <template v-if="job?.status === 'Running'">
      <!-- Current Shelf Card -->
      <q-card
        class="industrial-card current-target-card q-mb-md shadow-1"
        style="border-left: 4px solid var(--q-accent);"
      >
        <div
          class="bg-grey-1 q-px-md q-py-sm flex justify-between items-center"
          style="border-bottom: 1px solid #e2e8f0;"
        >
          <span
            class="text-weight-bold text-accent text-uppercase"
            style="font-size: 0.65rem; letter-spacing: 0.05em;"
          >CURRENT SHELF</span>
          <q-icon
            name="shelves"
            color="accent"
            size="sm"
          />
        </div>
        <q-card-section class="q-pa-md q-gutter-y-md">
          <template v-if="currentShelf">
            <div>
              <div
                class="text-grey-7 text-uppercase text-weight-bold"
                style="font-size: 0.65rem; letter-spacing: 0.05em;"
              >
                LOCATION
              </div>
              <div class="text-h5 text-primary text-weight-bold tracking-tight q-mt-xs">
                {{ currentShelf }}
              </div>
            </div>

            <div class="row q-col-gutter-md">
              <div class="col-6">
                <div
                  class="text-grey-7 text-uppercase text-weight-bold"
                  style="font-size: 0.65rem; letter-spacing: 0.05em;"
                >
                  OWNER
                </div>
                <div class="text-body2 text-primary text-weight-medium q-mt-xs">
                  {{ job?.owner?.name || '-' }}
                </div>
              </div>
              <div class="col-6">
                <div
                  class="text-grey-7 text-uppercase text-weight-bold"
                  style="font-size: 0.65rem; letter-spacing: 0.05em;"
                >
                  SIZE CLASS
                </div>
                <div class="text-body2 text-primary text-weight-medium q-mt-xs">
                  {{ job?.size_class?.name || '-' }}
                </div>
              </div>
            </div>

            <div
              class="q-mt-sm q-pt-sm"
              style="border-top: 1px dashed #e2e8f0;"
            >
              <BaseButton
                flat
                no-caps
                color="grey-7"
                label="Change Shelf"
                icon="refresh"
                dense
                size="sm"
                @click="clearShelf"
              />
            </div>
          </template>
          <template v-else>
            <div
              class="text-grey-7 text-weight-bold q-mb-sm"
              style="font-size: 0.75rem;"
            >
              SCAN SHELF BARCODE
            </div>
            <q-input
              v-model="shelfBarcodeInput"
              outlined
              dense
              bg-color="white"
              placeholder="Focus here to scan..."
              @keyup.enter="scanShelf"
              ref="shelfInput"
              autofocus
              :inputmode="keyboardEnabled ? 'numeric' : 'none'"
              @click="handleInputClick(shelfInput)"
              @blur="handleInputBlur"
              color="accent"
              class="scan-input-modern"
            >
              <template #prepend>
                <q-icon
                  name="qr_code_scanner"
                  color="accent"
                />
              </template>
            </q-input>
          </template>
        </q-card-section>
      </q-card>

      <!-- Container Scanning Card -->
      <q-card
        v-if="currentShelf"
        class="bg-grey-2 q-mb-md"
        style="border: 1px solid #e2e8f0;"
        flat
      >
        <q-card-section class="q-pa-md">
          <div
            class="text-grey-7 text-weight-bold q-mb-sm"
            style="font-size: 0.75rem;"
          >
            SCAN CONTAINER BARCODE TO SHELVE
          </div>
          <div class="row q-col-gutter-sm items-end">
            <div class="col-12 col-sm-8">
              <q-input
                v-model="containerBarcodeInput"
                outlined
                dense
                bg-color="white"
                placeholder="Scan container..."
                @keyup.enter="scanContainer"
                ref="containerInput"
                :inputmode="keyboardEnabled ? 'numeric' : 'none'"
                @click="handleInputClick(containerInput)"
                @blur="handleInputBlur"
                color="accent"
                class="scan-input-modern"
              >
                <template #prepend>
                  <q-icon
                    name="qr_code_scanner"
                    color="accent"
                  />
                </template>
              </q-input>
            </div>
            <div class="col-12 col-sm-4">
              <q-input
                v-model.number="positionNumber"
                type="number"
                outlined
                dense
                bg-color="white"
                min="1"
                :placeholder="nextPosition ? `Next: ${nextPosition}` : 'Pos'"
                color="accent"
                class="scan-input-modern"
              />
            </div>
            <div class="col-12">
              <BaseButton
                no-caps
                unelevated
                color="accent"
                label="Shelve Container"
                class="full-width q-mt-sm"
                :loading="scanning"
                :disable="!containerBarcodeInput || !positionNumber"
                @click="shelveContainer"
              />
            </div>
          </div>

          <div
            v-if="scanError"
            class="text-negative text-caption q-mt-sm text-weight-bold"
          >
            <q-icon name="error" /> {{ scanError }}
          </div>
        </q-card-section>
      </q-card>
    </template>

    <!-- Container List Collapsible -->
    <q-expansion-item
      class="industrial-card q-mb-xl rounded-borders overflow-hidden"
      header-class="q-pa-md"
      expand-icon="expand_more"
      expanded-icon="expand_less"
      default-opened
    >
      <template #header>
        <div
          class="row w-full items-center justify-between"
          style="width: 100%"
        >
          <div class="col flex items-center q-gutter-x-sm">
            <q-icon
              name="list_alt"
              color="grey-6"
              size="sm"
            />
            <div
              class="text-weight-bold text-uppercase"
              style="font-size: 0.65rem; letter-spacing: 0.05em;"
            >
              SHELVED CONTAINERS ({{ shelvedCount }}/{{ totalCount }})
            </div>
          </div>
        </div>
      </template>

      <q-card>
        <q-card-section class="q-pa-none border-top">
          <q-table
            :rows="containers"
            :columns="containerColumns"
            row-key="id"
            flat
            dense
            :pagination="{ rowsPerPage: 0 }"
            hide-pagination
            class="job-table"
          >
            <template #body-cell-barcode="props">
              <q-td :props="props">
                <span class="text-weight-medium">{{ props.row.barcode?.value || '-' }}</span>
              </q-td>
            </template>
            <template #body-cell-status="props">
              <q-td
                :props="props"
                class="text-center"
              >
                <q-chip
                  v-if="props.row.scanned_for_shelving"
                  color="positive"
                  text-color="white"
                  icon="check_circle"
                  label="Shelved"
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
          </q-table>
        </q-card-section>
      </q-card>
    </q-expansion-item>

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

    <!-- Mobile Action Bar -->
    <MobileActionBar
      v-if="currentScreenSize == 'xs' && job?.status !== 'Completed'"
      :button-one-color="'accent'"
      :button-one-label="job?.status === 'Created' || job?.status === 'Assigned' ? 'Start Job' : ''"
      :button-one-outline="false"
      @button-one-click="startJob"
      button-two-color="positive"
      :button-two-label="job?.status === 'Running' ? 'Complete Job' : ''"
      :button-two-disabled="!allContainersShelved"
      :button-two-loading="actionLoading"
      @button-two-click="showCompleteDialog = true"
    />
  </div>
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
import { ref, computed, onMounted, watch, nextTick, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useShelvingStore } from '@/stores/shelving-store'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { storeToRefs } from 'pinia'
import { notify } from '@/utils/notify'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import { useIndexDbHandler } from '@/composables/useIndexDbHandler.js'

// Shared Job Components
import JobPageHeader from '@/components/Job/JobPageHeader.vue'
import JobActionButtons from '@/components/Job/JobActionButtons.vue'
import JobConfirmDialog from '@/components/Job/JobConfirmDialog.vue'
import AuditTrail from '@/components/AuditTrail.vue'
import SelectInput from '@/components/SelectInput.vue'
import ShelvingBatchSheet from '@/components/Shelving/ShelvingBatchSheet.vue'
import MobileActionBar from '@/components/MobileActionBar.vue'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'

const router = useRouter()
const shelvingStore = useShelvingStore()

// Composables
const { currentScreenSize } = useCurrentScreenSize()
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
const keyboardEnabled = ref(false)

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
    hidden: !checkUserPermission('can_assign_jobs'),
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
    notify({
      type: 'positive',
      message: 'User assignment updated'
    })
    editJob.value = false
  } catch (error) {
    notify({
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
const handleInputClick = (inputRef) => {
  if (!keyboardEnabled.value) {
    keyboardEnabled.value = true
    nextTick(() => {
      inputRef?.focus()
    })
  }
}

const handleInputBlur = () => {
  keyboardEnabled.value = false
}

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
    notify({
      type: 'positive',
      message: 'Job started!'
    })
  } catch (error) {
    notify({
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
    notify({
      type: 'info',
      message: 'Job cancelled'
    })
    deleteDataInIndexDb('shelvingStore', 'shelvingJob')
    router.push({ name: 'shelving' })
  } catch (error) {
    notify({
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
    notify({
      type: 'negative',
      message: 'Permission denied'
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
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to scan shelf'
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

    notify({
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
    notify({
      type: 'positive',
      message: 'Job completed!'
    })
    deleteDataInIndexDb('shelvingStore', 'shelvingJob')
    router.push({ name: 'shelving' })
  } catch (error) {
    notify({
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
.table-header-filters {
  @media (max-width: 599px) {
    width: 100%;
    margin-top: 12px;
    flex-direction: column;
    margin-left: 0 !important;

    .q-btn-toggle {
      width: 100%;
      margin-left: 0;
      margin-bottom: 8px;
    }
  }
}

.industrial-card {
  background: white;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1);
}
.border-all {
  border: 1px solid #e2e8f0;
}
.border-top {
  border-top: 1px solid #e2e8f0;
}
.border-bottom {
  border-bottom: 1px solid #e2e8f0;
}
.job-table.q-table__card {
  box-shadow: none;
  background: transparent;
}
.job-table th {
  font-size: 10px;
  font-weight: 700;
  color: #64748b;
  text-transform: uppercase;
}
.scan-input-modern .q-field__control {
  background-color: white !important;
}
</style>
