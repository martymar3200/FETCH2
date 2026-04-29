<template>
  <div class="verification-container">
    <!-- TRAYED VERIFICATION LAYOUT -->
    <template v-if="verificationJob.trayed">
      <!-- Job Header -->
      <div class="row q-mb-lg items-center q-gutter-md">
        <div class="col">
          <div class="row items-center">
            <!-- Three-dot menu -->
            <MoreOptionsMenu
              :options="menuOptions"
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
          <!-- Edit buttons -->
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
          <!-- Normal buttons -->
          <div
            v-else
            class="row q-gutter-x-sm"
          >
            <!-- Resume button (only when paused) -->
            <BaseButton
              v-if="verificationJob.status === 'Paused'"
              no-caps
              unelevated
              color="accent"
              label="Resume"

              @click="updateVerificationJobStatus('Running')"
            />
            <!-- Complete Job button - always visible when running, disabled if not ready -->
            <BaseButton
              v-if="verificationJob.status !== 'Completed'"
              no-caps
              unelevated
              color="positive"
              label="Complete Job"

              :disabled="!allItemsVerified || !allTraysCompleted || verificationJob.status === 'Paused' || isItemQueueProcessing || isTrayQueueProcessing"
              @click="
                showConfirmation = {
                  type: 'completeJob',
                  text: 'Are you sure you want to complete the job?',
                }
              "
            />
          </div>
        </div>
      </div>

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
                <label class="form-group-label">Assigned User</label>
                <SelectInput
                  v-model="verificationJob.assigned_user_id"
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

      <!-- Scan Section Card -->
      <q-card
        v-if="['Running', 'Created', 'Assigned'].includes(verificationJob.status)"
        class="q-mb-lg"
      >
        <q-card-section>
          <div class="row q-col-gutter-md items-end">
            <!-- Tray Scan (when no tray selected) -->
            <template v-if="!verificationContainer.id">
              <div class="col-12 col-md-8">
                <div class="text-subtitle1 text-weight-bold q-mb-sm">
                  Scan Tray Barcode
                </div>
                <q-input
                  v-model="scanBarcodeInput"
                  outlined
                  dense
                  placeholder="Scan tray barcode to begin"
                  @keyup.enter="handleTrayScanInput"
                  autofocus
                >
                  <template #append>
                    <q-icon name="inventory_2" />
                  </template>
                </q-input>
              </div>
              <div class="col-12 col-md-4">
                <div class="text-caption text-grey-6 q-mb-sm">
                  Tip: Scan a tray barcode to verify items
                </div>
              </div>
            </template>

            <!-- Item Scan (when tray is selected) -->
            <template v-else>
              <div class="col-12 col-md-8">
                <div class="text-subtitle1 text-weight-bold q-mb-sm">
                  Scan Item Barcode
                </div>
                <q-input
                  v-model="scanBarcodeInput"
                  outlined
                  dense
                  placeholder="Scan or type item barcode and press Enter"
                  @keyup.enter="handleItemScanInput"
                  autofocus
                >
                  <template #append>
                    <q-icon name="qr_code_scanner" />
                  </template>
                </q-input>
              </div>
              <q-space />
              <div class="col-auto">
                <BaseButton
                  no-caps
                  unelevated
                  icon="skip_next"
                  color="accent"
                  label="Next Tray"

                  :disabled="
                    !verificationContainer.id ||
                      !allItemsVerified ||
                      verificationJob.status == 'Paused' ||
                      verificationJob.status == 'Completed' ||
                      allTraysCompleted
                  "
                  @click="setNextVerificationTray"
                >
                  <q-tooltip>Proceed to next tray (Shortcut: T)</q-tooltip>
                </BaseButton>
              </div>
            </template>
          </div>
        </q-card-section>
      </q-card>

      <!-- Active Tray Card -->
      <q-card class="q-mb-lg">
        <q-card-section>
          <div class="row items-center q-mb-sm">
            <div class="col">
              <div class="text-subtitle1 text-weight-bold">
                Active Tray
              </div>
            </div>
            <div
              v-if="verificationContainer.id"
              class="col-auto"
            >
              <BaseButton
                flat
                dense
                icon="edit"
                color="grey-7"
                @click="showEditTrayModal = true"
              >
                <q-tooltip>Edit Tray Barcode</q-tooltip>
              </BaseButton>
            </div>
          </div>
          <div class="text-h5 text-weight-medium text-center">
            {{ verificationContainer.barcode?.value || 'No tray selected' }}
          </div>
          <div
            v-if="!verificationContainer.id"
            class="text-caption text-grey-7 text-center"
          >
            Scan a tray barcode above to begin
          </div>
        </q-card-section>
      </q-card>

      <!-- Items List Card -->
      <q-card
        v-if="verificationContainer.id"
        class="q-mb-lg"
      >
        <q-card-section class="q-pb-none">
          <div class="text-subtitle1 text-weight-bold">
            Items in Tray ({{ verificationContainer.items?.length || 0 }})
          </div>
        </q-card-section>
        <q-list separator>
          <q-item
            v-for="item in verificationContainer.items.slice().reverse()"
            :key="item.id"
            class="q-py-sm justify-center"
          >
            <q-item-section
              class="text-center"
              style="flex: 1;"
            >
              <q-item-label
                class="text-weight-medium"
                :class="!item.scanned_for_verification ? 'text-grey-6' : 'text-black'"
              >
                {{ renderItemBarcodeDisplay(item) }}
              </q-item-label>
            </q-item-section>
            <q-item-section side>
              <q-icon
                v-if="item.scanned_for_verification"
                name="mdi-check-circle"
                color="positive"
                size="24px"
              >
                <q-tooltip>Verified</q-tooltip>
              </q-icon>
              <BaseButton
                v-else
                flat
                dense
                round
                icon="mdi-delete"
                color="grey-7"
                :disabled="verificationJob.status === 'Paused' || verificationJob.status === 'Completed'"
                @click="deleteItemConfirm(item)"
              >
                <q-tooltip>Remove Item</q-tooltip>
              </BaseButton>
            </q-item-section>
          </q-item>
          <q-item v-if="!verificationContainer.items || verificationContainer.items.length === 0">
            <q-item-section>
              <q-item-label
                caption
                class="text-grey-6 text-center"
              >
                No items in this tray.
              </q-item-label>
            </q-item-section>
          </q-item>
        </q-list>
      </q-card>

      <!-- Trays in Job (Collapsible) -->
      <q-card class="q-mb-lg">
        <q-expansion-item
          dense
          expand-separator
          :label="`Trays in Job (${verificationJob.trays?.length || 0})`"
          header-class="text-subtitle1 text-weight-bold"
        >
          <q-card-section>
            <q-list
              bordered
              separator
            >
              <q-item
                v-for="tray in verificationJob.trays"
                :key="tray.id"
                class="verification-next-tray-item"
                :active="verificationContainer.id === tray.id"
                active-class="bg-blue-1 text-grey-8"
                clickable
                @click="navigateToTray(tray)"
              >
                <q-item-section>
                  <q-item-label class="text-h6 text-color-black">
                    {{ tray.barcode?.value }}
                  </q-item-label>
                  <q-item-label caption>
                    {{ tray.items.length }} Items
                  </q-item-label>
                </q-item-section>
                <q-item-section side>
                  <q-icon
                    v-if="tray.collection_verified"
                    name="mdi-check-circle"
                    color="positive"
                    size="25px"
                  />
                </q-item-section>
              </q-item>
              <q-item v-if="!verificationJob.trays || verificationJob.trays.length === 0">
                <q-item-section>
                  <q-item-label caption>
                    No trays added yet.
                  </q-item-label>
                </q-item-section>
              </q-item>
            </q-list>
          </q-card-section>
        </q-expansion-item>
      </q-card>
    </template>

    <!-- NON-TRAY VERIFICATION LAYOUT -->
    <template v-else>
      <VerificationNonTrayInfo
        ref="nonTrayInfoComponent"
        @print="batchSheetComponent.printBatchReport()"
        class="col-12"
      />
    </template>

    <!-- Mobile Action Bar -->
    <MobileActionBar
      v-if="currentScreenSize == 'xs' && verificationJob.trayed"
      button-one-color="warning"
      :button-one-icon="
        verificationJob.status !== 'Paused' ? 'mdi-pause' : 'mdi-play'
      "
      :button-one-label="
        verificationJob.status == 'Paused' ? 'Resume' : 'Pause'
      "
      :button-one-outline="verificationJob.status !== 'Paused'"
      :button-one-disabled="verificationJob.status == 'Completed'"
      @button-one-click="
        verificationJob.status == 'Paused'
          ? updateVerificationJobStatus('Running')
          : updateVerificationJobStatus('Paused')
      "
      button-two-color="positive"
      button-two-label="Complete"
      :button-two-outline="false"
      :button-two-disabled="
        !allTraysCompleted || !allItemsVerified || verificationJob.status == 'Paused' || verificationJob.status == 'Completed' || isItemQueueProcessing || isTrayQueueProcessing
      "
      :button-two-loading="appActionIsLoadingData"
      @button-two-click="
        showConfirmation = {
          type: 'completeJob',
          text: 'Are you sure you want to complete the job?',
        }
      "
    />
  </div>

  <!-- barcode edit modal -->
  <PopupModal
    v-if="showBarcodeEdit"
    ref="barcodeEditModal"
    :title="selectedItems.length == 1 ? 'Edit Barcode' : 'Enter Barcode'"
    @reset="resetBarcodeEdit"
    aria-label="barcodeEditModal"
  >
    <template #main-content>
      <q-card-section class="column no-wrap items-center">
        <div class="form-group">
          <label class="form-group-label"> Type Barcode </label>
          <TextInput
            v-model="manualBarcodeEdit"
            placeholder="Please Enter Barcode"
            @keyup.enter="
              selectedItems.length == 1
                ? updateContainerItem(manualBarcodeEdit)
                : triggerItemScan(manualBarcodeEdit)
            "
          />
        </div>
      </q-card-section>
    </template>

    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <BaseButton
          no-caps
          unelevated
          color="accent"
          label="Submit"
          class="text-body1 full-width"
          :disabled="!manualBarcodeEdit"
          :loading="appActionIsLoadingData"
          @click="
            selectedItems.length == 1
              ? updateContainerItem(manualBarcodeEdit)
              : triggerItemScan(manualBarcodeEdit)
          "
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

  <!-- confirmation modal -->
  <PopupModal
    v-if="showConfirmation !== null"
    ref="confirmationModal"
    :title="'Confirm'"
    :text="showConfirmation.text"
    :show-actions="false"
    @reset="showConfirmation = null"
    aria-label="confirmationModal"
  >
    <template #footer-content="{ hideModal }">
      <q-card-section
        v-if="showConfirmation.type == 'completeJob'"
        class="row no-wrap justify-between items-center q-pt-sm"
      >
        <BaseButton
          no-caps
          unelevated
          color="positive"
          label="Complete"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="
            handleConfirmationModal('completeJob')
          "
        />

        <q-space class="q-mx-xs" />

        <BaseButton
          no-caps
          unelevated
          color="positive"
          label="Complete & Print"
          class="btn-no-wrap text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="
            handleConfirmationModal('completePrint')
          "
        />

        <q-space
          v-if="currentScreenSize !== 'xs'"
          class="q-mx-lg"
        />

        <BaseButton
          v-if="currentScreenSize !== 'xs'"
          outline
          no-caps
          label="Cancel"
          class="text-body1 full-width"
          @click="hideModal"
        />
      </q-card-section>
      <q-card-section
        v-else-if="showConfirmation.type == 'delete'"
        class="row no-wrap justify-between items-center q-pt-sm"
      >
        <BaseButton
          no-caps
          unelevated
          color="accent"
          label="Confirm"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="
            handleConfirmationModal('delete')
          "
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
      <q-card-section
        v-else-if="showConfirmation.type == 'addItem'"
        class="row no-wrap justify-between items-center q-pt-sm"
      >
        <BaseButton
          no-caps
          unelevated
          color="accent"
          label="Add New Item"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="
            handleConfirmationModal('addItem')
          "
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
      <q-card-section
        v-else-if="showConfirmation.type == 'deleteItem'"
        class="row no-wrap justify-between items-center q-pt-sm"
      >
        <BaseButton
          no-caps
          unelevated
          color="negative"
          label="Delete Item"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="
            handleConfirmationModal('deleteItem')
          "
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
      <q-card-section
        v-else-if="showConfirmation.type == 'cancel'"
        class="row no-wrap justify-between items-center q-pt-sm"
      >
        <BaseButton
          no-caps
          unelevated
          color="negative"
          label="Confirm Cancel"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="
            handleConfirmationModal('cancel')
          "
        />
        <q-space class="q-mx-xs" />
        <BaseButton
          outline
          no-caps
          label="Back"
          class="text-body1 full-width"
          @click="hideModal"
        />
      </q-card-section>
    </template>
  </PopupModal>

  <!-- Edit Job Modal -->
  <PopupModal
    v-if="showEditJobModal"
    ref="editJobModalRef"
    :title="'Edit Job Settings'"
    :show-actions="false"
    @reset="showEditJobModal = false"
    aria-label="editJobModal"
  >
    <template #main-content>
      <q-card-section>
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
        <BaseButton
          no-caps
          unelevated
          color="accent"
          label="Save Changes"
          class="text-body1 full-width btn-modern"
          :loading="appActionIsLoadingData"
          @click="updateJobMediaType(hideModal)"
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

  <!-- Edit Tray Modal -->
  <PopupModal
    v-if="showEditTrayModal"
    ref="editTrayModalRef"
    :title="'Edit Tray Barcode'"
    @reset="showEditTrayModal = false"
    aria-label="editTrayModal"
  >
    <template #main-content>
      <q-card-section class="column no-wrap items-center">
        <div class="form-group">
          <label class="form-group-label"> New Tray Barcode </label>
          <TextInput
            v-model="editTrayBarcodeInput"
            placeholder="Enter new tray barcode"
            @keyup.enter="updateTrayBarcode"
          />
        </div>
      </q-card-section>
    </template>
    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <BaseButton
          no-caps
          unelevated
          color="accent"
          label="Save Changes"
          class="text-body1 full-width btn-modern"
          :disabled="!editTrayBarcodeInput"
          :loading="appActionIsLoadingData"
          @click="updateTrayBarcode"
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

  <!-- print component used to handle printing the template -->
  <VerificationBatchSheet
    ref="batchSheetComponent"
    :verification-job-details="verificationJob"
  />

  <!-- Audit trail modal -->
  <AuditTrail
    v-if="showAuditTrailModal"
    ref="historyModalRef"
    @hide="showAuditTrailModal = null"
    :job-type="showAuditTrailModal"
    :job-id="verificationJob.id"
  />


</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
import { ref, watch, inject, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { notify } from '@/utils/notify'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store'
import { useUserStore } from '@/stores/user-store'
import { useVerificationStore } from '@/stores/verification-store'
import { useAccessionStore } from '@/stores/accession-store'
import { useBarcodeStore } from '@/stores/barcode-store'
import { useOptionStore } from '@/stores/option-store'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import { useScanQueue } from '@/composables/useScanQueue.js'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'

import SelectInput from '@/components/SelectInput.vue'
import TextInput from '@/components/TextInput.vue'
import PopupModal from '@/components/PopupModal.vue'
import MoreOptionsMenu from '@/components/MoreOptionsMenu.vue'
import MobileActionBar from '@/components/MobileActionBar.vue'
import VerificationNonTrayInfo from '@/components/Verification/VerificationNonTrayInfo.vue'
import VerificationBatchSheet from '@/components/Verification/VerificationBatchSheet.vue'
import { audioAlert } from '@/utils/audio.js'
import AuditTrail from '@/components/AuditTrail.vue'

const router = useRouter()
const route = useRoute()

// Composables
const { compiledBarCode } = useBarcodeScanHandler({ waitForEnterKey: true })

// Scan queues for sequential processing of rapid scans
const { enqueue: enqueueItemScan, isProcessing: isItemQueueProcessing } = useScanQueue(processItemScan)
const { enqueue: enqueueTrayScan, isProcessing: isTrayQueueProcessing } = useScanQueue(processTrayScan)
const { currentScreenSize } = useCurrentScreenSize()
const { checkUserPermission } = usePermissionHandler()



// Store Data
const { appIsLoadingData, appActionIsLoadingData } = storeToRefs(useGlobalStore())
const { userData } = storeToRefs(useUserStore())
const { verifyBarcode, deleteBarcode, patchBarcode } = useBarcodeStore()
const { barcodeDetails } = storeToRefs(useBarcodeStore())
const {
  getOptions
} = useOptionStore()
const { mediaTypes, users } = storeToRefs(useOptionStore())
const {
  resetVerificationContainer,
  patchVerificationJob,
  patchVerificationTray,
  cancelVerificationJob, // imported here
  getVerificationTray,
  getVerificationNonTrayItem,
  verifyTrayItem,
  verifyNonTrayItem,
  // updateVerificationJobStatus removed to avoid conflict with local function
  deleteVerificationTrayItem,
  deleteVerificationNonTrayItem,
  postVerificationTrayItem,
  postVerificationNonTrayItem,
  patchVerificationTrayItem,
  patchVerificationNonTrayItem
} = useVerificationStore()
const {
  allItemsVerified,
  allTraysCompleted,
  verificationJob,
  verificationContainer
} = storeToRefs(useVerificationStore())
const { patchAccessionJob } = useAccessionStore()

// Local Data
const barcodeEditModal = ref(null)
const confirmationModal = ref(null)
const batchSheetComponent = ref(null)
const nonTrayInfoComponent = ref(null)
// verificationTableComponent removed

// UI State
const scanBarcodeInput = ref('')
const showEditJobModal = ref(false)
const showEditTrayModal = ref(false)
const showBarcodeEdit = ref(false)
const manualBarcodeEdit = ref('')
const editTrayBarcodeInput = ref('')
const selectedMediaType = ref(null)
const showConfirmation = ref(null)
const showAuditTrailModal = ref(null)
const selectedItems = ref([])
const editJob = ref(false)
const actionLoading = ref(false)

watch(editJob, async (newVal) => {
  if (newVal) {
    try {
      appActionIsLoadingData.value = true
      await getOptions('users')
    } catch (error) {
      console.error('Failed to load users', error)
    } finally {
      appActionIsLoadingData.value = false
    }
  }
})

// renderIsEditMode removed

// Logic


const renderItemBarcodeDisplay = inject('render-item-barcode-display')
const currentIsoDate = inject('current-iso-date')

// verificationTableColumns removed

const isNextTrayEnabled = computed(() => {
  return verificationJob.value.status !== 'Paused' &&
         verificationJob.value.status !== 'Completed' &&
         allItemsVerified.value &&
         !allTraysCompleted.value
})

const isAssignedToOtherUser = computed(() => {
  return verificationJob.value.assigned_user_id && verificationJob.value.assigned_user_id !== userData.value.user_id
})

const getStatusColor = (status) => {
  switch (status) {
    case 'Running':
      return 'positive'
    case 'Paused':
      return 'warning'
    case 'Completed':
      return 'primary'
    case 'Cancelled':
      return 'negative'
    default:
      return 'grey'
  }
}

const handleKeyboardShortcut = (event) => {
  const activeEl = document.activeElement
  if (activeEl && (activeEl.tagName === 'INPUT' || activeEl.tagName === 'TEXTAREA')) {
    return
  }

  if (event.key.toLowerCase() === 't') {
    event.preventDefault()
    if (isNextTrayEnabled.value) {
      setNextVerificationTray()
    }
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeyboardShortcut)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeyboardShortcut)
})

watch(
  () => route.params.containerId,
  async (newContainerId) => {
    if (newContainerId) {
      if (verificationContainer.value.barcode?.value === newContainerId) {
        return
      }
      try {
        appIsLoadingData.value = true
        await getVerificationTray(newContainerId)

        // Ensure job is running if we navigate to a specific tray
        if (
          verificationJob.value.status !== 'Running' &&
          verificationJob.value.status !== 'Completed'
        ) {
          await updateVerificationJobStatus('Running')
        }
      } catch (error) {
        notify({
          type: 'negative',
          message: `Failed to load tray: ${error.response?.data?.detail || error}`
        })
      } finally {
        appIsLoadingData.value = false
      }
    } else {
      resetVerificationContainer()
    }
  },
  { immediate: true }
)

watch(compiledBarCode, (barcode_value) => {
  if (verificationJob.value.status == 'Paused' || verificationJob.value.status == 'Completed' || barcode_value === '') {
    return
  }

  // If on tray view, assume item scan
  if (verificationJob.value.trayed && verificationContainer.value.id) {
    enqueueItemScan(barcode_value)
  } else if (verificationJob.value.trayed && !verificationContainer.value.id) {
    // If no tray selected (trayed job), assume tray scan
    enqueueTrayScan(barcode_value)
  } else if (!verificationJob.value.trayed) {
    // If non-trayed
    enqueueItemScan(barcode_value)
  }
})

// Instant-clear input handlers for q-input @keyup.enter
const handleTrayScanInput = () => {
  const value = scanBarcodeInput.value
  scanBarcodeInput.value = ''
  enqueueTrayScan(value)
}

const handleItemScanInput = () => {
  const value = scanBarcodeInput.value
  scanBarcodeInput.value = ''
  enqueueItemScan(value)
}

// Queue processor for tray scans
async function processTrayScan (barcode_value) {
  // Prevent scan if assigned to another user
  if (isAssignedToOtherUser.value) {
    notify({
      type: 'negative',
      message: 'This job is assigned to another user. You cannot verify items.'
    })
    return
  }

  try {
    if (verificationJob.value.trays && !verificationJob.value.trays.some(tray => tray.barcode.value == barcode_value)) {
      notify({
        type: 'negative',
        message: `The scanned tray ${barcode_value} doesn't exist on this verification job. Please scan a tray that is associated to this job.`
      })
      return
    } else {
      appIsLoadingData.value = true
      await getVerificationTray(barcode_value)

      if (!verificationContainer.value.scanned_for_verification && verificationJob.value.status !== 'Completed') {
        await patchVerificationTray({
          id: verificationContainer.value.id,
          scanned_for_verification: true
        })
      }

      if (verificationJob.value.status !== 'Running' && verificationJob.value.status !== 'Completed') {
        await updateVerificationJobStatus('Running')
      }

      if (verificationContainer.value.id) {
        router.push({
          name: 'verification-container',
          params: {
            jobId: verificationJob.value.workflow_id,
            containerId: verificationContainer.value.barcode.value
          }
        })
      }
    }
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
    resetVerificationContainer()
  } finally {
    appIsLoadingData.value = false
  }
}

// Queue processor for item scans
async function processItemScan (barcode_value) {
  // Prevent scan if assigned to another user
  if (isAssignedToOtherUser.value) {
    notify({
      type: 'negative',
      message: 'This job is assigned to another user. You cannot verify items.'
    })
    return
  }

  try {
    appActionIsLoadingData.value = true
    await verifyBarcode(barcode_value, 'Item', true)

    if (verificationJob.value.trayed) {
      if (
        verificationJob.value.trayed &&
        verificationContainer.value.items.some(
          (item) => item.barcode.id == barcodeDetails.value.id
        )
      ) {
        await validateItemBarcode()
      } else {
        showConfirmation.value = {
          type: 'addItem',
          text: 'Are you sure you want to add a new item to the job?'
        }
        audioAlert()
      }
    } else {
      if (
        verificationContainer.value.id &&
        !verificationContainer.value.scanned_for_verification
      ) {
        await validateItemBarcode()
      }

      if (
        verificationJob.value.non_tray_items.some(
          (item) => item.barcode_id == barcodeDetails.value.id
        )
      ) {
        await getVerificationNonTrayItem(barcode_value)
      } else {
        showConfirmation.value = {
          type: 'addItem',
          text: 'Are you sure you want to add a new item to the job?'
        }
        audioAlert()
      }

      if (verificationJob.value.status !== 'Running' && verificationJob.value.status !== 'Completed') {
        await updateVerificationJobStatus('Running')
      }

      if (verificationContainer.value.id) {
        router.push({
          name: 'verification-container',
          params: {
            jobId: verificationJob.value.workflow_id,
            containerId: renderItemBarcodeDisplay(verificationContainer.value)
          }
        })
      }
    }
  } catch (error) {
    notify({
      type: 'negative',
      message: `Error with barcode "${barcode_value}": ${error.response?.data?.detail || error}`
    })
  } finally {
    appActionIsLoadingData.value = false
    if (showBarcodeEdit.value && barcodeEditModal.value) {
      barcodeEditModal.value.hideModal()
    }
  }
}

// Keep triggerItemScan as an alias for template barcode edit modal calls
const triggerItemScan = (barcode_value) => {
  enqueueItemScan(barcode_value)
}

const validateItemBarcode = async () => {
  try {
    appActionIsLoadingData.value = true
    if (verificationJob.value.trayed) {
      const trayItemId = verificationContainer.value.items.find(
        (item) => item.barcode.id == barcodeDetails.value.id
      ).id
      await verifyTrayItem(trayItemId)
      // Removed "Item Verified" notification as requested
    } else {
      const nonTrayItemId = verificationContainer.value.id
      await verifyNonTrayItem(nonTrayItemId)
    }
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}

const addContainerItem = async () => {
  try {
    appActionIsLoadingData.value = true
    const currentDate = new Date()
    // Mimic payload structure from previous implementation
    if (verificationJob.value.trayed) {
      const payload = {
        accession_dt: verificationContainer.value.accession_dt,
        arbitrary_data: 'Signed copy', // Default or derived?
        barcode_id: barcodeDetails.value.id,
        barcode_value: barcodeDetails.value.value,
        condition: 'Good',
        media_type_id: verificationContainer.value.media_type_id,
        scanned_for_verification: true,
        size_class_id: verificationContainer.value.size_class_id,
        status: 'In',
        tray_id: verificationContainer.value.id,
        verification_job_id: verificationJob.value.id,
        volume: 'I',
        withdrawal_dt: currentDate,
        user_id: userData.value.user_id
        // NOTE: This payload seems very specific to a certain item type.
        // Ideally we should use the same logic as Accession if creating new items.
        // But for verification add, we assume item exists or we are creating a stub?
      }
      await postVerificationTrayItem(payload)
    } else {
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
    }
    notify({
      type: 'positive',
      message: 'Item has been added to the job.'
    })
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
  } finally {
    appActionIsLoadingData.value = false
    showConfirmation.value = null
  }
}

const updateContainerItem = async (barcode_value) => {
  try {
    appActionIsLoadingData.value = true
    await verifyBarcode(barcode_value, 'Item', true)

    if (
      verificationJob.value.trayed &&
      verificationContainer.value.items.some(
        (item) => item.barcode.id == barcodeDetails.value.id
      )
    ) {
      notify({
        type: 'negative',
        message: 'This Barcode already exists in this container.'
      })
      return
    }

    // We assume selectedItems has 1 item if this is called
    const itemId = selectedItems.value[0]?.id
    if (!itemId) {
      return
    }

    await patchBarcode(selectedItems.value[0].barcode.id, barcode_value)

    if (verificationJob.value.trayed) {
      await patchVerificationTrayItem({
        id: itemId,
        barcode: { value: barcode_value }
      })
    } else {
      await patchVerificationNonTrayItem({
        id: itemId,
        barcode: { value: barcode_value }
      })

      router.push({
        name: 'verification-container',
        params: {
          jobId: verificationJob.value.workflow_id,
          containerId: barcode_value
        }
      })
    }

    // Update local state potentially? Store should handle it on refetch/reactivity
    notify({
      type: 'positive',
      message: 'The item has been updated.'
    })

    selectedItems.value = []
    if (showBarcodeEdit.value && barcodeEditModal.value) {
      barcodeEditModal.value.hideModal()
    }
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}

const deleteItemConfirm = (item) => {
  selectedItems.value = [item]
  showConfirmation.value = {
    type: 'deleteItem',
    text: `Are you sure you want to delete item ${renderItemBarcodeDisplay(item)}?`,
    item
  }
}

const handleConfirmationModal = async (type) => {
  try {
    appActionIsLoadingData.value = true
    if (type === 'completeJob' || type === 'completePrint') {
      if (verificationJob.value.status !== 'Completed') {
        await updateVerificationJobStatus('Completed')
      }
      if (type === 'completePrint') {
        batchSheetComponent.value.printBatchReport()
      }
      notify({
        type: 'positive',
        message: 'Job Completed'
      })
      router.push({
        name: 'verification',
        params: { jobId: null }
      })
    } else if (type === 'deleteItem') {
      const item = showConfirmation.value.item
      if (verificationJob.value.trayed) {
        await deleteVerificationTrayItem([item])
      } else {
        await deleteVerificationNonTrayItem([item])
      }
      // Also delete barcode?
      // Original code did: await deleteBarcode(item.barcode.id)
      // We should probably safeguard this. If we delete the verification item, do we delete the barcode globally?
      // Yes, usually we assume it was a mistake scan or we are removing it entirely.
      if (item.barcode?.id) {
        await deleteBarcode(item.barcode.id)
      }

      notify({
        type: 'positive',
        message: 'Item Deleted'
      })
      selectedItems.value = []
    } else if (type === 'delete') {
      // Bulk delete logic if needed
      if (verificationJob.value.trayed) {
        await deleteVerificationTrayItem(selectedItems.value)
      } else {
        await deleteVerificationNonTrayItem(selectedItems.value)
      }
      // Bulk delete barcodes
      await Promise.all(selectedItems.value.map(i => deleteBarcode(i.barcode.id)))
      notify({
        type: 'positive',
        message: 'Items removed'
      })
      selectedItems.value = []
    } else if (type === 'addItem') {
      await addContainerItem()
    } else if (type === 'cancel') {
      const accessionJobId = verificationJob.value.accession_job_id
      await cancelVerificationJob(verificationJob.value.id)

      if (accessionJobId) {
        await patchAccessionJob({
          id: accessionJobId,
          status: 'Running'
        })
        notify({
          type: 'positive',
          message: 'Verification Job cancelled. Accession Job returned to Running status.'
        })
      } else {
        notify({
          type: 'positive',
          message: 'Verification Job cancelled.'
        })
      }
      router.push({ name: 'home' })
    }
  } catch (err) {
    notify({
      type: 'negative',
      message: err.message || err
    })
  } finally {
    appActionIsLoadingData.value = false
    showConfirmation.value = null
  }
}

const resetBarcodeEdit = () => {
  showBarcodeEdit.value = false
  manualBarcodeEdit.value = ''
}

const setBarcodeEditDisplay = () => {
  if (selectedItems.value.length === 1) {
    manualBarcodeEdit.value = selectedItems.value[0].barcode.value
  } else {
    manualBarcodeEdit.value = ''
  }
  showBarcodeEdit.value = true
}

const navigateToTray = (tray) => {
  if (verificationContainer.value.id === tray.id) {
    return
  }
  router.push({
    name: 'verification-container',
    params: {
      jobId: verificationJob.value.workflow_id,
      containerId: tray.barcode.value
    }
  })
}

const setNextVerificationTray = async () => {
  // As per user request: "Next Tray" button should simply clear the active tray
  // and await the user to scan another tray, handling out-of-order execution.
  router.push({
    name: 'verification',
    params: {
      jobId: verificationJob.value.workflow_id
    }
  })
}

const handleOptionMenu = (option) => {
  if (option.action) {
    option.action()
    return
  }
  if (option.text === 'Edit') {
    selectedMediaType.value = verificationJob.value.media_type
    showEditJobModal.value = true
  } else if (option.text === 'Print Job') {
    batchSheetComponent.value.printBatchReport()
  } else if (option.text === 'Cancel Job') {
    // Implement cancel logic or use legacy modal
    // For now we assume permission check passed
    // We can call an action that shows a cancel confirm?
    // Accession has separate logic. Verification used to just use a text variable to show modal.
    showConfirmation.value = {
      type: 'cancel',
      text: 'Are you sure you want to cancel this job?'
    }
  } else if (option.text === 'View History') {
    showAuditTrailModal.value = 'verification_jobs'
  } else if (option.text == 'Edit Barcode' || option.text == 'Enter Barcode') {
    setBarcodeEditDisplay()
  } else if (option.text == 'Delete Items') {
    showConfirmation.value = {
      type: 'delete',
      text: 'Are you sure you want to delete selected items?'
    }
  }
}

const menuOptions = computed(() => {
  const commonOptions = [
    {
      text: 'Assign User',
      icon: 'person_add',
      color: 'grey',
      hidden: !checkUserPermission('can_assign_jobs'),
      disabled: editJob.value || verificationJob.value.status === 'Completed' || verificationJob.value.status === 'Paused',
      action: () => {
        editJob.value = true
      }
    },
    {
      text: 'Edit',
      disabled: verificationJob.value.status === 'Completed'
    },
    { text: 'Print Job' },
    {
      text: 'Cancel Job',
      optionClass: 'text-negative',
      disabled: verificationJob.value.status === 'Completed',
      hidden: !checkUserPermission('can_cancel_verification_job')
    },
    { text: 'View History' }
  ]
  return commonOptions
})

const updateUserAssignment = async () => {
  actionLoading.value = true
  try {
    const payload = {
      id: verificationJob.value.id,
      assigned_user_id: verificationJob.value.assigned_user_id,
      run_timestamp: new Date().toISOString()
    }
    await patchVerificationJob(payload)

    notify({
      type: 'positive',
      message: 'User assigned successfully'
    })
    editJob.value = false
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to update user assignment'
    })
  } finally {
    actionLoading.value = false
  }
}



const updateVerificationJobStatus = async (status) => {
  try {
    appIsLoadingData.value = true
    const payload = {
      id: verificationJob.value.id,
      status,
      assigned_user_id: verificationJob.value.assigned_user_id || userData.value.user_id,
      run_timestamp: currentIsoDate()
    }
    await patchVerificationJob(payload)

    notify({
      type: 'positive',
      message: `Job Status has been updated to: ${status}`
    })
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
  } finally {
    appIsLoadingData.value = false
  }
}

const updateJobMediaType = async (hideModal) => {
  try {
    appActionIsLoadingData.value = true
    await patchVerificationJob({
      id: verificationJob.value.id,
      media_type_id: selectedMediaType.value.id
    })
    notify({
      type: 'positive',
      message: 'Job Settings Updated'
    })
    hideModal()
  } catch (err) {
    notify({
      type: 'negative',
      message: err.message || err.toString()
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}

const updateTrayBarcode = async () => {
  // If we want to support renaming tray barcode (likely rare but possible)
  // verificationContainer.value.id
  // But we need backend support.
  // For now, let's just alert
  notify({
    type: 'warning',
    message: 'Editing tray barcode is not fully implemented.'
  })
  showEditTrayModal.value = false
}

// Ensure legacy function references that might be called from template are handled
// We removed 'updateVerificationJobStatus' duplication.
</script>
