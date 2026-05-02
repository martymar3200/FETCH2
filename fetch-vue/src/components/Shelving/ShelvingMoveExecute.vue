<template>
  <div class="shelving-move-execute">
    <!-- Header using shared component -->
    <JobPageHeader
      :title="moveType === 'tray-item' ? 'Move Items to Tray' : 'Move Containers to Shelf'"
      :job-id="job?.id"
      :status="job?.status"
      :status-color="getStatusColor(job?.status)"
      :subtitle="subtitle"
      :menu-options="menuOptions"
    >
      <template #actions>
        <JobActionButtons
          v-if="job?.status !== 'Completed'"
          :status="job?.status || 'Created'"
          :can-complete="containers.length > 0"
          :loading="actionLoading"
          @start="startJob"
          @complete="showCompleteDialog = true"
        />
      </template>
    </JobPageHeader>

    <!-- Progress Bar -->
    <JobProgressBar
      :completed="movedCount"
      :total="containers.length"
    />

    <!-- Not Started Message -->
    <q-card
      v-if="job?.status === 'Created' || job?.status === 'Assigned'"
      class="q-mb-lg"
    >
      <q-card-section class="text-center q-pa-lg">
        <q-icon
          name="swap_horiz"
          size="64px"
          color="accent"
          class="q-mb-md"
        />
        <div class="text-h6 q-mb-sm">
          Ready to Start
        </div>
        <p class="text-grey-7">
          Click "Start Job" to begin moving containers.
        </p>
      </q-card-section>
    </q-card>

    <!-- Destination Scan Card (only when job is Running) -->
    <q-card
      v-if="job?.status === 'Running'"
      class="q-mb-lg"
    >
      <q-card-section>
        <div class="row q-col-gutter-md">
          <!-- Destination Scan Section -->
          <div class="col-12 col-md-6">
            <label class="form-group-label">
              {{ moveType === 'tray-item' ? 'Destination Tray' : 'Destination Shelf' }}
            </label>
            <div
              v-if="currentDestination"
              class="text-h5 text-weight-bold text-accent"
            >
              {{ currentDestination }}
            </div>
            <q-input
              v-else
              v-model="destinationBarcodeInput"
              outlined
              dense
              :placeholder="moveType === 'tray-item' ? 'Scan tray barcode' : 'Scan shelf barcode'"
              @keyup.enter="scanDestination"
              ref="destinationInput"
              autofocus
            >
              <template #append>
                <q-icon :name="moveType === 'tray-item' ? 'inbox' : 'shelves'" />
              </template>
            </q-input>
          </div>

          <!-- Destination Details -->
          <div
            v-if="currentDestination"
            class="col-12 col-md-6"
          >
            <div class="row q-gutter-md">
              <div class="col">
                <div class="text-caption text-grey-6">
                  Owner
                </div>
                <div class="text-body1">
                  {{ destinationOwner || '-' }}
                </div>
              </div>
              <div class="col">
                <div class="text-caption text-grey-6">
                  Size Class
                </div>
                <div class="text-body1">
                  {{ destinationSizeClass || '-' }}
                </div>
              </div>
              <div class="col-auto">
                <BaseButton
                  flat
                  no-caps
                  color="grey-7"
                  :label="moveType === 'tray-item' ? 'Change Tray' : 'Change Shelf'"
                  icon="refresh"
                  @click="clearDestination"
                />
              </div>
            </div>
          </div>
        </div>
      </q-card-section>
    </q-card>

    <!-- Container Scan Section (only when destination is selected) -->
    <q-card
      v-if="currentDestination && job?.status === 'Running'"
      class="q-mb-lg bg-accent-1"
    >
      <q-card-section>
        <div class="row q-col-gutter-md items-end">
          <div :class="moveType === 'tray-item' ? 'col-12 col-md-9' : 'col-12 col-md-6'">
            <label class="form-group-label">
              {{ moveType === 'tray-item' ? 'Scan Item Barcode' : 'Scan Container Barcode' }}
            </label>
            <q-input
              v-model="containerBarcodeInput"
              outlined
              dense
              :placeholder="moveType === 'tray-item' ? 'Scan item barcode' : 'Scan tray or non-tray barcode'"
              @keyup.enter="scanContainer"
              ref="containerInput"
            >
              <template #append>
                <q-icon name="qr_code_scanner" />
              </template>
            </q-input>
          </div>
          <div
            v-if="moveType !== 'tray-item'"
            class="col-12 col-md-3"
          >
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
            <BaseButton
              no-caps
              unelevated
              color="accent"
              label="Move"
              class="full-width"
              :loading="scanning"
              :disable="!containerBarcodeInput || (moveType !== 'tray-item' && !positionNumber)"
              @click="moveContainer"
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
              Containers to Move
            </div>
          </div>
          <div class="col-auto">
            <q-badge
              color="accent"
              :label="`${movedCount}/${containers.length}`"
            />
          </div>
        </div>

        <q-table
          v-if="containers.length > 0"
          :rows="containers"
          :columns="containerColumns"
          row-key="barcode"
          flat
          dense
          :pagination="{ rowsPerPage: 15 }"
          class="essential-table"
        >
          <template #body-cell-barcode="props">
            <q-td :props="props">
              <span class="text-weight-medium">{{ props.row.barcode?.value || props.row.barcode || '-' }}</span>
            </q-td>
          </template>
          <template #body-cell-status="props">
            <q-td :props="props">
              <q-badge
                :color="props.row.scanned_for_transfer ? 'positive' : 'grey'"
                :label="props.row.scanned_for_transfer ? 'Moved' : 'Pending'"
              />
            </q-td>
          </template>
          <template #body-cell-actions="props">
            <q-td :props="props">
              <BaseButton
                v-if="!props.row.scanned_for_transfer"
                flat
                round
                size="sm"
                icon="delete"
                color="negative"
                @click="removeContainer(props.row)"
              />
            </q-td>
          </template>
        </q-table>

        <div
          v-else
          class="text-center text-grey-6 q-py-lg"
        >
          <q-icon
            name="inventory_2"
            size="48px"
            class="q-mb-sm"
          />
          <div>No containers scanned yet</div>
        </div>
      </q-card-section>
    </q-card>

    <!-- Complete Job Dialog -->
    <JobConfirmDialog
      v-model="showCompleteDialog"
      title="Complete Move Job?"
      message="Are you sure you want to complete this move job?"
      confirm-label="Complete"
      confirm-color="positive"
      :loading="completing"
      @confirm="completeJob"
    />

    <!-- Cancel Job Dialog -->
    <JobConfirmDialog
      v-model="showCancelDialog"
      title="Cancel Move Job?"
      message="Are you sure you want to cancel this move job?"
      warning="Any pending moves will need to be re-done."
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
  </div>
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
import { ref, computed, onMounted, watch, nextTick, inject } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useShelvingStore } from '@/stores/shelving-store'
import { useGlobalStore } from '@/stores/global-store'
import { useBarcodeStore } from '@/stores/barcode-store'
import { storeToRefs } from 'pinia'
import { notify } from '@/utils/notify'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import { useIndexDbHandler } from '@/composables/useIndexDbHandler.js'

// Shared Job Components
import JobPageHeader from '@/components/Job/JobPageHeader.vue'
import JobProgressBar from '@/components/Job/JobProgressBar.vue'
import JobActionButtons from '@/components/Job/JobActionButtons.vue'
import JobConfirmDialog from '@/components/Job/JobConfirmDialog.vue'
import AuditTrail from '@/components/AuditTrail.vue'

const router = useRouter()
const route = useRoute()
const shelvingStore = useShelvingStore()
const barcodeStore = useBarcodeStore()


// Composables
const { compiledBarCode } = useBarcodeScanHandler()
const { checkUserPermission } = usePermissionHandler()
const { addDataToIndexDb, getDataInIndexDb, deleteDataInIndexDb } = useIndexDbHandler()



// Store refs
const { appIsLoadingData } = storeToRefs(useGlobalStore())
const { shelvingJob } = storeToRefs(shelvingStore)
const {
  patchShelvingJob,
  postShelvingJobContainer,
  getShelfByBarcode,
  getShelvingTrayContainerDetails,
  getShelvingTrayItemDetails,
  getShelvingNonTrayItemDetails,
  getShelvingJob
} = shelvingStore
const { verifyBarcode, getBarcodeDetails } = barcodeStore

// Injected helpers
const currentIsoDate = inject('current-iso-date')
const getItemLocation = inject('get-item-location')

// Props from route
const moveType = computed(() => route.params.type || 'tray-non-tray')

// Local State
const job = computed(() => shelvingJob.value)
const containers = ref([])
const destinationBarcodeInput = ref('')
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

// Destination state
const currentDestination = ref('')
const currentDestinationLocation = ref('')
const destinationOwner = ref('')
const destinationSizeClass = ref('')
const destinationId = ref(null)
const nextPosition = ref(null)

// Input refs
const destinationInput = ref(null)
const containerInput = ref(null)

// Computed
const movedCount = computed(() => containers.value.filter(c => c.scanned_for_transfer).length)
const subtitle = computed(() => {
  const parts = []
  if (destinationOwner.value) {
    parts.push(destinationOwner.value)
  }
  if (destinationSizeClass.value) {
    parts.push(destinationSizeClass.value)
  }
  parts.push(`${movedCount.value}/${containers.value.length} moved`)
  return parts.join(' • ')
})

const menuOptions = computed(() => {
  const options = []

  options.push({
    label: 'View History',
    icon: 'history',
    color: 'grey',
    action: () => viewHistory()
  })

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

const containerColumns = computed(() => {
  const cols = [
    {
      name: 'barcode',
      label: 'Barcode',
      field: row => row.barcode?.value || row.barcode,
      align: 'left',
      sortable: true
    },
    {
      name: 'owner',
      label: 'Owner',
      field: row => row.owner?.name || '-',
      align: 'left'
    }
  ]

  if (moveType.value !== 'tray-item') {
    cols.push({
      name: 'location',
      label: 'Shelving Location',
      field: row => row.display_location || '-',
      align: 'left'
    })
  }

  cols.push(
    {
      name: 'status',
      label: 'Status',
      field: 'scanned_for_transfer',
      align: 'center'
    },
    {
      name: 'actions',
      label: '',
      align: 'center'
    }
  )

  return cols
})

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
    notify({
      type: 'positive',
      message: 'Move job started!'
    })
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

const viewHistory = () => {
  showAuditTrailModal.value = true
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
      message: 'Move job cancelled'
    })
    deleteDataInIndexDb('shelvingStore', 'moveJob')
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

const scanDestination = async () => {
  if (!destinationBarcodeInput.value) {
    return
  }

  if (!checkUserPermission('can_move_trays_and_items_shelving_locations')) {
    notify({
      type: 'negative',
      message: 'Permission denied'
    })
    return
  }

  try {
    appIsLoadingData.value = true
    scanError.value = ''

    if (moveType.value === 'tray-item') {
      // Scanning a tray as destination
      const res = await getShelvingTrayContainerDetails(destinationBarcodeInput.value)
      currentDestination.value = res.data.barcode.value
      destinationOwner.value = res.data.owner?.name || ''
      destinationSizeClass.value = res.data.size_class?.name || ''
      destinationId.value = res.data.id
    } else {
      // Scanning a shelf as destination
      const res = await getShelfByBarcode(destinationBarcodeInput.value)
      currentDestination.value = res.data.barcode.value
      currentDestinationLocation.value = res.data.location
      destinationOwner.value = res.data.owner?.name || ''
      destinationSizeClass.value = res.data.shelf_type?.size_class?.name || ''
      destinationId.value = res.data.id
      nextPosition.value = shelvingJob.value.nextAvailablePosition
    }

    destinationBarcodeInput.value = ''
    saveState()

    nextTick(() => {
      containerInput.value?.focus()
    })
  } catch (error) {
    notify({
      type: 'negative',
      message: error?.response?.data?.detail || 'Failed to scan destination'
    })
  } finally {
    appIsLoadingData.value = false
  }
}

const clearDestination = () => {
  currentDestination.value = ''
  currentDestinationLocation.value = ''
  destinationOwner.value = ''
  destinationSizeClass.value = ''
  destinationId.value = null
  nextPosition.value = null
  saveState()
}

const scanContainer = async () => {
  if (!containerBarcodeInput.value) {
    return
  }

  // Check if already in list
  if (containers.value.some(c => (c.barcode?.value || c.barcode) === containerBarcodeInput.value)) {
    scanError.value = 'Container already added'
    return
  }

  if (moveType.value !== 'tray-item' && !positionNumber.value) {
    scanError.value = 'Position required'
    return
  }

  try {
    appIsLoadingData.value = true
    scanError.value = ''

    // Verify barcode type
    if (moveType.value === 'tray-item') {
      await verifyBarcode(containerBarcodeInput.value, 'Item')
    } else {
      await verifyBarcode(containerBarcodeInput.value, [
        'Item',
        'Tray'
      ])
    }

    // Get container details
    let containerData = null
    const barcodeInfo = await getBarcodeDetails(containerBarcodeInput.value)

    if (moveType.value === 'tray-item') {
      const res = await getShelvingTrayItemDetails(containerBarcodeInput.value)
      containerData = {
        ...res.data,
        barcode: res.data.barcode,
        owner: res.data.owner,
        scanned_for_transfer: false,
        destination_tray_barcode: currentDestination.value
      }
    } else {
      // Compute display location based on current destination and position
      const displayLocation = getItemLocation({
        shelf_position: {
          location: `${currentDestinationLocation.value}-${positionNumber.value}`
        }
      })

      if (barcodeInfo.data.type.name === 'Tray') {
        const res = await getShelvingTrayContainerDetails(containerBarcodeInput.value)
        containerData = {
          ...res.data,
          barcode: res.data.barcode,
          owner: res.data.owner,
          size_class: res.data.size_class,
          container_type: { type: 'Tray' },
          scanned_for_transfer: false,
          new_shelf_position: positionNumber.value,
          display_location: displayLocation
        }
      } else {
        const res = await getShelvingNonTrayItemDetails(containerBarcodeInput.value)
        containerData = {
          ...res.data,
          barcode: res.data.barcode,
          owner: res.data.owner,
          size_class: res.data.size_class,
          container_type: { type: 'Non-Tray' },
          scanned_for_transfer: false,
          new_shelf_position: positionNumber.value,
          display_location: displayLocation
        }
      }
    }

    containers.value.push(containerData)
    containerBarcodeInput.value = ''

    // Auto-increment position for shelf moves
    if (moveType.value !== 'tray-item' && positionNumber.value) {
      positionNumber.value = positionNumber.value + 1
    }

    saveState()

    nextTick(() => {
      containerInput.value?.focus()
    })
  } catch (error) {
    scanError.value = error?.response?.data?.detail || 'Failed to scan container'
  } finally {
    appIsLoadingData.value = false
  }
}

const removeContainer = (container) => {
  const barcode = container.barcode?.value || container.barcode
  containers.value = containers.value.filter(c => (c.barcode?.value || c.barcode) !== barcode)
  saveState()
}

const moveContainer = async () => {
  if (!containerBarcodeInput.value) {
    return
  }

  if (moveType.value !== 'tray-item' && !positionNumber.value) {
    scanError.value = 'Position required'
    return
  }

  // First add to list
  await scanContainer()
}

const completeJob = async () => {
  completing.value = true
  try {
    // Process all moves via backend
    for (const container of containers.value) {
      if (!container.scanned_for_transfer) {
        const payload = {
          job_id: job.value.id,
          container_barcode_value: container.barcode?.value || container.barcode,
          scanned_for_shelving: true,
          shelved_dt: currentIsoDate()
        }

        if (moveType.value === 'tray-item') {
          payload.destination_tray_barcode_value = currentDestination.value
        } else {
          payload.shelf_barcode_value = currentDestination.value
          payload.shelf_position_number = parseInt(container.new_shelf_position)
          payload.trayed = container.container_type?.type === 'Tray'
        }

        await postShelvingJobContainer(payload)
        container.scanned_for_transfer = true
      }
    }

    await patchShelvingJob({
      id: job.value.id,
      status: 'Completed',
      run_timestamp: currentIsoDate()
    })

    showCompleteDialog.value = false
    notify({
      type: 'positive',
      message: 'Move job completed!'
    })
    deleteDataInIndexDb('shelvingStore', 'moveJob')
    router.push({ name: 'shelving' })
  } catch (error) {
    notify({
      type: 'negative',
      message: error?.response?.data?.detail || 'Failed to complete job'
    })
  } finally {
    completing.value = false
  }
}

const saveState = () => {
  const state = {
    jobId: job.value?.id,
    containers: JSON.parse(JSON.stringify(containers.value)),
    destination: currentDestination.value,
    destinationLocation: currentDestinationLocation.value,
    destinationOwner: destinationOwner.value,
    destinationSizeClass: destinationSizeClass.value,
    destinationId: destinationId.value,
    nextPosition: nextPosition.value
  }
  addDataToIndexDb('shelvingStore', 'moveJob', state)
}

const loadState = async () => {
  const res = await getDataInIndexDb('shelvingStore')
  // Use loose equality to handle string/number mismatch
  if (res?.data?.moveJob && res.data.moveJob.jobId == job.value?.id) {
    containers.value = res.data.moveJob.containers || []
    currentDestination.value = res.data.moveJob.destination || ''
    currentDestinationLocation.value = res.data.moveJob.destinationLocation || ''
    destinationOwner.value = res.data.moveJob.destinationOwner || ''
    destinationSizeClass.value = res.data.moveJob.destinationSizeClass || ''
    destinationId.value = res.data.moveJob.destinationId || null
    nextPosition.value = res.data.moveJob.nextPosition || null
  }
}

// Watch for barcode scans
watch(compiledBarCode, (barcode) => {
  if (!barcode || job.value?.status !== 'Running') {
    return
  }

  if (!currentDestination.value) {
    destinationBarcodeInput.value = barcode
    scanDestination()
  } else {
    containerBarcodeInput.value = barcode
    scanContainer()
  }
})

// Initialize
onMounted(async () => {
  // Load job data if jobId is provided
  if (route.params.jobId) {
    try {
      appIsLoadingData.value = true
      await getShelvingJob(route.params.jobId)

      // Load containers from job data for completed/resumed jobs
      if (shelvingJob.value?.shelving_job_containers?.length > 0) {
        containers.value = shelvingJob.value.shelving_job_containers.map(c => ({
          id: c.id,
          barcode: c.tray?.barcode || c.non_tray_item?.barcode || c.item?.barcode || { value: c.container_barcode },
          owner: c.tray?.owner || c.non_tray_item?.owner || c.item?.tray?.owner,
          size_class: c.tray?.size_class || c.non_tray_item?.size_class,
          container_type: c.tray ? { type: 'Tray' } : c.non_tray_item ? { type: 'Non-Tray' } : { type: 'Item' },
          new_shelf_position: c.shelf_position_number,
          scanned_for_transfer: true,
          destination_tray_barcode: c.destination_tray?.barcode?.value,
          display_location: c.actual_shelf_position ? getItemLocation(c.actual_shelf_position) : '-'
        }))

        // Infer destination from first container for resumed/completed jobs
        const first = shelvingJob.value.shelving_job_containers[0]
        if (moveType.value === 'tray-item') {
          if (first.destination_tray) {
            currentDestination.value = first.destination_tray.barcode?.value
            destinationOwner.value = first.destination_tray.owner?.name
            destinationSizeClass.value = first.destination_tray.size_class?.name
          }
        } else {
          if (first.actual_shelf_position) {
            currentDestination.value = first.actual_shelf_position.shelf.barcode?.value
            currentDestinationLocation.value = first.actual_shelf_position.shelf.location // Assumption: shelf object has location from API
            destinationOwner.value = first.actual_shelf_position.shelf.owner?.name
            // We don't have shelf size class directly in the nested schema yet, but owner is good
          }
        }
      }
    } catch (error) {
      notify({
        type: 'negative',
        message: 'Failed to load move job'
      })
      router.push({ name: 'shelving' })
      return
    } finally {
      appIsLoadingData.value = false
    }
  }

  // Only load local state if no containers loaded from backend
  if (containers.value.length === 0) {
    await loadState()
  }

  nextTick(() => {
    if (!currentDestination.value) {
      destinationInput.value?.focus()
    } else {
      containerInput.value?.focus()
    }
  })
})
</script>


