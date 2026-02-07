<template>
  <div class="request-batch-details">
    <JobPageHeader
      title="Batch Request Job"
      :job-id="requestBatchJob.id"
      :status="requestBatchJob.status"
      :status-color="getStatusColor(requestBatchJob.status)"
      :subtitle="headerSubtitle"
      :menu-options="batchMenuOptions"
    />

    <div class="">
      <q-card
        flat
        bordered
        class="table-card"
      >
        <EssentialTable
          ref="requestTableComponent"
          :table-columns="requestTableColumns"
          :table-visible-columns="requestTableVisibleColumns"
          :table-data="requestItems"
          :enable-table-reorder="false"
          :hide-table-rearrange="true"
          :enable-selection="showCreatePickList || showAddPickList"
          :heading-row-class="'justify-end q-mb-lg q-px-md q-pt-md'"
          :heading-filter-class="currentScreenSize == 'xs' ? 'col-xs-6 q-mr-auto' : 'q-ml-auto'"
          @selected-table-row="loadRequestJob($event.id)"
          @selected-data="selectedRequestItems = $event"
        >
          <template #heading-row>
            <div
              class="col-xs-7 col-sm-5 q-mb-md-sm q-mr-auto"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? '' : 'self-center'"
            >
              <h2 class="text-h6 text-bold q-my-none">
                Items in List:
              </h2>
            </div>

            <div
              class="col-xs-grow col-sm-7 col-md-auto flex"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? 'justify-end q-mb-md' : 'order-1'"
            >
              <q-btn
                no-caps
                rounded
                unelevated
                icon-right="arrow_drop_down"
                color="accent"
                label="Create"
                class="text-body1 btn-modern q-ml-xs-none q-ml-sm-sm"
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

          <template #table-th="{ col }">
            <q-tr class="filter-row">
              <q-th class="filter-cell">
                <!-- ID filter -->
                <q-input
                  v-if="col.name === 'id'"
                  v-model="columnFilters.id"
                  dense
                  outlined
                  clearable
                  placeholder="Search..."
                  class="column-filter-input"
                  @click.stop
                >
                  <template #prepend>
                    <q-icon
                      name="search"
                      size="16px"
                      color="grey-6"
                    />
                  </template>
                </q-input>

                <!-- Request Type filter -->
                <q-select
                  v-else-if="col.name === 'request_type'"
                  v-model="columnFilters.request_type"
                  dense
                  outlined
                  clearable
                  multiple
                  emit-value
                  map-options
                  :options="requestTypeOptionsFromData"
                  placeholder="All"
                  class="column-filter-input"
                  @click.stop
                />

                <!-- Barcode filter -->
                <q-input
                  v-else-if="col.name === 'barcode'"
                  v-model="columnFilters.barcode"
                  dense
                  outlined
                  clearable
                  placeholder="Search..."
                  class="column-filter-input"
                  @click.stop
                >
                  <template #prepend>
                    <q-icon
                      name="search"
                      size="16px"
                      color="grey-6"
                    />
                  </template>
                </q-input>

                <!-- External Request ID filter -->
                <q-input
                  v-else-if="col.name === 'external_request_id'"
                  v-model="columnFilters.external_request_id"
                  dense
                  outlined
                  clearable
                  placeholder="Search..."
                  class="column-filter-input"
                  @click.stop
                >
                  <template #prepend>
                    <q-icon
                      name="search"
                      size="16px"
                      color="grey-6"
                    />
                  </template>
                </q-input>

                <!-- Requestor Name filter -->
                <q-input
                  v-else-if="col.name === 'requestor_name'"
                  v-model="columnFilters.requestor_name"
                  dense
                  outlined
                  clearable
                  placeholder="Search..."
                  class="column-filter-input"
                  @click.stop
                >
                  <template #prepend>
                    <q-icon
                      name="search"
                      size="16px"
                      color="grey-6"
                    />
                  </template>
                </q-input>

                <!-- Status filter -->
                <q-select
                  v-else-if="col.name === 'status'"
                  v-model="columnFilters.status"
                  dense
                  outlined
                  clearable
                  multiple
                  emit-value
                  map-options
                  :options="statusOptionsFromData"
                  placeholder="All"
                  class="column-filter-input"
                  @click.stop
                />

                <!-- Priority filter -->
                <q-select
                  v-else-if="col.name === 'priority'"
                  v-model="columnFilters.priority"
                  dense
                  outlined
                  clearable
                  multiple
                  emit-value
                  map-options
                  :options="priorityOptionsFromData"
                  placeholder="All"
                  class="column-filter-input"
                  @click.stop
                />

                <!-- Media Type filter -->
                <q-select
                  v-else-if="col.name === 'media_type'"
                  v-model="columnFilters.media_type"
                  dense
                  outlined
                  clearable
                  multiple
                  emit-value
                  map-options
                  :options="mediaTypeOptionsFromData"
                  placeholder="All"
                  class="column-filter-input"
                  @click.stop
                />

                <!-- Item Location filter -->
                <q-input
                  v-else-if="col.name === 'item_location'"
                  v-model="columnFilters.location"
                  dense
                  outlined
                  clearable
                  placeholder="Search..."
                  class="column-filter-input"
                  @click.stop
                >
                  <template #prepend>
                    <q-icon
                      name="search"
                      size="16px"
                      color="grey-6"
                    />
                  </template>
                </q-input>

                <!-- Delivery Location filter -->
                <q-select
                  v-else-if="col.name === 'delivery_location'"
                  v-model="columnFilters.delivery_location"
                  dense
                  outlined
                  clearable
                  multiple
                  emit-value
                  map-options
                  :options="deliveryLocationOptionsFromData"
                  placeholder="All"
                  class="column-filter-input"
                  @click.stop
                />
              </q-th>
            </q-tr>
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
      </q-card>
    </div>
  </div>

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
import { Notify, useQuasar } from 'quasar'
import { api } from '@/boot/axios.js'
import inventoryServiceApi from '@/http/InventoryService.js'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { useRequestStore } from '@/stores/request-store'
import { usePicklistStore } from '@/stores/picklist-store'
import { storeToRefs } from 'pinia'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import JobPageHeader from '@/components/Job/JobPageHeader.vue'
import EssentialTable from '@/components/EssentialTable.vue'
import MobileActionBar from '@/components/MobileActionBar.vue'
import PopupModal from '@/components/PopupModal.vue'
import SelectInput from '@/components/SelectInput.vue'

const router = useRouter()
const $q = useQuasar()

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

// Column filter state
const columnFilters = ref({
  id: null,
  request_type: [],
  barcode: null,
  external_request_id: null,
  requestor_name: null,
  status: [],
  priority: [],
  media_type: [],
  location: null,
  delivery_location: []
})

// Dynamic filter options from current data
const requestTypeOptionsFromData = computed(() => {
  const types = new Set()
  requestBatchJob.value.requests?.forEach(row => {
    if (row.request_type?.type) {
      types.add(row.request_type.type)
    }
  })
  return Array.from(types).sort().map(t => ({
    label: t,
    value: t
  }))
})

const statusOptionsFromData = computed(() => {
  const statuses = new Set()
  requestBatchJob.value.requests?.forEach(row => {
    if (row.status) {
      statuses.add(row.status)
    }
  })
  return Array.from(statuses).sort().map(s => ({
    label: s,
    value: s
  }))
})

const priorityOptionsFromData = computed(() => {
  const priorities = new Set()
  requestBatchJob.value.requests?.forEach(row => {
    if (row.priority?.value) {
      priorities.add(row.priority.value)
    }
  })
  return Array.from(priorities).sort().map(p => ({
    label: p,
    value: p
  }))
})

const mediaTypeOptionsFromData = computed(() => {
  const types = new Set()
  requestBatchJob.value.requests?.forEach(row => {
    const type = row.item?.media_type?.name || row.non_tray_item?.media_type?.name
    if (type) {
      types.add(type)
    }
  })
  return Array.from(types).sort().map(t => ({
    label: t,
    value: t
  }))
})

const deliveryLocationOptionsFromData = computed(() => {
  const locations = new Set()
  requestBatchJob.value.requests?.forEach(row => {
    if (row.delivery_location?.name) {
      locations.add(row.delivery_location.name)
    }
  })
  return Array.from(locations).sort().map(l => ({
    label: l,
    value: l
  }))
})

const requestItems = computed(() => {
  let requests = requestBatchJob.value.requests || []

  // Filter out any requests that already belong to a picklist
  if (showCreatePickList.value || showAddPickList.value) {
    requests = requests.filter(r => !r.pick_list_id)
  }

  // Apply Column Filters
  const f = columnFilters.value
  if (f.id) {
    requests = requests.filter(r => r.id.toString().includes(f.id))
  }
  if (f.request_type && f.request_type.length) {
    requests = requests.filter(r => f.request_type.includes(r.request_type?.type))
  }
  if (f.barcode) {
    requests = requests.filter(r => {
      const b = r.item ? renderItemBarcodeDisplay(r.item) : renderItemBarcodeDisplay(r.non_tray_item)
      return b.toLowerCase().includes(f.barcode.toLowerCase())
    })
  }
  if (f.external_request_id) {
    requests = requests.filter(r => r.external_request_id?.toLowerCase().includes(f.external_request_id.toLowerCase()))
  }
  if (f.requestor_name) {
    requests = requests.filter(r => r.requestor_name?.toLowerCase().includes(f.requestor_name.toLowerCase()))
  }
  if (f.status && f.status.length) {
    requests = requests.filter(r => f.status.includes(r.status))
  }
  if (f.priority && f.priority.length) {
    requests = requests.filter(r => f.priority.includes(r.priority?.value))
  }
  if (f.media_type && f.media_type.length) {
    requests = requests.filter(r => {
      const type = r.item?.media_type?.name || r.non_tray_item?.media_type?.name
      return f.media_type.includes(type)
    })
  }
  if (f.location) {
    requests = requests.filter(r => {
      const loc = r.item ? getItemLocation(r.item.tray) : getItemLocation(r.non_tray_item)
      return loc.toLowerCase().includes(f.location.toLowerCase())
    })
  }
  if (f.delivery_location && f.delivery_location.length) {
    requests = requests.filter(r => f.delivery_location.includes(r.delivery_location?.name))
  }

  return requests
})
const showPickListModal = ref(null)
const addToPickListJob = ref(null)
const showCreatePickList = ref(false)
const showAddPickList = ref(false)
const selectedRequestItems = ref([])

// Logic

const formatDateTime = inject('format-date-time')
const getItemLocation = inject('get-item-location')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')

const headerSubtitle = computed(() => {
  const source = requestBatchJob.value.file_type || '-'
  const fileName = requestBatchJob.value.file_name || '-'
  const items = `${requestBatchJob.value.requests?.length || 0} items`
  const user = requestBatchJob.value.user ? `${requestBatchJob.value.user.first_name} ${requestBatchJob.value.user.last_name}` : 'Unassigned'
  const date = requestBatchJob.value.create_dt ? formatDateTime(requestBatchJob.value.create_dt).date : '-'
  return `${source} • ${fileName} • ${items} • ${user} • ${date}`
})

const getStatusColor = (status) => {
  const colors = {
    New: 'info',
    Processing: 'warning',
    Completed: 'positive',
    Failed: 'negative',
    Cancelled: 'grey'
  }
  return colors[status] || 'grey'
}

const canDeleteBatch = computed(() => {
  if (!checkUserPermission('delete_requests')) {
    return false
  }
  const deletableStatuses = [
    'New',
    'Processing',
    'Uploaded'
  ]
  return deletableStatuses.includes(requestBatchJob.value.status)
})

const batchMenuOptions = computed(() => {
  if (!canDeleteBatch.value) {
    return []
  }
  return [
    {
      label: 'Delete Batch',
      icon: 'delete',
      color: 'negative',
      action: () => confirmDeleteBatch()
    }
  ]
})

const confirmDeleteBatch = () => {
  const requestCount = requestBatchJob.value.requests?.length || 0
  $q.dialog({
    title: 'Delete Batch Upload',
    message: `Are you sure you want to delete this batch upload? This will also delete ${requestCount} associated request(s) and return all items to "In" status. This action cannot be undone.`,
    persistent: true,
    ok: {
      label: 'Delete',
      color: 'negative',
      flat: true
    },
    cancel: {
      label: 'Cancel',
      flat: true
    }
  }).onOk(async () => {
    await deleteBatch()
  })
}

const deleteBatch = async () => {
  try {
    appActionIsLoadingData.value = true
    await api.delete(`${inventoryServiceApi.batchUpload}${requestBatchJob.value.id}`)
    Notify.create({
      type: 'positive',
      message: 'Batch upload deleted successfully'
    })
    router.push({ name: 'request' })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to delete batch upload'
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}

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
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to load request job'
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
    Notify.create({
      type: 'positive',
      message: `Successfully created Pick List #: <a href='/picklist/${picklistJob.value.id}' style='color: white; text-decoration: underline;'>${picklistJob.value.id}</a>`,
      html: true,
      timeout: 0,
      actions: [
        {
          icon: 'close',
          color: 'white'
        }
      ]
    })
    await getRequestBatchJob(requestBatchJob.value.id)
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to create picklist'
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
    Notify.create({
      type: 'positive',
      message: `Successfully added items to Pick List #: <a href='/picklist/${picklistJob.value.id}' style='color: white; text-decoration: underline;'>${picklistJob.value.id}</a>`,
      html: true,
      timeout: 0,
      actions: [
        {
          icon: 'close',
          color: 'white'
        }
      ]
    })
    await getRequestBatchJob(requestBatchJob.value.id)
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to update picklist'
    })
  } finally {
    appActionIsLoadingData.value = false
    resetPickListForm()
  }
}
</script>

<style lang="scss" scoped>
.request-batch-details {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
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

:deep(.filter-row) {
  .filter-cell {
    min-width: 120px;
    padding: 4px 8px;
  }

  .column-filter-input {
    min-width: 100px;
    width: 100%;
  }
}

:deep(.q-table th) {
  min-width: 100px;
  white-space: nowrap;
}
</style>
