<template>
  <InfoDisplayLayout class="picklist-job">
    <template #number-box-content>
      <div class="flex q-mb-xs">
        <MoreOptionsMenu
          :options="[
            { text: 'Edit', hidden: !checkUserPermission('can_assign_and_reassign_picklist_job'), disabled: appIsOffline || editJob || picklistJob.status == 'Paused' || picklistJob.status == 'Completed' },
            { text: 'Delete Job', hidden: !checkUserPermission('can_delete_picklist_job'), optionClass: 'text-negative', disabled: appIsOffline || editJob || picklistJob.status == 'Completed' || picklistItems.some(itm => itm.status !== 'PickList')},
            { text: 'Print Job' },
            { text: 'View History' }
          ]"
          class="q-mr-xs"
          @click="handleOptionMenu"
        />
        <h1
          id="picklistJobId"
          class="info-display-details-label text-h4"
        >
          Pick List #:
        </h1>
      </div>
      <p class="info-display-number-box text-h4">
        {{ picklistJob.id }}
      </p>
    </template>

    <template #details-content>
      <div class="col-xs-6 col-sm-6 col-md-grow">
        <div class="info-display-details q-mb-xs-md q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-lg">
          <label
            class="info-display-details-label-2 text-h6"
          >
            Building:
          </label>
          <p class="text-body1">
            {{ picklistJob.building?.name }}
          </p>
        </div>
      </div>
      <div class="col-xs-6 col-sm-6 col-md-grow">
        <div class="info-display-details q-mb-xs-md q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-lg">
          <label
            class="info-display-details-label-2 text-h6"
          >
            Assigned User:
          </label>
          <p
            v-if="!editJob"
            class="text-body1"
          >
            {{ picklistJob.user?.name }}
          </p>
          <SelectInput
            v-else
            v-model="picklistJob.user_id"
            :options="users"
            option-type="users"
            option-value="id"
            option-label="name"
            aria-label="userSelect"
          >
            <template #no-option>
              <q-item>
                <q-item-section class="text-italic text-grey">
                  No Users Found
                </q-item-section>
              </q-item>
            </template>
          </SelectInput>
        </div>
      </div>
      <div class="col-xs-6 col-sm-6 col-md-grow">
        <div class="info-display-details q-mb-xs-md q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-lg">
          <label
            class="info-display-details-label-2 text-h6"
          >
            # of Items:
          </label>
          <p class="text-body1">
            {{ picklistJob.request_count }}
          </p>
        </div>
      </div>
      <div class="col-xs-6 col-sm-6 col-md-grow">
        <div class="info-display-details q-mb-xs-md q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-lg">
          <label
            class="info-display-details-label-2 text-h6"
          >
            Date Created:
          </label>
          <p class="text-body1">
            {{ formatDateTime(picklistJob.create_dt).date }}
          </p>
        </div>
      </div>
      <div class="col-xs-6 col-sm-auto col-md-auto q-mr-auto">
        <div class="info-display-details q-mb-xs-none q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-sm">
          <label
            class="info-display-details-label-2 text-h6"
          >
            Status:
          </label>
          <p
            class="text-body1 outline"
            :class="picklistJob.status == 'Completed' || picklistJob.status == 'Created' ? 'text-highlight' : picklistJob.status == 'Paused' || picklistJob.status == 'Running' ? 'text-highlight-warning' : picklistJob.status == 'New' ? 'text-highlight-accent' : null "
          >
            {{ picklistJob.status }}
          </p>
        </div>
      </div>

      <div
        v-if="currentScreenSize !== 'xs'"
        class="col-sm-12 col-md-12 col-lg-3 q-ml-auto"
      >
        <div
          v-if="editJob"
          class="info-display-details-action q-mt-sm-sm q-mt-md-md"
        >
          <q-btn
            no-caps
            unelevated
            color="accent"
            label="Save Edits"
            class="btn-no-wrap text-body1 q-mr-sm"
            :loading="appActionIsLoadingData"
            @click="updatePicklistJob"
          />
          <q-btn
            no-caps
            unelevated
            outline
            color="accent"
            label="Cancel"
            class="btn-no-wrap text-body1"
            @click="cancelPicklistJobEdits"
          />
        </div>
        <div
          v-else-if="picklistJob.status !== 'Completed'"
          class="info-display-details-action q-mt-sm-sm q-mt-md-md"
        >
          <q-btn
            v-if="picklistJob.status !== 'Created'"
            no-caps
            unelevated
            outline
            color="accent"
            :icon="picklistJob.status !== 'Paused' ? 'mdi-pause' : 'mdi-play'"
            :label="picklistJob.status == 'Paused' ? 'Resume Job' : 'Pause Job'"
            class="btn-no-wrap text-body1 q-mr-sm"
            :disabled="appPendingSync || !checkUserPermission('can_edit_picklist_job')"
            @click="picklistJob.status == 'Paused' ? updatePicklistJobStatus('Running') : updatePicklistJobStatus('Paused')"
          />
          <q-btn
            no-caps
            unelevated
            color="positive"
            :label="picklistJob.status == 'Created' ? 'Retrieve Pick List' : 'Complete Job'"
            class="btn-no-wrap text-body1"
            :disabled="appIsOffline || appPendingSync || picklistJob.status == 'Paused' || !allItemsRetrieved || !checkUserPermission('can_edit_picklist_job')"
            :loading="appActionIsLoadingData"
            @click="picklistJob.status == 'Created' ? executePicklistJob() : showConfirmationModal = 'CompleteJob'"
          />
        </div>
      </div>
      <MobileActionBar
        v-else-if="currentScreenSize == 'xs' && editJob"
        button-one-color="accent"
        :button-one-label="'Save Edits'"
        :button-one-outline="false"
        :button-one-loading="appActionIsLoadingData"
        @button-one-click="updatePicklistJob"
        button-two-color="accent"
        :button-two-label="'Cancel'"
        :button-two-outline="true"
        @button-two-click="cancelPicklistJobEdits"
      />
      <MobileActionBar
        v-else-if="picklistJob.status !== 'Completed'"
        button-one-color="accent"
        :button-one-icon="picklistJob.status !== 'Paused' ? 'mdi-pause' : 'mdi-play'"
        :button-one-label="picklistJob.status == 'Paused' ? 'Resume Job' : 'Pause Job'"
        :button-one-outline="true"
        :button-one-disabled="appPendingSync || picklistJob.status == 'Created' || !checkUserPermission('can_edit_picklist_job')"
        @button-one-click="picklistJob.status == 'Paused' ? updatePicklistJobStatus('Running') : updatePicklistJobStatus('Paused')"
        button-two-color="positive"
        :button-two-label="picklistJob.status == 'Created' ? 'Retrieve Pick List' : 'Complete Job'"
        :button-two-outline="false"
        :button-two-disabled="appIsOffline || appPendingSync || picklistJob.status == 'Paused' || !allItemsRetrieved || !checkUserPermission('can_edit_picklist_job')"
        :button-two-loading="appActionIsLoadingData"
        @button-two-click="picklistJob.status == 'Created' ? executePicklistJob() : showConfirmationModal = 'CompleteJob'"
      />
    </template>

    <template #table-content>
      <EssentialTable
        :table-columns="itemTableColumns"
        :table-visible-columns="itemTableVisibleColumns"
        :filter-options="itemTableFilters"
        :table-data="picklistItems"
        :enable-table-reorder="false"
        :enable-selection="false"
        :heading-row-class="'q-mb-lg q-px-xs-sm q-px-sm-md'"
        :heading-filter-class="currentScreenSize == 'xs' ? 'col-xs-6 q-mr-auto' : 'q-ml-auto'"
        :highlight-row-class="'justify-end bg-color-green-light'"
        :highlight-row-key="'status'"
        :highlight-row-value="'Out'"
        @selected-table-row="loadPicklistItem(renderItemBarcodeDisplay($event.item ? $event.item : $event.non_tray_item))"
      >
        <template #heading-row>
          <div class="col-xs-7 col-sm-5 q-mb-md-sm">
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
              :options="[{ text: 'Revert Item to Queue', disabled: props.row.status !== 'PickList' || picklistJob.status == 'Paused' || picklistJob.status == 'Completed' || !checkUserPermission('can_edit_picklist_job')}]"
              class=""
              @click="handleOptionMenu($event, props.row)"
            />
          </span>
          <span
            v-else-if="colName == 'status'"
            class="text-bold text-nowrap"
            :class="value !== 'PickList' ? 'text-positive' : ''"
          >
            {{ value !== 'PickList' ? 'Retrieved' : '' }}
            <q-icon
              v-if="value !== 'PickList'"
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
    ref="confirmationModal"
    :title="showConfirmationModal == 'CompleteJob' ? 'Confirm' : 'Delete'"
    :text="showConfirmationModal == 'CompleteJob' ? 'Are you sure you want to complete the job?' : 'Are you sure you want to delete the job?'"
    :show-actions="false"
    @reset="showConfirmationModal = null"
    aria-label="confirmationModal"
  >
    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <template v-if="showConfirmationModal == 'CompleteJob'">
          <q-btn
            no-caps
            unelevated
            color="accent"
            label="Complete & Print"
            class="btn-no-wrap text-body1 full-width"
            :loading="appActionIsLoadingData"
            @click="completePicklistJob(true)"
          />
          <q-space class="q-mx-xs" />
          <q-btn
            no-caps
            unelevated
            color="accent"
            label="Complete"
            class="text-body1 full-width"
            :loading="appActionIsLoadingData"
            @click="completePicklistJob(false)"
          />
        </template>
        <q-btn
          v-else
          no-caps
          unelevated
          color="negative"
          label="Delete Job"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="cancelPicklistJob(); hideModal();"
        />
        <q-space
          v-if="currentScreenSize !== 'xs'"
          class="q-mx-xs"
        />
        <q-btn
          v-if="currentScreenSize !== 'xs'"
          outline
          no-caps
          label="Cancel"
          class="text-body1 full-width"
          @click="hideModal"
        />
      </q-card-section>
    </template>
  </PopupModal>

  <!-- picklist item detail modal -->
  <PicklistItemDetailModal
    v-if="showPicklistItemDetailModal"
    @hide="showPicklistItemDetailModal = false"
  />

  <!-- print component: picklist job report -->
  <PicklistBatchSheet
    ref="batchSheetComponent"
    :picklist-job-details="picklistJob"
    :picklist-job-items="picklistItems"
  />

  <!-- audit trail modal -->
  <AuditTrail
    v-if="showAuditTrailModal"
    ref="historyModal"
    @reset="showAuditTrailModal = null"
    :job-type="showAuditTrailModal"
    :job-id="picklistJob.id"
  />
</template>

<script setup>
import { onBeforeMount, onMounted, ref, computed, inject, toRaw, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { useUserStore } from '@/stores/user-store'
import { usePicklistStore } from '@/stores/picklist-store'
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
import PicklistBatchSheet from '@/components/Picklist/PicklistBatchSheet.vue'
import AuditTrail from '@/components/AuditTrail.vue'
import PicklistItemDetailModal from '@/components/Picklist/PicklistItemDetailModal.vue'

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
  patchPicklistJob,
  deletePicklistJob,
  patchPicklistJobItemScanned,
  deletePicklistJobItem,
  getPicklistJobItem
} = usePicklistStore()
const {
  picklistJob,
  originalPicklistJob,
  allItemsRetrieved,
  picklistItems
} = storeToRefs(usePicklistStore())

// Local Data
const confirmationModal = ref(null)
const batchSheetComponent = ref(null)
const editJob = ref(false)
const itemTableVisibleColumns = ref([
  'actions',
  'barcode',
  'tray_barcode',
  'owner',
  'size_class',
  'item_location',
  'status'
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
    name: 'barcode',
    field: row => row.item ? renderItemBarcodeDisplay(row.item) : renderItemBarcodeDisplay(row.non_tray_item),
    label: 'Barcode',
    align: 'left',
    sortable: true
  },
  {
    name: 'tray_barcode',
    field: row => row.item ? renderItemBarcodeDisplay(row.item?.tray) : '',
    label: 'Tray Barcode',
    align: 'left',
    sortable: true
  },
  {
    name: 'owner',
    field: row => row.item ? row.item?.owner?.name : row.non_tray_item?.owner?.name,
    label: 'Owner',
    align: 'left',
    sortable: true
  },
  {
    name: 'size_class',
    field: row => row.item ? row.item?.size_class?.name : row.non_tray_item?.size_class?.name,
    label: 'Size Class',
    align: 'left',
    sortable: true
  },
  {
    name: 'item_location',
    field: row => row.item ? getItemLocation(row.item.tray) : getItemLocation(row.non_tray_item),
    label: 'Item Location',
    align: 'left',
    sortable: true
  },
  {
    name: 'status',
    field: row => row.item ? row.item.status : row.non_tray_item.status,
    label: '',
    align: 'center',
    sortable: false,
    required: true,
    headerStyle: 'max-width: 200px'
  }
])
const itemTableFilters = computed(() => {
  let tablesFilters = []
  if (picklistItems.value.length > 0) {
    tablesFilters = [
      {
        field: row => row.item ? row.item?.owner?.name : row.non_tray_item?.owner?.name,
        label: 'Owner',
        // render options based on the passed in table data
        // loop through all containers and return customized data set for table filtering and remove the duplicates
        options: getUniqueListByKey(picklistItems.value.map(tableEntry => {
          return {
            text: tableEntry.item ? tableEntry.item?.owner?.name : tableEntry.non_tray_item?.owner?.name,
            value: false
          }
        }), 'text')
      },
      {
        field: row => row.item ? row.item?.size_class?.name : row.non_tray_item?.size_class?.name,
        label: 'Size Class',
        options: getUniqueListByKey(picklistItems.value.map(tableEntry => {
          return {
            text: tableEntry.item ? tableEntry.item?.size_class?.name : tableEntry.non_tray_item?.size_class?.name,
            value: false
          }
        }), 'text')
      }
    ]
  }
  return tablesFilters
})
const showConfirmationModal = ref(null)
const historyModal = ref(null)
const showAuditTrailModal = ref(false)
const showPicklistItemDetailModal = ref(false)

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
      'barcode',
      'tray_barcode',
      'item_location',
      'status'
    ]
  }
})

onMounted(async () => {
  // when user is online and loads a job we store the current picklist job data and original in indexdb for reference offline
  if (!appIsOffline.value) {
    addDataToIndexDb('picklistStore', 'picklistJob', JSON.parse(JSON.stringify(picklistJob.value)))
    addDataToIndexDb('picklistStore', 'originalPicklistJob', JSON.parse(JSON.stringify(originalPicklistJob.value)))
  } else {
    // get saved picklist job data if were offline and page was reloaded/refreshed
    const res = await getDataInIndexDb('picklistStore')
    picklistJob.value = res.data.picklistJob
    originalPicklistJob.value = res.data.originalPicklistJob
  }
})

watch(compiledBarCode, (barcode) => {
  if (barcode !== '' && picklistJob.value.status == 'Running') {
    // only allow scans if the picklist job is in a running state
    triggerItemScan(barcode)
  }
})
const triggerItemScan = (barcode_value) => {
  // check if the scanned barcode is in the item data and that the barcode hasnt been retrieved already
  if (!picklistItems.value.some(itm => itm.item ? itm.item.barcode.value == barcode_value : itm.non_tray_item.barcode.value == barcode_value)) {
    handleAlert({
      type: 'error',
      text: 'The scanned item does not exist in this pick list job. Please try again.',
      autoClose: true
    })
    return
  } else if (picklistItems.value.some(itm => itm.item ? itm.item.barcode.value == barcode_value && itm.status !== 'PickList' : itm.non_tray_item.barcode.value == barcode_value && itm.status !== 'PickList')) {
    handleAlert({
      type: 'error',
      text: 'The scanned item has already been marked as retrieved.',
      autoClose: true
    })
    return
  } else {
    // update the scanned request item to retrieved
    updatePicklistItem(barcode_value)
  }
}

const handleOptionMenu = async (action, rowData) => {
  switch (action.text) {
    case 'Edit':
      editJob.value = true
      return
    case 'Delete Job':
      showConfirmationModal.value = 'DeleteJob'
      return
    case 'Revert Item to Queue':
      removePicklistItem(rowData.id)
      return
    case 'Print Job':
      batchSheetComponent.value.printBatchReport()
      return
    case 'View History':
      showAuditTrailModal.value = 'pick_lists'
      return
  }
}

const cancelPicklistJobEdits = () => {
  picklistJob.value = { ...toRaw(originalPicklistJob.value) }
  editJob.value = false
}
const executePicklistJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: picklistJob.value.id,
      status: 'Running',
      user_id: picklistJob.value.user_id ? picklistJob.value.user_id : userData.value.user_id,
      run_timestamp: currentIsoDate()
    }
    await patchPicklistJob(payload)

    // store the current picklist job data in indexdb for reference offline whenever job is executed
    addDataToIndexDb('picklistStore', 'picklistJob', JSON.parse(JSON.stringify(picklistJob.value)))
    addDataToIndexDb('picklistStore', 'originalPicklistJob', JSON.parse(JSON.stringify(originalPicklistJob.value)))

    handleAlert({
      type: 'success',
      text: 'Pick List Job Successfully Started',
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
const updatePicklistJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: picklistJob.value.id,
      user_id: picklistJob.value.user_id,
      run_timestamp: currentIsoDate()
    }
    await patchPicklistJob(payload)

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
    editJob.value = false
  }
}
const updatePicklistJobStatus = async (status) => {
  try {
    appIsLoadingData.value = true
    const payload = {
      id: picklistJob.value.id,
      status,
      run_timestamp: currentIsoDate()
    }
    await patchPicklistJob(payload)

    if (appIsOffline.value) {
      // when offline we update the status directly
      picklistJob.value.status = payload.status
      originalPicklistJob.value.status = payload.status
    }

    // store the current picklist job data in indexdb for reference offline whenever job is executed
    addDataToIndexDb('picklistStore', 'picklistJob', JSON.parse(JSON.stringify(picklistJob.value)))
    addDataToIndexDb('picklistStore', 'originalPicklistJob', JSON.parse(JSON.stringify(originalPicklistJob.value)))

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
const cancelPicklistJob = async () => {
  try {
    appIsLoadingData.value = true
    await deletePicklistJob(picklistJob.value.id)

    handleAlert({
      type: 'success',
      text: 'The Pick List Job has been canceled.',
      autoClose: true
    })

    router.push({
      name: 'picklist',
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
    deleteDataInIndexDb('picklistStore', 'picklistJob')
    deleteDataInIndexDb('picklistStore', 'originalPicklistJob')
  }
}
const completePicklistJob = async (printBool) => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: picklistJob.value.id,
      status: 'Completed',
      run_timestamp: currentIsoDate()
    }
    await patchPicklistJob(payload)

    if (printBool) {
      batchSheetComponent.value.printBatchReport()
    }
    handleAlert({
      type: 'success',
      text: 'The Pick List Job has been completed.',
      autoClose: true
    })

    router.push({
      name: 'picklist',
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
    deleteDataInIndexDb('picklistStore', 'picklistJob')
    deleteDataInIndexDb('picklistStore', 'originalPicklistJob')
    confirmationModal.value.hideModal()
  }
}
const removePicklistItem = async (itemId) => {
  try {
    appIsLoadingData.value = true
    await deletePicklistJobItem(itemId)

    if (appIsOffline.value) {
      // when offline we remove the picklistItem directly
      picklistJob.value.requests = picklistJob.value.requests.filter(r => r.id !== itemId)
      originalPicklistJob.value.requests = originalPicklistJob.value.requests.filter(r => r.id !== itemId)
    }

    // store the current picklist job data in indexdb for reference offline whenever job is executed
    addDataToIndexDb('picklistStore', 'picklistJob', JSON.parse(JSON.stringify(picklistJob.value)))
    addDataToIndexDb('picklistStore', 'originalPicklistJob', JSON.parse(JSON.stringify(originalPicklistJob.value)))

    handleAlert({
      type: 'success',
      text: `${itemId} has been sent back to the request queue.`,
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
const updatePicklistItem = async (barcode_value) => {
  try {
    appIsLoadingData.value = true
    const pickListItemToUpdateIndex = picklistJob.value.requests.findIndex(itm => itm.item ? itm.item.barcode.value == barcode_value : itm.non_tray_item.barcode.value == barcode_value)
    const pickListItemToUpdate = picklistJob.value.requests[pickListItemToUpdateIndex]
    const payload = {
      id: picklistJob.value.id,
      request_id: pickListItemToUpdate.id,
      run_timestamp: currentIsoDate(),
      status: 'Out'
    }
    await patchPicklistJobItemScanned(payload)

    // update the item directly in the picklist job and set it to retrieved
    pickListItemToUpdate.item ? pickListItemToUpdate.item.status = 'Out' : pickListItemToUpdate.non_tray_item.status = 'Out'

    // move the item to bottom of the list if in offline mode
    if (appIsOffline.value) {
      picklistJob.value.requests.splice(pickListItemToUpdateIndex, 1)
      picklistJob.value.requests.push(pickListItemToUpdate)
    }

    // update our original picklist job state
    originalPicklistJob.value = { ...toRaw(picklistJob.value) }

    // store the current picklist job data in indexdb for reference offline whenever job is executed
    addDataToIndexDb('picklistStore', 'picklistJob', JSON.parse(JSON.stringify(picklistJob.value)))
    addDataToIndexDb('picklistStore', 'originalPicklistJob', JSON.parse(JSON.stringify(originalPicklistJob.value)))
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
const loadPicklistItem = (barcode_value) => {
  // since we already have all the items data we just need to set the refileItem from the refileJob items directly
  getPicklistJobItem(barcode_value)
  showPicklistItemDetailModal.value = true
}
</script>

<style lang="scss" scoped>
</style>
