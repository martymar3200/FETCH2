<template>
  <div class="accession-container">
    <!-- TRAYED ACCESSION LAYOUT -->
    <template v-if="accessionJob.trayed">
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
          <!-- Complete Job button - always visible when running, disabled if not ready -->
          <q-btn
            v-if="accessionJob.status !== 'Completed'"
            no-caps
            unelevated
            color="positive"
            label="Complete Job"
            class="btn-modern"
            :disabled="!allItemsVerified || accessionJob.status === 'Paused' || isItemQueueProcessing || isTrayQueueProcessing"
            @click="
              showConfirmation = {
                type: 'completeJob',
                text: 'Are you sure you want to complete the job?',
              }
            "
          />
        </div>
      </div>

      <!-- Scan Section Card - Shows for both tray scanning and item scanning -->
      <q-card
        v-if="accessionJob.status === 'Running'"
        class="q-mb-lg"
      >
        <q-card-section>
          <div class="row q-col-gutter-md items-end">
            <!-- Tray Scan (when no tray selected) -->
            <template v-if="!accessionContainer.id">
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
                  Tip: Scan a tray barcode to add items
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
                <q-btn
                  no-caps
                  unelevated
                  icon="add"
                  color="accent"
                  label="Next Tray"
                  class="btn-modern"
                  :disabled="
                    !accessionContainer.id ||
                      !allItemsVerified ||
                      accessionJob.status == 'Paused' ||
                      accessionJob.status == 'Completed'
                  "
                  @click="addNewTray"
                >
                  <q-tooltip>Add a new tray to this job (Shortcut: T)</q-tooltip>
                </q-btn>
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
              v-if="accessionContainer.id"
              class="col-auto"
            >
              <q-btn
                flat
                dense
                icon="edit"
                color="grey-7"
                @click="handleOptionMenu({ text: 'Edit Tray Barcode' })"
              >
                <q-tooltip>Edit Tray Barcode</q-tooltip>
              </q-btn>
            </div>
          </div>
          <div class="text-h5 text-weight-medium text-center">
            {{ accessionContainer.barcode?.value || 'No tray selected' }}
          </div>
          <div
            v-if="!accessionContainer.id"
            class="text-caption text-grey-7 text-center"
          >
            Scan a tray barcode above to begin
          </div>
        </q-card-section>
      </q-card>

      <!-- Items List Card -->
      <q-card
        v-if="accessionContainer.id"
        class="q-mb-lg"
      >
        <q-card-section class="q-pb-none">
          <div class="text-subtitle1 text-weight-bold">
            Items in Tray ({{ accessionContainer.items?.length || 0 }})
          </div>
        </q-card-section>
        <q-list separator>
          <q-item
            v-for="item in accessionContainer.items.slice().reverse()"
            :key="item.id"
            class="q-py-sm justify-center"
          >
            <q-item-section
              class="text-center"
              style="flex: 1;"
            >
              <q-item-label class="text-weight-medium">
                {{ renderItemBarcodeDisplay(item) }}
              </q-item-label>
            </q-item-section>
            <q-item-section side>
              <q-btn
                flat
                dense
                round
                icon="undo"
                color="grey-7"
                :disabled="accessionJob.status === 'Paused' || accessionJob.status === 'Completed'"
                @click="deleteItemConfirm(item)"
              >
                <q-tooltip>Remove Item</q-tooltip>
              </q-btn>
            </q-item-section>
          </q-item>
          <q-item v-if="!accessionContainer.items || accessionContainer.items.length === 0">
            <q-item-section>
              <q-item-label
                caption
                class="text-grey-6"
              >
                No items scanned yet. Scan an item barcode to add it.
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
          :label="`Trays in Job (${accessionJob.trays?.length || 0})`"
          header-class="text-subtitle1 text-weight-bold"
        >
          <q-card-section>
            <q-list
              bordered
              separator
            >
              <q-item
                v-for="tray in accessionJob.trays"
                :key="tray.id"
                class="accession-next-tray-item"
                :active="accessionContainer.id === tray.id"
                active-class="bg-blue-1 text-grey-8"
                clickable
                @click="navigateToTray(tray)"
              >
                <q-item-section>
                  <q-item-label class="text-h6 text-color-black">
                    {{ tray.barcode.value }}
                  </q-item-label>
                  <q-item-label caption>
                    {{ tray.items.length }} Items
                  </q-item-label>
                </q-item-section>
              </q-item>
              <q-item v-if="!accessionJob.trays || accessionJob.trays.length === 0">
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

    <!-- NON-TRAYED ACCESSION LAYOUT (keep original behavior) -->
    <template v-else>
      <AccessionNonTrayInfo
        ref="nonTrayInfoComponent"
        @print="batchSheetComponent.printBatchReport()"
        @scan="triggerItemScan"
        class="col-12"
      />
    </template>

    <!-- Mobile Action Bar -->
    <MobileActionBar
      v-if="currentScreenSize == 'xs' && !renderIsEditMode && accessionJob.trayed"
      button-one-color="warning"
      :button-one-icon="
        accessionJob.status !== 'Paused' ? 'mdi-pause' : 'mdi-play'
      "
      :button-one-label="
        accessionJob.status == 'Paused' ? 'Resume' : 'Pause'
      "
      :button-one-outline="accessionJob.status !== 'Paused'"
      :button-one-disabled="accessionJob.status == 'Completed'"
      @button-one-click="
        accessionJob.status == 'Paused'
          ? updateAccessionJobStatus('Running')
          : updateAccessionJobStatus('Paused')
      "
      button-two-color="positive"
      button-two-label="Complete"
      :button-two-outline="false"
      :button-two-disabled="
        !allItemsVerified || accessionJob.status == 'Paused' || accessionJob.status == 'Completed' && verificationJobGenerated || isItemQueueProcessing || isTrayQueueProcessing
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
        <q-btn
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

  <!-- confirmation modal -->
  <PopupModal
    ref="confirmationModal"
    v-if="showConfirmation !== null"
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
        <q-btn
          no-caps
          unelevated
          color="accent"
          label="Complete"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="
            handleConfirmation('completeJob');
          "
        />

        <q-space class="q-mx-xs" />

        <q-btn
          no-caps
          unelevated
          color="accent"
          label="Complete & Print"
          class="btn-no-wrap text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="
            handleConfirmation('completePrint');
          "
        />

        <q-space
          v-if="currentScreenSize !== 'xs'"
          class="q-mx-lg"
        />

        <q-btn
          v-if="currentScreenSize !== 'xs'"
          outline
          no-caps
          label="Cancel"
          class="accession-modal-btn text-body1 full-width"
          @click="hideModal"
        />
      </q-card-section>
      <q-card-section
        v-else-if="showConfirmation.type == 'deleteItem'"
        class="row no-wrap justify-between items-center q-pt-sm"
      >
        <q-btn
          no-caps
          unelevated
          color="negative"
          label="Delete Item(s)"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="
            handleConfirmation('deleteItem')
          "
        />

        <q-space class="q-mx-xs" />

        <q-btn
          outline
          no-caps
          label="Cancel"
          class="accession-modal-btn text-body1 full-width"
          @click="hideModal"
        />
      </q-card-section>
      <q-card-section
        v-else-if="showConfirmation.type == 'confirmReaccession'"
        class="row no-wrap justify-between items-center q-pt-sm"
      >
        <q-btn
          no-caps
          unelevated
          color="accent"
          label="Confirm"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="
            handleConfirmation('confirmReaccession')
          "
        />

        <q-space class="q-mx-xs" />

        <q-btn
          outline
          no-caps
          label="Cancel"
          class="accession-modal-btn text-body1 full-width"
          @click="hideModal"
        />
      </q-card-section>
    </template>
  </PopupModal>

  <!-- Tray barcode edit modal -->
  <PopupModal
    v-if="showEditTrayModal"
    ref="trayBarcodeModalRef"
    :title="'Edit Tray Barcode'"
    @reset="showEditTrayModal = false; trayBarcodeInput = '';"
    aria-label="trayBarcodeEditModal"
  >
    <template #main-content>
      <q-card-section class="column no-wrap items-center">
        <div class="form-group">
          <label class="form-group-label">
            Type Barcode
          </label>
          <TextInput
            v-model="trayBarcodeInput"
            placeholder="Please Enter Tray Barcode"
            @keyup.enter="!trayBarcodeInput ? null : updateTrayContainerBarcode()"
          />
        </div>
      </q-card-section>
    </template>

    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <q-btn
          no-caps
          unelevated
          color="accent"
          label="Submit"
          class="text-body1 full-width"
          :disabled="!trayBarcodeInput"
          :loading="appActionIsLoadingData"
          @click="updateTrayContainerBarcode"
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

  <!-- Tray confirmation modal (cancel job / delete tray) -->
  <PopupModal
    v-if="showTrayConfirmation"
    ref="trayConfirmationModalRef"
    :title="'Confirm'"
    :text="showTrayConfirmation == 'CancelJob' ? 'Are you sure you want to cancel the accession job? Warning: All associated trays and items will be deleted.' : 'Are you sure you want to delete the tray? Warning: All associated tray items will be deleted.'"
    :show-actions="false"
    @reset="showTrayConfirmation = null"
    aria-label="trayConfirmationModal"
  >
    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <q-btn
          v-if="showTrayConfirmation == 'CancelJob'"
          no-caps
          unelevated
          color="negative"
          label="Cancel Job"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="cancelAccessionJob()"
        />
        <q-btn
          v-else
          no-caps
          unelevated
          color="negative"
          label="Delete Tray"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="removeTrayContainer()"
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
        <q-btn
          no-caps
          unelevated
          color="accent"
          label="Save Changes"
          class="text-body1 full-width btn-modern"
          :loading="appActionIsLoadingData"
          @click="updateJobMediaType(hideModal)"
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
    ref="historyModalRef"
    @reset="showAuditTrailModal = null"
    :job-type="showAuditTrailModal"
    :job-id="accessionJob.id"
  />

  <!-- print component used to handle printing the template -->
  <AccessionBatchSheet
    ref="batchSheetComponent"
    :accession-job-details="accessionJob"
  />
</template>

<script setup>
import { ref, watch, computed, inject, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Notify } from 'quasar'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store'
import { useUserStore } from '@/stores/user-store'
import { useAccessionStore } from '@/stores/accession-store'
import { useVerificationStore } from '@/stores/verification-store'
import { useBarcodeStore } from '@/stores/barcode-store'
import { useOptionStore } from '@/stores/option-store'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import { useScanQueue } from '@/composables/useScanQueue.js'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import AccessionNonTrayInfo from '@/components/Accession/AccessionNonTrayInfo.vue'
import TextInput from '@/components/TextInput.vue'
import PopupModal from '@/components/PopupModal.vue'
import MoreOptionsMenu from '@/components/MoreOptionsMenu.vue'
import MobileActionBar from '@/components/MobileActionBar.vue'
import AccessionBatchSheet from '@/components/Accession/AccessionBatchSheet.vue'
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
const { verifyBarcode, patchBarcode, deleteBarcode } = useBarcodeStore()
const { barcodeDetails } = storeToRefs(useBarcodeStore())
const {
  resetAccessionContainer,
  patchAccessionJob,
  patchAccessionTray,
  deleteAccessionTray,
  getAccessionTray,
  postAccessionTray,
  postAccessionTrayItem,
  patchAccessionTrayItem,
  deleteAccessionTrayItem,
  getAccessionNonTrayItem,
  postAccessionNonTrayItem,
  patchAccessionNonTrayItem,
  deleteAccessionNonTrayItem
} = useAccessionStore()
const { accessionJob, accessionContainer, allItemsVerified } = storeToRefs(useAccessionStore())
const { getVerificationJobByAccessionId } = useVerificationStore()
const { getOptions } = useOptionStore()
const { sizeClass, mediaTypes } = storeToRefs(useOptionStore())

// Local Data
const barcodeEditModal = ref(null)
const confirmationModal = ref(null)
const trayBarcodeModalRef = ref(null)
const trayConfirmationModalRef = ref(null)
const historyModalRef = ref(null)
const nonTrayInfoComponent = ref(null)
const batchSheetComponent = ref(null)
const selectedItems = ref([])
const showConfirmation = ref(null)
const showBarcodeEdit = ref(false)
const manualBarcodeEdit = ref('')
const scanBarcodeInput = ref('')
// Tray modal state
const showEditTrayModal = ref(false)
const trayBarcodeInput = ref('')
const showTrayConfirmation = ref(null)
const showAuditTrailModal = ref(null)
const showEditJobModal = ref(false)
const selectedMediaType = ref(null)
const verificationJobGenerated = ref(false)
// isProcessingItemScan removed — the scan queue handles sequential processing
const renderIsEditMode = computed(() => {
  return false
})

const dotMenuOptions = computed(() => {
  if (!route.params.containerId) {
    return [
      {
        text: 'Edit',
        disabled: accessionJob.value.status == 'Completed'
      },
      {
        text: 'Pause Job',
        disabled: accessionJob.value.status != 'Running'
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
  } else {
    return [
      {
        text: 'Edit',
        disabled: accessionJob.value.status == 'Completed'
      },
      {
        text: 'Pause Job',
        disabled: accessionJob.value.status != 'Running'
      },
      {
        text: 'Cancel Job',
        optionClass: 'text-negative',
        disabled: accessionJob.value.status == 'Completed',
        hidden: !(checkUserPermission('can_cancel_accession') || checkUserPermission('can_cancel_accession_job'))
      },
      {
        text: 'Delete Tray',
        optionClass: 'text-negative',
        disabled: accessionJob.value.status == 'Completed'
      },
      { text: 'Print Job' },
      { text: 'View History' }
    ]
  }
})

// Logic

const currentIsoDate = inject('current-iso-date')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')

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

const isAddTrayEnabled = computed(() => {
  return accessionContainer.value.id &&
         allItemsVerified.value &&
         accessionJob.value.status !== 'Paused' &&
         accessionJob.value.status !== 'Completed'
})

const handleKeyboardShortcut = (event) => {
  const activeEl = document.activeElement
  if (activeEl && (activeEl.tagName === 'INPUT' || activeEl.tagName === 'TEXTAREA')) {
    return
  }

  if (event.key.toLowerCase() === 't') {
    event.preventDefault()
    if (isAddTrayEnabled.value) {
      addNewTray()
    }
  }
}

onMounted(() => {
  if (accessionJob.value.status == 'Completed') {
    checkVerificationJobGeneration()
  }
  document.addEventListener('keydown', handleKeyboardShortcut)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeyboardShortcut)
})

watch(route, () => {
  if (!route.params.containerId) {
    resetAccessionContainer()
  }
})

watch(compiledBarCode, (barcode) => {
  if (accessionJob.value.status == 'Paused' || accessionJob.value.status == 'Completed' || barcode === '') {
    return
  }

  if (
    accessionJob.value.trayed &&
    accessionContainer.value.id
  ) {
    enqueueItemScan(barcode)
  } else if (accessionJob.value.trayed == false) {
    enqueueItemScan(barcode)
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
  try {
    appIsLoadingData.value = true

    // Stop the scan if no size class matches the scanned tray
    await getOptions('sizeClass', { short_name: barcode_value.slice(0, 2) })
    const generateSizeClass = sizeClass.value.find(size => size.short_name == barcode_value.slice(0, 2))?.id
    if (!generateSizeClass && accessionJob.value.status !== 'Completed') {
      Notify.create({
        type: 'negative',
        message: `The tray can not be added, the container size ${barcode_value.slice(0, 2)} doesnt exist in the system. Please add it and try again.`
      })
      return
    }

    // Check if the barcode is in the system otherwise create it
    await verifyBarcode(barcode_value, 'Tray', true)

    // If the scanned tray exists in the accessionJob load the tray details
    if (accessionJob.value.trays && (
      accessionJob.value.trays.some(tray => tray.barcode_id == barcodeDetails.value.id) ||
      accessionJob.value.trays.some(tray => tray.withdrawn_barcode?.id == barcodeDetails.value.id)
    )) {
      await getAccessionTray(barcode_value)
    } else if (accessionJob.value.status !== 'Completed') {
      // If the scanned tray barcode doesn't exist create the scanned tray
      const currentDate = new Date()
      const payload = {
        accession_dt: currentDate,
        accession_job_id: accessionJob.value.id,
        barcode_id: barcodeDetails.value.id,
        collection_accessioned: false,
        media_type_id: accessionJob.value.media_type_id,
        scanned_for_accession: true,
        size_class_id: generateSizeClass
      }
      await postAccessionTray(payload)
    }

    // Set the scanned tray barcode as the container id in the route
    if (accessionContainer.value.id) {
      router.push({
        name: 'accession-container',
        params: {
          jobId: accessionJob.value.workflow_id,
          containerId: renderItemBarcodeDisplay(accessionContainer.value)
        }
      })
    }
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
  } finally {
    appIsLoadingData.value = false
  }
}

// Queue processor for item scans
async function processItemScan (barcode_value) {
  try {
    appActionIsLoadingData.value = true
    const res = await verifyBarcode(barcode_value.trim(), 'Item', true)
    if (res == 'barcode_exists' && barcodeDetails.value.withdrawn) {
      showConfirmation.value = {
        type: 'confirmReaccession',
        text: 'This item barcode has been withdrawn. Are you sure you want to re-accession this item?'
      }
      return
    }

    if (accessionJob.value.trayed) {
      if (
        accessionJob.value.trayed &&
        accessionContainer.value.items.some(
          (item) => item.barcode.id == barcodeDetails.value.id
        )
      ) {
        Notify.create({
          type: 'negative',
          message: 'The scanned item was already added to this tray.'
        })
        return
      } else {
        await addContainerItem()
      }
    } else {
      // Check if item already exists in job by barcode_id OR barcode.value
      const existingItem = accessionJob.value.non_tray_items.find(
        (item) => item.barcode_id == barcodeDetails.value.id || item.barcode?.value === barcode_value.trim()
      )
      if (existingItem) {
        await getAccessionNonTrayItem(barcode_value)
      } else {
        await addContainerItem()
      }

      if (accessionContainer.value.id) {
        router.push({
          name: 'accession-container',
          params: {
            jobId: accessionJob.value.workflow_id,
            containerId: renderItemBarcodeDisplay(accessionContainer.value)
          }
        })
      }
    }
  } catch (error) {
    Notify.create({
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

// Keep triggerItemScan as an alias for the non-tray child component's @scan emit
const triggerItemScan = (barcode_value) => {
  enqueueItemScan(barcode_value)
}
const resetBarcodeEdit = () => {
  showBarcodeEdit.value = false
  manualBarcodeEdit.value = ''
}
const setBarcodeEditDisplay = () => {
  showBarcodeEdit.value = true
  if (selectedItems.value.length == 1) {
    manualBarcodeEdit.value = selectedItems.value[0].barcode.value
  }
}
const addContainerItem = async () => {
  try {
    appIsLoadingData.value = true
    const currentDate = new Date()
    if (accessionJob.value.trayed) {
      const payload = {
        accession_dt: currentDate,
        accession_job_id: accessionJob.value.id,
        barcode_id: barcodeDetails.value.id,
        media_type_id: accessionContainer.value.media_type_id,
        scanned_for_accession: true,
        size_class_id: accessionContainer.value.size_class_id,
        status: 'In',
        tray_id: accessionContainer.value.id
      }
      await postAccessionTrayItem(payload)
    } else {
      const payload = {
        accession_dt: currentDate,
        accession_job_id: accessionJob.value.id,
        barcode_id: barcodeDetails.value.id,
        media_type_id: accessionJob.value.media_type_id,
        scanned_for_accession:
          accessionJob.value.media_type_id && accessionJob.value.size_class_id
            ? true
            : false,
        size_class_id: accessionJob.value.size_class_id,
        status: 'In',
        withdrawal_dt: currentDate
      }
      await postAccessionNonTrayItem(payload)
    }
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
  } finally {
    appIsLoadingData.value = false
  }
}
const updateContainerItem = async (barcode_value) => {
  try {
    appActionIsLoadingData.value = true
    await patchBarcode(selectedItems.value[0].barcode.id, barcode_value)

    if (accessionJob.value.trayed) {
      const itemPayload = {
        id: selectedItems.value[0].id,
        barcode: {
          value: barcode_value
        }
      }
      await patchAccessionTrayItem(itemPayload)
    } else {
      const itemPayload = {
        id: selectedItems.value[0].id,
        barcode: {
          value: barcode_value
        }
      }
      await patchAccessionNonTrayItem(itemPayload)

      router.push({
        name: 'accession-container',
        params: {
          jobId: accessionJob.value.workflow_id,
          containerId: barcode_value
        }
      })
    }
    selectedItems.value[0].barcode.value = barcode_value

    Notify.create({
      type: 'positive',
      message: 'The item has been updated.'
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
  } finally {
    selectedItems.value = []
    appActionIsLoadingData.value = false
    barcodeEditModal.value.hideModal()
  }
}
const deleteContainerItem = async () => {
  try {
    appActionIsLoadingData.value = true
    const itemsToRemove = selectedItems.value.map((item) => item.id)
    if (accessionJob.value.trayed) {
      await deleteAccessionTrayItem(itemsToRemove)
    } else {
      await deleteAccessionNonTrayItem(itemsToRemove)

      router.push({
        name: 'accession',
        params: {
          jobId: accessionJob.value.workflow_id
        }
      })
    }

    await Promise.all(
      selectedItems.value.map((item) => {
        return deleteBarcode(item.barcode.id)
      })
    )

    Notify.create({
      type: 'positive',
      message: 'The selected item(s) has been removed.'
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
  } finally {
    selectedItems.value = []
    appActionIsLoadingData.value = false
  }
}

const handleConfirmation = async (confirmType) => {
  if (confirmType == 'deleteItem') {
    await deleteContainerItem()
  } else if (confirmType == 'confirmReaccession') {
    await addContainerItem()
  } else if (confirmType == 'completeJob') {
    await completeAccessionJob()
  } else if (confirmType == 'completePrint') {
    await completeAccessionJob()

    batchSheetComponent.value.printBatchReport()
  }
  confirmationModal.value.hideModal()
}

const handleOptionMenu = async (option) => {
  if (option.text == 'Edit') {
    // Load media types if not already loaded
    await getOptions('mediaTypes')
    // Find the full media type object by ID
    selectedMediaType.value = mediaTypes.value.find(mt => mt.id === accessionJob.value.media_type_id) || null
    showEditJobModal.value = true
  } else if (option.text == 'Pause Job') {
    updateAccessionJobStatus('Paused')
  } else if (option.text == 'Cancel Job') {
    showTrayConfirmation.value = 'CancelJob'
  } else if (option.text == 'Edit Tray Barcode') {
    showEditTrayModal.value = true
  } else if (option.text == 'Delete Tray') {
    showTrayConfirmation.value = 'DeleteTray'
  } else if (option.text == 'Print Job') {
    batchSheetComponent.value.printBatchReport()
  } else if (option.text == 'View History') {
    showAuditTrailModal.value = 'accession_jobs'
  } else if (option.text.startsWith('Add Tray')) {
    addNewTray()
  } else if (option.text == 'Enter Barcode' || option.text == 'Edit Barcode') {
    setBarcodeEditDisplay()
  } else if (option.text == 'Delete Items') {
    showConfirmation.value = {
      type: 'deleteItem',
      text: 'Are you sure you want to delete selected items?'
    }
  }
}

// Delete single item with confirmation
const deleteItemConfirm = (item) => {
  selectedItems.value = [item]
  showConfirmation.value = {
    type: 'deleteItem',
    text: `Are you sure you want to remove item "${renderItemBarcodeDisplay(item)}"?`
  }
}

// Update job media type
const updateJobMediaType = async (hideModal) => {
  try {
    appActionIsLoadingData.value = true
    await patchAccessionJob({
      media_type_id: selectedMediaType.value?.id || selectedMediaType.value
    })
    Notify.create({
      type: 'positive',
      message: 'Job settings updated successfully.'
    })
    hideModal()
    showEditJobModal.value = false
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}

// Tray modal action functions
const updateTrayContainerBarcode = async () => {
  try {
    appActionIsLoadingData.value = true

    // Check if the barcode is in the system otherwise create it
    await verifyBarcode(trayBarcodeInput.value, 'Tray', true)

    const payload = {
      id: accessionContainer.value.id,
      barcode_id: barcodeDetails.value.id
    }
    await patchAccessionTray(payload)

    Notify.create({
      type: 'positive',
      message: 'The tray has been updated.'
    })

    // Update router params without reloading the page
    router.replace({
      name: route.name,
      params: {
        jobId: accessionJob.value.workflow_id,
        containerId: trayBarcodeInput.value
      }
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
  } finally {
    appActionIsLoadingData.value = false
    trayBarcodeModalRef.value.hideModal()
  }
}

const cancelAccessionJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: accessionJob.value.id,
      status: 'Cancelled'
    }
    await patchAccessionJob(payload)

    Notify.create({
      type: 'positive',
      message: 'The Accession Job has been canceled.'
    })
    appActionIsLoadingData.value = false

    await nextTick()

    router.push({
      name: 'accession',
      params: {
        jobId: null
      }
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
    appActionIsLoadingData.value = false
  }
}

const removeTrayContainer = async () => {
  try {
    appActionIsLoadingData.value = true
    // Delete all tray items before deleting the tray
    await deleteAccessionTrayItem(accessionContainer.value.items.map(item => item.id))
    await deleteAccessionTray(accessionContainer.value.id)

    Notify.create({
      type: 'positive',
      message: 'The Tray Container has been deleted.'
    })
    trayConfirmationModalRef.value.hideModal()
    appActionIsLoadingData.value = false

    router.push({
      name: 'accession',
      params: {
        jobId: accessionJob.value.workflow_id
      }
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
    appActionIsLoadingData.value = false
  }
}

// NEW: Function to navigate to a specific tray
const navigateToTray = (tray) => {
  // Prevent navigating to the same tray again
  if (accessionContainer.value.id === tray.id) {
    return
  }

  router.push({
    name: 'accession-container',
    params: {
      jobId: accessionJob.value.workflow_id,
      containerId: tray.barcode.value
    }
  })
}

const addNewTray = async () => {
  appIsLoadingData.value = true
  await patchAccessionTray({
    id: accessionContainer.value.id,
    collection_accessioned: true
  })
  appIsLoadingData.value = false

  resetAccessionContainer()

  router.push({
    name: 'accession',
    params: {
      jobId: accessionJob.value.workflow_id
    }
  })
}

const updateAccessionJobStatus = async (status) => {
  try {
    appIsLoadingData.value = true
    const payload = {
      id: accessionJob.value.id,
      status,
      run_timestamp: currentIsoDate()
    }

    await patchAccessionJob(payload)

    Notify.create({
      type: 'positive',
      message: `Job Status has been updated to: ${status}`
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
  } finally {
    appIsLoadingData.value = false
  }
}
const completeAccessionJob = async () => {
  try {
    appActionIsLoadingData.value = true
    if (
      accessionJob.value.trayed &&
      !accessionContainer.value.collection_accessioned
    ) {
      await patchAccessionTray({
        id: accessionContainer.value.id,
        collection_accessioned: true
      })
    }

    const payload = {
      id: accessionJob.value.id,
      status: 'Completed',
      run_timestamp: currentIsoDate(),
      user_id: userData.value.user_id
    }
    await patchAccessionJob(payload)

    verificationJobGenerated.value = true
    Notify.create({
      type: 'positive',
      message: 'The Job has been completed and moved for verification.'
    })

    router.push({
      name: 'accession',
      params: {
        jobId: null
      }
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
const checkVerificationJobGeneration = async () => {
  try {
    appIsLoadingData.value = true
    const res = await getVerificationJobByAccessionId(accessionJob.value.id)
    if (res.data.id) {
      verificationJobGenerated.value = true
      return
    }
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error
    })
  } finally {
    appIsLoadingData.value = false
  }
}
</script>

<style lang="scss" scoped>
.accession-container {
  width: 80%;
  margin: 0 auto;
  padding: 16px 8px;
}

.form-group-label {
  display: block;
  font-weight: 500;
  margin-bottom: 4px;
  color: #555;
}

.accession-next-tray {
  &-item {
    border: 1px solid $secondary;
    border-radius: 3px;
  }
}
</style>