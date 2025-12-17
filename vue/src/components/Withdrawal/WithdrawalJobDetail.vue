<template>
  <InfoDisplayLayout class="withdrawal-job">
    <template #number-box-content>
      <div class="flex q-mb-xs no-wrap">
        <MoreOptionsMenu
          :options="[
            { text: 'Edit', disabled: editJob || withdrawJob.status == 'Completed' },
            { text: 'Delete Job', optionClass: 'text-negative', disabled: editJob || withdrawJob.status == 'Completed' || withdrawJobItems.some(itm => itm.status == 'Withdrawn')},
            { text: 'Print Job' },
            { text: 'View History' }
          ]"
          class="q-mr-xs"
          @click="handleOptionMenu"
        />
        <h1
          id="withdrawJobId"
          class="info-display-details-label text-h4"
        >
          Withdraw Job:
        </h1>
      </div>
      <p class="info-display-number-box text-h4">
        {{ withdrawJob.id }}
      </p>
    </template>

    <template #details-content>
      <div class="col-xs-6 col-sm-6 col-md-grow col-lg-2 q-mb-xs-md q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-lg">
        <div class="info-display-details">
          <label
            class="info-display-details-label-2 text-h6"
          >
            Assigned User:
          </label>
          <p
            v-if="!editJob"
            class="text-body1"
          >
            {{ withdrawJob.assigned_user?.name }}
          </p>
          <SelectInput
            v-else
            v-model="withdrawJob.assigned_user_id"
            :options="users"
            option-type="users"
            option-value="id"
            option-label="name"
            aria-label="userSelect"
            class="q-pr-xs-sm q-pr-md-none"
          />
        </div>
      </div>
      <div class="col-xs-6 col-sm-6 col-md-grow col-lg-auto q-mb-xs-md q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-lg">
        <div class="info-display-details">
          <label
            class="info-display-details-label-2 text-h6"
          >
            # of Items:
          </label>
          <p class="text-body1">
            {{ withdrawJobItems.length }}
          </p>
        </div>
      </div>
      <div class="col-xs-6 col-sm-6 col-md-grow col-lg-auto q-mb-xs-md q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-lg">
        <div class="info-display-details">
          <label
            class="info-display-details-label-2 text-h6"
          >
            Date Created
          </label>
          <p class="text-body1">
            {{ formatDateTime(withdrawJob.create_dt).date }}
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
            :class="withdrawJob.status == 'Completed' || withdrawJob.status == 'Created' ? 'text-highlight' : null "
          >
            {{ withdrawJob.status }}
          </p>
        </div>
      </div>

      <div
        v-if="currentScreenSize !== 'xs'"
        class="col-sm-12 col-md-12 col-lg-grow q-ml-auto"
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
            @click="updateWithdrawJob"
          />
          <q-btn
            no-caps
            unelevated
            outline
            color="accent"
            label="Cancel"
            class="btn-no-wrap text-body1"
            @click="cancelWithdrawJobEdits"
          />
        </div>
        <div
          v-else-if="withdrawJob.status !== 'Completed'"
          class="info-display-details-action q-mt-sm-sm q-mt-md-md"
        >
          <q-btn
            v-if="withdrawJobItems.some(itm => itm.status !== 'Out')"
            no-caps
            unelevated
            color="accent"
            :label="withdrawJob.pick_list_id ? 'Add To Pick List Job' : 'Create Pick List Job'"
            class="btn-no-wrap text-body1 q-mr-sm"
            :disabled="withdrawJob.pick_list_id && !withdrawJobItems.some(itm => itm.status == 'In')"
            @click="withdrawJob.pick_list_id ? addToPicklistJob() : createPicklistJob()"
          />
          <q-btn
            no-caps
            unelevated
            color="positive"
            :label="'Withdraw Items'"
            class="btn-no-wrap text-body1"
            :disabled="withdrawJobItems.length == 0 || withdrawJobItems.some(itm => itm.status !== 'Out')"
            :loading="appActionIsLoadingData"
            @click="showConfirmationModal = 'CompleteJob'"
          />
        </div>
      </div>
      <MobileActionBar
        v-else-if="currentScreenSize == 'xs' && editJob"
        button-one-color="accent"
        :button-one-label="'Save Edits'"
        :button-one-outline="false"
        :button-one-loading="appActionIsLoadingData"
        @button-one-click="updateWithdrawJob"
        button-two-color="accent"
        :button-two-label="'Cancel'"
        :button-two-outline="true"
        @button-two-click="cancelWithdrawJobEdits"
      />
      <MobileActionBar
        v-else-if="withdrawJob.status !== 'Completed'"
        button-one-color="accent"
        :button-one-label="withdrawJob.pick_list_id ? 'Add To Pick List Job' : 'Create Pick List Job'"
        :button-one-outline="false"
        :button-one-disabled="!withdrawJobItems.some(itm => itm.status == 'In') || (withdrawJob.pick_list_id && !withdrawJobItems.some(itm => itm.status == 'In'))"
        @button-one-click="withdrawJob.pick_list_id ? addToPicklistJob() : createPicklistJob()"
        button-two-color="positive"
        :button-two-label="'Withdraw Items'"
        :button-two-outline="false"
        :button-two-disabled="withdrawJobItems.length == 0 || withdrawJobItems.some(itm => itm.status !== 'Out')"
        :button-two-loading="appActionIsLoadingData"
        @button-two-click="showConfirmationModal = 'CompleteJob'"
      />
    </template>

    <template #table-content>
      <EssentialTable
        v-if="withdrawJob.trays.length > 0"
        :table-columns="trayTableColumns"
        :table-visible-columns="trayTableVisibleColumns"
        :filter-options="trayTableFilters"
        :table-data="withdrawJob.trays"
        :row-key="'id'"
        :enable-table-reorder="false"
        :enable-selection="false"
        :heading-row-class="'q-mb-lg q-px-xs-sm q-px-sm-md'"
        :heading-filter-class="currentScreenSize == 'xs' ? 'col-xs-6 q-mr-auto' : 'q-ml-auto'"
        class="q-mb-lg"
      >
        <template #heading-row>
          <div class="col-xs-7 col-sm-5 col-md-auto">
            <label class="text-h4 text-bold">
              Trays in Job:
            </label>
          </div>
        </template>

        <template #table-td="{ colName, props }">
          <span
            v-if="colName == 'actions'"
          >
            <MoreOptionsMenu
              :options="[{ text: 'Remove Item', disabled: withdrawJob.status == 'Completed' }]"
              class=""
              @click="handleOptionMenu($event, props.row)"
            />
          </span>
        </template>
      </EssentialTable>

      <EssentialTable
        :table-columns="itemTableColumns"
        :table-visible-columns="itemTableVisibleColumns"
        :filter-options="itemTableFilters"
        :table-data="withdrawJobItems"
        :row-key="'id'"
        :enable-table-reorder="false"
        :enable-selection="false"
        :heading-row-class="'justify-end q-mb-lg q-px-xs-sm q-px-sm-md'"
        :heading-filter-class="currentScreenSize == 'xs' ? 'col-xs-6 q-mr-auto' : 'q-ml-auto'"
        :highlight-row-class="'bg-color-green-light'"
        :highlight-row-key="'status'"
        :highlight-row-value="'Withdrawn'"
      >
        <template #heading-row>
          <div class="col-xs-7 col-sm-5 col-md-auto q-mb-md-sm q-mr-auto">
            <h2 class="text-h4 text-bold">
              Items in Job:
            </h2>
          </div>

          <div
            class="col-xs-grow col-sm-7 col-md-auto flex"
            :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? 'justify-end q-mb-md' : 'order-1'"
          >
            <q-btn
              no-caps
              unelevated
              icon-right="arrow_drop_down"
              color="accent"
              label="Add Items"
              class="text-body1 q-ml-xs-none q-ml-sm-sm"
              :disabled="withdrawJob.status == 'Completed'"
              aria-label="addWithdrawItemsMenu"
              aria-haspopup="menu"
              :aria-expanded="withdrawItemsMenuState"
            >
              <q-menu
                @show="withdrawItemsMenuState = true"
                @hide="withdrawItemsMenuState = false"
                aria-label="withdrawItemsMenuList"
              >
                <q-list>
                  <q-item
                    clickable
                    v-close-popup
                    @click="showAddItemModal = 'Manual'"
                    role="menuitem"
                  >
                    <q-item-section>
                      <q-item-label>
                        <span class="text-no-wrap">
                          Manually Enter Barcode
                        </span>
                      </q-item-label>
                    </q-item-section>
                  </q-item>
                  <q-item
                    clickable
                    v-close-popup
                    @click="showAddItemModal = 'Scan'"
                    role="menuitem"
                  >
                    <q-item-section>
                      <q-item-label>
                        <span>
                          Scan Item(s)
                        </span>
                      </q-item-label>
                    </q-item-section>
                  </q-item>
                  <q-item
                    clickable
                    v-close-popup
                    @click="showAddItemModal = 'Bulk'"
                    role="menuitem"
                  >
                    <q-item-section>
                      <q-item-label>
                        <span>
                          Bulk Upload Items
                        </span>
                      </q-item-label>
                    </q-item-section>
                  </q-item>
                </q-list>
              </q-menu>
            </q-btn>
          </div>
        </template>

        <template #table-td="{ colName, props, value }">
          <span
            v-if="colName == 'actions'"
          >
            <MoreOptionsMenu
              :options="[{ text: 'Remove Item', disabled: props.row.status == 'Withdrawn' || withdrawJob.status == 'Completed' }]"
              class=""
              @click="handleOptionMenu($event, props.row)"
            />
          </span>
          <span
            v-else-if="colName == 'status'"
            class="text-nowrap"
            :class="value == 'Withdrawn' ? 'text-positive' : 'text-highlight-negative outline'"
          >
            {{ value == 'Withdrawn' ? 'Withdrawn' : value }}
            <q-icon
              v-if="value == 'Withdrawn'"
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
    :text="showConfirmationModal == 'CompleteJob' ? 'Are you sure you want to withdraw these items from the system?' : 'Are you sure you want to delete the job?'"
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
          label="Withdraw & Print"
          class="btn-no-wrap text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="completeWithdrawJob('withdrawAndPrint'); hideModal();"
        />

        <q-space class="q-mx-xs" />

        <q-btn
          v-if="showConfirmationModal == 'CompleteJob'"
          no-caps
          unelevated
          color="accent"
          label="Withdraw Items"
          class="btn-no-wrap text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="completeWithdrawJob('withdraw'); hideModal();"
        />
        <q-btn
          v-else
          no-caps
          unelevated
          color="negative"
          label="Delete Job"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="cancelWithdrawJob(); hideModal();"
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

  <!-- add item modal -->
  <WithdrawalJobAddItemModal
    v-if="showAddItemModal"
    :entry-type="showAddItemModal"
    @hide="showAddItemModal = null"
  />

  <!-- Print detail -->
  <WithdrawalBatchSheet
    ref="batchSheetComponent"
    :withdrawal-job-details="withdrawJob"
  />

  <!-- audit trail modal -->
  <AuditTrail
    v-if="showAuditTrailModal"
    ref="historyModal"
    @reset="showAuditTrailModal = null"
    :job-type="showAuditTrailModal"
    :job-id="withdrawJob.id"
  />
</template>

<script setup>
import { onBeforeMount, ref, computed, inject, toRaw } from 'vue'
import { useRouter } from 'vue-router'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { useUserStore } from '@/stores/user-store'
import { useWithdrawalStore } from '@/stores/withdrawal-store'
import { storeToRefs } from 'pinia'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import InfoDisplayLayout from '@/components/InfoDisplayLayout.vue'
import EssentialTable from '@/components/EssentialTable.vue'
import MobileActionBar from '@/components/MobileActionBar.vue'
import MoreOptionsMenu from '@/components/MoreOptionsMenu.vue'
import SelectInput from '@/components/SelectInput.vue'
import PopupModal from '@/components/PopupModal.vue'
import WithdrawalJobAddItemModal from '@/components/Withdrawal/WithdrawalJobAddItemModal.vue'
import AuditTrail from '@/components/AuditTrail.vue'
import WithdrawalBatchSheet from '@/components/Withdrawal/WithdrawalBatchSheet.vue'

const router = useRouter()

// Composables
const { currentScreenSize } = useCurrentScreenSize()

// Store Data
const {
  appIsLoadingData,
  appActionIsLoadingData
} = storeToRefs(useGlobalStore())
const { userData } = storeToRefs(useUserStore())
const { users } = storeToRefs(useOptionStore())
const {
  patchWithdrawJob,
  deleteWithdrawJob,
  deleteWithdrawJobItems
} = useWithdrawalStore()
const {
  withdrawJob,
  originalWithdrawJob,
  withdrawJobItems
} = storeToRefs(useWithdrawalStore())

// Local Data
const batchSheetComponent = ref(null)
const withdrawItemsMenuState = ref(false)
const editJob = ref(false)
const itemTableVisibleColumns = ref([
  'actions',
  'shelf_barcode',
  'tray_barcode',
  'barcode',
  'owner',
  'status',
  'withdrawn_location'
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
    name: 'shelf_barcode',
    field: row => row.status === 'Withdrawn' ? renderWithdrawnShelfBarcode(row) : (row.tray ? row.tray?.shelf_position?.shelf?.barcode?.value : row.shelf_position?.shelf?.barcode?.value),
    label: 'Shelf Barcode',
    align: 'left',
    sortable: true
  },
  {
    name: 'tray_barcode',
    field: row => row.status === 'Withdrawn' ? renderWithdrawnTrayBarcode(row) : renderItemBarcodeDisplay(row.tray),
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
    name: 'status',
    field: 'status',
    label: 'Item Status',
    align: 'left',
    sortable: false
  },
  {
    name: 'withdrawn_location',
    field: row => renderWithdrawnItemLocation(row),
    label: 'Location',
    align: 'left',
    sortable: true
  }
])
const itemTableFilters = computed(() => {
  let tablesFilters = []
  if (withdrawJobItems.value.length > 0) {
    tablesFilters = [
      {
        field: row => row.owner?.name,
        label: 'Owner',
        // render options based on the passed in table data
        // loop through all containers and return customized data set for table filtering and remove the duplicates
        options: getUniqueListByKey(withdrawJobItems.value.map(tableEntry => {
          return {
            text: tableEntry.owner?.name,
            value: false
          }
        }), 'text')
      }
    ]
  }
  return tablesFilters
})
const trayTableVisibleColumns = ref([
  'actions',
  'shelf_barcode',
  'tray_barcode',
  'owner'
])
const trayTableColumns = ref([
  {
    name: 'actions',
    field: 'actions',
    label: '',
    align: 'center',
    sortable: false,
    required: true
  },
  {
    name: 'shelf_barcode',
    field: row => row.status === 'Withdrawn' ? renderWithdrawnShelfBarcode(row) : row.shelf_position?.shelf?.barcode?.value,
    label: 'Shelf Barcode',
    align: 'left',
    sortable: true
  },
  {
    name: 'tray_barcode',
    field: row => row.status === 'Withdrawn' ? renderWithdrawnTrayBarcode(row) : renderItemBarcodeDisplay(row),
    label: 'Tray Barcode',
    align: 'left',
    sortable: true
  },
  {
    name: 'owner',
    field: row => row.owner?.name,
    label: 'Owner',
    align: 'left',
    sortable: true
  }
])
const trayTableFilters = computed(() => {
  let tablesFilters = []
  if (withdrawJob.value.trays.length > 0) {
    tablesFilters = [
      {
        field: row => row.owner?.name,
        label: 'Owner',
        options: getUniqueListByKey(withdrawJob.value.trays.map(tableEntry => {
          return {
            text: tableEntry.owner?.name,
            value: false
          }
        }), 'text')
      }
    ]
  }
  return tablesFilters
})
const showConfirmationModal = ref(null)
const showAddItemModal = ref(null)
const historyModal = ref(null)
const showAuditTrailModal = ref(false)

// Logic
const handleAlert = inject('handle-alert')
const currentIsoDate = inject('current-iso-date')
const formatDateTime = inject('format-date-time')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')
const renderWithdrawnTrayBarcode = inject('render-withdrawn-tray-barcode')
const renderWithdrawnShelfBarcode = inject('render-withdrawn-shelf-barcode')
const renderWithdrawnItemLocation = inject('render-withdrawn-item-location')
const getUniqueListByKey = inject('get-uniqure-list-by-key')

onBeforeMount(() => {
  if (currentScreenSize.value == 'xs') {
    itemTableVisibleColumns.value = [
      'actions',
      'shelf_barcode',
      'tray_barcode',
      'barcode',
      'status'
    ]
  }
})

const handleOptionMenu = async (action, rowData) => {
  switch (action.text) {
    case 'Edit':
      editJob.value = true
      return
    case 'Delete Job':
      showConfirmationModal.value = 'DeleteJob'
      return
    case 'Remove Item':
      removeWithdrawItems([rowData.barcode.value])
      return
    case 'Print Job':
      batchSheetComponent.value.printBatchReport()
      return
    case 'View History':
      showAuditTrailModal.value = 'withdraw_jobs'
      return
  }
}

const cancelWithdrawJobEdits = () => {
  withdrawJob.value = { ...toRaw(originalWithdrawJob.value) }
  editJob.value = false
}
const updateWithdrawJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: withdrawJob.value.id,
      assigned_user_id: withdrawJob.value.assigned_user_id,
      run_timestamp: currentIsoDate()
    }
    await patchWithdrawJob(payload)

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
const cancelWithdrawJob = async () => {
  try {
    appIsLoadingData.value = true
    await deleteWithdrawJob(withdrawJob.value.id)

    handleAlert({
      type: 'success',
      text: 'The Withdraw Job has been canceled.',
      autoClose: true
    })

    router.push({
      name: 'withdrawal',
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
  }
}
const completeWithdrawJob = async (withdrawType) => {
  try {
    // check if an associated picklist exists and make sure it is completed
    if (withdrawJob.value.pick_list && withdrawJob.value.pick_list.status !== 'Completed') {
      handleAlert({
        type: 'error',
        text: `A Pick list job # <a href='/picklist/${withdrawJob.value.pick_list_id}' tabindex='0'>${withdrawJob.value.pick_list_id}</a> was generated  for withdrawal but not completed yet, please complete the picklist job inorder to complete withdrawal process.`,
        autoClose: false
      })
      return
    }

    appActionIsLoadingData.value = true
    const payload = {
      id: withdrawJob.value.id,
      status: 'Completed',
      assigned_user_id: withdrawJob.value.assigned_user_id ? withdrawJob.value.assigned_user_id : userData.value.user_id,
      run_timestamp: currentIsoDate()
    }
    await patchWithdrawJob(payload)

    handleAlert({
      type: 'success',
      text: 'All items have been successfully withdrawn, the job has been completed.',
      autoClose: true
    })

    // If the user has selected complete and print, let's print!
    if (withdrawType && withdrawType === 'withdrawAndPrint') {
      batchSheetComponent.value.printBatchReport()
    }

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
const removeWithdrawItems = async (barcode_values) => {
  try {
    appIsLoadingData.value = true
    // const payload = {
    //   barcode_values
    // }
    // TEMP singular barcode delete until multidelete is implemented
    const payload = {
      barcode_value: barcode_values[0]
    }
    await deleteWithdrawJobItems(payload)

    handleAlert({
      type: 'success',
      text: 'The item has been removed from the job.',
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

const createPicklistJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: withdrawJob.value.id,
      create_pick_list: true
    }
    await patchWithdrawJob(payload)

    // display an alert with the created picklist job id so you can click that and link directly to the job if needed
    handleAlert({
      type: 'success',
      text: `Successfully created Pick List #: <a href='/picklist/${withdrawJob.value.pick_list_id}' tabindex='0'>${withdrawJob.value.pick_list_id}</a>`,
      autoClose: false
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
const addToPicklistJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: withdrawJob.value.id,
      add_to_picklist: true
    }
    await patchWithdrawJob(payload)

    // display an alert with the updated picklist job id so you can click that and link directly to the job if needed
    handleAlert({
      type: 'success',
      text: `Successfully updated Pick List #: <a href='/picklist/${withdrawJob.value.pick_list_id}' tabindex='0'>${withdrawJob.value.pick_list_id}</a>`,
      autoClose: false
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
</script>

<style lang="scss" scoped>
</style>
