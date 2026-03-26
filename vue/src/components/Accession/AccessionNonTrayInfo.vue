<template>
  <div class="accession-nontray-container">
    <!-- Job Header -->
    <div class="row q-mb-lg items-center q-gutter-md">
      <div class="col">
        <div class="row items-center">
          <!-- Three-dot menu -->
          <MoreOptionsMenu
            :options="dotMenuOptions"
            class="q-mr-sm"
            @click="handleOptionMenu"
          />
          <h1 class="text-h4 text-bold q-mb-none">
            Accession Job #{{ accessionJob.workflow_id }}
            <q-badge
              :color="getStatusColor(accessionJob.status)"
              :label="accessionJob.status"
              class="q-ml-sm"
            />
          </h1>
        </div>
        <!-- Subtitle with job metadata -->
        <p class="text-grey-7 q-mb-none">
          {{ accessionJob.owner?.name }} • {{ accessionJob.media_type?.name || accessionContainer.media_type?.name || 'Unknown Media' }}
        </p>
      </div>
      <div class="col-auto">
        <!-- Resume button (only when paused) -->
        <q-btn
          v-if="accessionJob.status === 'Paused'"
          no-caps
          unelevated
          color="accent"
          label="Resume"
          class="btn-modern q-mr-sm"
          @click="updateAccessionJobStatus('Running')"
        />
        <!-- Complete Job button -->
        <q-btn
          v-if="accessionJob.status !== 'Completed'"
          no-caps
          unelevated
          color="positive"
          label="Complete Job"
          class="btn-modern"
          :disabled="!canComplete || accessionJob.status === 'Paused'"
          :loading="appActionIsLoadingData"
          @click="showCompleteConfirmation = true"
        />
      </div>
    </div>

    <!-- Scan Card -->
    <q-card class="q-mb-lg">
      <q-card-section>
        <div class="row items-end q-col-gutter-md">
          <div class="col-12 col-md-8">
            <div class="text-subtitle1 text-weight-bold q-mb-sm">
              Scan Non-Tray Item Barcode
            </div>
            <q-input
              v-model="scanBarcodeInput"
              outlined
              dense
              placeholder="Scan or type item barcode and press Enter"
              @keyup.enter="triggerItemScan"
              autofocus
              :disabled="accessionJob.status === 'Paused' || accessionJob.status === 'Completed'"
            >
              <template #append>
                <q-icon name="qr_code_scanner" />
              </template>
            </q-input>
          </div>
        </div>
      </q-card-section>
    </q-card>

    <!-- Active Item Card -->
    <q-card class="q-mb-lg">
      <q-card-section>
        <div class="row items-center q-mb-sm">
          <div class="col">
            <div class="text-subtitle1 text-weight-bold">
              Active Item
            </div>
          </div>
          <div
            v-if="accessionContainer.id"
            class="col-auto"
          >
            <q-btn
              flat
              dense
              icon="edit"
              color="grey-7"
              @click="openEditModal"
            >
              <q-tooltip>Edit Item Details</q-tooltip>
            </q-btn>
          </div>
        </div>
        <div class="text-h5 text-weight-medium text-center">
          {{ accessionContainer.barcode?.value || 'No item selected' }}
        </div>
        <div
          v-if="!accessionContainer.id"
          class="text-caption text-grey-7 text-center"
        >
          Scan an item barcode above to begin
        </div>
        <!-- Item Details -->
        <div
          v-if="accessionContainer.id"
          class="row q-mt-md q-col-gutter-md justify-center"
        >
          <div class="col-auto text-center">
            <div class="text-caption text-grey-6">
              Container Size
            </div>
            <div class="text-body1">
              {{ accessionContainer.size_class?.name || accessionJob.size_class?.name || '-' }}
            </div>
          </div>
          <div class="col-auto text-center">
            <div class="text-caption text-grey-6">
              Media Type
            </div>
            <div class="text-body1">
              {{ accessionContainer.media_type?.name || accessionJob.media_type?.name || '-' }}
            </div>
          </div>
        </div>
      </q-card-section>
    </q-card>

    <!-- Items in Job Card (expansion panel) -->
    <q-card class="q-mb-lg">
      <q-expansion-item
        dense
        expand-separator
        :label="`Items in Job (${accessionJob.non_tray_items?.length || 0})`"
        header-class="text-subtitle1 text-weight-bold"
      >
        <q-card-section>
          <q-list
            bordered
            separator
          >
            <q-item
              v-for="item in accessionJob.non_tray_items"
              :key="item.id"
              clickable
              @click="navigateToItem(item)"
              :class="{ 'bg-accent-1': item.id === accessionContainer.id }"
            >
              <q-item-section>
                <q-item-label>{{ item.barcode?.value || 'Unknown' }}</q-item-label>
                <q-item-label caption>
                  {{ item.size_class?.name || 'No size' }} • {{ item.media_type?.name || 'No media type' }}
                </q-item-label>
              </q-item-section>
              <q-item-section side>
                <q-badge
                  :color="item.scanned_for_accession ? 'positive' : 'warning'"
                  :label="item.scanned_for_accession ? 'Accessioned' : 'Pending'"
                />
              </q-item-section>
            </q-item>
            <q-item v-if="!accessionJob.non_tray_items?.length">
              <q-item-section class="text-grey-6 text-center">
                No items scanned yet
              </q-item-section>
            </q-item>
          </q-list>
        </q-card-section>
      </q-expansion-item>
    </q-card>
  </div>

  <!-- Edit Item Modal -->
  <PopupModal
    v-if="showEditModal"
    ref="editModalRef"
    :title="'Edit Item Settings'"
    :show-actions="false"
    @reset="showEditModal = false"
    aria-label="editItemModal"
  >
    <template #main-content>
      <q-card-section>
        <div class="text-subtitle1 q-mb-sm">
          Container Size
        </div>
        <q-select
          v-model="selectedSizeClass"
          :options="sizeClass"
          option-label="name"
          option-value="id"
          outlined
          dense
          placeholder="Select size class"
          class="q-mb-md"
        />
        <div class="text-subtitle1 q-mb-sm">
          Media Type
        </div>
        <q-select
          v-model="selectedMediaType"
          :options="mediaTypes"
          option-label="name"
          option-value="id"
          outlined
          dense
          placeholder="Select media type"
        />
      </q-card-section>
    </template>
    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <q-btn
          no-caps
          unelevated
          color="accent"
          label="Save Changes"
          class="text-body1 full-width btn-modern"
          :loading="appActionIsLoadingData"
          @click="saveItemEdits(hideModal)"
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

  <!-- Cancel Job Confirmation Modal -->
  <PopupModal
    v-if="showCancelConfirmation"
    ref="cancelConfirmationModal"
    :title="'Confirm'"
    :text="'Are you sure you want to cancel the accession job? Warning: All associated items will be deleted.'"
    :show-actions="false"
    @reset="showCancelConfirmation = false"
    aria-label="cancelConfirmationModal"
  >
    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <q-btn
          no-caps
          unelevated
          color="negative"
          label="Cancel Job"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="cancelAccessionJob()"
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

  <!-- Complete Job Confirmation Modal -->
  <PopupModal
    v-if="showCompleteConfirmation"
    ref="completeConfirmationModal"
    :title="'Confirm'"
    :text="'Are you sure you want to complete the job?'"
    :show-actions="false"
    @reset="showCompleteConfirmation = false"
    aria-label="completeConfirmationModal"
  >
    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <q-btn
          no-caps
          unelevated
          color="accent"
          label="Complete"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="completeAccessionJob(hideModal, false)"
        />

        <q-space class="q-mx-xs" />

        <q-btn
          no-caps
          unelevated
          color="accent"
          label="Complete & Print"
          class="btn-no-wrap text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="completeAccessionJob(hideModal, true)"
        />

        <q-space
          v-if="currentScreenSize !== 'xs'"
          class="q-mx-lg"
        />

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

  <!-- Audit trail modal -->
  <AuditTrail
    v-if="showAuditTrailModal"
    ref="historyModal"
    @reset="showAuditTrailModal = null"
    :job-type="showAuditTrailModal"
    :job-id="accessionJob.id"
  />
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Notify } from 'quasar'
import { storeToRefs } from 'pinia'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import { useGlobalStore } from '@/stores/global-store'
import { useAccessionStore } from '@/stores/accession-store'
import { useOptionStore } from '@/stores/option-store'
import MoreOptionsMenu from '@/components/MoreOptionsMenu.vue'
import PopupModal from '@/components/PopupModal.vue'
import AuditTrail from '@/components/AuditTrail.vue'

const router = useRouter()

// Composables
const { checkUserPermission } = usePermissionHandler()

// Store Data
const { appActionIsLoadingData } = storeToRefs(useGlobalStore())
const { getOptions } = useOptionStore()
const { sizeClass, mediaTypes } = storeToRefs(useOptionStore())
const {
  patchAccessionJob,
  patchAccessionNonTrayItem,
  getAccessionNonTrayItem
} = useAccessionStore()
const {
  accessionJob,
  accessionContainer
} = storeToRefs(useAccessionStore())

// Emits
const emit = defineEmits([
  'print',
  'scan'
])

// Local Data
const scanBarcodeInput = ref('')
const showEditModal = ref(false)
const showCancelConfirmation = ref(false)
const showCompleteConfirmation = ref(false)
const showAuditTrailModal = ref(false)
const selectedSizeClass = ref(null)
const selectedMediaType = ref(null)
// isProcessingScan removed — the parent's scan queue handles sequential processing

// Logic

const dotMenuOptions = computed(() => {
  return [
    {
      text: 'Edit',
      disabled: accessionJob.value.status == 'Completed'
    },
    {
      text: 'Pause Job',
      disabled: accessionJob.value.status == 'Completed' || accessionJob.value.status == 'Paused'
    },
    {
      text: 'Cancel Job',
      optionClass: 'text-negative',
      disabled: accessionJob.value.status == 'Completed',
      hidden: !(checkUserPermission('can_cancel_accession') || checkUserPermission('can_cancel_accession_job'))
    },
    { text: 'Print Job' },
    { text: 'View History' }
  ]
})

// Computed
const canComplete = computed(() => {
  // Job can be completed if all items are verified (scanned_for_accession = true)
  if (!accessionJob.value.non_tray_items || accessionJob.value.non_tray_items.length === 0) {
    return false
  }
  return accessionJob.value.non_tray_items.every(item => item.scanned_for_accession)
})

// Status color helper
const getStatusColor = (status) => {
  switch (status) {
    case 'Created':
      return 'blue-6'
    case 'Running':
      return 'green-6'
    case 'Paused':
      return 'orange-6'
    case 'Completed':
      return 'positive'
    case 'Cancelled':
      return 'negative'
    default:
      return 'grey-6'
  }
}

const handleOptionMenu = async (option) => {
  if (option.text == 'Edit') {
    await openEditModal()
  } else if (option.text == 'Pause Job') {
    await updateAccessionJobStatus('Paused')
  } else if (option.text == 'Cancel Job') {
    showCancelConfirmation.value = true
  } else if (option.text == 'Print Job') {
    emit('print')
  } else if (option.text == 'View History') {
    showAuditTrailModal.value = 'accession_jobs'
  }
}

const openEditModal = async () => {
  // Load options if not loaded
  await getOptions('sizeClass')
  await getOptions('mediaTypes')

  // Set current values
  if (accessionContainer.value.id) {
    selectedSizeClass.value = sizeClass.value.find(sc => sc.id === accessionContainer.value.size_class_id) || null
    selectedMediaType.value = mediaTypes.value.find(mt => mt.id === accessionContainer.value.media_type_id) || null
  } else {
    selectedSizeClass.value = sizeClass.value.find(sc => sc.id === accessionJob.value.size_class_id) || null
    selectedMediaType.value = mediaTypes.value.find(mt => mt.id === accessionJob.value.media_type_id) || null
  }

  showEditModal.value = true
}

const saveItemEdits = async (hideModal) => {
  try {
    appActionIsLoadingData.value = true

    if (accessionContainer.value.id) {
      // Update item
      let addVerifiedAlert = false
      let payload = {
        id: accessionContainer.value.id,
        media_type_id: selectedMediaType.value?.id,
        size_class_id: selectedSizeClass.value?.id
      }

      // If item hasn't been verified and now has both fields, verify it
      if (!accessionContainer.value.scanned_for_accession && payload.media_type_id && payload.size_class_id) {
        payload.scanned_for_accession = true
        addVerifiedAlert = true
      }

      await patchAccessionNonTrayItem(payload)

      if (addVerifiedAlert) {
        Notify.create({
          type: 'positive',
          message: 'The non-tray item has been updated and verified.'
        })
      } else {
        Notify.create({
          type: 'positive',
          message: 'The non-tray item has been updated.'
        })
      }
    } else {
      // Update job defaults
      await patchAccessionJob({
        id: accessionJob.value.id,
        media_type_id: selectedMediaType.value?.id,
        size_class_id: selectedSizeClass.value?.id
      })

      Notify.create({
        type: 'positive',
        message: 'Job settings updated successfully.'
      })
    }

    hideModal()
    showEditModal.value = false
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error?.message || error?.toString() || 'An error occurred'
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}

const triggerItemScan = async () => {
  const barcode = scanBarcodeInput.value.trim()
  scanBarcodeInput.value = '' // Instant clear to prevent concatenation
  if (!barcode) {
    return
  }

  // Check if item already exists in job
  const existingItem = accessionJob.value.non_tray_items?.find(item => item.barcode?.value === barcode)

  if (existingItem) {
    // Load existing item and navigate
    try {
      await getAccessionNonTrayItem(barcode)
      router.push({
        name: 'accession-container',
        params: {
          jobId: accessionJob.value.workflow_id,
          containerId: barcode
        }
      })
      Notify.create({
        type: 'info',
        message: 'Loaded existing item.'
      })
    } catch (error) {
      Notify.create({
        type: 'negative',
        message: error?.message || error?.toString() || 'An error occurred'
      })
    }
  } else {
    // Emit scan event to parent for proper barcode verification flow
    // Parent handles all loading state and error handling via scan queue
    emit('scan', barcode)
  }
}

const navigateToItem = (item) => {
  router.push({
    name: 'accession-container',
    params: {
      jobId: accessionJob.value.workflow_id,
      containerId: item.barcode?.value || item.id
    }
  })
}

const updateAccessionJobStatus = async (status) => {
  try {
    appActionIsLoadingData.value = true
    await patchAccessionJob({
      id: accessionJob.value.id,
      status
    })
    Notify.create({
      type: 'positive',
      message: `Job ${status === 'Paused' ? 'paused' : 'resumed'} successfully.`
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error?.message || error?.toString() || 'An error occurred'
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}

const cancelAccessionJob = async () => {
  try {
    appActionIsLoadingData.value = true
    await patchAccessionJob({
      id: accessionJob.value.id,
      status: 'Cancelled'
    })

    Notify.create({
      type: 'positive',
      message: 'The Accession Job has been canceled.'
    })

    await nextTick()

    router.push({
      name: 'accession',
      params: { jobId: null }
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error?.message || error?.toString() || 'An error occurred'
    })
  } finally {
    appActionIsLoadingData.value = false
    showCancelConfirmation.value = false
  }
}

const completeAccessionJob = async (hideModal, shouldPrint = false) => {
  try {
    appActionIsLoadingData.value = true
    await patchAccessionJob({
      id: accessionJob.value.id,
      status: 'Completed'
    })

    Notify.create({
      type: 'positive',
      message: 'The Accession Job has been completed.'
    })

    if (shouldPrint) {
      emit('print')
    }

    hideModal()
    showCompleteConfirmation.value = false

    router.push({
      name: 'accession',
      params: { jobId: null }
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error?.message || error?.toString() || 'An error occurred'
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}


</script>

<style lang="scss" scoped>
.accession-nontray-container {
  width: 80%;
  margin: 0 auto;
  padding: 16px 8px;
}

.btn-modern {
  border-radius: 8px;
  padding: 8px 24px;
  font-weight: 600;
}

.bg-accent-1 {
  background: linear-gradient(135deg, rgba(var(--q-accent), 0.1) 0%, rgba(var(--q-accent), 0.05) 100%);
}
</style>
