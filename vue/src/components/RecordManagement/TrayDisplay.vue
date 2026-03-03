<template>
  <div class="tray">
    <!-- Header with title and menu -->
    <div class="row items-center">
      <div class="col-auto">
        <MoreOptionsMenu
          :options="[
            {
              text: 'Edit Tray',
              hidden: isEditHidden
            }
          ]"
          class="q-mr-sm"
          @click="handleOptionMenu"
        />
      </div>
      <div class="col-auto">
        <h1 class="text-h4 text-bold q-mb-xs-md q-mb-sm-lg">
          Tray Details
        </h1>
      </div>
    </div>

    <!-- Barcode and Status header -->
    <div class="barcode-header q-mb-lg">
      <span class="text-h4 text-bold">{{ trayDetails.barcode?.value }}</span>
      <span
        class="text-h4 text-bold status-text"
        :class="trayDetails.status === 'Out' ? 'text-negative' : 'text-positive'"
      >
        {{ trayDetails.status || '' }}
      </span>
    </div>

    <!-- Sections Container -->
    <div class="row q-col-gutter-md q-mb-lg">
      <!-- General Section -->
      <div class="col-xs-12 col-md-6">
        <div class="section-card">
          <h2 class="section-title text-h6 text-bold q-mb-md">
            General
          </h2>
          <div class="section-content">
            <div class="detail-row">
              <span class="detail-label">Owner</span>
              <span class="detail-value">{{ trayDetails.owner?.name || '—' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Size</span>
              <span class="detail-value">{{ trayDetails.size_class?.name || '—' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Media Type</span>
              <span class="detail-value">{{ trayDetails.media_type?.name || '—' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Shelf Barcode</span>
              <EssentialLink
                v-if="trayDetails.shelf_position?.shelf?.barcode?.value"
                :title="trayDetails.shelf_position.shelf.barcode.value"
                @click="routeToShelfDetail(trayDetails.shelf_position.shelf.barcode.value)"
                dense
                class="detail-value q-pa-none"
              />
              <span
                v-else
                class="detail-value"
              >—</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Location</span>
              <span class="detail-value">
                <template v-if="renderTrayBuilding()">{{ renderTrayBuilding() }} - </template>
                {{ getItemLocation(trayDetails) || '—' }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- History Section -->
      <div class="col-xs-12 col-md-6">
        <div class="section-card">
          <div
            class="row items-center justify-between q-mb-md"
            style="border-bottom: 1px solid rgba(0, 0, 0, 0.1); padding-bottom: 0.5rem; margin-bottom: 1rem;"
          >
            <h2
              class="text-h6 text-bold text-primary q-ma-none"
              style="line-height: normal;"
            >
              History
            </h2>
            <q-btn
              flat
              dense
              color="primary"
              label="View Full History"
              no-caps
              size="sm"
              @click="showAuditTrailModal = true"
            />
          </div>
          <div class="section-content">
            <!-- Accession Job -->
            <div class="history-row">
              <span class="history-label">Accession Job</span>
              <span class="history-date">{{ formatDateTime(trayDetails.accession_dt).date || '—' }}</span>
              <span class="history-job">
                <EssentialLink
                  v-if="trayDetails.accession_job_id"
                  :title="`#${trayDetails.accession_job_id}`"
                  @click="routeToAccessionJob(trayDetails.accession_job_id)"
                  dense
                  class="q-pa-none"
                />
                <template v-else>—</template>
              </span>
            </div>
            <!-- Verification Job -->
            <div class="history-row">
              <span class="history-label">Verification Job</span>
              <span class="history-date">{{ formatDateTime(trayDetails.verification_job?.update_dt).date || '—' }}</span>
              <span class="history-job">
                <EssentialLink
                  v-if="trayDetails.verification_job?.workflow_id"
                  :title="`#${trayDetails.verification_job.workflow_id}`"
                  @click="routeToVerificationJob(trayDetails.verification_job_id)"
                  dense
                  class="q-pa-none"
                />
                <template v-else>—</template>
              </span>
            </div>
            <!-- Shelving Job -->
            <div class="history-row">
              <span class="history-label">Shelving Job</span>
              <span class="history-date">{{ formatDateTime(trayDetails.shelving_job?.update_dt || trayDetails.shelved_dt).date || '—' }}</span>
              <span class="history-job">
                <EssentialLink
                  v-if="trayDetails.shelving_job_id"
                  :title="`#${trayDetails.shelving_job_id}`"
                  @click="routeToShelvingJob(trayDetails.shelving_job_id)"
                  dense
                  class="q-pa-none"
                />
                <template v-else>—</template>
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Items in Tray Table -->
    <div class="row q-mt-lg q-mb-xs-xl q-mb-sm-none">
      <div class="col-grow q-mb-xs-md q-mb-sm-none">
        <EssentialTable
          :table-columns="itemTableColumns"
          :table-visible-columns="itemTableVisibleColumns"
          :table-data="trayDetails.items"
          :heading-row-class="'q-mb-xs-md q-mb-md-lg'"
          :heading-rearrange-class="'q-ml-auto'"
          @selected-table-row="routeToItemDetail($event)"
        >
          <template #heading-row>
            <div
              class="col-sm-5 col-md-12 col-lg-auto"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? '' : 'self-center'"
            >
              <h1 class="text-h4 text-bold">
                Items in Tray
              </h1>
            </div>
          </template>
          <template #table-td="{ colName, value }">
            <span
              v-if="colName == 'status'"
              class="outline text-nowrap"
              :class="value == 'In' ? 'text-highlight' : value == 'Out' ? 'text-highlight-negative' : 'text-highlight-warning'"
            >
              {{ value }}
            </span>
          </template>
        </EssentialTable>
      </div>
    </div>

    <EditTrayModal
      v-if="isEditModalVisible"
      :tray-data="trayDetails"
      @success="handleEditSuccess"
      @hide="isEditModalVisible = false"
    />

    <AuditTrail
      v-if="showAuditTrailModal"
      @reset="showAuditTrailModal = false"
      entity-type="trays"
      :entity-id="trayDetails.id"
    />
  </div>
</template>

<script setup>
import { ref, inject, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { useRecordManagementStore } from '@/stores/record-management-store'
import { useUserStore } from '@/stores/user-store'
import { storeToRefs } from 'pinia'
import EssentialTable from 'src/components/EssentialTable.vue'
import EssentialLink from '@/components/EssentialLink.vue'
import MoreOptionsMenu from '@/components/MoreOptionsMenu.vue'
import EditTrayModal from '@/components/EditTrayModal.vue'
import AuditTrail from '@/components/AuditTrail.vue'

const router = useRouter()
const { currentScreenSize } = useCurrentScreenSize()

// Store Setup
const recordManagementStore = useRecordManagementStore()
const { trayDetails } = storeToRefs(recordManagementStore)
const userStore = useUserStore()

// Computed Property for Edit Button Visibility
const { canEditTray } = storeToRefs(userStore)
const isEditHidden = computed(() => {
  if (!trayDetails.value?.id) {
    return true
  }
  const isShelved = trayDetails.value.shelf_position_id !== null
  const hasNoPermission = !canEditTray.value
  return isShelved || hasNoPermission
})

// Modal Control
const isEditModalVisible = ref(false)
const showAuditTrailModal = ref(false)

const openEditTrayModal = () => {
  isEditModalVisible.value = true
}

const handleEditSuccess = () => {
  isEditModalVisible.value = false
}

// Table Configuration
const itemTableVisibleColumns = ref([
  'barcode_value',
  'status'
])
const itemTableColumns = ref([
  {
    name: 'barcode_value',
    field: (row) => renderItemBarcodeDisplay(row),
    label: 'Barcode',
    align: 'left',
    sortable: true
  },
  {
    name: 'status',
    field: 'status',
    label: 'Status',
    align: 'left',
    sortable: true
  }
])

// Injected Helper Functions
const formatDateTime = inject('format-date-time')
const getItemLocation = inject('get-item-location')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')

// Helper Methods
const renderTrayBuilding = () => {
  let building = ''
  if (trayDetails.value && trayDetails.value.shelf_position) {
    building = trayDetails.value.shelf_position.location?.split('-')[0]
  }
  return building
}

// Navigation Methods
const routeToItemDetail = (rowData) => {
  router.push({
    name: 'record-management-items',
    params: {
      barcode: rowData.barcode.value
    }
  })
}

const routeToShelfDetail = (barcode) => {
  router.push({
    name: 'record-management-shelf',
    params: {
      barcode
    }
  })
}

const routeToAccessionJob = (jobId) => {
  router.push({
    name: 'accession',
    params: { jobId }
  })
}

const routeToVerificationJob = (jobId) => {
  router.push({
    name: 'verification',
    params: { jobId }
  })
}

const routeToShelvingJob = (jobId) => {
  // Route to appropriate shelving page based on origin
  const origin = trayDetails.value.shelving_job?.origin
  if (origin === 'Direct') {
    router.push({
      name: 'shelving-dts',
      params: { jobId }
    })
  } else if (origin === 'List') {
    router.push({
      name: 'ShelveByListExecute',
      params: { jobId }
    })
  } else {
    // Verification origin jobs are deprecated - redirect to dashboard
    router.push({ name: 'shelving' })
  }
}

const handleOptionMenu = (selectedOption) => {
  if (selectedOption.text === 'Edit Tray') {
    openEditTrayModal()
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

.status-text {
  margin-left: auto;
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

// History section 3-column layout
.history-row {
  display: grid;
  grid-template-columns: 1fr 1fr auto;
  gap: 1rem;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);

  &:last-child {
    border-bottom: none;
  }
}

.history-label {
  font-weight: 500;
  color: rgba(0, 0, 0, 0.7);
}

.history-date {
  text-align: center;
  color: rgba(0, 0, 0, 0.87);
}

.history-job {
  text-align: right;
  min-width: 60px;
}
</style>