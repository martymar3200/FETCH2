<template>
  <div class="admin-location-manager q-pa-xs-sm q-pa-sm-lg">
    <div class="row q-mb-xs-xl q-mb-sm-none">
      <div class="col-grow q-mb-xs-md q-mb-sm-none">
        <EssentialTable
          :table-columns="locationTableColumns"
          :table-visible-columns="locationTableVisibleColumns"
          :filter-options="locationTableFilters"
          :table-data="locationData"
          :enable-table-reorder="false"
          :heading-row-class="'q-mb-xs-md q-mb-md-lg'"
          :heading-filter-class="currentScreenSize == 'xs' ? 'col-xs-6 q-mr-auto' : 'q-ml-auto'"
          :heading-rearrange-class="'q-mr-xs-auto q-mr-sm-none q-ml-sm-auto'"
          :enable-pagination="mainProps.locationType == 'buildings' || mainProps.locationType == 'shelves' ? true : false"
          :pagination-total="locationDataTotal"
          :pagination-loading="appIsLoadingData"
          @update-pagination="loadLocationData($event)"
        >
          <template #heading-row>
            <div
              class="col-xs-12 col-lg-auto q-mr-auto q-pb-xs-sm q-pb-lg-none"
              :class="currentScreenSize == 'xs' ? '' : 'self-center'"
            >
              <h1 class="text-h4 text-bold">
                {{ renderLocationTableTitle }}
              </h1>
            </div>

            <div
              class="col-xs-5 col-sm-auto flex justify-end"
              :class="'order-1'"
            >
              <q-btn
                no-caps
                unelevated
                icon="add"
                color="accent"
                :label="renderLocationTableAction"
                class="btn-no-wrap text-body1 q-ml-sm"
                @click="showLocationModal.type = 'Add'"
              />
            </div>
          </template>

          <template #table-td="{ colName, props, value }">
            <span
              v-if="colName == 'actions'"
            >
              <MoreOptionsMenu
                :options="renderLocationTableOptionsMenu"
                class=""
                @click="handleOptionMenu(props.row)"
              />
            </span>

            <span v-if="colName.includes('_dt')">
              {{ formatDateTime(value).date }}
            </span>
          </template>
        </EssentialTable>
      </div>
    </div>
  </div>

  <!-- add/edit location property modal -->
  <AdminLocationManagerModal
    v-if="showLocationModal.type !== ''"
    :location-type="locationType"
    :action-type="showLocationModal.type"
    :location-data="showLocationModal.locationData"
    @hide="showLocationModal.type = ''; showLocationModal.locationData = {}"
    @new-location-added="locationDataTotal++"
  />
</template>

<script setup>
import { onBeforeMount, ref, inject, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useGlobalStore } from '@/stores/global-store'
import { useBuildingStore } from '@/stores/building-store'
import { useOptionStore } from '@/stores/option-store'
import { storeToRefs } from 'pinia'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import EssentialTable from 'src/components/EssentialTable.vue'
import MoreOptionsMenu from '@/components/MoreOptionsMenu.vue'
import AdminLocationManagerModal from '@/components/Admin/AdminLocationManagerModal.vue'

const route = useRoute()

// Props
const mainProps = defineProps({
  locationType: {
    type: String,
    default: 'buildings',
    required: true
  }
})

// Composables
const { currentScreenSize } = useCurrentScreenSize()

// Store Data
const { appIsLoadingData } = storeToRefs(useGlobalStore())
const {
  getBuildingsList,
  getBuildingDetails,
  getModuleDetails,
  getAisleDetails,
  getSideDetails,
  getLadderDetails,
  getShelveList
} = useBuildingStore()
const {
  buildings,
  buildingDetails,
  moduleDetails,
  aisleDetails,
  sideDetails,
  ladderDetails,
  shelves,
  shelvesTotal,
  buildingsTotal
} = storeToRefs(useBuildingStore())
const { getOptions } = useOptionStore()

// Local Data
const locationDataTotal = ref(0)
const locationData = computed(() => {
  let tableData = []
  switch (mainProps.locationType) {
    case 'buildings':
      tableData = buildings.value
      break
    case 'modules':
      tableData = buildingDetails.value.modules
      break
    case 'aisles':
      tableData = moduleDetails.value.aisles
      break
    case 'ladders':
      tableData = sideDetails.value.ladders
      break
    case 'shelves':
      tableData = shelves.value
      break
    default:
      break
  }
  return tableData
})
const locationTableVisibleColumns = ref([])
const locationTableColumns = ref([])
const locationTableFilters =  ref([])
const renderLocationTableTitle = computed(() => {
  let title = ''
  const building = buildingDetails.value.name
  const module = moduleDetails.value?.module_number
  const aisle = aisleDetails.value?.aisle_number?.number
  const side = sideDetails.value?.side_orientation?.name
  const ladder = ladderDetails.value?.ladder_number?.number
  if (mainProps.locationType == 'buildings') {
    // returns a title in title case format
    title = `${mainProps.locationType.replace(mainProps.locationType[0], mainProps.locationType[0].toUpperCase())}`
  } else if (mainProps.locationType == 'modules') {
    // returns a title in the format of 'building: Modules'
    // ex: Fort Meade (1-2-L-3): Shelves
    title = `${building}: ${mainProps.locationType.replace(mainProps.locationType[0], mainProps.locationType[0].toUpperCase())}`
  } else {
    // returns a title in the format of 'building (location selection): locationType'
    // ex: Fort Meade (1-2-L-3): Shelves
    title = `${building} (${module ?? ''}${aisle ? '-' + aisle : ''}${side && side == 'Left' ? '-L' : side && side == 'Right' ? '-R' : ''}${ladder ? '-' + ladder : ''}): ${mainProps.locationType.replace(mainProps.locationType[0], mainProps.locationType[0].toUpperCase())}`
  }
  return title
})
const renderLocationTableAction = computed(() => {
  let actionText = ''
  switch (mainProps.locationType) {
    case 'buildings':
      actionText = 'Add Building'
      break
    case 'modules':
      actionText = 'Add Module'
      break
    case 'aisles':
      actionText = 'Add Aisle'
      break
    case 'ladders':
      actionText = 'Add Ladder'
      break
    case 'shelves':
      actionText = 'Add Shelf'
      break
    default:
      break
  }
  return actionText
})
const renderLocationTableOptionsMenu = computed(() => {
  let options = []
  switch (mainProps.locationType) {
    case 'buildings':
      options = [{ text: 'Edit Building' }]
      break
    case 'modules':
      options = [{ text: 'Edit Module' }]
      break
    case 'aisles':
      options = [{ text: 'Edit Aisle' }]
      break
    case 'ladders':
      options = [{ text: 'Edit Ladder' }]
      break
    case 'shelves':
      options = [{ text: 'Edit Shelf' }]
      break
    default:
      break
  }
  return options
})
const showLocationModal = ref({
  type: '',
  locationType: mainProps.locationType,
  locationData: {}
})


// Logic
const handleAlert = inject('handle-alert')
const formatDateTime = inject('format-date-time')

onBeforeMount(() => {
  loadLocationData()
  generateLocationTableInfo()
})

const handleOptionMenu = async (rowData) => {
  // load any options info that will be needed in our modal popup
  if (mainProps.locationType == 'shelves') {
    appIsLoadingData.value = true
    await Promise.all([
      getOptions('owners'),
      getOptions('sizeClass'),
      getOptions('shelfTypes'),
      getOptions('containerTypes')
    ])
    appIsLoadingData.value = false
  }

  showLocationModal.value.locationData = rowData
  showLocationModal.value.type = 'Edit'
}

const generateLocationTableInfo = () => {
  // creates the report table fields needed based on the selected location type
  switch (mainProps.locationType) {
    case 'buildings':
      locationTableColumns.value = [
        {
          name: 'actions',
          field: 'actions',
          label: '',
          align: 'center',
          sortable: false,
          required: true
        },
        {
          name: 'name',
          field: 'name',
          label: 'Building',
          align: 'left',
          sortable: true
        },
        {
          name: 'create_dt',
          field: 'create_dt',
          label: 'Created Date',
          align: 'left',
          sortable: true
        },
        {
          name: 'update_dt',
          field: 'update_dt',
          label: 'Last Updated',
          align: 'left',
          sortable: true
        }
      ]
      locationTableVisibleColumns.value = [
        'actions',
        'name',
        'create_dt',
        'update_dt'
      ]
      break
    case 'modules':
      locationTableColumns.value = [
        {
          name: 'actions',
          field: 'actions',
          label: '',
          align: 'center',
          sortable: false,
          required: true
        },
        {
          name: 'module',
          field: 'module_number',
          label: 'Module',
          align: 'left',
          sortable: true
        },
        {
          name: 'create_dt',
          field: 'create_dt',
          label: 'Created Date',
          align: 'left',
          sortable: true
        },
        {
          name: 'update_dt',
          field: 'update_dt',
          label: 'Last Updated',
          align: 'left',
          sortable: true
        }
      ]
      locationTableVisibleColumns.value = [
        'actions',
        'module',
        'create_dt',
        'update_dt'
      ]
      break
    case 'aisles':
      locationTableColumns.value = [
        {
          name: 'actions',
          field: 'actions',
          label: '',
          align: 'center',
          sortable: false,
          required: true
        },
        {
          name: 'aisle',
          field: row => row.aisle_number?.number,
          label: 'Aisle',
          align: 'left',
          sortable: true
        },
        {
          name: 'sort_priority',
          field: 'sort_priority',
          label: 'Location Logical Order',
          align: 'left',
          sortable: true
        },
        {
          name: 'create_dt',
          field: 'create_dt',
          label: 'Created Date',
          align: 'left',
          sortable: true
        },
        {
          name: 'update_dt',
          field: 'update_dt',
          label: 'Last Updated',
          align: 'left',
          sortable: true
        }
      ]
      locationTableVisibleColumns.value = [
        'actions',
        'aisle',
        'sort_priority',
        'create_dt',
        'update_dt'
      ]
      break
    case 'ladders':
      locationTableColumns.value = [
        {
          name: 'actions',
          field: 'actions',
          label: '',
          align: 'center',
          sortable: false,
          required: true
        },
        {
          name: 'ladder',
          field: row => row.ladder_number?.number,
          label: 'Ladder',
          align: 'left',
          sortable: true
        },
        {
          name: 'sort_priority',
          field: 'sort_priority',
          label: 'Location Logical Order',
          align: 'left',
          sortable: true
        },
        {
          name: 'create_dt',
          field: 'create_dt',
          label: 'Created Date',
          align: 'left',
          sortable: true
        },
        {
          name: 'update_dt',
          field: 'update_dt',
          label: 'Last Updated',
          align: 'left',
          sortable: true
        }
      ]
      locationTableVisibleColumns.value = [
        'actions',
        'ladder',
        'sort_priority',
        'create_dt',
        'update_dt'
      ]
      break
    case 'shelves':
      locationTableColumns.value = [
        {
          name: 'actions',
          field: 'actions',
          label: '',
          align: 'center',
          sortable: false,
          required: true
        },
        {
          name: 'shelf_number',
          field: row => row.shelf_number?.number,
          label: 'Shelf Number',
          align: 'left',
          sortable: true
        },
        {
          name: 'width',
          field: 'width',
          label: 'Shelf Width',
          align: 'left',
          sortable: true
        },
        {
          name: 'height',
          field: 'height',
          label: 'Shelf Height',
          align: 'left',
          sortable: true
        },
        {
          name: 'depth',
          field: 'depth',
          label: 'Shelf Depth',
          align: 'left',
          sortable: true
        },
        {
          name: 'size_class',
          field: row => row.shelf_type?.size_class?.name,
          label: 'Size Class',
          align: 'left',
          sortable: true
        },
        {
          name: 'shelf_type',
          field: row => row.shelf_type?.type,
          label: 'Shelf Type',
          align: 'left',
          sortable: true
        },
        {
          name: 'container_type',
          field: row => row.container_type?.type,
          label: 'Container Type',
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
          name: 'barcode_value',
          field: row => row.barcode?.value,
          label: 'Shelf Barcode',
          align: 'left',
          sortable: true
        },
        {
          name: 'sort_priority',
          field: 'sort_priority',
          label: 'Location Logical Order',
          align: 'left',
          sortable: true
        }
      ]
      locationTableVisibleColumns.value = [
        'actions',
        'shelf_number',
        'width',
        'height',
        'depth',
        'size_class',
        'shelf_type',
        'container_type',
        'owner',
        'barcode_value',
        'sort_priority'
      ]
      break
    default:
      break
  }
}

const loadLocationData = async (qParams) => {
  try {
    // TODO update each case to load the locations list info while passing in the route params needed for the location to be filtered down
    // ex: shelves should be getShelves(building_id, module_id, aisle_id, side_id, ladder_id) instead of loading by parent
    appIsLoadingData.value = true
    switch (mainProps.locationType) {
      case 'buildings': {
        await getBuildingsList(qParams)

        // set the location total for pagination tracking
        // TODO need to update other building fields to render their lists data instead of rendering from the parent
        locationDataTotal.value = buildingsTotal.value
        break
      }
      case 'modules':
        if (!buildingDetails.value.id) {
          await getBuildingDetails(route.params.buildingId)
        }
        break
      case 'aisles':
        if (!moduleDetails.value.id) {
          await getBuildingDetails(route.params.buildingId)
          await getModuleDetails(route.params.moduleId)
        }
        break
      case 'ladders':
        if (!sideDetails.value.id) {
          await getBuildingDetails(route.params.buildingId)
          await getModuleDetails(route.params.moduleId)
          await getAisleDetails(route.params.aisleId)
          await getSideDetails(route.params.sideId)
        }
        break
      case 'shelves':
        if (!ladderDetails.value.id) {
          await getBuildingDetails(route.params.buildingId)
          await getModuleDetails(route.params.moduleId)
          await getAisleDetails(route.params.aisleId)
          await getSideDetails(route.params.sideId)
          await getLadderDetails(route.params.ladderId)
        }

        // load the shelves via the list endpoint filtered by building, module, aisle, side and ladder
        await getShelveList({
          ...qParams,
          building_id: route.params.buildingId,
          module_id: route.params.moduleId,
          aisle_id: route.params.aisleId,
          side_id: route.params.sideId,
          ladder_id: route.params.ladderId
        })

        // set the location total for pagination tracking
        locationDataTotal.value = shelvesTotal.value
        break
      default:
        break
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
</script>

<style lang="scss" scoped>
</style>