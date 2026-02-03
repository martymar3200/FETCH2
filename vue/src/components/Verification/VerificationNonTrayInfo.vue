<template>
  <div class="verification-nontray-container">
    <!-- Job Header -->
    <div class="row q-mb-lg items-center q-gutter-md">
      <div class="col">
        <div class="row items-center">
          <!-- Three-dot menu -->
          <MoreOptionsMenu
            :options="[
              { text: 'Edit', disabled: verificationJob.status == 'Completed' },
              { text: 'Print Job' },
              { text: 'Cancel Job', optionClass: 'text-negative', disabled: verificationJob.status == 'Completed', hidden: !checkUserPermission('can_cancel_verification_job')},
              { text: 'View History'}
            ]"
            class="q-mr-sm"
            @click="handleOptionMenu"
          />
          <h1 class="text-h4 text-bold q-mb-none">
            Verification Job #{{ verificationJob.workflow_id }}
            <q-badge
              :color="getStatusColor(verificationJob.status)"
              :label="verificationJob.status"
              class="q-ml-sm"
            />
          </h1>
        </div>
        <!-- Subtitle with job metadata -->
        <p class="text-grey-7 q-mb-none">
          {{ verificationJob.owner?.name }} • {{ verificationJob.media_type?.name || verificationContainer.media_type?.name || 'Unknown Media' }}
        </p>
      </div>
      <div class="col-auto">
        <!-- Resume button (only when paused) -->
        <q-btn
          v-if="verificationJob.status === 'Paused'"
          no-caps
          unelevated
          color="accent"
          label="Resume"
          class="btn-modern q-mr-sm"
          @click="updateVerificationJobStatus('Running')"
        />
        <!-- Complete Job button -->
        <q-btn
          v-if="verificationJob.status !== 'Completed'"
          no-caps
          unelevated
          color="positive"
          label="Complete Job"
          class="btn-modern"
          :disabled="!canComplete || verificationJob.status === 'Paused'"
          :loading="appActionIsLoadingData"
          @click="showConfirmationModal = { type: 'complete', text: 'Are you sure you want to complete the job?' }"
        />
      </div>
    </div>

    <!-- Scan Card -->
    <q-card
      v-if="['Running', 'Created'].includes(verificationJob.status)"
      class="q-mb-lg"
    >
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
              :disabled="verificationJob.status === 'Paused' || verificationJob.status === 'Completed'"
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
            v-if="verificationContainer.id"
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
          {{ verificationContainer.barcode?.value || 'No item selected' }}
        </div>
        <div
          v-if="!verificationContainer.id"
          class="text-caption text-grey-7 text-center"
        >
          Scan an item barcode above to begin
        </div>
        <!-- Item Details -->
        <div
          v-if="verificationContainer.id"
          class="row q-mt-md q-col-gutter-md justify-center"
        >
          <div class="col-auto text-center">
            <div class="text-caption text-grey-6">
              Container Size
            </div>
            <div class="text-body1">
              {{ verificationContainer.size_class?.name || verificationJob.size_class?.name || '-' }}
            </div>
          </div>
          <div class="col-auto text-center">
            <div class="text-caption text-grey-6">
              Media Type
            </div>
            <div class="text-body1">
              {{ verificationContainer.media_type?.name || verificationJob.media_type?.name || '-' }}
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
        :label="`Items in Job (${verificationJob.non_tray_items?.length || 0})`"
        header-class="text-subtitle1 text-weight-bold"
      >
        <q-card-section>
          <q-list
            bordered
            separator
          >
            <q-item
              v-for="item in verificationJob.non_tray_items"
              :key="item.id"
              clickable
              @click="navigateToItem(item)"
              :class="{ 'bg-accent-1': item.id === verificationContainer.id }"
            >
              <q-item-section>
                <q-item-label>{{ item.barcode?.value || 'Unknown' }}</q-item-label>
                <q-item-label caption>
                  {{ item.size_class?.name || 'No size' }} • {{ item.media_type?.name || 'No media type' }}
                </q-item-label>
              </q-item-section>
              <q-item-section side>
                <q-badge
                  :color="item.scanned_for_verification ? 'positive' : 'warning'"
                  :label="item.scanned_for_verification ? 'Verified' : 'Pending'"
                />
              </q-item-section>
            </q-item>
            <q-item v-if="!verificationJob.non_tray_items?.length">
              <q-item-section class="text-grey-6 text-center">
                No items scanned yet
              </q-item-section>
            </q-item>
          </q-list>
        </q-card-section>
      </q-expansion-item>
    </q-card>

    <!-- Edit Item Modal -->
    <PopupModal
      v-if="editMode"
      ref="editModalRef"
      :title="'Edit Item Settings'"
      :show-actions="false"
      @reset="cancelNonTrayEdit"
      aria-label="editItemModal"
    >
      <template #main-content>
        <q-card-section>
          <div class="text-subtitle1 q-mb-sm">
            Container Size
          </div>
          <q-select
            v-model="verificationContainer.size_class_id"
            :options="sizeClass"
            option-label="name"
            option-value="id"
            emit-value
            map-options
            outlined
            dense
            placeholder="Select size class"
            class="q-mb-md"
          />
          <div class="text-subtitle1 q-mb-sm">
            Media Type
          </div>
          <q-select
            v-model="verificationContainer.media_type_id"
            :options="mediaTypes"
            option-label="name"
            option-value="id"
            emit-value
            map-options
            outlined
            dense
            placeholder="Select media type"
          />
        </q-card-section>
      </template>
      <template #footer-content>
        <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
          <q-btn
            no-caps
            unelevated
            color="accent"
            label="Save Changes"
            class="text-body1 full-width btn-modern"
            :loading="appActionIsLoadingData"
            @click="updateNonTrayItem()"
          />
          <q-space class="q-mx-xs" />
          <q-btn
            outline
            no-caps
            label="Cancel"
            class="text-body1 full-width"
            @click="cancelNonTrayEdit"
          />
        </q-card-section>
      </template>
    </PopupModal>

    <!-- Confirmation/Cancel Modals remain similar but need unified styling -->

  </div>
  <!-- confirmation modal -->
  <PopupModal
    v-if="showConfirmationModal"
    ref="confirmationModal"
    :title="showConfirmationModal.type === 'cancel' ? 'Cancel' : (showConfirmationModal.type === 'addItem' ? 'Add Item' : 'Complete')"
    :text="showConfirmationModal.text"
    :show-actions="false"
    @reset="showConfirmationModal = null"
    aria-label="confirmationModal"
  >
    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <q-btn
          no-caps
          unelevated
          :color="showConfirmationModal.type === 'cancel' ? 'negative' : 'positive'"
          :label="showConfirmationModal.type === 'cancel' ? 'Cancel Verification' : (showConfirmationModal.type === 'addItem' ? 'Add Item' : 'Complete Job')"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="handleConfirmationAction"
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
  <!-- Audit trail modal -->
  <AuditTrail
    v-if="showAuditTrailModal"
    ref="historyModal"
    @reset="showAuditTrailModal = null"
    :job-type="showAuditTrailModal"
    :job-id="verificationJob.id"
  />
</template>

<script setup>
import { ref, computed, inject, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Notify } from 'quasar'
import { storeToRefs } from 'pinia'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import { useGlobalStore } from '@/stores/global-store'
import { useVerificationStore } from '@/stores/verification-store'
import { useOptionStore } from '@/stores/option-store'
import { useBarcodeStore } from '@/stores/barcode-store'
import { useUserStore } from '@/stores/user-store'
import { audioAlert } from '@/utils/audio.js'
import MoreOptionsMenu from '@/components/MoreOptionsMenu.vue'
import AuditTrail from '@/components/AuditTrail.vue'
import PopupModal from '@/components/PopupModal.vue'

const router = useRouter()

// Emits
const emit = defineEmits([
  'print',
  'scan'
])

// Composables
const { checkUserPermission } = usePermissionHandler()

// Store Data
const { appIsLoadingData, appActionIsLoadingData } = storeToRefs(useGlobalStore())
const {
  getOptions
} = useOptionStore()
const {
  mediaTypes,
  sizeClass
} = storeToRefs(useOptionStore())
const {
  patchVerificationJob,
  patchVerificationNonTrayItem,
  cancelVerificationJob,
  getVerificationNonTrayItem,
  verifyNonTrayItem
} = useVerificationStore()
const {
  verificationJob,
  verificationContainer
} = storeToRefs(useVerificationStore())
const { verifyBarcode } = useBarcodeStore()
const { barcodeDetails } = storeToRefs(useBarcodeStore())
const { userData } = storeToRefs(useUserStore())
const { postVerificationNonTrayItem } = useVerificationStore()

// Local Data
const scanBarcodeInput = ref('')
const editMode = ref(false)
const showAuditTrailModal = ref(false)
const showConfirmationModal = ref(null)
const isProcessingScan = ref(false)

// Logic

const currentIsoDate = inject('current-iso-date')

// Computed
const canComplete = computed(() => {
  if (!verificationJob.value.non_tray_items || verificationJob.value.non_tray_items.length === 0) {
    return false
  }
  return verificationJob.value.non_tray_items.every(item => item.scanned_for_verification)
})

// Status color helper
const getStatusColor = (status) => {
  switch (status) {
    case 'Created':
      return 'blue-6'
    case 'Running':
      return 'positive'
    case 'Paused':
      return 'warning'
    case 'Completed':
      return 'primary'
    case 'Cancelled':
      return 'negative'
    default:
      return 'grey-6'
  }
}

const handleOptionMenu = async (option) => {
  if (option.text == 'Edit') {
    await openEditModal()
  } else if (option.text == 'Print Job') {
    emit('print')
  } else if (option.text == 'Cancel Job') {
    showConfirmationModal.value = {
      type: 'cancel',
      text: 'Are you sure you want to cancel the Verification Job?'
    }
  } else if (option.text == 'View History') {
    showAuditTrailModal.value = 'verification_jobs'
  }
}

const openEditModal = async () => {
  try {
    appIsLoadingData.value = true
    // Load options
    await Promise.all([
      getOptions('sizeClass'),
      getOptions('mediaTypes')
    ])

    // Only need specific lookups if we want to ensure data integrity, but getOptions loads the lists.
    // If we want to set selected values we can rely on v-model binding to IDs.

    editMode.value = true
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
  } finally {
    appIsLoadingData.value = false
  }
}

const triggerItemScan = async () => {
  const barcode = scanBarcodeInput.value.trim()
  if (!barcode) {
    return
  }

  if (isProcessingScan.value) {
    return
  }
  isProcessingScan.value = true
  scanBarcodeInput.value = ''

  try {
    appActionIsLoadingData.value = true
    await verifyBarcode(barcode, 'Item', true)

    // Check if item exists in job
    if (verificationJob.value.non_tray_items?.some(item => item.barcode?.id === barcodeDetails.value.id)) {
      // Load item
      await getVerificationNonTrayItem(barcode)
      // Verify it if not verified
      if (!verificationContainer.value.scanned_for_verification) {
        await verifyNonTrayItem(verificationContainer.value.id)
      } else {
        Notify.create({
          type: 'info',
          message: 'Item already verified'
        })
      }

      if (verificationJob.value.status !== 'Running' && verificationJob.value.status !== 'Completed') {
        await updateVerificationJobStatus('Running')
      }
    } else {
      showConfirmationModal.value = {
        type: 'addItem',
        text: 'Are you sure you want to add a new item to the job?'
      }
      audioAlert()
    }
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error,
      timeout: 0,
      actions: [
        {
          label: 'Dismiss',
          color: 'white',
          handler: () => { /* ... */ }
        }
      ]
    })
  } finally {
    isProcessingScan.value = false
    appActionIsLoadingData.value = false
  }
}

const handleConfirmationAction = () => {
  if (showConfirmationModal.value.type === 'cancel') {
    cancelVerification()
  } else if (showConfirmationModal.value.type === 'addItem') {
    addItemToJob()
  } else {
    completeVerificationJob()
  }
}

const addItemToJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      barcode_id: barcodeDetails.value.id,
      barcode_value: barcodeDetails.value.value,
      media_type_id: verificationJob.value.media_type_id,
      size_class_id: verificationJob.value.size_class_id,
      status: 'In',
      verification_job_id: verificationJob.value.id,
      user_id: userData.value.user_id
    }
    await postVerificationNonTrayItem(payload)
    Notify.create({
      type: 'positive',
      message: 'Item has been added to the job.'
    })

    if (verificationJob.value.status !== 'Running' && verificationJob.value.status !== 'Completed') {
      await updateVerificationJobStatus('Running')
    }
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
  } finally {
    appActionIsLoadingData.value = false
    showConfirmationModal.value = null
  }
}

const navigateToItem = async (item) => {
  try {
    appActionIsLoadingData.value = true
    await getVerificationNonTrayItem(item.barcode.value)
  } catch (e) {
    console.error(e)
  } finally {
    appActionIsLoadingData.value = false
  }
}

const updateVerificationJobStatus = async (status) => {
  try {
    appActionIsLoadingData.value = true
    await patchVerificationJob({
      id: verificationJob.value.id,
      status,
      run_timestamp: currentIsoDate()
    })
    Notify.create({
      type: 'positive',
      message: `Job ${status === 'Paused' ? 'paused' : 'resumed'} successfully.`
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}

const cancelVerification = async () => {
  try {
    appActionIsLoadingData.value = true
    await cancelVerificationJob(verificationJob.value.id)

    Notify.create({
      type: 'positive',
      message: 'Verification Job canceled'
    })

    await nextTick()
    router.push({
      name: 'verification',
      params: { jobId: null }
    })

  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
  } finally {
    appActionIsLoadingData.value = false
    showConfirmationModal.value = null
  }
}

const completeVerificationJob = async () => {
  try {
    appActionIsLoadingData.value = true
    await patchVerificationJob({
      id: verificationJob.value.id,
      status: 'Completed'
    })
    Notify.create({
      type: 'positive',
      message: 'Job Completed'
    })
    router.push({
      name: 'verification',
      params: { jobId: null }
    })
  } catch (e) {
    Notify.create({
      type: 'negative',
      message: e.response?.data?.detail || e
    })
  } finally {
    appActionIsLoadingData.value = false
    showConfirmationModal.value = null
  }
}

const cancelNonTrayEdit = () => {
  // Reset changes
  // verificationContainer is a store ref, so if we mutated it directly in v-model, we might need to revert.
  // However, we usually patch on save.
  // If we want to revert, we might need to reload.
  editMode.value = false
}

const updateNonTrayItem = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: verificationContainer.value.id,
      media_type_id: verificationContainer.value.media_type_id,
      size_class_id: verificationContainer.value.size_class_id
    }
    await patchVerificationNonTrayItem(payload)

    Notify.create({
      type: 'positive',
      message: 'The non-tray item has been updated.'
    })
    editMode.value = false
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}

defineExpose({ editMode })
</script>

<style lang="scss" scoped>
.verification-nontray-container {
  width: 80%;
  margin: 0 auto;
  padding: 16px 8px;
}

.btn-modern {
    font-weight: 500;
}

.bg-accent-1 {
    background-color: #e3f2fd; // Light blue highlight for active item
}
</style>
