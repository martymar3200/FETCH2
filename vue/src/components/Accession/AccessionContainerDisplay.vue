<template>
  <div class="row accession-container flex-lg-grow">
    <div
      v-if="accessionJob.trayed"
      class="col-12 col-md-2 accession-tray-list"
    >
      <div class="row items-center q-mb-md">
        <MoreOptionsMenu
          :options="!route.params.containerId ? [
            { text: 'Edit', disabled: accessionJob.status == 'Completed' },
            { text: 'Cancel Job', optionClass: 'text-negative', disabled: accessionJob.status == 'Completed', hidden: !checkUserPermission('can_cancel_accession')},
            { text: 'Print Job' },
            { text: 'View History' }
          ] : [
            { text: 'Edit', disabled: accessionJob.status == 'Completed'},
            { text: 'Cancel Job', optionClass: 'text-negative', disabled: accessionJob.status == 'Completed', hidden: !checkUserPermission('can_cancel_accession')},
            { text: 'Edit Tray Barcode', disabled: barcodeScanAllowed || accessionJob.status == 'Completed'},
            { text: 'Delete Tray', optionClass: 'text-negative', disabled: accessionJob.status == 'Completed'},
            { text: 'Print Job' },
            { text: 'View History' }
          ]"
          class="q-mr-sm"
          @click="handleOptionMenu"
        />
        <h1 class="text-h4 text-bold">
          {{ `Job: ${accessionJob.workflow_id}` }}
        </h1>
      </div>

      <h3 class="text-h5 text-bold q-mb-md">
        Trays in Job
      </h3>
      <div
        v-if="accessionJob.trayed && currentScreenSize !== 'xs'"
        class="q-mb-md"
      >
        <q-btn
          no-caps
          unelevated
          icon="add"
          color="accent"
          :label="`Add Tray (${accessionJob.trays.length})`"
          class="btn-no-wrap text-body1 full-width"
          :disabled="
            !accessionContainer.id ||
              !allItemsVerified ||
              accessionJob.status == 'Paused' ||
              accessionJob.status == 'Completed'
          "
          @click="addNewTray"
        >
          <q-tooltip>
            Add a new tray to this job (Shortcut: T)
          </q-tooltip>
        </q-btn>
      </div>

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
    </div>

    <AccessionNonTrayInfo
      v-if="!accessionJob.trayed"
      ref="nonTrayInfoComponent"
      @print="batchSheetComponent.printBatchReport()"
      class="col-12 col-md-3"
    />
    <AccessionTrayInfo
      v-else
      ref="trayInfoComponent"
      @print="batchSheetComponent.printBatchReport()"
      class="col-12 col-md-3"
    />

    <div class="col-12 col-md-7 accession-container-scan">
      <div class="row items-center q-mb-xs-md q-mb-sm-lg">
        <div
          v-if="currentScreenSize == 'xs'"
          class="col-auto"
        >
          <MoreOptionsMenu
            v-if="!accessionJob.trayed"
            :options="[
              {
                text: `${
                  selectedItems.length == 1 ? 'Edit Barcode' : 'Enter Barcode'
                }`,
                disabled:
                  (accessionJob.trayed && !accessionContainer.id) ||
                  accessionJob.status == 'Paused' || accessionJob.status == 'Completed' ||
                  barcodeScanAllowed,
              },
              {
                text: 'Delete Items',
                disabled:
                  selectedItems.length == 0 || accessionJob.status == 'Paused' || accessionJob.status == 'Completed'
              },
            ]"
            class="q-mr-sm"
            @click="handleOptionMenu"
          />
          <MoreOptionsMenu
            v-else
            :options="[
              {
                text: `Add Tray (${accessionJob.trays.length})`,
                disabled:
                  !accessionContainer.id ||
                  !allItemsVerified ||
                  accessionJob.status == 'Paused' || accessionJob.status == 'Completed'
              },
              {
                text: `${
                  selectedItems.length == 1 ? 'Edit Barcode' : 'Enter Barcode'
                }`,
                disabled:
                  (accessionJob.trayed && !accessionContainer.id) ||
                  accessionJob.status == 'Paused' || accessionJob.status == 'Completed' ||
                  barcodeScanAllowed,
              },
              {
                text: 'Delete Items',
                disabled:
                  selectedItems.length == 0 || accessionJob.status == 'Paused' || accessionJob.status == 'Completed'
              },
            ]"
            class="q-mr-sm"
            @click="handleOptionMenu"
          />
        </div>

        <div class="col-auto">
          <h2 class="text-h4 text-bold">
            Scan Items
          </h2>
        </div>

        <div class="col-auto q-ml-sm">
          <span class="outline text-h6">
            {{
              accessionJob.trayed
                ? accessionContainer.items.length
                : accessionJob.non_tray_items.length
            }}
            Items
          </span>
        </div>
      </div>

      <div
        v-if="currentScreenSize !== 'xs'"
        class="row q-mb-xs-lg"
      >
        <div
          class="col-xs-12 col-md-auto flex no-wrap justify-between q-mb-xs-md q-mb-md-none q-mr-md-auto"
        >
          <q-btn
            no-caps
            unelevated
            icon="add"
            color="accent"
            :label="
              selectedItems.length == 1 ? 'Edit Barcode' : 'Enter Barcode'
            "
            class="btn-no-wrap text-body1 q-mr-sm-md"
            :disabled="
              (accessionJob.trayed && !accessionContainer.id) ||
                accessionJob.status == 'Paused' ||
                accessionJob.status == 'Completed' ||
                barcodeScanAllowed
            "
            @click="setBarcodeEditDisplay"
          />
          <q-btn
            no-caps
            unelevated
            icon="mdi-delete"
            color="negative"
            label="Delete"
            class="btn-no-wrap text-body1"
            :disabled="
              selectedItems.length == 0 || accessionJob.status == 'Paused' || accessionJob.status == 'Completed'
            "
            @click="
              showConfirmation = {
                type: 'deleteItem',
                text: 'Are you sure you want to delete selected items?',
              }
            "
          />
        </div>

        <div class="col-xs-12 col-md-auto flex justify-between">
          <q-btn
            v-if="accessionJob.status !== 'Paused'"
            no-caps
            unelevated
            outline
            icon="mdi-pause"
            color="accent"
            label="Pause Job"
            class="btn-no-wrap text-body1"
            :class="currentScreenSize == 'xs' ? 'full-width q-mb-md' : ''"
            :disabled="accessionJob.status == 'Completed'"
            @click="updateAccessionJobStatus('Paused')"
          />
          <q-btn
            v-else
            no-caps
            unelevated
            outline
            icon="mdi-play"
            color="accent"
            label="Resume Job"
            class="btn-no-wrap text-body1"
            :class="currentScreenSize == 'xs' ? 'full-width q-mb-md' : ''"
            :disabled="accessionJob.status == 'Completed'"
            @click="updateAccessionJobStatus('Running')"
          />
          <q-btn
            no-caps
            unelevated
            icon="check"
            color="positive"
            label="Complete Job"
            class="btn-no-wrap text-body1 q-ml-sm"
            :class="currentScreenSize == 'xs' ? 'full-width' : ''"
            :outline="!allItemsVerified || accessionJob.status == 'Paused'"
            :disabled="!allItemsVerified || accessionJob.status == 'Paused' || accessionJob.status == 'Completed' && verificationJobGenerated"
            @click="
              showConfirmation = {
                type: 'completeJob',
                text: 'Are you sure you want to complete the job?',
              }
            "
          />
        </div>
      </div>

      <div class="row q-mb-xs-xl q-mb-sm-none">
        <div class="col-12 q-mb-xs-md q-mb-sm-none">
          <EssentialTable
            ref="accessionTableComponent"
            :table-columns="accessionTableColumns"
            :table-data="
              accessionJob.trayed
                ? accessionContainer.items.slice().reverse()
                : accessionJob.non_tray_items.slice().reverse()
            "
            :hide-table-rearrange="true"
            :enable-selection="true"
            @selected-data="selectedItems = $event"
          >
            <template #table-td="{ colName, value }">
              <span v-if="colName == 'verified'">
                <q-icon
                  v-if="value == true || accessionJob.trayed"
                  name="check"
                  color="positive"
                  size="30px"
                  class="text-bold"
                />
                <q-icon
                  v-else
                  name="close"
                  color="negative"
                  size="30px"
                  class="text-bold"
                />
              </span>
            </template>
          </EssentialTable>
        </div>
      </div>
    </div>

    <!-- mobile actions menu -->
    <MobileActionBar
      v-if="currentScreenSize == 'xs' && !renderIsEditMode"
      button-one-color="accent"
      :button-one-icon="
        accessionJob.status !== 'Paused' ? 'mdi-pause' : 'mdi-play'
      "
      :button-one-label="
        accessionJob.status == 'Paused' ? 'Resume Job' : 'Pause Job'
      "
      :button-one-outline="true"
      :button-one-disabled="accessionJob.status == 'Completed'"
      @button-one-click="
        accessionJob.status == 'Paused'
          ? updateAccessionJobStatus('Running')
          : updateAccessionJobStatus('Paused')
      "
      button-two-color="positive"
      button-two-label="Complete Job"
      :button-two-outline="false"
      :button-two-disabled="
        !allItemsVerified || accessionJob.status == 'Paused' || accessionJob.status == 'Completed' && verificationJobGenerated
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
          label="Complete & Print"
          class="btn-no-wrap text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="
            handleConfirmation('completePrint');
          "
        />

        <q-space class="q-mx-xs" />

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

  <!-- print component used to handle printing the template -->
  <AccessionBatchSheet
    ref="batchSheetComponent"
    :accession-job-details="accessionJob"
  />
</template>

<script setup>
import { ref, watch, computed, inject, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store'
import { useUserStore } from '@/stores/user-store'
import { useAccessionStore } from '@/stores/accession-store'
import { useVerificationStore } from '@/stores/verification-store'
import { useBarcodeStore } from '@/stores/barcode-store'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import AccessionNonTrayInfo from '@/components/Accession/AccessionNonTrayInfo.vue'
import AccessionTrayInfo from '@/components/Accession/AccessionTrayInfo.vue'
import EssentialTable from '@/components/EssentialTable.vue'
import TextInput from '@/components/TextInput.vue'
import PopupModal from '@/components/PopupModal.vue'
import MoreOptionsMenu from '@/components/MoreOptionsMenu.vue'
import MobileActionBar from '@/components/MobileActionBar.vue'
import AccessionBatchSheet from '@/components/Accession/AccessionBatchSheet.vue'

const router = useRouter()
const route = useRoute()

// Composables
const { compiledBarCode } = useBarcodeScanHandler({ waitForEnterKey: true })
const { currentScreenSize } = useCurrentScreenSize()
const { checkUserPermission } = usePermissionHandler()

// Store Data
const { appIsLoadingData, appActionIsLoadingData } = storeToRefs(useGlobalStore())
const { userData } = storeToRefs(useUserStore())
const { verifyBarcode, patchBarcode, deleteBarcode } = useBarcodeStore()
const { barcodeDetails, barcodeScanAllowed } = storeToRefs(useBarcodeStore())
const {
  resetAccessionContainer,
  patchAccessionJob,
  patchAccessionTray,
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

// Local Data
const barcodeEditModal = ref(null)
const confirmationModal = ref(null)
const trayInfoComponent = ref(null)
const nonTrayInfoComponent = ref(null)
const accessionTableComponent = ref(null)
const batchSheetComponent = ref(null)
const accessionTableColumns = ref([
  {
    name: 'barcode_value',
    field: (row) => renderItemBarcodeDisplay(row),
    label: 'Barcode',
    align: 'left',
    sortable: true
  },
  {
    name: 'verified',
    field: 'scanned_for_accession',
    label: '',
    align: 'right',
    sortable: false
  }
])
const selectedItems = ref([])
const showConfirmation = ref(null)
const showBarcodeEdit = ref(false)
const manualBarcodeEdit = ref('')
const verificationJobGenerated = ref(false)
const renderIsEditMode = computed(() => {
  if (trayInfoComponent.value && trayInfoComponent.value.editMode) {
    return true
  } else if (
    nonTrayInfoComponent.value &&
    nonTrayInfoComponent.value.editMode
  ) {
    return true
  } else {
    return false
  }
})

// Logic
const handleAlert = inject('handle-alert')
const currentIsoDate = inject('current-iso-date')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')

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
    triggerItemScan(barcode)
  } else if (accessionJob.value.trayed == false) {
    triggerItemScan(barcode)
  }
})
const triggerItemScan = async (barcode_value) => {
  try {
    appActionIsLoadingData.value = true
    const res = await verifyBarcode(barcode_value, 'Item', true)
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
        handleAlert({
          type: 'error',
          text: 'The scanned item was already added to this tray.',
          autoClose: true
        })
        return
      } else {
        await addContainerItem()
      }
    } else {
      if (
        accessionJob.value.non_tray_items.some(
          (item) => item.barcode_id == barcodeDetails.value.id
        )
      ) {
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
    handleAlert({
      type: 'error',
      text: `Error with barcode "${barcode_value}": ${error}`,
      persistent: true
    })
  } finally {
    appActionIsLoadingData.value = false
    if (showBarcodeEdit.value && barcodeEditModal.value) {
      barcodeEditModal.value.hideModal()
    }
  }
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
    handleAlert({
      type: 'error',
      text: error,
      persistent: true
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

    handleAlert({
      type: 'success',
      text: 'The item has been updated.',
      autoClose: true
    })
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      persistent: true
    })
  } finally {
    accessionTableComponent.value.clearSelectedData()
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

    handleAlert({
      type: 'success',
      text: 'The selected item(s) has been removed.',
      autoClose: true
    })
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      persistent: true
    })
  } finally {
    accessionTableComponent.value.clearSelectedData()
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

const handleOptionMenu = (option) => {
  if (option.text == 'Edit') {
    trayInfoComponent.value.editMode = true
  } else if (option.text == 'Cancel Job') {
    trayInfoComponent.value.showConfirmationModal = 'CancelJob'
  } else if (option.text == 'Edit Tray Barcode') {
    trayInfoComponent.value.showEditTrayModal = true
  } else if (option.text == 'Delete Tray') {
    trayInfoComponent.value.showConfirmationModal = 'DeleteTray'
  } else if (option.text == 'Print Job') {
    batchSheetComponent.value.printBatchReport()
  } else if (option.text == 'View History') {
    trayInfoComponent.value.showAuditTrailModal = 'accession_jobs'
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

    handleAlert({
      type: 'success',
      text: `Job Status has been updated to: ${status}`,
      autoClose: true
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
    handleAlert({
      type: 'success',
      text: 'The Job has been completed and moved for verification.',
      autoClose: true
    })

    router.push({
      name: 'accession',
      params: {
        jobId: null
      }
    })
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      persistent: true
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
    handleAlert({
      type: 'error',
      text: error,
      persistent: true
    })
  } finally {
    appIsLoadingData.value = false
  }
}
</script>

<style lang="scss" scoped>
.accession-container {
  &-scan {
    & > div:first-child {
      padding: 0 1.5rem;
      padding-top: 3rem;

      @media (max-width: $breakpoint-sm-min) {
        padding: 0 1rem;
        padding-top: 2rem;
      }
    }
    & > div:nth-child(2) {
      padding: 0 1.5rem;

      @media (max-width: $breakpoint-sm-min) {
        padding: 0 1rem;
      }
    }
  }
}

.accession-tray-list {
  border-right: 1px solid;
  border-color: $secondary;
  padding: 3rem;

  @media (max-width: $breakpoint-md-max) {
    border-right: none;
    border-bottom: 1px solid;
    padding: 1.5rem;
  }
}

.accession-next-tray {
  &-item {
    border: 1px solid $secondary;
    border-radius: 3px;
  }
}
</style>