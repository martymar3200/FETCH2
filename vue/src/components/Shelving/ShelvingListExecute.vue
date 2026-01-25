<template>
  <div class="shelving-list-execute">
    <!-- Header -->
    <div class="row q-mb-lg items-center">
      <div class="col">
        <div class="row items-center">
          <!-- Three-dot menu - left of title -->
          <q-btn
            flat
            round
            dense
            icon="more_vert"
            class="q-mr-sm"
          >
            <q-menu>
              <q-list style="min-width: 150px">
                <!-- View History -->
                <q-item
                  clickable
                  v-close-popup
                  @click="showAuditTrailModal = true"
                >
                  <q-item-section avatar>
                    <q-icon
                      name="history"
                      color="grey"
                    />
                  </q-item-section>
                  <q-item-section>View History</q-item-section>
                </q-item>
                <!-- Print Job -->
                <q-item
                  clickable
                  v-close-popup
                  @click="printJob"
                >
                  <q-item-section avatar>
                    <q-icon
                      name="print"
                      color="grey"
                    />
                  </q-item-section>
                  <q-item-section>Print Job</q-item-section>
                </q-item>
                <!-- Cancel Job -->
                <q-item
                  v-if="job?.status === 'Created'"
                  clickable
                  v-close-popup
                  @click="showCancelDialog = true"
                >
                  <q-item-section avatar>
                    <q-icon
                      name="cancel"
                      color="negative"
                    />
                  </q-item-section>
                  <q-item-section>Cancel Job</q-item-section>
                </q-item>
              </q-list>
            </q-menu>
          </q-btn>
          <h1 class="text-h4 text-bold q-mb-none">
            Shelving Job #{{ jobId }}
            <q-badge
              :color="getStatusColor(job?.status)"
              :label="job?.status"
              class="q-ml-sm"
            />
          </h1>
        </div>
        <p class="text-grey-7 q-mb-none">
          {{ job?.mode === 'PreAssigned' ? 'Pre-Assigned Mode' : 'Manual Mode' }} •
          {{ shelvedCount }}/{{ totalCount }} shelved
        </p>
      </div>
      <div class="col-auto">
        <q-btn
          v-if="job?.status === 'Created'"
          no-caps
          unelevated
          color="accent"
          label="Start Job"
          class="btn-modern q-mr-sm"
          @click="startJob"
        />
        <q-btn
          v-if="job?.status === 'Running'"
          no-caps
          flat
          color="warning"
          label="Pause"
          @click="pauseJob"
        />
        <q-btn
          v-if="job?.status === 'Paused'"
          no-caps
          unelevated
          color="accent"
          label="Resume"
          class="btn-modern"
          @click="resumeJob"
        />
        <q-btn
          v-if="allShelved && job?.status === 'Running'"
          no-caps
          unelevated
          color="positive"
          label="Complete Job"
          class="btn-modern q-ml-sm"
          @click="completeJob"
        />
      </div>
    </div>

    <!-- Progress Bar -->
    <q-linear-progress
      :value="progressValue"
      color="accent"
      size="12px"
      class="q-mb-lg rounded-borders"
    />

    <!-- Scan Section -->
    <q-card
      v-if="job?.status === 'Running'"
      class="q-mb-lg"
    >
      <q-card-section>
        <div class="row q-col-gutter-md items-end">
          <!-- Manual Mode: Scan shelf first -->
          <template v-if="job?.mode === 'Manual'">
            <div class="col-12 col-md-4">
              <label class="form-group-label">1. Scan Shelf Barcode</label>
              <q-input
                v-model="shelfBarcode"
                outlined
                dense
                placeholder="Scan shelf barcode"
                @keyup.enter="scanShelf"
                ref="shelfInput"
              >
                <template #append>
                  <q-icon name="shelves" />
                </template>
              </q-input>
            </div>
            <div class="col-12 col-md-2">
              <label class="form-group-label">Position #</label>
              <q-input
                v-model.number="positionNumber"
                type="number"
                outlined
                dense
                min="1"
              />
            </div>
          </template>

          <!-- Container Scan -->
          <div
            class="col-12"
            :class="job?.mode === 'Manual' ? 'col-md-4' : 'col-md-8'"
          >
            <label class="form-group-label">
              {{ job?.mode === 'Manual' ? '2. Scan Container Barcode' : 'Scan Container Barcode' }}
            </label>
            <q-input
              v-model="containerBarcode"
              outlined
              dense
              placeholder="Scan container barcode"
              @keyup.enter="scanAndShelve"
              ref="containerInput"
            >
              <template #append>
                <q-icon name="qr_code_scanner" />
              </template>
            </q-input>
          </div>

          <div class="col-12 col-md-2">
            <q-btn
              no-caps
              unelevated
              color="accent"
              :label="job?.mode === 'Manual' ? 'Shelve' : 'Scan'"
              class="btn-modern full-width"
              :loading="scanning"
              @click="scanAndShelve"
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

    <!-- Current Container (Pre-Assigned Mode) -->
    <q-card
      v-if="currentContainer && job?.mode === 'PreAssigned'"
      class="q-mb-lg bg-accent-1"
    >
      <q-card-section>
        <div class="row q-col-gutter-md">
          <!-- Scanned Container Info -->
          <div class="col-12 col-md-6">
            <div class="text-caption text-grey-6">
              Scanned Container
            </div>
            <div class="text-h5 text-weight-medium">
              {{ currentContainer.barcode?.value }}
            </div>
            <div class="text-body2 text-grey-7">
              {{ currentContainer.container_type }} • {{ currentContainer.owner?.name || 'No Owner' }}
            </div>
          </div>

          <!-- Proposed Location -->
          <div class="col-12 col-md-6">
            <div class="text-caption text-grey-6">
              Shelve Here
            </div>
            <div class="text-h5 text-weight-bold text-accent">
              {{ currentContainer.proposed_location || 'Not Assigned' }}
            </div>
          </div>

          <!-- Shelf Barcode Scan -->
          <div class="col-12">
            <div class="row q-col-gutter-md items-end">
              <div class="col-12 col-md-8">
                <label class="form-group-label">Scan Shelf Barcode to Confirm</label>
                <q-input
                  v-model="confirmShelfBarcode"
                  outlined
                  dense
                  placeholder="Scan shelf barcode"
                  @keyup.enter="confirmShelveWithScan"
                  ref="confirmShelfInput"
                  autofocus
                >
                  <template #append>
                    <q-icon name="shelves" />
                  </template>
                </q-input>
              </div>
              <div class="col-12 col-md-4">
                <q-btn
                  no-caps
                  flat
                  color="grey-7"
                  label="Use Different Location"
                  icon="edit_location"
                  class="full-width"
                  @click="showOverrideDialog = true"
                />
              </div>
            </div>
          </div>
        </div>
      </q-card-section>
    </q-card>

    <!-- Container List -->
    <q-card>
      <q-card-section>
        <div class="row items-center q-mb-md">
          <div class="col">
            <div class="text-h6">
              Containers
            </div>
          </div>
          <div class="col-auto">
            <q-btn-toggle
              v-model="containerFilter"
              no-caps
              rounded
              unelevated
              toggle-color="accent"
              :options="[
                { label: 'All', value: 'all' },
                { label: 'Pending', value: 'Pending' },
                { label: 'Assigned', value: 'Assigned' },
                { label: 'Shelved', value: 'Shelved' }
              ]"
            />
          </div>
        </div>

        <q-table
          :rows="filteredContainers"
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
                :color="getStatusColor(props.row.status)"
                :label="props.row.status"
              />
              <q-icon
                v-if="props.row.was_overridden"
                name="published_with_changes"
                color="warning"
                size="sm"
                class="q-ml-xs"
              >
                <q-tooltip>Location was overridden</q-tooltip>
              </q-icon>
            </q-td>
          </template>
          <template #body-cell-actions="props">
            <q-td
              :props="props"
              class="text-right"
            >
              <q-btn
                v-if="props.row.status !== 'Shelved'"
                flat
                dense
                round
                icon="edit_location"
                color="grey-7"
                size="sm"
                @click="openOverride(props.row)"
              >
                <q-tooltip>Override location</q-tooltip>
              </q-btn>
            </q-td>
          </template>
        </q-table>
      </q-card-section>
    </q-card>

    <!-- Override Location Dialog -->
    <q-dialog v-model="showOverrideDialog">
      <q-card style="min-width: 400px">
        <q-card-section>
          <div class="text-h6">
            Override Location
          </div>
          <div class="text-caption text-grey-7">
            Container: {{ overrideContainer?.barcode?.value }}
          </div>
        </q-card-section>
        <q-card-section>
          <div class="form-group q-mb-md">
            <label class="form-group-label">Shelf Barcode</label>
            <q-input
              v-model="overrideShelfBarcode"
              outlined
              dense
              placeholder="Scan or type shelf barcode"
            />
          </div>
          <div class="form-group q-mb-md">
            <label class="form-group-label">Position Number</label>
            <q-input
              v-model.number="overridePosition"
              type="number"
              outlined
              dense
              min="1"
            />
          </div>
          <div class="form-group">
            <label class="form-group-label">Reason (optional)</label>
            <q-input
              v-model="overrideReason"
              outlined
              dense
              placeholder="e.g., Position occupied"
            />
          </div>
        </q-card-section>
        <q-card-actions align="right">
          <q-btn
            flat
            label="Cancel"
            color="grey"
            v-close-popup
          />
          <q-btn
            unelevated
            label="Confirm Override"
            color="accent"
            :loading="overriding"
            @click="confirmOverride"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <!-- Cancel Job Confirmation Dialog -->
    <q-dialog v-model="showCancelDialog">
      <q-card style="min-width: 350px">
        <q-card-section>
          <div class="text-h6">
            Cancel Shelving Job?
          </div>
        </q-card-section>
        <q-card-section class="q-pt-none">
          <p>Are you sure you want to cancel this job?</p>
          <p class="text-negative">
            All containers will be removed from this job and can be added to a new job.
          </p>
        </q-card-section>
        <q-card-actions align="right">
          <q-btn
            flat
            label="No, Keep Job"
            color="grey"
            v-close-popup
          />
          <q-btn
            unelevated
            label="Yes, Cancel Job"
            color="negative"
            :loading="cancelling"
            :disable="cancelling"
            @click="cancelJob"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <!-- Audit Trail Modal -->
    <AuditTrail
      v-if="showAuditTrailModal"
      ref="historyModal"
      @reset="showAuditTrailModal = false"
      job-type="shelving_jobs"
      :job-id="jobId"
    />

    <!-- Print Component -->
    <ShelvingBatchSheet
      ref="batchSheetComponent"
      :shelving-job-details="job"
      :containers="containers"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useShelvingStore } from '@/stores/shelving-store'
import { useQuasar } from 'quasar'
import AuditTrail from '@/components/AuditTrail.vue'
import ShelvingBatchSheet from '@/components/Shelving/ShelvingBatchSheet.vue'

const route = useRoute()
const router = useRouter()
const shelvingStore = useShelvingStore()
const $q = useQuasar()

// Props
const jobId = computed(() => route.params.id)

// State
const job = ref(null)
const containers = ref([])
const containerFilter = ref('all')

// Scanning
const shelfBarcode = ref('')
const positionNumber = ref(1)
const containerBarcode = ref('')
const scanning = ref(false)
const scanError = ref('')
const currentContainer = ref(null)
const confirmShelfBarcode = ref('')

// Input refs for auto-focus
const containerInput = ref(null)
const confirmShelfInput = ref(null)

// Override
const showOverrideDialog = ref(false)
const overrideContainer = ref(null)
const overrideShelfBarcode = ref('')
const overridePosition = ref(1)
const overrideReason = ref('')
const overriding = ref(false)

// Cancel
const showCancelDialog = ref(false)
const cancelling = ref(false)
const showAuditTrailModal = ref(false)
const batchSheetComponent = ref(null)

// Computed
const filteredContainers = computed(() => {
  if (containerFilter.value === 'all') {
    return containers.value
  }
  return containers.value.filter(c => c.status === containerFilter.value)
})

const totalCount = computed(() => containers.value.length)
const shelvedCount = computed(() => containers.value.filter(c => c.status === 'Shelved').length)
const progressValue = computed(() => totalCount.value > 0 ? shelvedCount.value / totalCount.value : 0)
const allShelved = computed(() => totalCount.value > 0 && shelvedCount.value === totalCount.value)

const containerColumns = [
  {
    name: 'barcode',
    label: 'Barcode',
    field: 'barcode',
    align: 'left',
    sortable: true
  },
  {
    name: 'container_type',
    label: 'Type',
    field: 'container_type',
    align: 'left'
  },
  {
    name: 'owner',
    label: 'Owner',
    field: row => row.owner?.name || '-',
    align: 'left'
  },
  {
    name: 'proposed_location',
    label: 'Proposed',
    field: 'proposed_location',
    align: 'left'
  },
  {
    name: 'actual_location',
    label: 'Actual',
    field: 'actual_location',
    align: 'left'
  },
  {
    name: 'status',
    label: 'Status',
    field: 'status',
    align: 'center'
  },
  {
    name: 'actions',
    label: '',
    field: 'actions',
    align: 'right'
  }
]

// Methods
const getStatusColor = (status) => {
  const colors = {
    Created: 'grey',
    Running: 'info',
    Paused: 'warning',
    Completed: 'positive',
    Pending: 'grey',
    Assigned: 'info',
    Unassigned: 'warning',
    Shelved: 'positive',
    Error: 'negative'
  }
  return colors[status] || 'grey'
}

const loadJob = async () => {
  try {
    await shelvingStore.getShelvingJob(jobId.value)
    job.value = shelvingStore.shelvingJob
    containers.value = await shelvingStore.getShelveByListContainers(jobId.value)
  } catch (error) {
    $q.notify({
      type: 'negative',
      message: 'Failed to load job'
    })
  }
}

const startJob = async () => {
  try {
    await shelvingStore.patchShelvingJob({
      id: jobId.value,
      status: 'Running'
    })
    job.value = shelvingStore.shelvingJob
  } catch (error) {
    $q.notify({
      type: 'negative',
      message: 'Failed to start job'
    })
  }
}

const pauseJob = async () => {
  try {
    await shelvingStore.patchShelvingJob({
      id: jobId.value,
      status: 'Paused'
    })
    job.value = shelvingStore.shelvingJob
  } catch (error) {
    $q.notify({
      type: 'negative',
      message: 'Failed to pause job'
    })
  }
}

const resumeJob = async () => {
  try {
    await shelvingStore.patchShelvingJob({
      id: jobId.value,
      status: 'Running'
    })
    job.value = shelvingStore.shelvingJob
  } catch (error) {
    $q.notify({
      type: 'negative',
      message: 'Failed to resume job'
    })
  }
}

const completeJob = async () => {
  let success = false
  try {
    await shelvingStore.patchShelvingJob({
      id: jobId.value,
      status: 'Completed'
    })
    success = true
  } catch (error) {
    $q.notify({
      type: 'negative',
      message: error.response?.data?.detail || 'Failed to complete job'
    })
    return
  }

  if (success) {
    $q.notify({
      type: 'positive',
      message: 'Job completed!'
    })
    router.push({ name: 'shelving' })
  }
}

const printJob = () => {
  batchSheetComponent.value?.printBatchReport()
}

const cancelJob = async () => {
  console.log('cancelJob called')
  if (cancelling.value) {
    console.log('Already cancelling, returning')
    return
  }
  cancelling.value = true
  try {
    console.log('Calling cancelShelveByListJob...')
    await shelvingStore.cancelShelveByListJob(jobId.value)
    console.log('Cancel succeeded, navigating...')
    cancelling.value = false
    showCancelDialog.value = false
    $q.notify({
      type: 'info',
      message: 'Job cancelled. Containers have been released.'
    })
    router.push({ name: 'shelving' })
  } catch (error) {
    console.log('Cancel failed:', error)
    cancelling.value = false
    $q.notify({
      type: 'negative',
      message: error.response?.data?.detail || 'Failed to cancel job'
    })
  }
}

const scanAndShelve = async () => {
  if (!containerBarcode.value) {
    return
  }

  scanning.value = true
  scanError.value = ''

  try {
    // Scan the container
    const container = await shelvingStore.scanContainerForShelveByList(jobId.value, containerBarcode.value)

    if (job.value?.mode === 'PreAssigned') {
      // Show the proposed location
      currentContainer.value = container
      confirmShelfBarcode.value = ''
      // Focus on shelf barcode input for confirmation
      nextTick(() => {
        confirmShelfInput.value?.focus()
      })
    } else {
      // Manual mode - shelve immediately at scanned shelf
      if (!shelfBarcode.value) {
        scanError.value = 'Please scan shelf first'
        scanning.value = false
        return
      }

      // Confirm the shelve
      await shelvingStore.confirmContainerShelved(jobId.value, {
        container_id: container.id,
        shelf_position_id: null, // Backend will look up from shelf barcode
        override: true,
        override_reason: 'Manual mode'
      })

      // Update local state
      const idx = containers.value.findIndex(c => c.id === container.id)
      if (idx >= 0) {
        containers.value[idx].status = 'Shelved'
        containers.value[idx].actual_location = `${shelfBarcode.value}-${positionNumber.value}`
      }

      $q.notify({
        type: 'positive',
        message: 'Container shelved!'
      })
      containerBarcode.value = ''
    }
  } catch (error) {
    scanError.value = error.response?.data?.detail || 'Scan failed'
  } finally {
    scanning.value = false
  }
}

const confirmShelveWithScan = async () => {
  if (!currentContainer.value || !confirmShelfBarcode.value) {
    scanError.value = 'Please scan the shelf barcode'
    return
  }

  scanning.value = true
  scanError.value = ''

  try {
    // The backend will validate shelf barcode matches proposed location
    // or handle as override if different
    await shelvingStore.confirmContainerShelved(jobId.value, {
      container_id: currentContainer.value.id,
      shelf_barcode: confirmShelfBarcode.value,
      shelf_position_id: currentContainer.value.proposed_shelf_position_id,
      override: false
    })

    // Update local state
    const idx = containers.value.findIndex(c => c.id === currentContainer.value.id)
    if (idx >= 0) {
      containers.value[idx].status = 'Shelved'
      containers.value[idx].actual_location = currentContainer.value.proposed_location
    }

    currentContainer.value = null
    containerBarcode.value = ''
    confirmShelfBarcode.value = ''

    $q.notify({
      type: 'positive',
      message: 'Container shelved!'
    })

    // Focus back to container scan for next item
    setTimeout(() => {
      containerInput.value?.focus()
    }, 100)
  } catch (error) {
    scanError.value = error.response?.data?.detail || 'Failed to confirm - shelf barcode may not match'
  } finally {
    scanning.value = false
  }
}

const openOverride = (container) => {
  overrideContainer.value = container
  overrideShelfBarcode.value = ''
  overridePosition.value = 1
  overrideReason.value = ''
  showOverrideDialog.value = true
}

const confirmOverride = async () => {
  if (!overrideContainer.value || !overrideShelfBarcode.value) {
    return
  }

  overriding.value = true
  try {
    const updated = await shelvingStore.overrideContainerLocation(
      jobId.value,
      overrideContainer.value.id,
      {
        shelf_barcode: overrideShelfBarcode.value,
        shelf_position_number: overridePosition.value,
        reason: overrideReason.value
      }
    )

    // Update local state
    const idx = containers.value.findIndex(c => c.id === overrideContainer.value.id)
    if (idx >= 0) {
      containers.value[idx] = updated
    }

    if (currentContainer.value?.id === overrideContainer.value.id) {
      currentContainer.value = updated
    }

    showOverrideDialog.value = false
    $q.notify({
      type: 'positive',
      message: 'Location updated'
    })
  } catch (error) {
    $q.notify({
      type: 'negative',
      message: error.response?.data?.detail || 'Override failed'
    })
  } finally {
    overriding.value = false
  }
}

onMounted(async () => {
  await loadJob()
  // Auto-focus on container barcode input after load
  nextTick(() => {
    containerInput.value?.focus()
  })
})
</script>

<style scoped lang="scss">
.shelving-list-execute {
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
</style>
