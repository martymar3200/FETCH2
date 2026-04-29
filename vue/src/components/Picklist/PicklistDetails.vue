<template>
  <div class="picklist-details">
    <JobPageHeader
      title="Pick List"
      :job-id="picklistJob.id"
      :status="picklistJob.status"
      :status-color="getStatusColor(picklistJob.status)"
      :subtitle="headerSubtitle"
      :menu-options="headerMenuOptions"
    >
      <template #actions>
        <div v-if="picklistJob.status !== 'Completed'">
          <BaseButton
            v-if="picklistJob.status !== 'Created'"
            no-caps
            unelevated
            outline
            color="accent"
            :icon="picklistJob.status !== 'Paused' ? 'mdi-pause' : 'mdi-play'"
            :label="picklistJob.status == 'Paused' ? 'Resume Job' : 'Pause Job'"
            class="q-mr-sm"
            :disabled="appPendingSync || !checkUserPermission('can_edit_picklist_job')"
            @click="picklistJob.status == 'Paused' ? updatePicklistJobStatus('Running') : updatePicklistJobStatus('Paused')"
          />
          <BaseButton
            no-caps
            unelevated
            color="positive"
            :label="picklistJob.status == 'Created' ? 'Retrieve Pick List' : 'Complete Job'"
            :disabled="appIsOffline || appPendingSync || picklistJob.status == 'Paused' || !allItemsRetrieved || !checkUserPermission('can_edit_picklist_job')"
            :loading="appActionIsLoadingData"
            @click="picklistJob.status == 'Created' ? executePicklistJob() : showConfirmationModal = 'CompleteJob'"
          />
        </div>
      </template>
    </JobPageHeader>

    <!-- Quick User Assign Card -->
    <q-card
      v-if="editJob"
      flat
      bordered
      class="q-mb-lg user-assign-card"
    >
      <q-card-section class="row items-center q-pb-none">
        <div class="text-h6 text-bold">
          Assign User
        </div>
        <q-space />
        <BaseButton
          flat
          round
          dense
          icon="close"
          @click="cancelPicklistJobEdits"
        />
      </q-card-section>

      <q-card-section class="row q-col-gutter-md items-end">
        <div class="col-12 col-md-4">
          <label class="form-group-label">Select User</label>
          <SelectInput
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
        <div class="col-auto">
          <BaseButton
            no-caps
            unelevated
            color="accent"
            label="Save Assignment"

            :loading="appActionIsLoadingData"
            @click="updatePicklistJob"
          />
        </div>
      </q-card-section>
    </q-card>

    <!-- Items Table -->
    <q-card
      flat
      bordered
      class="table-card"
    >
      <EssentialTable
        :table-columns="itemTableColumns"
        :table-visible-columns="itemTableVisibleColumns"
        :filter-options="itemTableFilters"
        :table-data="picklistItems"
        :enable-table-reorder="false"
        :enable-selection="false"
        :heading-row-class="'q-mb-md q-px-md q-pt-md'"
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

        <template #table-td="{ colName, props: cellProps, value }">
          <span v-if="colName == 'actions'">
            <BaseButton
              v-if="cellProps.row.status === 'PickList' && picklistJob.status !== 'Paused' && picklistJob.status !== 'Completed' && checkUserPermission('can_edit_picklist_job')"
              flat
              round
              dense
              size="sm"
              icon="undo"
              color="negative"
              @click="handleOptionMenu({text: 'Revert Item to Queue'}, cellProps.row)"
            >
              <q-tooltip>Revert Item to Queue</q-tooltip>
            </BaseButton>
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
    </q-card>

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
            <BaseButton
              no-caps
              unelevated
              color="accent"
              label="Complete & Print"
              class="btn-no-wrap text-body1 full-width"
              :loading="appActionIsLoadingData"
              @click="completePicklistJob(true)"
            />
            <q-space class="q-mx-xs" />
            <BaseButton
              no-caps
              unelevated
              color="accent"
              label="Complete"
              class="text-body1 full-width"
              :loading="appActionIsLoadingData"
              @click="completePicklistJob(false)"
            />
          </template>
          <BaseButton
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
          <BaseButton
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
  </div>
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
import { onBeforeMount, onMounted, ref, computed, inject, toRaw, watch } from 'vue'
import { useRouter } from 'vue-router'
import { notify } from '@/utils/notify'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { useUserStore } from '@/stores/user-store'
import { usePicklistStore } from '@/stores/picklist-store'
import { storeToRefs } from 'pinia'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import { useIndexDbHandler } from '@/composables/useIndexDbHandler.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import JobPageHeader from '@/components/Job/JobPageHeader.vue'
import EssentialTable from '@/components/EssentialTable.vue'
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
  'barcode',
  'tray_barcode',
  'owner',
  'size_class',
  'item_location',
  'status',
  'actions'
])
const itemTableColumns = ref([
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
  },
  {
    name: 'actions',
    field: 'actions',
    label: '',
    align: 'center',
    sortable: false,
    required: true
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

const currentIsoDate = inject('current-iso-date')
const formatDateTime = inject('format-date-time')
const getItemLocation = inject('get-item-location')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')
const getUniqueListByKey = inject('get-unique-list-by-key')

const headerSubtitle = computed(() => {
  const building = picklistJob.value.building?.name || '-'
  const items = `${picklistJob.value.request_count || 0} items`
  const user = picklistJob.value.user?.name || 'Unassigned'
  const date = picklistJob.value.create_dt ? formatDateTime(picklistJob.value.create_dt).date : '-'
  return `${building} • ${items} • ${user} • ${date}`
})

const headerMenuOptions = computed(() => [
  {
    label: 'Assign User',
    hidden: !checkUserPermission('can_assign_jobs'),
    disabled: appIsOffline.value || editJob.value || picklistJob.value.status == 'Paused' || picklistJob.value.status == 'Completed',
    action: () => {
      editJob.value = true
    }
  },
  {
    label: 'Delete Job',
    hidden: !checkUserPermission('can_delete_picklist_job'),
    color: 'negative',
    disabled: appIsOffline.value || editJob.value || picklistJob.value.status == 'Completed' || picklistItems.value.some(itm => itm.status !== 'PickList'),
    action: () => {
      showConfirmationModal.value = 'DeleteJob'
    }
  },
  {
    label: 'Print Job',
    action: () => {
      batchSheetComponent.value.printBatchReport()
    }
  },
  {
    label: 'View History',
    action: () => {
      showAuditTrailModal.value = 'pick_lists'
    }
  }
])

const getStatusColor = (status) => {
  const colors = {
    Created: 'grey',
    Running: 'info',
    Paused: 'warning',
    Completed: 'positive'
  }
  return colors[status] || 'grey'
}

onBeforeMount(() => {
  if (currentScreenSize.value == 'xs') {
    itemTableVisibleColumns.value = [
      'barcode',
      'tray_barcode',
      'item_location',
      'status',
      'actions'
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
    notify({
      type: 'negative',
      message: 'The scanned item does not exist in this pick list job. Please try again.'
    })
    return
  } else if (picklistItems.value.some(itm => itm.item ? itm.item.barcode.value == barcode_value && itm.status !== 'PickList' : itm.non_tray_item.barcode.value == barcode_value && itm.status !== 'PickList')) {
    notify({
      type: 'negative',
      message: 'The scanned item has already been marked as retrieved.'
    })
    return
  } else {
    // update the scanned request item to retrieved
    updatePicklistItem(barcode_value)
  }
}

const handleOptionMenu = async (action, rowData) => {
  switch (action.text) {
    case 'Assign User':
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

    notify({
      type: 'positive',
      message: 'Pick List Job Successfully Started'
    })
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to start job'
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

    notify({
      type: 'positive',
      message: 'The job has been updated.'
    })
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to update job'
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

    notify({
      type: 'positive',
      message: `Job Status has been updated to: ${status}`
    })
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to update status'
    })
  } finally {
    appIsLoadingData.value = false
  }
}
const cancelPicklistJob = async () => {
  try {
    appIsLoadingData.value = true
    await deletePicklistJob(picklistJob.value.id)

    notify({
      type: 'positive',
      message: 'The Pick List Job has been canceled.'
    })

    router.push({
      name: 'picklist',
      params: {
        jobId: null
      }
    })
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to cancel job'
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
    notify({
      type: 'positive',
      message: 'The Pick List Job has been completed.'
    })

    router.push({
      name: 'picklist',
      params: {
        jobId: null
      }
    })
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to complete job'
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

    notify({
      type: 'positive',
      message: `${itemId} has been sent back to the request queue.`
    })
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to remove item'
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

    // Note: The store's optimisticUpdate handles moving the item to the bottom of the list and syncing IndexDb.
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to update item'
    })
  } finally {
    appIsLoadingData.value = false
  }
}
const loadPicklistItem = (barcode_value) => {
  // since we already have all the items data we just need to set the refileItem from the picklistJob items directly
  getPicklistJobItem(barcode_value)
  showPicklistItemDetailModal.value = true
}
</script>

<style lang="scss" scoped>
.picklist-details {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.user-assign-card {
  border-radius: 12px;
  background: white;
}

.table-card {
  border-radius: 12px;
  overflow: hidden;
}

.form-group-label {
  display: block;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  color: #a0aec0;
  letter-spacing: 0.05em;
  margin-bottom: 4px;
}

</style>
