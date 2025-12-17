<template>
  <InfoDisplayLayout class="refile-job">
    <template #number-box-content>
      <div class="flex q-mb-xs">
        <MoreOptionsMenu
          :options="[
            { text: 'Edit Job Info', hidden: !checkUserPermission('can_assign_and_reassign_refile_job'), disabled: appIsOffline || editJobInfo || editItems || refileJob.status == 'Paused' || refileJob.status == 'Completed' },
            { text: 'Edit Items', disabled: appIsOffline || editJobInfo || editItems || refileJob.status == 'Paused' || refileJob.status == 'Completed' },
            { text: 'Delete Job', hidden: !checkUserPermission('can_delete_refile_job'), optionClass: 'text-negative', disabled: appIsOffline || editJobInfo || editItems || refileJob.status == 'Completed' || (refileJob.refile_job_items && refileJob.refile_job_items.some(itm => itm.status == 'In'))},
            { text: 'Print Job' },
            { text: 'View History' }
          ]"
          class="q-mr-xs"
          @click="handleOptionMenu"
        />
        <h1
          id="refileJobId"
          class="info-display-details-label text-h4"
        >
          Refile Job #:
        </h1>
      </div>
      <p class="info-display-number-box text-h4">
        {{ refileJob.id }}
      </p>
    </template>

    <template #details-content>
      <div class="col-xs-6 col-sm-6 col-md-grow q-mb-xs-md q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-lg">
        <div class="info-display-details">
          <label
            class="info-display-details-label-2 text-h6"
          >
            Assigned User:
          </label>
          <p
            v-if="!editJobInfo"
            class="text-body1"
          >
            {{ refileJob.assigned_user?.name }}
          </p>
          <SelectInput
            v-else
            v-model="refileJob.assigned_user_id"
            :options="users"
            option-type="users"
            option-value="id"
            option-label="name"
            aria-label="userSelect"
            class="q-pr-xs-sm q-pr-md-none"
          />
        </div>
      </div>
      <div class="col-xs-6 col-sm-6 col-md-grow q-mb-xs-md q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-lg">
        <div class="info-display-details">
          <label
            class="info-display-details-label-2 text-h6"
          >
            # of Items:
          </label>
          <p class="text-body1">
            {{ refileJob.refile_job_items ? refileJob.refile_job_items.length : 0 }}
          </p>
        </div>
      </div>
      <div class="col-xs-6 col-sm-6 col-md-grow q-mb-xs-md q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-lg">
        <div class="info-display-details">
          <label
            class="info-display-details-label-2 text-h6"
          >
            Date Created
          </label>
          <p class="text-body1">
            {{ formatDateTime(refileJob.create_dt).date }}
          </p>
        </div>
      </div>
      <div class="col-xs-6 col-sm-auto col-md-auto q-mb-xs-none q-mb-sm-md q-mb-md-none q-mr-sm-auto">
        <div class="info-display-details">
          <label
            class="info-display-details-label-2 text-h6"
          >
            Status
          </label>
          <p
            class="text-body1 text-center outline"
            :class="refileJob.status == 'Completed' || refileJob.status == 'Created' ? 'text-highlight' : refileJob.status == 'Paused' || refileJob.status == 'Running' ? 'text-highlight-warning' : refileJob.status == 'New' ? 'text-highlight-accent' : null "
          >
            {{ refileJob.status }}
          </p>
        </div>
      </div>

      <div
        v-if="currentScreenSize !== 'xs'"
        class="col-sm-12 col-md-12 col-lg-3 q-ml-auto"
      >
        <div
          v-if="editJobInfo || editItems"
          class="info-display-details-action q-mt-sm-sm q-mt-md-md"
        >
          <q-btn
            no-caps
            unelevated
            :color="editJobInfo ? 'accent' : 'negative'"
            :label="editJobInfo ? 'Save Edits' : 'Revert Items to Queue'"
            :disable="editItems && !selectedItems.length"
            class="btn-no-wrap text-body1 q-mr-sm"
            :loading="appActionIsLoadingData"
            @click="editJobInfo ? updateRefileJob() : revertItemsToQueue()"
          />
          <q-btn
            no-caps
            unelevated
            outline
            color="accent"
            label="Cancel"
            class="btn-no-wrap text-body1"
            @click="cancelRefileJobEdits"
          />
        </div>
        <div
          v-else-if="refileJob.status !== 'Completed'"
          class="info-display-details-action q-mt-sm-sm q-mt-md-md"
        >
          <q-btn
            v-if="refileJob.status !== 'Created'"
            no-caps
            unelevated
            outline
            color="accent"
            :icon="refileJob.status !== 'Paused' ? 'mdi-pause' : 'mdi-play'"
            :label="refileJob.status == 'Paused' ? 'Resume Job' : 'Pause Job'"
            class="btn-no-wrap text-body1 q-mr-sm"
            :disabled="appPendingSync || !checkUserPermission('can_edit_refile_job')"
            @click="refileJob.status == 'Paused' ? updateRefileJobStatus('Running') : updateRefileJobStatus('Paused')"
          />
          <q-btn
            no-caps
            unelevated
            color="positive"
            :label="refileJob.status == 'Created' ? 'Execute Refile Job' : 'Complete Job'"
            class="btn-no-wrap text-body1"
            :disabled="appIsOffline || appPendingSync || refileJob.status == 'Paused' || !allItemsRefiled || !checkUserPermission('can_execute_and_complete_refile_job')"
            :loading="appActionIsLoadingData"
            @click="refileJob.status == 'Created' ? executeRefileJob() : showConfirmationModal = 'CompleteJob'"
          />
        </div>
      </div>
      <MobileActionBar
        v-else-if="currentScreenSize == 'xs' && (editJobInfo || editItems)"
        :button-one-color="editJobInfo ? 'accent' : 'negative'"
        :button-one-label="editJobInfo ? 'Save Edits' : 'Revert Items to Queue'"
        :button-one-outline="false"
        :button-one-loading="appActionIsLoadingData"
        :button-one-disabled="editItems && !selectedItems.length"
        @button-one-click="editJobInfo ? updateRefileJob() : revertItemsToQueue()"
        button-two-color="accent"
        :button-two-label="'Cancel'"
        :button-two-outline="true"
        @button-two-click="cancelRefileJobEdits"
      />
      <MobileActionBar
        v-else-if="refileJob.status !== 'Completed'"
        button-one-color="accent"
        :button-one-icon="refileJob.status !== 'Paused' ? 'mdi-pause' : 'mdi-play'"
        :button-one-label="refileJob.status == 'Paused' ? 'Resume Job' : 'Pause Job'"
        :button-one-outline="true"
        :button-one-disabled="appPendingSync || refileJob.status == 'Created' || !checkUserPermission('can_edit_refile_job')"
        @button-one-click="refileJob.status == 'Paused' ? updateRefileJobStatus('Running') : updateRefileJobStatus('Paused')"
        button-two-color="positive"
        :button-two-label="refileJob.status == 'Created' ? 'Execute Refile Job' : 'Complete Job'"
        :button-two-outline="false"
        :button-two-disabled="appIsOffline || appPendingSync || refileJob.status == 'Paused' || !allItemsRefiled || !checkUserPermission('can_execute_and_complete_refile_job')"
        :button-two-loading="appActionIsLoadingData"
        @button-two-click="refileJob.status == 'Created' ? executeRefileJob() : showConfirmationModal = 'CompleteJob'"
      />
    </template>

    <template #table-content>
      <EssentialTable
        ref="refileItemsTableComponent"
        :table-columns="itemTableColumns"
        :table-visible-columns="itemTableVisibleColumns"
        :filter-options="itemTableFilters"
        :table-data="refileJobItems"
        :row-key="row => renderItemBarcodeDisplay(row)"
        :enable-table-reorder="false"
        :enable-selection="editItems"
        :heading-row-class="'q-mb-lg q-px-xs-sm q-px-sm-md'"
        :heading-filter-class="currentScreenSize == 'xs' ? 'col-xs-6 q-mr-auto' : 'q-ml-auto'"
        :highlight-row-class="'justify-end bg-color-green-light'"
        :highlight-row-key="'status'"
        :highlight-row-value="'In'"
        @selected-table-row="loadRefileItem(renderItemBarcodeDisplay($event))"
        @selected-data="selectedItems = $event"
      >
        <template #heading-row>
          <div class="col-xs-7 col-sm-5 q-mb-md-sm q-mr-auto">
            <h2 class="text-h4 text-bold">
              Items in Job:
            </h2>
          </div>
        </template>

        <template #table-td="{ colName, props, value }">
          <span
            v-if="colName == 'actions'"
          >
            <MoreOptionsMenu
              :options="[{ text: 'Revert Item to Queue', disabled: props.row.status !== 'Out' || refileJob.status == 'Paused' || refileJob.status == 'Completed' || !checkUserPermission('can_edit_refile_job') }]"
              class=""
              @click="handleOptionMenu($event, props.row)"
            />
          </span>
          <span
            v-else-if="colName == 'scanned_for_refile'"
            class="text-bold text-nowrap"
            :class="value == 'In' ? 'text-positive' : ''"
          >
            {{ value == 'In' ? 'Refiled' : '' }}
            <q-icon
              v-if="value == 'In'"
              name="mdi-check-circle"
              color="positive"
              size="25px"
              class="text-bold q-ml-xs"
            />
          </span>
        </template>
      </EssentialTable>
    </template>
  </InfoDisplayLayout>

  <!-- confirmation modal -->
  <PopupModal
    v-if="showConfirmationModal"
    :title="showConfirmationModal == 'CompleteJob' ? 'Confirm' : 'Delete'"
    :text="showConfirmationModal == 'CompleteJob' ? 'Are you sure you want to complete the job?' : 'Are you sure you want to delete the job?'"
    :show-actions="false"
    @reset="showConfirmationModal = null"
    aria-label="confirmationModal"
  >
    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <q-btn
          v-if="showConfirmationModal == 'CompleteJob'"
          no-caps
          unelevated
          color="accent"
          label="Complete"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="completeRefileJob(); hideModal();"
        />
        <q-btn
          v-else
          no-caps
          unelevated
          color="negative"
          label="Delete Job"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="cancelRefileJob(); hideModal();"
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

  <!-- refile item detail modal -->
  <RefileItemDetailModal
    v-if="showRefileItemDetailModal"
    @hide="showRefileItemDetailModal = false"
  />
  <RefileBatchSheet
    ref="batchSheetComponent"
    :refile-job-details="refileJob"
  />

  <!-- audit trail modal -->
  <AuditTrail
    v-if="showAuditTrailModal"
    ref="historyModal"
    @reset="showAuditTrailModal = null"
    :job-type="showAuditTrailModal"
    :job-id="refileJob.id"
  />
</template>

<script setup>
import { onBeforeMount, onMounted, ref, computed, inject, toRaw, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { useUserStore } from '@/stores/user-store'
import { useRefileStore } from '@/stores/refile-store'
import { storeToRefs } from 'pinia'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import { useIndexDbHandler } from '@/composables/useIndexDbHandler.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import InfoDisplayLayout from '@/components/InfoDisplayLayout.vue'
import EssentialTable from '@/components/EssentialTable.vue'
import MobileActionBar from '@/components/MobileActionBar.vue'
import MoreOptionsMenu from '@/components/MoreOptionsMenu.vue'
import SelectInput from '@/components/SelectInput.vue'
import PopupModal from '@/components/PopupModal.vue'
import RefileItemDetailModal from '@/components/Refile/RefileItemDetailModal.vue'
import AuditTrail from '@/components/AuditTrail.vue'
import RefileBatchSheet from '@/components/Refile/RefileBatchSheet.vue'

const router = useRouter()

// Composables
const { currentScreenSize } = useCurrentScreenSize()
const { compiledBarCode } = useBarcodeScanHandler()
const {
  addDataToIndexDb,
  getDataInIndexDb,
  deleteDataInIndexDb
} = useIndexDbHandler()
const { checkUserPermission } = usePermissionHandler()

// Store Data
const {
  appIsLoadingData,
  appActionIsLoadingData,
  appPendingSync,
  appIsOffline
} = storeToRefs(useGlobalStore())
const { userData } = storeToRefs(useUserStore())
const { users } = storeToRefs(useOptionStore())
const {
  patchRefileJob,
  deleteRefileJob,
  deleteRefileJobItems,
  getRefileJobItem
} = useRefileStore()
const {
  refileJob,
  originalRefileJob,
  allItemsRefiled,
  refileItem
} = storeToRefs(useRefileStore())

// Local Data
const refileItemsTableComponent = ref(null)
const batchSheetComponent = ref(null)
const editJobInfo = ref(false)
const editItems = ref(false)
const selectedItems = ref([])
const itemTableVisibleColumns = ref([
  'actions',
  'item_location',
  'tray_barcode',
  'barcode',
  'owner',
  'size_class',
  'scanned_for_refile'
])
const itemTableColumns = ref([
  {
    name: 'actions',
    field: 'actions',
    label: '',
    align: 'center',
    sortable: false,
    required: true
  },
  {
    name: 'item_location',
    field: row => row.tray ? getItemLocation(row.tray) : getItemLocation(row),
    label: 'Item Location',
    align: 'left',
    sortable: true
  },
  {
    name: 'tray_barcode',
    field: row => renderItemBarcodeDisplay(row.tray),
    label: 'Tray Barcode',
    align: 'left',
    sortable: true
  },
  {
    name: 'barcode',
    field: row => renderItemBarcodeDisplay(row),
    label: 'Barcode',
    align: 'left',
    sortable: true
  },
  {
    name: 'owner',
    field: row => row.owner?.name,
    label: 'Owner',
    align: 'left',
    sortable: true
  },
  {
    name: 'size_class',
    field: row => row.size_class?.name,
    label: 'Size Class',
    align: 'left',
    sortable: true
  },
  {
    name: 'scanned_for_refile',
    field: 'status',
    label: '',
    align: 'center',
    sortable: false,
    required: true,
    headerStyle: 'max-width: 200px'
  }
])
const refileJobItems = computed(() => {
  if (!refileJob.value.refile_job_items) {
    return []
  }
  return editItems.value ? refileJob.value.refile_job_items.filter(item => item.status == 'Out') : refileJob.value.refile_job_items
})
const itemTableFilters = computed(() => {
  let tablesFilters = []
  if (refileJob.value.refile_job_items && refileJob.value.refile_job_items.length > 0) {
    tablesFilters = [
      {
        field: row => row.owner?.name,
        label: 'Owner',
        // render options based on the passed in table data
        // loop through all containers and return customized data set for table filtering and remove the duplicates
        options: getUniqueListByKey(refileJob.value.refile_job_items.map(tableEntry => {
          return {
            text: tableEntry.owner?.name,
            value: false
          }
        }), 'text')
      },
      {
        field: row => row.size_class?.name,
        label: 'Size Class',
        options: getUniqueListByKey(refileJob.value.refile_job_items.map(tableEntry => {
          return {
            text: tableEntry.size_class?.name,
            value: false
          }
        }), 'text')
      }
    ]
  }
  return tablesFilters
})
const showConfirmationModal = ref(null)
const showRefileItemDetailModal = ref(false)
const historyModal = ref(null)
const showAuditTrailModal = ref(false)

// Logic
const handleAlert = inject('handle-alert')
const currentIsoDate = inject('current-iso-date')
const formatDateTime = inject('format-date-time')
const getItemLocation = inject('get-item-location')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')
const getUniqueListByKey = inject('get-uniqure-list-by-key')

onBeforeMount(() => {
  if (currentScreenSize.value == 'xs') {
    itemTableVisibleColumns.value = [
      'actions',
      'item_location',
      'tray_barcode',
      'barcode',
      'scanned_for_refile'
    ]
  }
})

onMounted(async () => {
  // when user is online and loads a job we store the current refile job data and original in indexdb for reference offline
  if (!appIsOffline.value) {
    addDataToIndexDb('refileStore', 'refileJob', JSON.parse(JSON.stringify(refileJob.value)))
    addDataToIndexDb('refileStore', 'originalRefileJob', JSON.parse(JSON.stringify(originalRefileJob.value)))
  } else {
    // get saved refile job data if were offline and page was reloaded/refreshed
    const res = await getDataInIndexDb('refileStore')
    refileJob.value = res.data.refileJob
    originalRefileJob.value = res.data.originalRefileJob
  }
})

watch(compiledBarCode, (barcode) => {
  if (barcode !== '' && refileJob.value.status == 'Running' && !refileItem.value.id) {
    // only allow scans if the job is in a running state and there is no active refileItem set in state
    triggerItemScan(barcode)
  }
})
const triggerItemScan = (barcode_value) => {
  // check if the scanned barcode is in the item data and that the barcode hasnt been refiled already
  if (!refileJob.value.refile_job_items?.some(itm => itm.barcode.value == barcode_value)) {
    handleAlert({
      type: 'error',
      text: 'The scanned item does not exist in this refile job. Please try again.',
      autoClose: true
    })
    return
  } else if (refileJob.value.refile_job_items?.some(itm => itm.barcode.value == barcode_value && itm.status !== 'Out')) {
    handleAlert({
      type: 'error',
      text: 'The scanned item has already been marked as refiled.',
      autoClose: true
    })
    return
  } else {
    // load the scanned request item by id of the scanned item barcode
    loadRefileItem(barcode_value)
  }
}

const handleOptionMenu = async (action, rowData) => {
  switch (action.text) {
    case 'Edit Job Info':
      editJobInfo.value = true
      return
    case 'Edit Items':
      editItems.value = true
      return
    case 'Delete Job':
      showConfirmationModal.value = 'DeleteJob'
      return
    case 'Revert Item to Queue':
      removeRefileItems([rowData.barcode.value])
      return
    case 'Print Job':
      batchSheetComponent.value.printBatchReport()
      return
    case 'View History':
      showAuditTrailModal.value = 'refile_jobs'
      return
  }
}

const cancelRefileJobEdits = () => {
  // Reset the refile job
  refileJob.value = { ...toRaw(originalRefileJob.value) }
  editJobInfo.value = false
  // Reset the items
  editItems.value = false
  refileItemsTableComponent.value.clearSelectedData()
}
const executeRefileJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: refileJob.value.id,
      status: 'Running',
      assigned_user_id: refileJob.value.assigned_user_id ? refileJob.value.assigned_user_id : userData.value.user_id,
      run_timestamp: currentIsoDate()
    }
    await patchRefileJob(payload)

    // store the current refile job data in indexdb for reference offline whenever job is executed
    addDataToIndexDb('refileStore', 'refileJob', JSON.parse(JSON.stringify(refileJob.value)))
    addDataToIndexDb('refileStore', 'originalRefileJob', JSON.parse(JSON.stringify(originalRefileJob.value)))

    handleAlert({
      type: 'success',
      text: 'Refile Job Successfully Started',
      autoClose: true
    })
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}
const revertItemsToQueue = async () => {
  try {
    appActionIsLoadingData.value = true
    await removeRefileItems(selectedItems.value.map(item => item.barcode.value))
    refileItemsTableComponent.value.clearSelectedData()
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}
const updateRefileJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: refileJob.value.id,
      assigned_user_id: refileJob.value.assigned_user_id,
      run_timestamp: currentIsoDate()
    }
    await patchRefileJob(payload)

    handleAlert({
      type: 'success',
      text: 'The job has been updated.',
      autoClose: true
    })
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
    editJobInfo.value = false
  }
}
const updateRefileJobStatus = async (status) => {
  try {
    appIsLoadingData.value = true
    const payload = {
      id: refileJob.value.id,
      status,
      run_timestamp: currentIsoDate()
    }
    await patchRefileJob(payload)

    if (appIsOffline.value) {
      // when offline we update the status directly
      refileJob.value.status = payload.status
      originalRefileJob.value.status = payload.status
    }

    // store the current refile job data in indexdb for reference offline whenever job is executed
    addDataToIndexDb('refileStore', 'refileJob', JSON.parse(JSON.stringify(refileJob.value)))
    addDataToIndexDb('refileStore', 'originalRefileJob', JSON.parse(JSON.stringify(originalRefileJob.value)))

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
const cancelRefileJob = async () => {
  try {
    appIsLoadingData.value = true
    await deleteRefileJob(refileJob.value.id)

    handleAlert({
      type: 'success',
      text: 'The Refile Job has been canceled.',
      autoClose: true
    })

    router.push({
      name: 'refile',
      params: {
        jobId: null
      }
    })
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appIsLoadingData.value = false
    deleteDataInIndexDb('refileStore', 'refileJob')
    deleteDataInIndexDb('refileStore', 'originalRefileJob')
  }
}
const completeRefileJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: refileJob.value.id,
      status: 'Completed',
      run_timestamp: currentIsoDate()
    }
    await patchRefileJob(payload)

    handleAlert({
      type: 'success',
      text: 'The Refile Job has been completed.',
      autoClose: true
    })

    router.push({
      name: 'refile',
      params: {
        jobId: null
      }
    })
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
    deleteDataInIndexDb('refileStore', 'refileJob')
    deleteDataInIndexDb('refileStore', 'originalRefileJob')
  }
}
const loadRefileItem = (barcode_value) => {
  // since we already have all the items data we just need to set the refileItem from the refileJob items directly
  getRefileJobItem(barcode_value)
  showRefileItemDetailModal.value = true
}
const removeRefileItems = async (barcode_values) => {
  try {
    appIsLoadingData.value = true
    const payload = {
      barcode_values
    }
    await deleteRefileJobItems(payload)

    if (appIsOffline.value) {
      // when offline we remove the refile items directly by filtering out the matching barcodes in either items or nonTrayItems
      refileJob.value.items = refileJob.value.items.filter(itm => !barcode_values.includes(itm.barcode.value))
      refileJob.value.non_tray_items = refileJob.value.non_tray_items.filter(itm => !barcode_values.includes(itm.barcode.value))
      originalRefileJob.value.items = originalRefileJob.value.items.filter(itm => !barcode_values.includes(itm.barcode.value))
      originalRefileJob.value.non_tray_items = originalRefileJob.value.non_tray_items.filter(itm => !barcode_values.includes(itm.barcode.value))
    }

    // store the current refile job data in indexdb for reference offline whenever job is executed
    addDataToIndexDb('refileStore', 'refileJob', JSON.parse(JSON.stringify(refileJob.value)))
    addDataToIndexDb('refileStore', 'originalRefileJob', JSON.parse(JSON.stringify(originalRefileJob.value)))

    const alertMessage = (editItems.value && selectedItems.value.length) > 1 ? 'items have' : 'item has'
    handleAlert({
      type: 'success',
      text: `The ${alertMessage} been sent back to the refile queue.`,
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
</script>

<style lang="scss" scoped>
</style>
