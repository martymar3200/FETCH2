<template>
  <InfoDisplayLayout class="request-job">
    <template #number-box-content>
      <h1
        id="requestJobId"
        class="info-display-details-label text-h4 q-mb-xs"
      >
        Request List:
      </h1>
      <p class="info-display-number-box text-h4">
        {{ requestBatchJob.id }}
      </p>
    </template>

    <template #details-content>
      <div class="col-xs-6 col-sm-6 col-md-grow">
        <div class="info-display-details q-mb-xs-md q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-lg">
          <label
            class="info-display-details-label-2 text-h6"
          >
            Import Source:
          </label>
          <p class="text-body1">
            {{ requestBatchJob.file_type }}
          </p>
        </div>
      </div>
      <div class="col-xs-6 col-sm-6 col-md-grow">
        <div class="info-display-details q-mb-xs-md q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-lg">
          <label
            class="info-display-details-label-2 text-h6"
          >
            Uploaded By:
          </label>
          <p class="text-body1">
            {{ `${requestBatchJob.user.first_name} ${requestBatchJob.user.last_name}` }}
          </p>
        </div>
      </div>
      <div class="col-xs-6 col-sm-6 col-md-grow">
        <div class="info-display-details q-mb-xs-md q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-lg">
          <label
            class="info-display-details-label-2 text-h6"
          >
            # of Requests
          </label>
          <p class="text-body1">
            {{ requestBatchJob.requests?.length }}
          </p>
        </div>
      </div>
      <div class="col-xs-6 col-sm-6 col-md-grow">
        <div class="info-display-details q-mb-xs-md q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-lg">
          <label
            class="info-display-details-label-2 text-h6"
          >
            Date Imported:
          </label>
          <p class="text-body1">
            {{ formatDateTime(requestBatchJob.create_dt).date }}
          </p>
        </div>
      </div>
    </template>

    <template #table-content>
      <EssentialTable
        ref="requestTableComponent"
        :table-columns="requestTableColumns"
        :table-visible-columns="requestTableVisibleColumns"
        :filter-options="requestTableFilters"
        :table-data="requestItems"
        :enable-table-reorder="false"
        :enable-selection="showCreatePickList || showAddPickList"
        :heading-row-class="'justify-end q-mb-lg q-px-xs-sm q-px-sm-md'"
        :heading-filter-class="currentScreenSize == 'xs' ? 'col-xs-6 q-mr-auto' : 'q-ml-auto'"
        @selected-table-row="loadRequestJob($event.id)"
        @selected-data="selectedRequestItems = $event"
      >
        <template #heading-row>
          <div
            class="col-xs-7 col-sm-5 q-mb-md-sm q-mr-auto"
            :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? '' : 'self-center'"
          >
            <h2 class="text-h4 text-bold">
              Items in List:
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
                <q-list class="text-no-wrap">
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
                    @click="showCreatePickList = true"
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
                </q-list>
              </q-menu>
            </q-btn>
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
                @click="resetPickListForm()"
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
            @button-two-click="resetPickListForm()"
          />
        </template>

        <template #table-td="{ colName, value }">
          <span
            v-if="colName == 'request_type'"
            class="text-nowrap"
            :class="value ? 'text-highlight outline' : null"
          >
            {{ value }}
          </span>
          <span
            v-else-if="colName == 'status'"
            class="outline text-nowrap"
            :class="value == 'Completed' || value == 'New' ? 'text-highlight' : value == 'Paused' || value == 'Running' ? 'text-highlight-warning' : null "
          >
            {{ value }}
          </span>
          <span
            v-else-if="colName == 'media_type'"
            class="text-nowrap"
            :class="value ? 'text-highlight outline' : null"
          >
            {{ value }}
          </span>
          <span v-else-if="colName == 'create_dt'">
            {{ formatDateTime(value).date }}
          </span>
        </template>
      </EssentialTable>
    </template>
  </InfoDisplayLayout>

  <!-- Add To Picklist Modal -->
  <PopupModal
    v-if="showPickListModal"
    :show-actions="false"
    @reset="showPickListModal = null"
    aria-label="picklistJobModal"
  >
    <template #header-content="{ hideModal }">
      <q-card-section class="row items-center justify-between q-pb-none">
        <h2 class="text-h6">
          {{ 'Select Pick List Job' }}
        </h2>

        <q-btn
          icon="close"
          flat
          round
          dense
          aria-label="Close"
          @click="addToPickListJob = null; hideModal();"
        />
      </q-card-section>
    </template>

    <template #main-content>
      <q-card-section class="column no-wrap items-center">
        <div
          class="form-group"
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
          class="text-body1 full-width text-nowrap"
          :disabled="!addToPickListJob"
          :loading="appActionIsLoadingData"
          @click="showAddPickList = true; hideModal();"
        />

        <q-space class="q-mx-xs" />

        <q-btn
          outline
          no-caps
          label="Cancel"
          class="text-body1 full-width"
          @click="addToPickListJob = null; hideModal();"
        />
      </q-card-section>
    </template>
  </PopupModal>
</template>

<script setup>
import { onBeforeMount, ref, inject, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { useRequestStore } from '@/stores/request-store'
import { usePicklistStore } from '@/stores/picklist-store'
import { storeToRefs } from 'pinia'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import InfoDisplayLayout from '@/components/InfoDisplayLayout.vue'
import EssentialTable from '@/components/EssentialTable.vue'
import MobileActionBar from '@/components/MobileActionBar.vue'
import PopupModal from '@/components/PopupModal.vue'
import SelectInput from '@/components/SelectInput.vue'

const router = useRouter()

// Composables
const { currentScreenSize } = useCurrentScreenSize()
const { checkUserPermission } = usePermissionHandler()

// Store Data
const { appActionIsLoadingData, appIsLoadingData } = storeToRefs(useGlobalStore())
const { picklists } = storeToRefs(useOptionStore())
const { requestBatchJob } = storeToRefs(useRequestStore())
const { getRequestBatchJob, getRequestJob } = useRequestStore()
const { postPicklistJob, patchPicklistJobItem } = usePicklistStore()
const { picklistJob } = storeToRefs(usePicklistStore())

// Local Data
const requestJobMenuState = ref(false)
const requestTableComponent = ref(null)
const requestTableVisibleColumns = ref([
  'id',
  'request_type',
  'barcode',
  'external_request_id',
  'requestor_name',
  'status',
  'priority',
  'media_type',
  'item_location',
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
    name: 'barcode',
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
    name: 'item_location',
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
const requestTableFilters = computed(() => {
  let tablesFilters = []
  if (requestItems.value && requestItems.value.length > 0) {
    tablesFilters = [
      {
        field: row => row.request_type?.type,
        label: 'Request Type',
        // render options based on the passed in table data
        // loop through all containers and return customized data set for table filtering and remove the duplicates
        options: getUniqueListByKey(requestItems.value.flatMap(tableEntry => {
          // these fields are optional so we need to account for blank entries using flatMap
          if (tableEntry.request_type?.type) {
            return {
              text: tableEntry.request_type.type,
              value: false
            }
          } else {
            return []
          }
        }), 'text')
      },
      {
        field: 'status',
        label: 'Request Status',
        options: getUniqueListByKey(requestItems.value.map(tableEntry => {
          return {
            text: tableEntry.status,
            value: false
          }
        }), 'text')
      },
      {
        field: row => row.priority?.value,
        label: 'Priority',
        options: getUniqueListByKey(requestItems.value.flatMap(tableEntry => {
          // these fields are optional so we need to account for blank entries using flatMap
          if (tableEntry.priority?.value) {
            return {
              text: tableEntry.priority.value,
              value: false
            }
          } else {
            return []
          }
        }), 'text')
      },
      {
        field: row => row.item ? row.item?.media_type?.name : row.non_tray_item?.media_type?.name,
        label: 'Media Type',
        options: getUniqueListByKey(requestItems.value.map(tableEntry => {
          return {
            text: tableEntry.item ? tableEntry.item?.media_type?.name : tableEntry.non_tray_item?.media_type?.name,
            value: false
          }
        }), 'text')
      }
    ]
  }
  return tablesFilters
})
const requestItems = computed(() => {
  let requests = requestBatchJob.value.requests
  if (showCreatePickList.value || showAddPickList.value) {
    // filter out any requests that already belong to a picklist
    requests = requests.filter(r => !r.pick_list_id)
  }
  return requests
})
const showPickListModal = ref(null)
const addToPickListJob = ref(null)
const showCreatePickList = ref(false)
const showAddPickList = ref(false)
const selectedRequestItems = ref([])

// Logic
const handleAlert = inject('handle-alert')
const formatDateTime = inject('format-date-time')
const getItemLocation = inject('get-item-location')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')
const getUniqueListByKey = inject('get-uniqure-list-by-key')

onBeforeMount(() => {
  if (currentScreenSize.value == 'xs') {
    requestTableVisibleColumns.value = [
      'id',
      'request_type',
      'barcode',
      'requestor_name'
    ]
  }
})

const resetPickListForm = () => {
  showCreatePickList.value = false
  showAddPickList.value = false
  showPickListModal.value = null
  addToPickListJob.value = null
  clearTableSelection()
}

const clearTableSelection = () => {
  requestTableComponent.value.clearSelectedData()
  selectedRequestItems.value = []
}

const loadRequestJob = async (id) => {
  try {
    appIsLoadingData.value = false
    await getRequestJob(id)
    router.push({
      name: 'request-details',
      params: {
        jobId: id
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
const createPickListJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      request_ids: selectedRequestItems.value.map(item => item.id)
    }
    await postPicklistJob(payload)

    // display an alert with the created picklist job id so you can click that and link directly to the new job if needed
    handleAlert({
      type: 'success',
      text: `Successfully created Pick List #: <a href='/picklist/${picklistJob.value.id}' tabindex='0'>${picklistJob.value.id}</a>`,
      autoClose: false
    })
    await getRequestBatchJob(requestBatchJob.value.id)
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
    await getRequestBatchJob(requestBatchJob.value.id)
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
