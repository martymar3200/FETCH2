<template>
  <div class="admin-location-explorer q-pa-xs-sm q-pa-sm-lg">
    <!-- Location Navigator (sidebar + table in a row layout) -->
    <div class="row q-col-gutter-md">
      <!-- Left Sidebar: Cascading Location Selectors -->
      <div
        v-show="!isNavigatorCollapsed"
        class="col-xs-12 col-md-3 transition-width"
      >
        <q-card class="location-navigator">
          <q-card-section class="row items-center q-pb-none">
            <h2 class="text-h6 text-bold q-mb-none">
              Location Navigator
            </h2>
            <q-space />
            <BaseButton
              flat
              round
              dense
              icon="chevron_left"
              @click="isNavigatorCollapsed = true"
              aria-label="collapseNavigator"
            >
              <q-tooltip>Collapse Navigator</q-tooltip>
            </BaseButton>
          </q-card-section>

          <q-card-section class="q-pt-md">
            <!-- Building Select -->
            <div class="form-group q-mb-md">
              <label class="form-group-label">Building</label>
              <SelectInput
                v-model="selectedIds.building_id"
                :options="buildings"
                option-type="buildings"
                option-value="id"
                option-label="name"
                :clearable="false"
                placeholder="Select Building"
                @update:model-value="handleSelectionChange('building')"
                aria-label="buildingSelect"
              />
            </div>

            <!-- Module Select -->
            <div
              v-if="selectedIds.building_id"
              class="form-group q-mb-md"
            >
              <label class="form-group-label">Module</label>
              <SelectInput
                v-model="selectedIds.module_id"
                :options="modules"
                option-type="modules"
                :option-query="{ building_id: selectedIds.building_id }"
                option-value="id"
                option-label="module_number"
                placeholder="Select Module"
                :clearable="true"
                :disabled="!selectedIds.building_id"
                @update:model-value="handleSelectionChange('module')"
                aria-label="moduleSelect"
              />
            </div>

            <!-- Aisle Select -->
            <div
              v-if="selectedIds.module_id"
              class="form-group q-mb-md"
            >
              <label class="form-group-label">Aisle</label>
              <SelectInput
                v-model="selectedIds.aisle_id"
                :options="aisles"
                option-type="aisles"
                :option-query="{
                  building_id: selectedIds.building_id,
                  module_id: selectedIds.module_id,
                  sort_by: 'aisle_number'
                }"
                option-value="id"
                option-label="aisle_number"
                placeholder="Select Aisle"
                :clearable="true"
                :disabled="!selectedIds.module_id"
                @update:model-value="handleSelectionChange('aisle')"
                aria-label="aisleSelect"
              />
            </div>

            <!-- Side Toggle -->
            <div
              v-if="selectedIds.aisle_id"
              class="form-group q-mb-md"
            >
              <label class="form-group-label">Side</label>
              <ToggleButtonInput
                v-model="selectedIds.side_id"
                :options="sides"
                option-value="id"
                option-label="side_orientation.name"
                :disabled="!selectedIds.aisle_id"
                @update:model-value="handleSelectionChange('side')"
              />
            </div>

            <!-- Ladder Select -->
            <div
              v-if="selectedIds.side_id"
              class="form-group q-mb-md"
            >
              <label class="form-group-label">Ladder</label>
              <SelectInput
                v-model="selectedIds.ladder_id"
                :options="ladders"
                option-type="ladders"
                :option-query="{
                  building_id: selectedIds.building_id,
                  module_id: selectedIds.module_id,
                  aisle_id: selectedIds.aisle_id,
                  side_id: selectedIds.side_id,
                  sort_by: 'ladder_number'
                }"
                option-value="id"
                option-label="ladder_number"
                placeholder="Select Ladder"
                :clearable="true"
                :disabled="!selectedIds.side_id"
                @update:model-value="handleSelectionChange('ladder')"
                aria-label="ladderSelect"
              />
            </div>

            <!-- Current Viewing Level -->
            <div class="q-mt-md">
              <q-chip
                :label="`Viewing: ${currentViewLevel}`"
                color="accent"
                text-color="white"
                icon="visibility"
              />
            </div>
          </q-card-section>
        </q-card>
      </div>

      <!-- Right Panel: Data Table -->
      <div :class="isNavigatorCollapsed ? 'col-12 transition-width' : 'col-xs-12 col-md-9 transition-width'">
        <EssentialTable
          ref="locationTableRef"
          :table-columns="tableColumns"
          :table-visible-columns="tableVisibleColumns"
          :table-data="tableData"
          :enable-table-reorder="false"
          :heading-row-class="'q-mb-xs-md q-mb-md-lg'"
          :heading-filter-class="currentScreenSize == 'xs' ? 'col-xs-6 q-mr-auto' : 'q-ml-auto'"
          :heading-rearrange-class="'q-mr-xs-auto q-mr-sm-none q-ml-sm-auto'"
          :enable-pagination="currentViewLevel === 'Buildings' || currentViewLevel === 'Shelves'"
          :pagination-total="tableDataTotal"
          :pagination-loading="appIsLoadingData"
          :hide-table-rearrange="true"
          @update-pagination="loadTableData($event)"
        >
          <template #heading-row>
            <div
              class="col-xs-12 col-lg-auto q-mr-auto q-pb-xs-sm q-pb-lg-none flex items-center"
              :class="currentScreenSize == 'xs' ? '' : 'self-center'"
            >
              <BaseButton
                v-if="isNavigatorCollapsed"
                flat
                round
                dense
                icon="menu"
                class="q-mr-sm"
                @click="isNavigatorCollapsed = false"
                aria-label="expandNavigator"
              >
                <q-tooltip>Expand Navigator</q-tooltip>
              </BaseButton>
              <h1 class="text-h4 text-bold q-mb-none">
                {{ tableTitle }}
              </h1>
            </div>

            <div
              class="col-xs-12 col-sm-auto flex justify-end"
              :class="'order-1'"
            >

              <BaseButton
                v-if="currentViewLevel === 'Shelves'"
                no-caps
                outline
                icon="playlist_add"
                label="Insert & Shift"
                class="btn-no-wrap text-body1 q-ml-sm"
                @click="openInsertModal"
              />
              <BaseButton
                no-caps
                unelevated
                icon="add"
                color="accent"
                :label="`Add ${singularLevel}`"
                class="btn-no-wrap text-body1 q-ml-sm"
                @click="openAddModal"
              />
            </div>
          </template>

          <template #table-td="{ colName, props, value }">
            <span v-if="colName == 'actions'">
              <MoreOptionsMenu
                :options="tableMenuOptions"
                @click="(opt) => handleOptionMenu(opt, props.row)"
              />
            </span>

            <span v-if="colName.includes('_dt')">
              {{ formatDateTime(value).date }}
            </span>
          </template>
        </EssentialTable>
      </div>
    </div>

    <!-- Add/Edit Location Modal -->
    <AdminLocationEditModal
      v-if="showModal.visible"
      :action-type="showModal.actionType"
      :location-level="currentViewLevel"
      :location-data="showModal.locationData"
      :parent-ids="selectedIds"
      @hide="closeModal"
      @saved="handleSaved"
    />


  </div>
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
import { onBeforeMount, ref, computed, inject } from 'vue'
import { Notify, useQuasar } from 'quasar'
import { useGlobalStore } from '@/stores/global-store'
import { useBuildingStore } from '@/stores/building-store'
import { useOptionStore } from '@/stores/option-store'
import { storeToRefs } from 'pinia'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import EssentialTable from 'src/components/EssentialTable.vue'
import MoreOptionsMenu from '@/components/MoreOptionsMenu.vue'
import SelectInput from '@/components/SelectInput.vue'
import ToggleButtonInput from '@/components/ToggleButtonInput.vue'
import AdminLocationEditModal from '@/components/Admin/AdminLocationEditModal.vue'

// Composables
const { currentScreenSize } = useCurrentScreenSize()
const formatDateTime = inject('format-date-time')
const $q = useQuasar()

// Store Data
const { appIsLoadingData } = storeToRefs(useGlobalStore())
const {
  getBuildingsList,
  getBuildingDetails,
  getModuleDetails,
  getAisleDetails,
  getSideDetails,
  getSideList,
  getLadderDetails,
  getShelveList,
  resetBuildingStore,
  resetBuildingChildren,
  resetModuleChildren,
  resetAisleChildren,
  resetSideChildren,
  deleteBuilding,
  deleteModule,
  deleteAisle,
  deleteLadder,
  deleteShelve
} = useBuildingStore()
const {
  buildings,
  buildingDetails,
  moduleDetails,
  aisleDetails,
  sideDetails,
  ladderDetails,
  sides,
  shelves,
  shelvesTotal,
  buildingsTotal
} = storeToRefs(useBuildingStore())
const {
  modules,
  aisles,
  ladders
} = storeToRefs(useOptionStore())
const { getOptions } = useOptionStore()

// Local State
const locationTableRef = ref(null)
const selectedIds = ref({
  building_id: null,
  module_id: null,
  aisle_id: null,
  side_id: null,
  ladder_id: null
})
const tableDataTotal = ref(0)

const showModal = ref({
  visible: false,
  actionType: '',
  locationData: {}
})
const isNavigatorCollapsed = ref(false)

// Computed: Current viewing level
const currentViewLevel = computed(() => {
  if (selectedIds.value.ladder_id) {
    return 'Shelves'
  }
  if (selectedIds.value.side_id) {
    return 'Ladders'
  }
  if (selectedIds.value.aisle_id) {
    return 'Sides'
  }
  if (selectedIds.value.module_id) {
    return 'Aisles'
  }
  if (selectedIds.value.building_id) {
    return 'Modules'
  }
  return 'Buildings'
})

const singularLevel = computed(() => {
  const map = {
    Buildings: 'Building',
    Modules: 'Module',
    Aisles: 'Aisle',
    Sides: 'Side',
    Ladders: 'Ladder',
    Shelves: 'Shelf'
  }
  return map[currentViewLevel.value]
})

// Computed: Table title with breadcrumb context
const tableTitle = computed(() => {
  const level = currentViewLevel.value
  if (level === 'Buildings') {
    return 'Buildings'
  }

  const building = buildingDetails.value?.name || ''
  if (level === 'Modules') {
    return `${building}: Modules`
  }

  const module = moduleDetails.value?.module_number ?? ''
  if (level === 'Aisles') {
    return `${building} (${module}): Aisles`
  }

  // Sides are not shown in their own table, we skip to ladders view
  const aisle = aisleDetails.value?.aisle_number ?? ''
  const side = sideDetails.value?.side_orientation?.name
  const sideAbbrev = side === 'Left' ? 'L' : side === 'Right' ? 'R' : ''
  if (level === 'Sides') {
    return `${building} (${module}-${aisle}): Sides`
  }
  if (level === 'Ladders') {
    return `${building} (${module}-${aisle}-${sideAbbrev}): Ladders`
  }

  const ladder = ladderDetails.value?.ladder_number ?? ''
  if (level === 'Shelves') {
    return `${building} (${module}-${aisle}-${sideAbbrev}-${ladder}): Shelves`
  }

  return level
})

// Computed: Table data based on current level
const tableData = computed(() => {
  switch (currentViewLevel.value) {
    case 'Buildings': return buildings.value
    case 'Modules': return buildingDetails.value?.modules || []
    case 'Aisles': return moduleDetails.value?.aisles || []
    case 'Ladders': return sideDetails.value?.ladders || []
    case 'Shelves': return shelves.value
    default: return []
  }
})

// Computed: Table columns based on current level
const tableColumns = computed(() => {
  const actionsCol = {
    name: 'actions',
    field: 'actions',
    label: '',
    align: 'center',
    sortable: false,
    required: true
  }

  switch (currentViewLevel.value) {
    case 'Buildings':
      return [
        actionsCol,
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
    case 'Modules':
      return [
        actionsCol,
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
    case 'Aisles':
      return [
        actionsCol,
        {
          name: 'aisle',
          field: 'aisle_number',
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
    case 'Ladders':
      return [
        actionsCol,
        {
          name: 'ladder',
          field: 'ladder_number',
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
    case 'Shelves':
      return [
        actionsCol,
        {
          name: 'shelf_number',
          field: 'shelf_number',
          label: 'Shelf Number',
          align: 'left',
          sortable: true
        },
        {
          name: 'width',
          field: 'width',
          label: 'Width',
          align: 'left',
          sortable: true
        },
        {
          name: 'height',
          field: 'height',
          label: 'Height',
          align: 'left',
          sortable: true
        },
        {
          name: 'depth',
          field: 'depth',
          label: 'Depth',
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
    default:
      return []
  }
})

const tableVisibleColumns = computed(() => {
  switch (currentViewLevel.value) {
    case 'Buildings': return [
      'actions',
      'name',
      'create_dt',
      'update_dt'
    ]
    case 'Modules': return [
      'actions',
      'module',
      'create_dt',
      'update_dt'
    ]
    case 'Aisles': return [
      'actions',
      'aisle',
      'sort_priority',
      'create_dt',
      'update_dt'
    ]
    case 'Ladders': return [
      'actions',
      'ladder',
      'sort_priority',
      'create_dt',
      'update_dt'
    ]
    case 'Shelves': return [
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
    default: return []
  }
})

const tableMenuOptions = computed(() => {
  const editLabel = `Edit ${singularLevel.value}`
  const deleteLabel = `Delete ${singularLevel.value}`
  return [
    { text: editLabel },
    { text: deleteLabel }
  ]
})

// Methods

const handleSelectionChange = async (level) => {
  try {
    appIsLoadingData.value = true

    switch (level) {
      case 'building':
        resetBuildingChildren()
        selectedIds.value.module_id = null
        selectedIds.value.aisle_id = null
        selectedIds.value.side_id = null
        selectedIds.value.ladder_id = null
        if (selectedIds.value.building_id) {
          await getBuildingDetails(selectedIds.value.building_id)
        }
        break

      case 'module':
        resetModuleChildren()
        selectedIds.value.aisle_id = null
        selectedIds.value.side_id = null
        selectedIds.value.ladder_id = null
        if (selectedIds.value.module_id) {
          await getModuleDetails(selectedIds.value.module_id)
        }
        break

      case 'aisle':
        resetAisleChildren()
        selectedIds.value.side_id = null
        selectedIds.value.ladder_id = null
        if (selectedIds.value.aisle_id) {
          await getAisleDetails(selectedIds.value.aisle_id)
          await getSideList({
            building_id: selectedIds.value.building_id,
            module_id: selectedIds.value.module_id,
            aisle_id: selectedIds.value.aisle_id
          })
        }
        break

      case 'side':
        resetSideChildren()
        selectedIds.value.ladder_id = null
        if (selectedIds.value.side_id) {
          await getSideDetails(selectedIds.value.side_id)
        }
        break

      case 'ladder':
        if (selectedIds.value.ladder_id) {
          await getLadderDetails(selectedIds.value.ladder_id)
          // Load shelves when ladder is selected
          await loadTableData()
        }
        break
    }
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to load location data'
    })
  } finally {
    appIsLoadingData.value = false
  }
}

const loadTableData = async (qParams) => {
  try {
    appIsLoadingData.value = true

    switch (currentViewLevel.value) {
      case 'Buildings':
        await getBuildingsList(qParams)
        tableDataTotal.value = buildingsTotal.value
        break
      case 'Shelves':
        await getShelveList({
          ...qParams,
          building_id: selectedIds.value.building_id,
          module_id: selectedIds.value.module_id,
          aisle_id: selectedIds.value.aisle_id,
          side_id: selectedIds.value.side_id,
          ladder_id: selectedIds.value.ladder_id
        })
        tableDataTotal.value = shelvesTotal.value
        break
      // Modules, Aisles, Ladders are loaded from parent detail responses
      default:
        break
    }
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to load location data'
    })
  } finally {
    appIsLoadingData.value = false
  }
}

const handleOptionMenu = async (option, rowData) => {
  if (option.text.includes('Delete')) {
    $q.dialog({
      title: 'Confirm Delete',
      message: `Are you sure you want to delete this ${singularLevel.value}? This action cannot be undone.`,
      cancel: true,
      persistent: true
    }).onOk(async () => {
      try {
        appIsLoadingData.value = true
        switch (currentViewLevel.value) {
          case 'Buildings': await deleteBuilding(rowData.id); break
          case 'Modules': await deleteModule(rowData.id); break
          case 'Aisles': await deleteAisle(rowData.id); break
          case 'Ladders': await deleteLadder(rowData.id); break
          case 'Shelves': await deleteShelve(rowData.id); break
        }

        Notify.create({
          type: 'positive',
          message: `Successfully deleted the ${singularLevel.value}`
        })

        // Reload table data
        if (currentViewLevel.value === 'Buildings') {
          await loadTableData()
        } else if (currentViewLevel.value === 'Modules') {
          await getBuildingDetails(selectedIds.value.building_id)
        } else if (currentViewLevel.value === 'Aisles') {
          await getModuleDetails(selectedIds.value.module_id)
        } else if (currentViewLevel.value === 'Ladders') {
          await getSideDetails(selectedIds.value.side_id)
        } else if (currentViewLevel.value === 'Shelves') {
          await loadTableData()
        }

      } catch (error) {
        Notify.create({
          type: 'negative',
          message: error.response?.data?.detail || error.message || `Failed to delete ${singularLevel.value}`
        })
      } finally {
        appIsLoadingData.value = false
      }
    })
    return
  }

  // Edit action
  // For shelves, load options before opening modal
  if (currentViewLevel.value === 'Shelves') {
    appIsLoadingData.value = true
    await Promise.all([
      getOptions('owners'),
      getOptions('sizeClass'),
      getOptions('shelfTypes'),
      getOptions('containerTypes')
    ])
    appIsLoadingData.value = false
  }

  showModal.value = {
    visible: true,
    actionType: 'Edit',
    locationData: rowData
  }
}

const openAddModal = async () => {
  // For shelves, load options before opening modal
  if (currentViewLevel.value === 'Shelves') {
    appIsLoadingData.value = true
    await Promise.all([
      getOptions('owners'),
      getOptions('sizeClass'),
      getOptions('shelfTypes'),
      getOptions('containerTypes')
    ])
    appIsLoadingData.value = false
  }

  showModal.value = {
    visible: true,
    actionType: 'Add',
    locationData: {}
  }
}

const openInsertModal = async () => {
  // For shelves, load options before opening modal
  if (currentViewLevel.value === 'Shelves') {
    appIsLoadingData.value = true
    await Promise.all([
      getOptions('owners'),
      getOptions('sizeClass'),
      getOptions('shelfTypes'),
      getOptions('containerTypes')
    ])
    appIsLoadingData.value = false
  }

  showModal.value = {
    visible: true,
    actionType: 'Insert',
    locationData: {}
  }
}

const closeModal = () => {
  showModal.value = {
    visible: false,
    actionType: '',
    locationData: {}
  }
}

const handleSaved = () => {
  // Reload the table data after a save
  loadTableData()
  closeModal()
}

// Lifecycle
onBeforeMount(() => {
  resetBuildingStore()
  loadTableData()
})
</script>

<style lang="scss" scoped>
.location-navigator {
  position: sticky;
  top: 80px;
}

.transition-width {
  transition: width 0.3s ease-in-out;
}
</style>
