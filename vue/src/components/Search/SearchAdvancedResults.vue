<template>
  <div class="advanced-search-results">
    <div class="row q-mb-xs-xl q-mb-sm-none">
      <div class="col-grow q-mb-xs-md q-mb-sm-none">
        <EssentialTable
          :table-columns="searchResultsTableColumns"
          :table-visible-columns="searchResultsTableVisibleColumns"
          :table-data="searchResults"
          :enable-table-reorder="false"
          :heading-row-class="'q-mb-xs-md q-mb-md-lg'"
          :heading-rearrange-class="'q-ml-auto'"
          :enable-pagination="true"
          :pagination-total="searchResultsTotal"
          :pagination-loading="appIsLoadingData"
          @update-pagination="loadAdvancedSearch($event)"
          @selected-table-row="handleResultSelection($event)"
        >
          <template #heading-row>
            <div
              class="col-12 q-mb-sm"
            >
              <h1 class="text-h4 text-bold">
                Advanced Search
              </h1>
            </div>

            <div
              v-if="route.params.searchType == 'Item' || route.params.searchType == 'TrayItem'"
              class="col-xs-12 col-sm-auto col-md-auto q-mb-xs-md q-mb-sm-none"
            >
              <ToggleButtonInput
                v-model="toggleSearchTab"
                :options="[
                  {label: 'Non-Tray Items', value: 'nonTrayItem'},
                  {label: 'Tray Items', value: 'trayItem'}
                ]"
                @update:model-value="loadAdvancedSearch(advancedSearchHistory);"
                class="text-no-wrap"
              />
            </div>
            <div
              v-if="showDownloadReport"
              class="col-xs-12 col-sm-12 col-md-auto flex"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? 'justify-end q-mb-md' : 'order-1'"
            >
              <q-btn
                no-caps
                unelevated
                color="accent"
                label="Download Report"
                class="text-body1 q-ml-xs-none q-ml-sm-sm"
                :disabled="appIsOffline"
                @click="downloadAdvancedSearchReport()"
              />
            </div>
          </template>

          <template #table-td="{ colName, value }">
            <span
              v-if="colName == 'status'"
              class="outline text-nowrap"
              :class="value == 'Created' || value == 'Completed' ? 'text-highlight' : value == 'Paused' || value == 'Running' ? 'text-highlight-warning' : 'text-highlight-negative'"
            >
              {{ value }}
            </span>
            <span v-else-if="colName.includes('_dt')">
              {{ formatDateTime(value).date }}
            </span>
          </template>
        </EssentialTable>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onBeforeMount, ref, inject, watch } from 'vue'
import { useRoute, useRouter  } from 'vue-router'
import { useSearchStore } from '@/stores/search-store'
import { useGlobalStore } from '@/stores/global-store'
import { storeToRefs } from 'pinia'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import EssentialTable from '@/components/EssentialTable.vue'
import ToggleButtonInput from '@/components/ToggleButtonInput.vue'

const route = useRoute()
const router = useRouter()

// Composables
const { currentScreenSize } = useCurrentScreenSize()
const { appIsOffline } = storeToRefs(useGlobalStore())

// Store Data
const {
  searchResults,
  searchResultsTotal,
  advancedSearchHistory
} = storeToRefs(useSearchStore())
const { getAdvancedSearchResults, downloadAdvancedSearchResults } = useSearchStore()
const { appIsLoadingData } = storeToRefs(useGlobalStore())

// Local Data
const toggleSearchTab = ref(null)
const searchResultsTableVisibleColumns = ref([])
const searchResultsTableColumns = ref([])
const showDownloadReport = ref(false)

// Logic
const formatDateTime = inject('format-date-time')
const handleAlert = inject('handle-alert')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')

onBeforeMount(() => {
  loadAdvancedSearch(route.query)
  generateSearchTableFields()
})
watch(route, () => {
  generateSearchTableFields()
})

const generateSearchTableFields = () => {
  showDownloadReport.value = false
  // creates the search table fields needed based on the route searchType
  switch (route.params.searchType) {
    case 'Item':
    case 'TrayItem':
    // set the default tab for advance item search
      toggleSearchTab.value = route.params.searchType == 'TrayItem' ? 'trayItem' : 'nonTrayItem'
      searchResultsTableColumns.value = [
        {
          name: 'accession_dt',
          field: 'accession_dt',
          label: 'Accession Date',
          align: 'left',
          sortable: true,
          classes: row => row.withdrawn_barcode_id ? 'q-td--no-hover' : ''
        },
        {
          name: 'status',
          field: 'status',
          label: 'Status',
          align: 'left',
          sortable: true,
          classes: row => row.withdrawn_barcode_id ? 'q-td--no-hover' : ''
        },
        {
          name: 'owner',
          field: row => row.owner?.name,
          label: 'Owner',
          align: 'left',
          sortable: true,
          classes: row => row.withdrawn_barcode_id ? 'q-td--no-hover' : ''
        },
        {
          name: 'size_class',
          field: row => row.size_class?.name,
          label: 'Size Class',
          align: 'left',
          sortable: true,
          classes: row => row.withdrawn_barcode_id ? 'q-td--no-hover' : ''
        },
        {
          name: 'media_type',
          field: row => row.media_type?.name,
          label: 'Media Type',
          align: 'left',
          sortable: true,
          classes: row => row.withdrawn_barcode_id ? 'q-td--no-hover' : ''
        },
        {
          name: 'barcode_value',
          field: row => renderItemBarcodeDisplay(row),
          label: 'Item Barcode',
          align: 'left',
          sortable: true,
          classes: row => row.withdrawn_barcode_id ? 'q-td--no-hover' : ''
        }
      ]
      searchResultsTableVisibleColumns.value = [
        'accession_dt',
        'status',
        'owner',
        'size_class',
        'media_type',
        'barcode_value'
      ]
      if (currentScreenSize.value == 'xs') {
        searchResultsTableVisibleColumns.value = [
          'accession_dt',
          'status',
          'size_class',
          'media_type',
          'barcode_value'
        ]
      }
      showDownloadReport.value = true
      break
    case 'Tray':
      searchResultsTableColumns.value = [
        {
          name: 'accession_dt',
          field: 'accession_dt',
          label: 'Accession Date',
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
          name: 'media_type',
          field: row => row.media_type?.name,
          label: 'Media Type',
          align: 'left',
          sortable: true
        },
        {
          name: 'barcode_value',
          field: row => renderItemBarcodeDisplay(row),
          label: 'Tray Barcode',
          align: 'left',
          sortable: true
        }
      ]
      searchResultsTableVisibleColumns.value = [
        'accession_dt',
        'owner',
        'size_class',
        'media_type',
        'barcode_value'
      ]
      if (currentScreenSize.value == 'xs') {
        searchResultsTableVisibleColumns.value = [
          'accession_dt',
          'size_class',
          'media_type',
          'barcode_value'
        ]
      }
      break
    case 'Shelf':
      searchResultsTableColumns.value = [
        {
          name: 'barcode_value',
          field: row => renderItemBarcodeDisplay(row),
          label: 'Shelf Barcode',
          align: 'left',
          sortable: true
        },
        {
          name: 'location',
          field: 'location',
          label: 'Shelf Location',
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
          field: row => row.shelf_type?.size_class?.name,
          label: 'Size Class',
          align: 'left',
          sortable: true
        }
      ]
      searchResultsTableVisibleColumns.value = [
        'barcode_value',
        'location',
        'owner',
        'size_class'
      ]
      break
    case 'Accession':
      searchResultsTableColumns.value = [
        {
          name: 'create_dt',
          field: 'create_dt',
          label: 'Create Date',
          align: 'left',
          sortable: true
        },
        {
          name: 'workflow_id',
          field: 'workflow_id',
          label: 'Job Number',
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
          name: 'created_by_id',
          field: row => renderUserName(row.created_by),
          label: 'Created By',
          align: 'left',
          sortable: true
        },
        {
          name: 'user_id',
          field: row => renderUserName(row.user),
          label: 'Completed By',
          align: 'left',
          sortable: true
        }
      ]
      searchResultsTableVisibleColumns.value = [
        'create_dt',
        'workflow_id',
        'status',
        'created_by_id',
        'user_id'
      ]
      if (currentScreenSize.value == 'xs') {
        searchResultsTableVisibleColumns.value = [
          'create_dt',
          'workflow_id',
          'status',
          'user_id'
        ]
      }
      break
    case 'Verification':
      searchResultsTableColumns.value = [
        {
          name: 'create_dt',
          field: 'create_dt',
          label: 'Create Date',
          align: 'left',
          sortable: true
        },
        {
          name: 'workflow_id',
          field: 'workflow_id',
          label: 'Job Number',
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
          name: 'created_by_id',
          field: row => renderUserName(row.created_by),
          label: 'Created By',
          align: 'left',
          sortable: true
        },
        {
          name: 'user_id',
          field: row => renderUserName(row.user),
          label: 'Completed By',
          align: 'left',
          sortable: true
        }
      ]
      searchResultsTableVisibleColumns.value = [
        'create_dt',
        'workflow_id',
        'status',
        'created_by_id',
        'user_id'
      ]
      if (currentScreenSize.value == 'xs') {
        searchResultsTableVisibleColumns.value = [
          'create_dt',
          'workflow_id',
          'status',
          'user_id'
        ]
      }
      break
    case 'Request':
      searchResultsTableColumns.value = [
        {
          name: 'requested_by',
          field: row => renderUserName(row?.requested_by),
          label: 'Requested By',
          align: 'left',
          sortable: true
        },
        {
          name: 'barcode_value',
          field: row => renderItemBarcodeDisplay(row.non_tray_item ? row.non_tray_item : row.item),
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
          name: 'request_type',
          field: row => row.request_type?.type,
          label: 'Request Type',
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
          name: 'delivery_location',
          field: row => row.delivery_location?.address,
          label: 'Delivery Location',
          align: 'left',
          sortable: true
        },
        {
          name: 'create_dt',
          field: 'create_dt',
          label: 'Create Date',
          align: 'left',
          sortable: true
        }
      ]
      searchResultsTableVisibleColumns.value = [
        'requested_by',
        'barcode_value',
        'external_request_id',
        'requestor_name',
        'request_type',
        'priority',
        'delivery_location',
        'create_dt'
      ]
      if (currentScreenSize.value == 'xs') {
        searchResultsTableVisibleColumns.value = [
          'barcode_value',
          'external_request_id',
          'priority',
          'delivery_location',
          'create_dt'
        ]
      }
      break
    case 'Refile':
      searchResultsTableColumns.value = [
        {
          name: 'create_dt',
          field: 'create_dt',
          label: 'Create Date',
          align: 'left',
          sortable: true
        },
        {
          name: 'id',
          field: 'id',
          label: 'Job Number',
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
          name: 'created_by_id',
          field: row => renderUserName(row.created_by),
          label: 'Created By',
          align: 'left',
          sortable: true
        },
        {
          name: 'assigned_user_id',
          field: row => renderUserName(row.assigned_user),
          label: 'Completed By',
          align: 'left',
          sortable: true
        }
      ]
      searchResultsTableVisibleColumns.value = [
        'create_dt',
        'id',
        'status',
        'created_by_id',
        'assigned_user_id'
      ]
      if (currentScreenSize.value == 'xs') {
        searchResultsTableVisibleColumns.value = [
          'create_dt',
          'id',
          'status',
          'assigned_user_id'
        ]
      }
      break
    case 'Withdraw':
      searchResultsTableColumns.value = [
        {
          name: 'create_dt',
          field: 'create_dt',
          label: 'Create Date',
          align: 'left',
          sortable: true
        },
        {
          name: 'id',
          field: 'id',
          label: 'Job Number',
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
          name: 'created_by_id',
          field: row => renderUserName(row.created_by),
          label: 'Created By',
          align: 'left',
          sortable: true
        },
        {
          name: 'assigned_user_id',
          field: row => renderUserName(row.assigned_user),
          label: 'Completed By',
          align: 'left',
          sortable: true
        }
      ]
      searchResultsTableVisibleColumns.value = [
        'create_dt',
        'id',
        'status',
        'created_by_id',
        'assigned_user_id'
      ]
      if (currentScreenSize.value == 'xs') {
        searchResultsTableVisibleColumns.value = [
          'create_dt',
          'id',
          'status',
          'assigned_user_id'
        ]
      }
      break
    default:
      searchResultsTableColumns.value = [
        {
          name: 'create_dt',
          field: 'create_dt',
          label: 'Create Date',
          align: 'left',
          sortable: true
        },
        {
          name: 'id',
          field: 'id',
          label: 'Job Number',
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
          name: 'created_by_id',
          field: row => renderUserName(row.created_by),
          label: 'Created By',
          align: 'left',
          sortable: true
        },
        {
          name: 'user_id',
          field: row => renderUserName(row.user),
          label: 'Completed By',
          align: 'left',
          sortable: true
        }
      ]
      searchResultsTableVisibleColumns.value = [
        'create_dt',
        'id',
        'status',
        'created_by_id',
        'user_id'
      ]
      if (currentScreenSize.value == 'xs') {
        searchResultsTableVisibleColumns.value = [
          'create_dt',
          'id',
          'status',
          'user_id'
        ]
      }
      break
  }
}

const renderUserName = (userObj) => {
  let userName = ''
  if (userObj && userObj.first_name && userObj.last_name) {
    userName = `${userObj.first_name} ${userObj.last_name}`
  }
  return userName
}

const handleResultSelection = (rowData) => {
  switch (route.params.searchType) {
    case 'Item':
    case 'TrayItem':
      if (rowData.barcode) {
        router.push({
          name: 'record-management-items',
          params: {
            barcode: rowData.barcode.value
          }
        })
      }
      break
    case 'Tray':
      router.push({
        name: 'record-management-tray',
        params: {
          barcode: rowData.barcode.value
        }
      })
      break
    case 'Shelf':
      router.push({
        name: 'record-management-shelf',
        params: {
          barcode: rowData.barcode.value
        }
      })
      break
    case 'Accession':
      router.push({
        name: 'accession',
        params: {
          jobId: rowData.workflow_id
        }
      })
      break
    case 'Verification':
      router.push({
        name: 'verification',
        params: {
          jobId: rowData.workflow_id
        }
      })
      break
    case 'Shelving':
      router.push({
        name: 'shelving',
        params: {
          jobId: rowData.id
        }
      })
      break
    case 'Request':
      router.push({
        name: 'request-details',
        params: {
          jobId: rowData.id
        }
      })
      break
    case 'Picklist':
      router.push({
        name: 'picklist',
        params: {
          jobId: rowData.id
        }
      })
      break
    case 'Refile':
      router.push({
        name: 'refile',
        params: {
          jobId: rowData.id
        }
      })
      break
    case 'Withdraw':
      router.push({
        name: 'withdrawal',
        params: {
          jobId: rowData.id
        }
      })
      break
    default:
      break
  }
}

const loadAdvancedSearch = async (qParams) => {
  try {
    appIsLoadingData.value = true
    if (route.params.searchType == 'Item' || route.params.searchType == 'TrayItem') {
      await router.replace({
        params: {
          searchType: toggleSearchTab.value == 'trayItem' ? 'TrayItem' : 'Item'
        }
      })
    }

    await getAdvancedSearchResults({
      ...advancedSearchHistory.value,
      ...qParams
    }, route.params.searchType)

    // update route queries to match new searches
    router.replace({
      query: {
        ...advancedSearchHistory.value,
        ...qParams
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

const downloadAdvancedSearchReport = async () => {
  try {
    appIsLoadingData.value = true
    // Send the download the endpoint to use based on which tab we are looking at
    // The endpoints set here need to match the keys in InventoryService.js
    let endpoint
    switch (route.params.searchType) {
      case 'Item':
        endpoint = 'nonTrayItems'
        break
      case 'TrayItem':
        endpoint = 'items'
        break
      default:
        return
    }
    await downloadAdvancedSearchResults(endpoint)
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
