<template>
  <div class="shelf">
    <div class="row">
      <div class="col">
        <h1 class="text-h4 text-bold q-mb-xs-md q-mb-sm-lg">
          Shelf Details
        </h1>
      </div>
    </div>

    <div class="row">
      <div class="col-xs-12 col-lg-4 q-pr-xs-none q-pr-lg-md q-pb-xs-md q-pb-lg-none">
        <BarcodeBox
          :barcode="shelfDetails.barcode.value"
          class="q-py-xs-sm q-py-sm-md"
        />
      </div>
      <template v-if="currentScreenSize !== 'xs'">
        <div class="col-sm-4 col-lg-3">
          <div class="column no-wrap">
            <div class="shelf-details">
              <label class="shelf-details-label text-h6">
                Shelf Number
              </label>
              <p class="shelf-details-text">
                {{ shelfDetails.shelf_number.number }}
              </p>
            </div>

            <div class="shelf-details">
              <label class="shelf-details-label text-h6">
                Owner
              </label>
              <p class="shelf-details-text">
                {{ shelfDetails.owner?.name ? shelfDetails.owner?.name : "" }}
              </p>
            </div>

            <div class="shelf-details">
              <label class="shelf-details-label text-h6">
                Size Class
              </label>
              <p class="shelf-details-text outline">
                {{ shelfDetails.shelf_type?.size_class.name }}
              </p>
            </div>

            <div class="shelf-details">
              <label class="shelf-details-label text-h6">
                Created Date
              </label>
              <p class="shelf-details-text">
                {{ formatDateTime(shelfDetails.create_dt).date }}
              </p>
            </div>
          </div>
        </div>
        <div
          class="col-sm-4 col-lg-3"
        >
          <div class="column no-wrap">
            <div class="shelf-details">
              <label class="shelf-details-label text-h6">
                Width
              </label>
              <p class="shelf-details-text">
                {{ shelfDetails.width }} in
              </p>
            </div>

            <div class="shelf-details">
              <label class="shelf-details-label text-h6">
                Height
              </label>
              <p class="shelf-details-text">
                {{ shelfDetails.height }} in
              </p>
            </div>

            <div class="shelf-details">
              <label class="shelf-details-label text-h6">
                Depth
              </label>
              <p class="shelf-details-text">
                {{ shelfDetails.depth }} in
              </p>
            </div>

            <div class="shelf-details">
              <label class="shelf-details-label text-h6">
                Available Quantity
              </label>
              <p class="shelf-details-text">
                {{ shelfDetails.available_space }}
              </p>
            </div>
          </div>
        </div>
        <div class="col-sm-4 col-lg-2">
          <div class="column no-wrap">
            <div class="shelf-details">
              <label class="shelf-details-label text-h6">
                Used Quantity
              </label>
              <p class="shelf-details-text">
                {{ renderUsedCapacity() }}
              </p>
            </div>

            <div class="shelf-details">
              <label class="shelf-details-label text-h6">
                Max Quantity
              </label>
              <p class="shelf-details-text">
                {{ shelfDetails.shelf_type?.max_capacity }}
              </p>
            </div>

            <div class="shelf-details">
              <label class="shelf-details-label text-h6">
                Shelf Location
              </label>
              <p
                v-if="renderShelfBuilding()"
                class="shelf-details-text outline q-mr-sm"
              >
                {{ renderShelfBuilding() }}
              </p>
              <p class="shelf-details-text outline">
                {{ renderShelfLocation(shelfDetails) }}
              </p>
            </div>
          </div>
        </div>
      </template>
      <template v-else>
        <div class="col-12 q-pb-sm">
          <div class="shelf-details">
            <label class="shelf-details-label">
              Shelf Number
            </label>
            <p class="shelf-details-text">
              {{ shelfDetails.id }}
            </p>
          </div>
          <div class="shelf-details">
            <label class="shelf-details-label">
              Owner
            </label>
            <p class="shelf-details-text">
              {{ shelfDetails.owner?.name ? shelfDetails.owner?.name : "" }}
            </p>
          </div>
          <div class="shelf-details">
            <label class="shelf-details-label">
              Size Class
            </label>
            <p class="shelf-details-text outline">
              {{ shelfDetails.shelf_type?.size_class.name }}
            </p>
          </div>
          <div class="shelf-details">
            <label class="shelf-details-label">
              Created Date
            </label>
            <p class="shelf-details-text">
              {{ formatDateTime(shelfDetails.create_dt).date }}
            </p>
          </div>
        </div>
        <div class="col-12 q-pb-sm">
          <h1 class="text-h4 q-mb-xs-sm q-mb-sm-md">
            Shelf Dimensions
          </h1>

          <div class="shelf-details">
            <label class="shelf-details-label">
              Width
            </label>
            <p class="shelf-details-text">
              {{ shelfDetails.width }} in
            </p>
          </div>
          <div class="shelf-details">
            <label class="shelf-details-label">
              Height
            </label>
            <p class="shelf-details-text">
              {{ shelfDetails.height }} in
            </p>
          </div>
          <div class="shelf-details">
            <label class="shelf-details-label">
              Depth
            </label>
            <p class="shelf-details-text">
              {{ shelfDetails.depth }} in
            </p>
          </div>
        </div>
        <div class="col-12 q-pb-sm">
          <h1 class="text-h4 q-mb-xs-sm q-mb-sm-md">
            Shelf Capacity
          </h1>

          <div class="shelf-details">
            <label class="shelf-details-label">
              Available Quantity
            </label>
            <p class="shelf-details-text">
              {{ shelfDetails.available_space }}
            </p>
          </div>
          <div class="shelf-details">
            <label class="shelf-details-label">
              Used Quantity
            </label>
            <p class="shelf-details-text">
              {{ renderUsedCapacity() }}
            </p>
          </div>
          <div class="shelf-details">
            <label class="shelf-details-label">
              Max Quantity
            </label>
            <p class="shelf-details-text">
              {{ shelfDetails.shelf_type?.max_capacity }}
            </p>
          </div>
        </div>
        <div class="col-12 q-pb-sm">
          <h1 class="text-h4 q-mb-xs-sm q-mb-sm-md">
            Shelf Location
          </h1>

          <div class="shelf-details">
            <p
              v-if="renderShelfBuilding()"
              class="shelf-details-text outline q-mr-sm"
            >
              {{ renderShelfBuilding() }}
            </p>
            <p class="shelf-details-text outline">
              {{ renderShelfLocation(shelfDetails) }}
            </p>
          </div>
        </div>
      </template>
    </div>

    <div class="row q-mt-lg q-mb-xs-xl q-mb-sm-none">
      <div class="col-grow q-mb-xs-md q-mb-sm-none">
        <EssentialTable
          :table-columns="containerTableColumns"
          :table-visible-columns="containerTableVisibleColumns"
          :table-data="shelfContainers"
          :heading-row-class="'q-mb-xs-md q-mb-md-lg'"
          :heading-rearrange-class="'q-ml-auto'"
          :enable-pagination="true"
          :pagination-total="shelfContainersTotal"
          :pagination-loading="appIsLoadingData"
          @update-pagination="loadShelfContainers($event)"
          @selected-table-row="routeToItemDetail($event)"
        >
          <template #heading-row>
            <div
              class="col-sm-5 col-md-12 col-lg-auto"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? '' : 'self-center'"
            >
              <h1 class="text-h4 text-bold">
                Containers in Shelf
              </h1>
            </div>
          </template>
        </EssentialTable>
      </div>
    </div>
  </div>
</template>

<script setup>
import { inject, onMounted, ref, watch } from 'vue'
import { useRouter  } from 'vue-router'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { useRecordManagementStore } from '@/stores/record-management-store'
import { useGlobalStore } from '@/stores/global-store'
import { storeToRefs } from 'pinia'
import BarcodeBox from '@/components/BarcodeBox.vue'
import EssentialTable from '@/components/EssentialTable.vue'

const router = useRouter()

// Composables
const { currentScreenSize } = useCurrentScreenSize()

// Store Data
const { getShelfContainers } = useRecordManagementStore()
const {
  shelfDetails,
  shelfContainers,
  shelfContainersTotal
} = storeToRefs(useRecordManagementStore())
const { appIsLoadingData } = storeToRefs(useGlobalStore())

// Local Data
const containerTableVisibleColumns = ref([
  'barcode_value',
  'shelf_position_number'
])
const containerTableColumns = ref([
  {
    name: 'barcode_value',
    field: 'barcode_value',
    label: 'Barcode',
    align: 'left',
    sortable: true
  },
  {
    name: 'shelf_position_number',
    field: 'shelf_position_number',
    label: 'Position', // The column header text
    align: 'left',
    sortable: true
  }
])

// Logic
const formatDateTime = inject('format-date-time')
const handleAlert = inject('handle-alert')

onMounted(() => {
  loadShelfContainers()
})

// if user changes to another shelf while in the shelf display we need to make sure to load that shelves request history
watch(() => shelfDetails.value.barcode, () => {
  loadShelfContainers()
})

const renderShelfBuilding = () => {
  let building = ''
  if (shelfDetails.value.location) {
    building = shelfDetails.value.location.split('-')[0]
  }
  return building
}
const renderShelfLocation = () => {
  let module, aisle, side, ladder, shelf = ''
  if (shelfDetails.value.location) {
    const locationValues = shelfDetails.value.location.split('-')
    module = locationValues[1]
    aisle = locationValues[2]
    side = locationValues[3]
    ladder = locationValues[4]
    shelf = locationValues[5]
  }
  return `${module}-${aisle}-${side == 'Right' ? 'R' : side == 'Left' ? 'L' : side}-${ladder}-${shelf}`.replace('undefined-', '')
}
const renderUsedCapacity = () => {
  const usedSpace = shelfDetails.value.shelf_type?.max_capacity - shelfDetails.value.available_space
  return typeof usedSpace === 'number' && !Number.isNaN(usedSpace) ? usedSpace : 0
}

const routeToItemDetail = (item) => {
  if (item.type == 'tray') {
    router.push({
      name: 'record-management-tray',
      params: {
        barcode: item.barcode_value
      }
    })
  } else {
    router.push({
      name: 'record-management-items',
      params: {
        barcode: item.barcode_value
      }
    })
  }
}

const loadShelfContainers = async (qParams) => {
  try {
    appIsLoadingData.value = true
    await getShelfContainers({ ...qParams })
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
.shelf {
  &-details {
    position: relative;
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;
    width: 100%;
    margin-bottom: 1rem;

    @media (max-width: $breakpoint-sm-min) {
      margin-bottom: 8px;
    }

    &-label {
      width: 100%;

      @media (max-width: $breakpoint-sm-min) {
        width: initial;
        margin-right: 4px;
      }
    }

    &-text {
      min-width: 1px;
      min-height: 28px; // this offsets any text with outline/highlight classes

      @media (max-width: $breakpoint-sm-min) {
        min-height: initial;
      }
    }
  }
}
</style>
