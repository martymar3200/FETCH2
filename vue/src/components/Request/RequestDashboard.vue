<template>
  <div class="request-dashboard">
    <div class="row q-mb-xs-xl q-mb-sm-none">
      <div class="col-grow q-mb-xs-md q-mb-sm-none">
        <EssentialTable
          ref="requestTableComponent"
          :table-columns="requestDisplayType == 'request_view' ? requestTableColumns : requestBatchTableColumns"
          :table-visible-columns="requestDisplayType == 'request_view' ? requestTableVisibleColumns : requestBatchTableVisibleColumns"
          :filter-options="showCreatePickList || showAddPickList ? [] : requestDisplayType == 'request_view' ? requestTableFilters : requestBatchTableFilters"
          :table-data="requestJobList"
          :enable-table-reorder="false"
          :enable-selection="showCreatePickList || showAddPickList"
          :heading-row-class="'q-mb-xs-md q-mb-md-xl'"
          :heading-filter-class="currentScreenSize == 'xs' ? 'col-xs-6 q-mr-auto' : 'q-ml-auto'"
          :heading-rearrange-class="showCreatePickList || showAddPickList ? 'q-ml-auto' : null"
          :enable-pagination="true"
          :pagination-total="requestJobListTotal"
          :pagination-loading="appIsLoadingData"
          :rows-per-page-options="[25, 50, 75, 100, 250, 500]"
          @update-pagination="loadRequestJobs($event)"
          @selected-table-row="loadRequestJob($event.id)"
          @selected-data="selectedRequestItems = $event"
        >
          <template #heading-row>
            <div
              class="col-sm-5 col-md-12 q-mb-md-sm"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? '' : 'self-center'"
            >
              <h1 class="text-h4 text-bold">
                {{ requestDisplayType == 'request_view' ? 'Requests' : 'Batch Requests' }}
              </h1>
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
                label="Create"
                class="text-body1 q-ml-xs-none q-ml-sm-sm"
                :disabled="showCreatePickList || showAddPickList"
                aria-label="createRequestJobMenu"
                aria-haspopup="menu"
                :aria-expanded="requestJobMenuState"
              >
                <q-menu
                  @show="requestJobMenuState = true"
                  @hide="requestJobMenuState = false"
                  aria-label="requestJobMenuList"
                >
                  <q-list>
                    <q-item
                      v-if="checkUserPermission('can_add_to_picklist_job')"
                      clickable
                      v-close-popup
                      @click="showPickListModal = 'Add'"
                      role="menuitem"
                    >
                      <q-item-section>
                        <q-item-label>
                          <span>
                            Add to Pick List
                          </span>
                        </q-item-label>
                      </q-item-section>
                    </q-item>
                    <q-item
                      v-if="checkUserPermission('can_create_picklist_job')"
                      clickable
                      v-close-popup
                      @click="showPickListModal = 'Create'"
                      role="menuitem"
                    >
                      <q-item-section>
                        <q-item-label>
                          <span>
                            Create a Pick List
                          </span>
                        </q-item-label>
                      </q-item-section>
                    </q-item>
                    <q-item
                      v-if="checkUserPermission('can_create_and_submit_manual_requests')"
                      clickable
                      v-close-popup
                      @click="showCreateRequestByType = 'manual'"
                      role="menuitem"
                    >
                      <q-item-section>
                        <q-item-label>
                          <span>
                            Create Manual Requests
                          </span>
                        </q-item-label>
                      </q-item-section>
                    </q-item>
                    <q-item
                      v-if="checkUserPermission('can_create_and_submit_batch_requests')"
                      clickable
                      v-close-popup
                      @click="showCreateRequestByType = 'bulk'"
                      role="menuitem"
                    >
                      <q-item-section>
                        <q-item-label>
                          <span class="text-no-wrap">
                            Import Requests from File
                          </span>
                        </q-item-label>
                      </q-item-section>
                    </q-item>
                  </q-list>
                </q-menu>
              </q-btn>
            </div>

            <div
              class="col-xs-12 col-sm-auto col-md-auto q-mb-xs-md q-mb-sm-none"
            >
              <ToggleButtonInput
                v-model="requestDisplayType"
                :options="[
                  {label: 'Request View', value: 'request_view'},
                  {label: 'Batch View', value: 'batch_view'}
                ]"
                @update:model-value="clearTableSelection(); requestTableComponent.resetTablePagination(); loadRequestJobs();"
                class="text-no-wrap"
              />
            </div>

            <div
              v-if="(showCreatePickList || showAddPickList) && currentScreenSize !== 'xs'"
              class="col-12 order-2 flex"
            >
              <div class="request-dashboard-actions q-ml-auto q-mt-md">
                <q-btn
                  no-caps
                  unelevated
                  :color="showCreatePickList ? 'accent' : 'positive'"
                  :label="`(${selectedRequestItems.length}) ${showCreatePickList ? 'Create Pick List' : 'Add To Pick List'}`"
                  class="btn-no-wrap text-body1 q-mr-xs full-height"
                  :disabled="selectedRequestItems.length == 0"
                  :loading="appActionIsLoadingData"
                  @click="showCreatePickList ? createPickListJob() : updatePickListJob()"
                />
                <q-btn
                  no-caps
                  outline
                  label="Cancel"
                  class="btn-no-wrap text-body1 q-ml-xs full-height"
                  @click="resetPickListForm(); requestTableComponent.resetTablePagination(); loadRequestJobs();"
                />
              </div>
            </div>
            <MobileActionBar
              v-else-if="(showCreatePickList || showAddPickList) && currentScreenSize == 'xs'"
              :button-one-color="showCreatePickList ? 'accent' : 'positive'"
              :button-one-label="`(${selectedRequestItems.length}) ${showCreatePickList ? 'Create Pick List' : 'Add To Pick List'}`"
              :button-one-outline="false"
              :button-one-loading="appActionIsLoadingData"
              :button-one-disabled="selectedRequestItems.length == 0"
              @button-one-click="showCreatePickList ? createPickListJob() : updatePickListJob()"
              :button-two-color="'black'"
              :button-two-label="'Cancel'"
              :button-two-outline="true"
              @button-two-click="resetPickListForm(); requestTableComponent.resetTablePagination(); loadRequestJobs();"
            />
          </template>

          <template #table-td="{ colName, value }">
            <span
              v-if="colName == 'request_type'"
              class="text-nowrap"
              :class="value == '' ? 'text-highlight outline' : ''"
            >
              {{ value }}
            </span>
            <span
              v-else-if="colName == 'status'"
              class="outline text-nowrap"
              :class="value == 'Completed' ? 'text-highlight' : value == 'InProgress' ? 'text-highlight-warning' : null"
            >
              {{ value }}
            </span>
            <span
              v-else-if="colName == 'media_type'"
              class="outline text-nowrap"
              :class="'text-highlight'"
            >
              {{ value }}
            </span>
            <span v-else-if="colName == 'create_dt'">
              {{ formatDateTime(value).date }}
            </span>
          </template>
        </EssentialTable>
      </div>
    </div>

    <!-- Request Creation Modal -->
    <RequestCreateEditModal
      v-if="showCreateRequestByType"
      :type="showCreateRequestByType"
      :request-data="requestJob"
      @change-display="requestDisplayType = $event"
      @hide="showCreateRequestByType = null"
    />

    <!-- Create/Add To Picklist Modal -->
    <PopupModal
      v-if="showPickListModal"
      ref="picklistModalComponent"
      :show-actions="false"
      @reset="showPickListModal = null"
      aria-label="picklistJobModal"
    >
      <template #header-content="{ hideModal }">
        <q-card-section class="row items-center justify-between q-pb-none">
          <h2 class="text-h6">
            {{ showPickListModal == 'Create' ? 'Filter Requests By Building' : 'Filter Requests & Select Pick List' }}
          </h2>

          <q-btn
            icon="close"
            flat
            round
            dense
            aria-label="Close"
            @click="filterRequestsByBuilding = null; addToPickListJob = null; hideModal();"
          />
        </q-card-section>
      </template>

      <template #main-content>
        <q-card-section class="column no-wrap items-center">
          <div class="form-group">
            <label class="form-group-label">
              Building
            </label>
            <SelectInput
              v-model="filterRequestsByBuilding"
              :options="buildings"
              option-type="buildings"
              option-value="id"
              option-label="name"
              :placeholder="'Select Building'"
              aria-label="buildingSelect"
            />
          </div>

          <div
            v-if="showPickListModal == 'Add'"
            class="form-group q-mt-md"
          >
            <label class="form-group-label">
              Add To Pick List
            </label>
            <SelectInput
              v-model="addToPickListJob"
              :options="picklists"
              option-type="picklists"
              :option-query="{status: [
                'Created',
                'Paused'
              ]}"
              option-value="id"
              option-label="id"
              :force-option-type-reload="true"
              :placeholder="'Select Pick List Job'"
              aria-label="picklistJobSelect"
            >
              <template #option="{ itemProps, opt, selected, toggleOption }">
                <q-item v-bind="itemProps">
                  <q-item-section>
                    <q-item-label class="text-body1">
                      <span>Job #: {{ opt.id }}</span>
                      <span class="text-secondary"> - {{ opt.request_count }} Items ({{ formatDateTime(opt.create_dt).date }})</span>
                    </q-item-label>
                  </q-item-section>
                  <q-item-section side>
                    <q-checkbox
                      :model-value="selected"
                      @update:model-value="toggleOption(opt)"
                    />
                  </q-item-section>
                </q-item>
              </template>
            </SelectInput>
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
            class="text-body1 full-width text-nowrap"
            :disabled="showPickListModal == 'Create' ? !filterRequestsByBuilding : (!filterRequestsByBuilding || !addToPickListJob)"
            :loading="appActionIsLoadingData"
            @click="loadRequestJobsByBuilding()"
          />

          <q-space class="q-mx-xs" />

          <q-btn
            outline
            no-caps
            label="Cancel"
            class="text-body1 full-width"
            @click="filterRequestsByBuilding = null; addToPickListJob = null; hideModal();"
          />
        </q-card-section>
      </template>
    </PopupModal>
  </div>
</template>

<script setup>
import { onBeforeMount, ref, reactive, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useGlobalStore } from '@/stores/global-store'
import { useUserStore } from '@/stores/user-store'
import { useOptionStore } from '@/stores/option-store'
import { useRequestStore } from '@/stores/request-store'
import { usePicklistStore } from '@/stores/picklist-store'
import { storeToRefs } from 'pinia'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import EssentialTable from '@/components/EssentialTable.vue'
import ToggleButtonInput from '@/components/ToggleButtonInput.vue'
import MobileActionBar from '@/components/MobileActionBar.vue'
import RequestCreateEditModal from '@/components/Request/RequestCreateEditModal.vue'
import PopupModal from '@/components/PopupModal.vue'
import SelectInput from '@/components/SelectInput.vue'

const router = useRouter()

// Composables
const { currentScreenSize } = useCurrentScreenSize()
const { checkUserPermission } = usePermissionHandler()

// Store Data
const { appIsLoadingData, appActionIsLoadingData } = storeToRefs(useGlobalStore())
const {
  buildings,
  picklists,
  mediaTypes,
  requestsPriorities,
  requestsLocations
} = storeToRefs(useOptionStore())
const {
  getRequestJobList,
  getRequestJob,
  getRequestBatchJobList,
  getRequestBatchJob
} = useRequestStore()
const {
  requestJobList,
  requestJobListTotal,
  requestJob
} = storeToRefs(useRequestStore())
const { postPicklistJob, patchPicklistJobItem } = usePicklistStore()
const { picklistJob } = storeToRefs(usePicklistStore())
const { userData } = storeToRefs(useUserStore())

// Local Data
const requestJobMenuState = ref(false)
const requestTableComponent = ref(null)
const requestTableVisibleColumns = ref([
  'id',
  'request_type',
  'barcode_value',
  'external_request_id',
  'building_name',
  'requestor_name',
  'status',
  'priority',
  'media_type',
  'location',
  'delivery_location',
  'create_dt'
])
const requestTableColumns = ref([
  {
    name: 'id',
    field: 'id',
    label: 'Request ID #',
    align: 'left',
    sortable: true
  },
  {
    name: 'request_type',
    field: row => row.request_type?.type,
    label: 'Request Type',
    align: 'left',
    sortable: true
  },
  {
    name: 'barcode_value',
    field: row => row.item ? renderItemBarcodeDisplay(row.item) : renderItemBarcodeDisplay(row.non_tray_item),
    label: 'Barcode',
    align: 'left',
    sortable: true
  },
  {
    name: 'external_request_id',
    field: 'external_request_id',
    label: 'External Request ID',
    align: 'left',
    sortable: true
  },
  {
    name: 'building_name',
    field: row => row.building?.name,
    label: 'Building',
    align: 'left',
    sortable: true
  },
  {
    name: 'requestor_name',
    field: 'requestor_name',
    label: 'Requestor Name',
    align: 'left',
    sortable: true
  },
  {
    name: 'status',
    field: 'status',
    label: 'Request Status',
    align: 'left',
    sortable: true
  },
  {
    name: 'priority',
    field: row => row.priority?.value,
    label: 'Priority',
    align: 'left',
    sortable: true
  },
  {
    name: 'media_type',
    field: row => row.item ? row.item?.media_type?.name : row.non_tray_item?.media_type?.name,
    label: 'Media Type',
    align: 'left',
    sortable: true
  },
  {
    name: 'location',
    field: row => row.item ? getItemLocation(row.item.tray) : getItemLocation(row.non_tray_item),
    label: 'Item Location',
    align: 'left',
    sortable: true
  },
  {
    name: 'delivery_location',
    field: row => row.delivery_location?.name,
    label: 'Delivery Location',
    align: 'left',
    sortable: true
  },
  {
    name: 'create_dt',
    field: 'create_dt',
    label: 'Date Created',
    align: 'left',
    sortable: true
  }
])
const requestTableFilters =  reactive([
  {
    field: row => row.request_type?.type,
    label: 'Request Type',
    apiField: 'request_type',
    options: [
      {
        text: 'General Delivery',
        value: false
      },
      {
        text: 'ILL (InterLibrary Loan)',
        value: false
      },
      {
        text: 'Scan and Deliver',
        value: false
      }
    ]
  },
  {
    field: row => row.building?.name,
    label: 'Building',
    apiField: 'building_name',
    options: buildings.value.map(b => {
      return {
        text: b.name,
        value: false
      }
    })
  },
  {
    field: 'status',
    label: 'Request Status',
    options: [
      {
        text: 'New',
        value: false
      },
      {
        text: 'InProgress',
        value: false
      }
    ]
  },
  {
    field: row => row.priority?.value,
    label: 'Priority',
    apiField: 'priority',
    options: requestsPriorities.value.map(p => {
      return {
        text: p.value,
        value: false
      }
    })
  },
  {
    field: row => row.item ? row.item?.media_type?.name : row.non_tray_item?.media_type?.name,
    label: 'Media Type',
    apiField: 'media_type',
    options: mediaTypes.value.map(m => {
      return {
        text: m.name,
        value: false
      }
    })
  },
  {
    field: row => row.delivery_location?.name,
    label: 'Delivery Location',
    apiField: 'delivery_location',
    options: requestsLocations.value.map(dl => {
      return {
        text: dl.name,
        value: false
      }
    })
  }
])
const requestBatchTableVisibleColumns = ref([
  'file_type',
  'request_count',
  'status',
  'user_id',
  'create_dt'
])
const requestBatchTableColumns = ref([
  {
    name: 'file_type',
    field: 'file_type',
    label: 'Import Source',
    align: 'left',
    sortable: true
  },
  {
    name: 'request_count',
    field: row => row.requests?.length,
    label: '# of Requests',
    align: 'left',
    sortable: true
  },
  {
    name: 'status',
    field: 'status',
    label: 'Status',
    align: 'left',
    sortable: true
  },
  {
    name: 'user_id',
    field: row => row.user?.name,
    label: 'Uploaded By',
    align: 'left',
    sortable: true
  },
  {
    name: 'create_dt',
    field: 'create_dt',
    label: 'Date Imported',
    align: 'left',
    sortable: true
  }
])
const requestBatchTableFilters =  ref([
  {
    field: 'status',
    label: 'Status',
    options: [
      {
        text: 'New',
        value: false
      },
      {
        text: 'Processing',
        value: false
      },
      {
        text: 'Failed',
        value: false
      },
      {
        text: 'Cancelled',
        value: false
      },
      {
        text: 'Completed',
        value: false
      }
    ]
  }
])
const requestDisplayType = ref('request_view')
const picklistModalComponent = ref(null)
const showCreatePickList = ref(false)
const showAddPickList = ref(false)
const showPickListModal = ref(null)
const addToPickListJob = ref(null)
const selectedRequestItems = ref([])
const showCreateRequestByType = ref(null)
const filterRequestsByBuilding = ref(null)

// Logic
const handleAlert = inject('handle-alert')
const formatDateTime = inject('format-date-time')
const getItemLocation = inject('get-item-location')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')

onBeforeMount(() => {
  loadRequestJobs()

  if (currentScreenSize.value == 'xs') {
    requestTableVisibleColumns.value = [
      'id',
      'request_type',
      'barcode_value',
      'requestor_name'
    ]
    requestBatchTableVisibleColumns.value = [
      'file_type',
      'request_count',
      'status',
      'create_dt'
    ]
  }
})

const clearTableSelection = () => {
  requestTableComponent.value.clearSelectedData()
  selectedRequestItems.value = []
}
const resetPickListForm = () => {
  showCreatePickList.value = false
  showAddPickList.value = false
  showPickListModal.value = null
  filterRequestsByBuilding.value = null
  addToPickListJob.value = null
  clearTableSelection()
}

const loadRequestJobs = async (qParams) => {
  try {
    appIsLoadingData.value = true
    if (requestDisplayType.value == 'request_view') {
      await getRequestJobList({
        ...qParams,
        queue: true,
        unassociated_pick_list: showCreatePickList.value || showAddPickList.value ? true : false
      })
    } else {
      await getRequestBatchJobList({ ...qParams })
    }
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
const loadRequestJobsByBuilding = async () => {
  try {
    appActionIsLoadingData.value = true
    // this function only gets called during the creation/add picklist workflow
    // change table view back to request view and clear out any pagination settings
    requestDisplayType.value = 'request_view'
    requestTableComponent.value.resetTablePagination()
    await getRequestJobList({
      building_id: filterRequestsByBuilding.value,
      unassociated_pick_list: true
    })

    // display next step in picklist creation
    if (showPickListModal.value == 'Create') {
      showCreatePickList.value = true
    } else {
      showAddPickList.value = true
    }
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
    picklistModalComponent.value.hideModal()
  }
}
const loadRequestJob = async (id) => {
  try {
    appIsLoadingData.value = true

    if (requestDisplayType.value == 'batch_view') {
      await getRequestBatchJob(id)
      router.push({
        name: 'request-batch',
        params: {
          jobId: id
        }
      })
    } else {
      await getRequestJob(id)
      router.push({
        name: 'request-details',
        params: {
          jobId: id
        }
      })
    }
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
const createPickListJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      request_ids: selectedRequestItems.value.map(item => item.id),
      created_by_id: userData.value.user_id
    }
    await postPicklistJob(payload)

    // display an alert with the created picklist job id so you can click that and link directly to the new job if needed
    handleAlert({
      type: 'success',
      text: `Successfully created Pick List #: <a href='/picklist/${picklistJob.value.id}' tabindex='0'>${picklistJob.value.id}</a>`,
      autoClose: false
    })
    requestTableComponent.value.resetTablePagination()
    loadRequestJobs()
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
    resetPickListForm()
  }
}
const updatePickListJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: addToPickListJob.value,
      request_ids: selectedRequestItems.value.map(item => item.id)
    }
    await patchPicklistJobItem(payload)

    // display an alert with the updated picklist job id so you can click that and link directly to the job if needed
    handleAlert({
      type: 'success',
      text: `Successfully added items to Pick List #: <a href='/picklist/${picklistJob.value.id}' tabindex='0'>${picklistJob.value.id}</a>`,
      autoClose: false
    })
    requestTableComponent.value.resetTablePagination()
    loadRequestJobs()
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
    resetPickListForm()
  }
}
</script>

<style lang="scss" scoped>
</style>
