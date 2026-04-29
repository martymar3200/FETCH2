<template>
  <div class="shelf">
    <!-- Header with title -->
    <div class="row">
      <div class="col">
        <h1 class="text-h4 text-bold q-mb-xs-md q-mb-sm-lg">
          Shelf Details
        </h1>
      </div>
    </div>

    <!-- Barcode and Location header -->
    <div class="barcode-header q-mb-lg">
      <span class="text-h4 text-bold">{{ shelfDetails.barcode?.value }}</span>
      <span class="text-h4 text-bold location-text">
        <template v-if="renderShelfBuilding()">{{ renderShelfBuilding() }} - </template>
        {{ renderShelfLocation() }}
      </span>
    </div>

    <!-- Sections Container -->
    <div class="row q-col-gutter-md q-mb-lg">
      <!-- General Section -->
      <div class="col-xs-12 col-sm-6 col-md-3">
        <div class="section-card">
          <h2 class="section-title text-h6 text-bold q-mb-md">
            General
          </h2>
          <div class="section-content">
            <div class="detail-row">
              <span class="detail-label">Owner</span>
              <span class="detail-value">{{ shelfDetails.owner?.name || '—' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Size</span>
              <span class="detail-value">{{ shelfDetails.shelf_type?.size_class?.name || '—' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Created Date</span>
              <span class="detail-value">{{ formatDateTime(shelfDetails.create_dt).date || '—' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Shelf Type</span>
              <span class="detail-value">{{ shelfDetails.shelf_type?.name || '—' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Dimensions Section -->
      <div class="col-xs-12 col-sm-6 col-md-3">
        <div class="section-card">
          <h2 class="section-title text-h6 text-bold q-mb-md">
            Dimensions
          </h2>
          <div class="section-content">
            <div class="detail-row">
              <span class="detail-label">Width</span>
              <span class="detail-value">{{ shelfDetails.width }} in</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Height</span>
              <span class="detail-value">{{ shelfDetails.height }} in</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Depth</span>
              <span class="detail-value">{{ shelfDetails.depth }} in</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Available Space Section -->
      <div class="col-xs-12 col-sm-6 col-md-3">
        <div class="section-card">
          <h2 class="section-title text-h6 text-bold q-mb-md">
            Available Space
          </h2>
          <div class="section-content">
            <div class="detail-row">
              <span class="detail-label">Max Quantity</span>
              <span class="detail-value">{{ shelfDetails.shelf_type?.max_capacity || '—' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Used Quantity</span>
              <span class="detail-value">{{ renderUsedCapacity() }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Available Quantity</span>
              <span class="detail-value">{{ shelfDetails.available_space ?? '—' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- History Section -->
      <div class="col-xs-12 col-sm-6 col-md-3">
        <div class="section-card">
          <h2 class="section-title text-h6 text-bold q-mb-md">
            History
          </h2>
          <div
            class="section-content column flex-center"
            style="height: calc(100% - 60px);"
          >
            <BaseButton
              outline
              color="primary"
              label="View Full History"
              no-caps
              @click="showAuditTrailModal = true"
            />
            <div class="text-caption text-grey-7 q-mt-sm text-center">
              Comprehensive audit log for this shelf.
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Containers in Shelf Table -->
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

    <AuditTrail
      v-if="showAuditTrailModal"
      @reset="showAuditTrailModal = false"
      entity-type="shelves"
      :entity-id="shelfDetails.id"
    />
  </div>
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
import { inject, onMounted, ref, watch } from 'vue'
import { notify } from '@/utils/notify'
import { useRouter } from 'vue-router'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { useRecordManagementStore } from '@/stores/record-management-store'
import { useGlobalStore } from '@/stores/global-store'
import { storeToRefs } from 'pinia'
import EssentialTable from '@/components/EssentialTable.vue'
import AuditTrail from '@/components/AuditTrail.vue'

const router = useRouter()
const { currentScreenSize } = useCurrentScreenSize()
const showAuditTrailModal = ref(false)

// Store Data
const { getShelfContainers } = useRecordManagementStore()
const {
  shelfDetails,
  shelfContainers,
  shelfContainersTotal
} = storeToRefs(useRecordManagementStore())
const { appIsLoadingData } = storeToRefs(useGlobalStore())

// Table Configuration
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
    label: 'Position',
    align: 'left',
    sortable: true
  }
])

// Injected Helper Functions
const formatDateTime = inject('format-date-time')


onMounted(() => {
  loadShelfContainers()
})

watch(() => shelfDetails.value.barcode, () => {
  loadShelfContainers()
})

// Helper Methods
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

// Navigation
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
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to load shelf containers'
    })
  } finally {
    appIsLoadingData.value = false
  }
}
</script>

<style lang="scss" scoped>
.barcode-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 2px solid $primary;
}

.location-text {
  margin-left: auto;
  color: rgba(0, 0, 0, 0.7);
}

.section-card {
  background: $color-white;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  padding: 1.25rem;
  height: 100%;
}

.section-title {
  color: $primary;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  padding-bottom: 0.5rem;
  margin-bottom: 1rem;
}

.section-content {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.25rem 0;
}

.detail-label {
  font-weight: 500;
  color: rgba(0, 0, 0, 0.7);
  flex-shrink: 0;
}

.detail-value {
  text-align: right;
  color: rgba(0, 0, 0, 0.87);
}
</style>
