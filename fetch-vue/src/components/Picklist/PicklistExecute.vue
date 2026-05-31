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
        <div
          v-if="editJob"
          class="row q-gutter-x-sm"
        >
          <BaseButton
            no-caps
            unelevated
            color="accent"
            label="Save Edits"

            :loading="actionLoading"
            @click="updateUserAssignment"
          />
          <BaseButton
            no-caps
            unelevated
            outline
            color="accent"
            label="Cancel"
            class="btn-modern-outline"
            @click="editJob = false"
          />
        </div>
        <JobActionButtons
          v-else-if="job?.status !== 'Completed' && currentScreenSize !== 'xs'"
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

    <!-- Quick Edit Card -->
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
        {{ retrievedCount }}/{{ totalCount }}
      </div>
      <div class="col">
        <q-linear-progress
          :value="totalCount > 0 ? retrievedCount / totalCount : 0"
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
        {{ totalCount > 0 ? Math.round((retrievedCount / totalCount) * 100) : 0 }}%
      </div>
    </div>

    <!-- Scan Section -->
    <template v-if="job?.status === 'Running'">
      <!-- Current Target Card -->
      <q-card
        v-if="currentTarget"
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
          >CURRENT TARGET</span>
          <q-icon
            name="my_location"
            color="accent"
            size="sm"
          />
        </div>
        <q-card-section class="q-pa-md q-gutter-y-md">
          <div>
            <div
              class="text-grey-7 text-uppercase text-weight-bold"
              style="font-size: 0.65rem; letter-spacing: 0.05em;"
            >
              LOCATION
            </div>
            <div class="text-h5 text-primary text-weight-bold tracking-tight q-mt-xs">
              {{ currentTarget.item ? getItemLocation(currentTarget.item.tray) : getItemLocation(currentTarget.non_tray_item) || 'Unknown' }}
            </div>
          </div>

          <div class="row q-col-gutter-md">
            <div class="col-6">
              <div
                class="text-grey-7 text-uppercase text-weight-bold"
                style="font-size: 0.65rem; letter-spacing: 0.05em;"
              >
                TRAY BARCODE
              </div>
              <div
                class="text-body2 text-weight-bold bg-grey-1 q-pa-sm rounded-borders q-mt-xs inline-block"
                style="font-family: 'JetBrains Mono', monospace; border: 1px solid #e2e8f0;"
              >
                {{ currentTarget.item?.tray?.barcode?.value || 'N/A' }}
              </div>
            </div>
            <div class="col-6">
              <div
                class="text-grey-7 text-uppercase text-weight-bold"
                style="font-size: 0.65rem; letter-spacing: 0.05em;"
              >
                OWNER
              </div>
              <div class="text-body2 text-primary text-weight-medium q-mt-xs">
                {{ currentTarget.item?.owner?.name || currentTarget.non_tray_item?.owner?.name || 'Unknown' }}
              </div>
            </div>
          </div>

          <div
            class="q-mt-md q-pt-md"
            style="border-top: 1px dashed #e2e8f0;"
          >
            <div
              class="text-grey-7 text-uppercase text-weight-bold"
              style="font-size: 0.65rem; letter-spacing: 0.05em;"
            >
              ITEM BARCODE
            </div>
            <div class="text-h6 text-primary text-weight-bold q-mt-xs">
              {{ currentTarget.item?.barcode?.value || currentTarget.non_tray_item?.barcode?.value || 'Unknown' }}
            </div>
          </div>
        </q-card-section>
      </q-card>

      <!-- Scanning Card -->
      <q-card
        class="bg-grey-2 q-mb-md"
        style="border: 1px solid #e2e8f0;"
        flat
      >
        <q-card-section class="q-pa-md">
          <div
            class="text-grey-7 text-weight-bold q-mb-sm"
            style="font-size: 0.75rem;"
          >
            SCAN ITEM BARCODE TO RETRIEVE
          </div>
          <q-input
            v-model="barcodeInput"
            outlined
            dense
            bg-color="white"
            placeholder="Focus here to scan..."
            @keyup.enter="handleManualScan"
            ref="scanInput"
            autofocus
            :inputmode="keyboardEnabled ? 'numeric' : 'none'"
            @click="handleInputClick"
            @blur="handleInputBlur"
            color="accent"
            class="scan-input-modern"
            :loading="scanning"
          >
            <template #prepend>
              <q-icon
                name="qr_code_scanner"
                color="accent"
              />
            </template>
            <template
              #append
              v-if="currentScreenSize !== 'xs'"
            >
              <BaseButton
                no-caps
                unelevated
                color="accent"
                label="Retrieve"
                dense
                class="q-px-sm"
                :loading="scanning"
                @click="handleManualScan"
              />
            </template>
          </q-input>
        </q-card-section>
      </q-card>
    </template>

    <!-- Item List Collapsible -->
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
              PICKLIST ({{ totalCount - retrievedCount }} REMAINING)
            </div>
          </div>
        </div>
      </template>

      <q-card>
        <q-card-section class="q-pa-none border-top">
          <div class="table-header-row row items-center justify-end q-pa-sm bg-grey-1 border-bottom">
            <div class="table-header-filters col-auto flex q-gutter-x-sm">
              <q-btn-toggle
                v-model="filter"
                no-caps
                rounded
                unelevated
                toggle-color="accent"
                color="white"
                text-color="grey-7"
                class="toggle-modern-rounded"
                size="sm"
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
            :pagination="{ rowsPerPage: 0 }"
            hide-pagination
            class="job-table"
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
                <q-chip
                  v-if="props.row.status !== 'PickList'"
                  color="positive"
                  text-color="white"
                  icon="check_circle"
                  label="Retrieved"
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

            <template #body-cell-actions="props">
              <q-td
                :props="props"
                class="text-right"
              >
                <BaseButton
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
                </BaseButton>
              </q-td>
            </template>
          </q-table>
        </q-card-section>
      </q-card>
    </q-expansion-item>

    <JobConfirmDialog
      v-model="showCompleteDialog"
      title="Complete Job"
      message="Are you sure you want to complete this Picklist job?"
      :loading="actionLoading"
      :complete-job-mode="shippingEnabled"
      @confirm="(print) => completeJob(print)"
    />

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

    <!-- Mobile Action Bar -->
    <MobileActionBar
      v-if="currentScreenSize == 'xs' && job?.status !== 'Completed'"
      :button-one-color="job?.status === 'Running' ? 'primary' : 'accent'"
      :button-one-label="job?.status === 'Created' || job?.status === 'Assigned' ? 'Start Job' : (job?.status === 'Running' ? 'Pause' : 'Resume')"
      :button-one-outline="job?.status === 'Running'"
      @button-one-click="job?.status === 'Created' || job?.status === 'Assigned' ? startJob() : (job?.status === 'Running' ? pauseJob() : resumeJob())"
      button-two-color="positive"
      :button-two-label="job?.status === 'Created' || job?.status === 'Assigned' ? '' : 'Complete Job'"
      :button-two-disabled="!allItemsRetrieved"
      :button-two-loading="actionLoading"
      @button-two-click="showCompleteDialog = true"
    />
  </div>
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
import { ref, computed, onMounted, watch, nextTick, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePicklistStore } from '@/stores/picklist-store'
import { useUserStore } from '@/stores/user-store'
import { useOptionStore } from '@/stores/option-store'
import { storeToRefs } from 'pinia'
import { notify } from '@/utils/notify'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import { useIndexDbHandler } from '@/composables/useIndexDbHandler.js'

// Shared Components
import JobPageHeader from '@/components/Job/JobPageHeader.vue'
import JobActionButtons from '@/components/Job/JobActionButtons.vue'
import JobConfirmDialog from '@/components/Job/JobConfirmDialog.vue'
import AuditTrail from '@/components/AuditTrail.vue'
import PicklistBatchSheet from '@/components/Picklist/PicklistBatchSheet.vue'
import SelectInput from '@/components/SelectInput.vue'
import PicklistItemDetailModal from '@/components/Picklist/PicklistItemDetailModal.vue'
import MobileActionBar from '@/components/MobileActionBar.vue'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'

const route = useRoute()
const router = useRouter()
const picklistStore = usePicklistStore()

// Composables
const { currentScreenSize } = useCurrentScreenSize()
const { compiledBarCode } = useBarcodeScanHandler()
const { checkUserPermission } = usePermissionHandler()
const { addDataToIndexDb, deleteDataInIndexDb } = useIndexDbHandler()

// Store Refs
const { picklistJob, picklistItems, allItemsRetrieved } = storeToRefs(picklistStore)
const { userData } = storeToRefs(useUserStore())
const { users } = storeToRefs(useOptionStore())

// Local State
const jobId = computed(() => route.params.id || route.params.jobId)
const job = computed(() => picklistJob.value)
const filter = ref('all')
const barcodeInput = ref('')
const actionLoading = ref(false)
const scanning = ref(false)
const shippingEnabled = ref(false)
const showCompleteDialog = ref(false)
const showCancelDialog = ref(false)
const showAuditTrailModal = ref(false)
const editJob = ref(false)
const showItemDetailModal = ref(false)
const batchSheetComponent = ref(null)
const scanInput = ref(null)
const keyboardEnabled = ref(false)

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

const currentTarget = computed(() => {
  return picklistItems.value.find(i => i.status === 'PickList')
})

const menuOptions = computed(() => [
  {
    label: 'Assign User',
    icon: 'person_add',
    color: 'grey',
    hidden: !checkUserPermission('can_assign_jobs'),
    disabled: editJob.value || job.value?.status === 'Paused' || job.value?.status === 'Completed',
    action: () => {
      editJob.value = true
    }
  },
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
      assigned_user_id: userData.value.user_id,
      run_timestamp: currentIsoDate()
    }
    await picklistStore.patchPicklistJob(payload)
    saveState()
  } catch (error) {
    notify({
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
    notify({
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
    notify({
      type: 'negative',
      message: 'Failed to resume job'
    })
  } finally {
    actionLoading.value = false
  }
}

const handleInputClick = () => {
  if (!keyboardEnabled.value) {
    keyboardEnabled.value = true
    nextTick(() => {
      scanInput.value?.focus()
    })
  }
}

const handleInputBlur = () => {
  keyboardEnabled.value = false
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
    notify({
      type: 'negative',
      message: 'Item not in this pick list'
    })
    barcodeInput.value = ''
    scanning.value = false
    return
  }

  if (item.status !== 'PickList') {
    notify({
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

    notify({
      type: 'positive',
      message: 'Item retrieved',
      position: 'top',
      timeout: 1000
    })
  } catch (error) {
    notify({
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
    notify({
      type: 'info',
      message: 'Item reverted to queue'
    })
    saveState()
  } catch (error) {
    notify({
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
    notify({
      type: 'negative',
      message: 'Failed to complete job'
    })
  } finally {
    actionLoading.value = false
    showCompleteDialog.value = false
  }
}

const updateUserAssignment = async () => {
  try {
    actionLoading.value = true
    const payload = {
      id: job.value.id,
      assigned_user_id: job.value.assigned_user_id,
      run_timestamp: new Date().toISOString()
    }
    await picklistStore.patchPicklistJob(payload)

    notify({
      type: 'positive',
      message: 'User assigned successfully'
    })
    editJob.value = false
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to assign user'
    })
  } finally {
    actionLoading.value = false
  }
}

const cancelJob = async () => {
  actionLoading.value = true
  try {
    await picklistStore.deletePicklistJob(jobId.value)
    deleteDataInIndexDb('picklistStore', 'activeJob')
    router.push({ name: 'picklist' })
  } catch (error) {
    notify({
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

  try {
    const setting = await useOptionStore().getSystemSetting('shipping_module_enabled')
    shippingEnabled.value = setting.value === 'true'
  } catch (e) {
    // Default to false
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
