<template>
  <div class="shelving-list-create">
    <!-- Header -->
    <div class="row q-mb-lg items-center">
      <div class="col">
        <h1 class="text-h4 text-bold q-mb-none">
          Create Shelve by List Job
        </h1>
        <p class="text-grey-7 q-mb-none">
          Build a list of containers and choose how to shelve them
        </p>
      </div>
      <div class="col-auto">
        <q-btn
          flat
          no-caps
          color="grey-7"
          label="Cancel"
          @click="handleCancel"
          class="q-mr-sm"
        />
      </div>
    </div>

    <!-- Step 1: Job Configuration -->
    <q-card class="q-mb-lg">
      <q-card-section>
        <div class="text-h6 q-mb-md">
          1. Job Configuration
        </div>

        <div class="row q-col-gutter-md">
          <!-- Building Selection -->
          <div class="col-12 col-md-6">
            <div class="form-group">
              <label class="form-group-label">Building *</label>
              <q-select
                v-model="jobConfig.building_id"
                :options="buildingOptions"
                option-value="id"
                option-label="name"
                emit-value
                map-options
                outlined
                dense
                :disable="jobCreated"
              />
            </div>
          </div>

          <!-- Mode Selection -->
          <div class="col-12 col-md-6">
            <div class="form-group">
              <label class="form-group-label">Shelving Mode *</label>
              <q-select
                v-model="jobConfig.mode"
                :options="modeOptions"
                outlined
                dense
                :disable="jobCreated"
              />
            </div>
          </div>

          <!-- Pre-Assignment Options (shown if PreAssigned mode) -->
          <div
            v-if="jobConfig.mode === 'PreAssigned'"
            class="col-12"
          >
            <div class="text-subtitle2 q-mb-sm">
              Pre-Assignment Options
            </div>
            <div class="row q-col-gutter-sm">
              <div class="col-auto">
                <q-checkbox
                  v-model="jobConfig.allow_unassigned_size"
                  label="Allow unassigned size class shelves"
                  :disable="jobCreated"
                />
              </div>
              <div class="col-auto">
                <q-checkbox
                  v-model="jobConfig.allow_unassigned_owner"
                  label="Allow unassigned owner shelves"
                  :disable="jobCreated"
                />
              </div>
              <div class="col-auto">
                <q-checkbox
                  v-model="jobConfig.allow_tiered_owner"
                  label="Allow tiered ownership (child → parent)"
                  :disable="jobCreated"
                />
              </div>
            </div>
          </div>
        </div>
      </q-card-section>
    </q-card>

    <!-- Step 2: Add Containers -->
    <q-card class="q-mb-lg">
      <q-card-section>
        <div class="text-h6 q-mb-md">
          2. Add Containers
        </div>

        <!-- Tab selection for adding method -->
        <q-tabs
          v-model="addMethod"
          class="q-mb-md"
          align="left"
          active-color="accent"
          indicator-color="accent"
        >
          <q-tab
            name="verification"
            label="From Verification Jobs"
          />
          <q-tab
            name="manual"
            label="Manual Scan"
          />
        </q-tabs>

        <!-- From Verification Jobs -->
        <div
          v-if="addMethod === 'verification'"
          class="q-pa-md bg-grey-1 rounded-borders"
        >
          <div class="row q-col-gutter-md items-end">
            <div class="col-12 col-md-8">
              <label class="form-group-label">Select Completed Verification Jobs</label>
              <q-select
                v-model="selectedVerificationJobs"
                :options="verificationJobOptions"
                option-value="id"
                option-label="label"
                emit-value
                map-options
                multiple
                outlined
                dense
                use-chips
                :loading="loadingVerificationJobs"
              />
            </div>
            <div class="col-12 col-md-4">
              <q-btn
                no-caps
                unelevated
                color="accent"
                label="Add from Jobs"
                class="btn-modern full-width"
                :disable="selectedVerificationJobs.length === 0"
                :loading="addingFromJobs"
                @click="addFromVerificationJobs"
              />
            </div>
          </div>
        </div>

        <!-- Manual Scan -->
        <div
          v-else
          class="q-pa-md bg-grey-1 rounded-borders"
        >
          <div class="row q-col-gutter-md items-end">
            <div class="col-12 col-md-8">
              <label class="form-group-label">Scan Container Barcode</label>
              <q-input
                v-model="manualBarcode"
                outlined
                dense
                placeholder="Scan or type barcode"
                @keyup.enter="addManualContainer"
                ref="barcodeInput"
              >
                <template #append>
                  <q-icon name="qr_code_scanner" />
                </template>
              </q-input>
            </div>
            <div class="col-12 col-md-4">
              <q-btn
                no-caps
                unelevated
                color="accent"
                label="Add Container"
                class="btn-modern full-width"
                :disable="!manualBarcode"
                :loading="addingContainer"
                @click="addManualContainer"
              />
            </div>
          </div>
          <div
            v-if="addError"
            class="text-negative q-mt-sm"
          >
            {{ addError }}
          </div>
        </div>
      </q-card-section>
    </q-card>

    <!-- Step 3: Container List -->
    <q-card class="q-mb-lg">
      <q-card-section>
        <div class="row items-center q-mb-md">
          <div class="col">
            <div class="text-h6">
              3. Container List
            </div>
          </div>
          <div class="col-auto">
            <q-badge
              color="accent"
              :label="`${containers.length} containers`"
            />
          </div>
        </div>

        <q-table
          :rows="containers"
          :columns="containerColumns"
          row-key="id"
          flat
          dense
          :pagination="{ rowsPerPage: 10 }"
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
            </q-td>
          </template>
          <template #body-cell-actions="props">
            <q-td
              :props="props"
              class="text-right"
            >
              <q-btn
                flat
                dense
                round
                icon="delete"
                color="negative"
                size="sm"
                :disable="props.row.status === 'Shelved'"
                @click="removeContainer(props.row.id)"
              >
                <q-tooltip>Remove from list</q-tooltip>
              </q-btn>
            </q-td>
          </template>
          <template #no-data>
            <div class="text-center q-pa-lg text-grey-6">
              <q-icon
                name="inventory_2"
                size="48px"
                class="q-mb-sm"
              />
              <div>No containers added yet</div>
              <div class="text-caption">
                Use the options above to add containers
              </div>
            </div>
          </template>
        </q-table>
      </q-card-section>
    </q-card>

    <!-- Action Buttons -->
    <div class="row q-gutter-md justify-end">
      <q-btn
        v-if="!jobCreated"
        no-caps
        unelevated
        color="accent"
        label="Create Job"
        class="btn-modern"
        :disable="!canCreateJob"
        :loading="creatingJob"
        @click="createJob('Manual')"
      />
      <q-btn
        v-if="!jobCreated && jobConfig.mode === 'PreAssigned'"
        no-caps
        unelevated
        color="primary"
        label="Create & Pre-Assign"
        class="btn-modern"
        :disable="!canCreateJob"
        :loading="creatingJob"
        @click="createJob('PreAssigned')"
      />
      <q-btn
        v-if="jobCreated && jobConfig.mode === 'PreAssigned' && !preAssignmentRun"
        no-caps
        unelevated
        color="primary"
        label="Run Pre-Assignment"
        class="btn-modern"
        :loading="runningPreAssign"
        @click="runPreAssignment"
      />
      <q-btn
        v-if="jobCreated"
        no-caps
        unelevated
        color="accent"
        label="Start Shelving"
        class="btn-modern"
        @click="startShelving"
      />
    </div>

    <!-- Pre-Assignment Results Dialog -->
    <q-dialog v-model="showPreAssignDialog">
      <q-card style="min-width: 400px">
        <q-card-section>
          <div class="text-h6">
            Pre-Assignment Results
          </div>
        </q-card-section>
        <q-card-section>
          <div class="row q-col-gutter-md">
            <div class="col-6 text-center">
              <div class="text-h4 text-positive">
                {{ preAssignResult?.assigned_count || 0 }}
              </div>
              <div class="text-caption">
                Assigned
              </div>
            </div>
            <div class="col-6 text-center">
              <div class="text-h4 text-warning">
                {{ preAssignResult?.unassigned_count || 0 }}
              </div>
              <div class="text-caption">
                Unassigned
              </div>
            </div>
          </div>
          <div
            v-if="preAssignResult?.unassigned_count > 0"
            class="q-mt-md"
          >
            <div class="text-subtitle2">
              Unassigned Barcodes:
            </div>
            <div class="text-caption text-grey-7">
              {{ preAssignResult?.unassigned_barcodes?.slice(0, 5).join(', ') }}
              <span v-if="preAssignResult?.unassigned_barcodes?.length > 5">
                ... and {{ preAssignResult.unassigned_barcodes.length - 5 }} more
              </span>
            </div>
          </div>
        </q-card-section>
        <q-card-actions align="right">
          <q-btn
            flat
            label="Close"
            color="primary"
            v-close-popup
          />
          <q-btn
            unelevated
            label="Start Shelving"
            color="accent"
            @click="startShelving"
            v-close-popup
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useShelvingStore } from '@/stores/shelving-store'
import { useOptionStore } from '@/stores/option-store'
import { useVerificationStore } from '@/stores/verification-store'
import { useQuasar } from 'quasar'

const router = useRouter()
const shelvingStore = useShelvingStore()
const optionStore = useOptionStore()
const verificationStore = useVerificationStore()
const $q = useQuasar()

// State
const jobConfig = ref({
  building_id: null,
  mode: 'PreAssigned',
  allow_unassigned_size: false,
  allow_unassigned_owner: false,
  allow_tiered_owner: false
})

const addMethod = ref('verification')
const selectedVerificationJobs = ref([])
const manualBarcode = ref('')
const containers = ref([])
const jobCreated = ref(false)
const createdJobId = ref(null)

// Loading states
const loadingVerificationJobs = ref(false)
const addingFromJobs = ref(false)
const addingContainer = ref(false)
const creatingJob = ref(false)
const addError = ref('')

// Pre-assignment
const showPreAssignDialog = ref(false)
const preAssignResult = ref(null)
const preAssignmentRun = ref(false)
const runningPreAssign = ref(false)

// Options
const modeOptions = [
  'Manual',
  'PreAssigned'
]
const buildingOptions = computed(() => optionStore.buildings || [])
const verificationJobOptions = ref([])

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
    name: 'size_class',
    label: 'Size Class',
    field: row => row.size_class?.short_name || '-',
    align: 'left'
  },
  {
    name: 'status',
    label: 'Status',
    field: 'status',
    align: 'center'
  },
  {
    name: 'proposed_location',
    label: 'Proposed Location',
    field: 'proposed_location',
    align: 'left'
  },
  {
    name: 'actions',
    label: '',
    field: 'actions',
    align: 'right'
  }
]

// Computed
const canCreateJob = computed(() => {
  return jobConfig.value.building_id && containers.value.length > 0
})

// Methods
const getStatusColor = (status) => {
  const colors = {
    Pending: 'grey',
    Assigned: 'info',
    Unassigned: 'warning',
    Shelved: 'positive',
    Error: 'negative'
  }
  return colors[status] || 'grey'
}

const loadVerificationJobs = async () => {
  loadingVerificationJobs.value = true
  try {
    // Load completed verification jobs that haven't been shelved yet
    await verificationStore.getVerificationJobList({
      unshelved: true
    })
    verificationJobOptions.value = verificationStore.verificationJobList.map(vj => ({
      id: vj.id,
      label: `VJ #${vj.id} - ${vj.trayed ? 'Trayed' : 'Non-Trayed'}`
    }))
  } catch (error) {
    console.error('Failed to load verification jobs:', error)
  } finally {
    loadingVerificationJobs.value = false
  }
}

const addFromVerificationJobs = async () => {
  if (!createdJobId.value) {
    // Need to create the job first
    await createJobOnly()
    if (!createdJobId.value) {
      return
    }
  }

  addingFromJobs.value = true
  try {
    // The job was created with verification_job_ids, so refresh containers
    const containerList = await shelvingStore.getShelveByListContainers(createdJobId.value)
    containers.value = containerList
    selectedVerificationJobs.value = []
    $q.notify({
      type: 'positive',
      message: `Added ${containerList.length} containers from verification jobs`
    })
  } catch (error) {
    $q.notify({
      type: 'negative',
      message: error.response?.data?.detail || 'Failed to add containers'
    })
  } finally {
    addingFromJobs.value = false
  }
}

const addManualContainer = async () => {
  if (!manualBarcode.value) {
    return
  }

  if (!createdJobId.value) {
    // Need to create the job first
    await createJobOnly()
    if (!createdJobId.value) {
      return
    }
  }

  addingContainer.value = true
  addError.value = ''
  try {
    const container = await shelvingStore.addContainerToShelveByList(
      createdJobId.value,
      manualBarcode.value
    )
    containers.value.push(container)
    manualBarcode.value = ''
    $q.notify({
      type: 'positive',
      message: 'Container added successfully'
    })
  } catch (error) {
    addError.value = error.response?.data?.detail || 'Failed to add container'
  } finally {
    addingContainer.value = false
  }
}

const removeContainer = async (containerId) => {
  try {
    await shelvingStore.removeContainerFromShelveByList(createdJobId.value, containerId)
    containers.value = containers.value.filter(c => c.id !== containerId)
    $q.notify({
      type: 'info',
      message: 'Container removed'
    })
  } catch (error) {
    $q.notify({
      type: 'negative',
      message: error.response?.data?.detail || 'Failed to remove container'
    })
  }
}

const createJobOnly = async () => {
  creatingJob.value = true
  try {
    const payload = {
      origin: 'List',
      building_id: jobConfig.value.building_id,
      mode: jobConfig.value.mode,
      allow_unassigned_size: jobConfig.value.allow_unassigned_size,
      allow_unassigned_owner: jobConfig.value.allow_unassigned_owner,
      allow_tiered_owner: jobConfig.value.allow_tiered_owner,
      verification_job_ids: selectedVerificationJobs.value.length > 0
        ? selectedVerificationJobs.value
        : null
    }
    console.log('Creating Shelve by List job with payload:', JSON.stringify(payload, null, 2))
    const job = await shelvingStore.createShelvingJob(payload)
    console.log('Job created:', job.id)
    createdJobId.value = job.id
    jobCreated.value = true
  } catch (error) {
    $q.notify({
      type: 'negative',
      message: error.response?.data?.detail || 'Failed to create job'
    })
  } finally {
    creatingJob.value = false
  }
}

const createJob = async (mode) => {
  jobConfig.value.mode = mode

  if (!createdJobId.value) {
    await createJobOnly()
    if (!createdJobId.value) {
      return
    }
  }

  // Refresh containers
  const containerList = await shelvingStore.getShelveByListContainers(createdJobId.value)
  containers.value = containerList

  if (mode === 'PreAssigned' && containers.value.length > 0) {
    // Run pre-assignment
    creatingJob.value = true
    try {
      preAssignResult.value = await shelvingStore.runPreAssignment(createdJobId.value, {
        building_id: jobConfig.value.building_id
      })
      // Refresh containers to show assigned locations
      containers.value = await shelvingStore.getShelveByListContainers(createdJobId.value)
      showPreAssignDialog.value = true
    } catch (error) {
      $q.notify({
        type: 'negative',
        message: error.response?.data?.detail || 'Pre-assignment failed'
      })
    } finally {
      creatingJob.value = false
    }
  } else {
    $q.notify({
      type: 'positive',
      message: 'Job created successfully'
    })
  }
}

const runPreAssignment = async () => {
  runningPreAssign.value = true
  try {
    preAssignResult.value = await shelvingStore.runPreAssignment(createdJobId.value, {
      building_id: jobConfig.value.building_id
    })
    // Refresh containers to show assigned locations
    containers.value = await shelvingStore.getShelveByListContainers(createdJobId.value)
    preAssignmentRun.value = true
    showPreAssignDialog.value = true
  } catch (error) {
    $q.notify({
      type: 'negative',
      message: error.response?.data?.detail || 'Pre-assignment failed'
    })
  } finally {
    runningPreAssign.value = false
  }
}

const startShelving = () => {
  router.push({
    name: 'ShelveByListExecute',
    params: { id: createdJobId.value }
  })
}

const handleCancel = () => {
  router.push({ name: 'shelving' })
}

onMounted(() => {
  optionStore.getOptions('buildings')
  loadVerificationJobs()
})
</script>

<style scoped lang="scss">
.shelving-list-create {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.form-group {
  margin-bottom: 0;
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
