<template>
  <div class="item">
    <!-- Header with title and menu -->
    <div class="row items-center">
      <div class="col-auto">
        <MoreOptionsMenu
          :options="[
            {
              text: 'Edit Item',
              hidden: isEditHidden
            }
          ]"
          class="q-mr-sm"
          @click="handleOptionMenu"
        />
      </div>
      <div class="col-auto">
        <h1 class="text-h4 text-bold q-mb-xs-md q-mb-sm-lg">
          {{ itemDetails.tray ? 'Tray Item Details' : 'Non-Tray Item Details' }}
        </h1>
      </div>
    </div>

    <!-- Barcode and Status header -->
    <div class="barcode-header q-mb-lg">
      <span class="text-h4 text-bold">{{ itemDetails.barcode?.value }}</span>
      <span
        class="text-h4 text-bold status-text"
        :class="itemDetails.status === 'Out' ? 'text-negative' : 'text-positive'"
      >
        {{ itemDetails.status || '' }}
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
              <span class="detail-value">{{ itemDetails.owner?.name || '—' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Size</span>
              <span class="detail-value">{{ itemDetails.size_class?.name || '—' }}</span>
            </div>

            <div class="detail-row">
              <span class="detail-label">Media Type</span>
              <span class="detail-value">{{ itemDetails.media_type?.name || '—' }}</span>
            </div>
            <!-- Tray Barcode - only show for tray items -->
            <div
              v-if="itemDetails.tray"
              class="detail-row"
            >
              <span class="detail-label">Tray Barcode</span>
              <EssentialLink
                :title="itemDetails.tray.barcode?.value || '—'"
                @click="routeToTrayDetail(itemDetails.tray.barcode?.value)"
                :disabled="!itemDetails.tray.barcode?.value"
                dense
                class="detail-value q-pa-none"
              />
            </div>
            <div class="detail-row">
              <span class="detail-label">Shelf Barcode</span>
              <EssentialLink
                v-if="renderShelfBarcode()"
                :title="renderShelfBarcode()"
                @click="routeToShelfDetail()"
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
                <template v-if="renderItemBuilding()">{{ renderItemBuilding() }} - </template>
                {{ getItemLocation(itemDetails.tray ?? itemDetails) || '—' }}
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
              <span class="history-date">{{ formatDateTime(itemDetails.accession_dt).date || '—' }}</span>
              <span class="history-job">
                <EssentialLink
                  v-if="itemDetails.accession_job_id"
                  :title="`#${itemDetails.accession_job_id}`"
                  @click="routeToAccessionJob(itemDetails.accession_job_id)"
                  dense
                  class="q-pa-none"
                />
                <template v-else>—</template>
              </span>
            </div>
            <!-- Verification Job -->
            <div class="history-row">
              <span class="history-label">Verification Job</span>
              <span class="history-date">{{ formatDateTime(itemDetails.verification_job?.update_dt).date || '—' }}</span>
              <span class="history-job">
                <EssentialLink
                  v-if="itemDetails.verification_job?.workflow_id"
                  :title="`#${itemDetails.verification_job.workflow_id}`"
                  @click="routeToVerificationJob(itemDetails.verification_job_id)"
                  dense
                  class="q-pa-none"
                />
                <template v-else>—</template>
              </span>
            </div>
            <!-- Shelving Job -->
            <div class="history-row">
              <span class="history-label">Shelving Job</span>
              <span class="history-date">{{ formatDateTime(getShelvingDate()).date || '—' }}</span>
              <span class="history-job">
                <EssentialLink
                  v-if="getShelvingJobId()"
                  :title="`#${getShelvingJobId()}`"
                  @click="routeToShelvingJob(getShelvingJobId())"
                  dense
                  class="q-pa-none"
                />
                <template v-else>—</template>
              </span>
            </div>
            <!-- Last Requested -->
            <div class="history-row">
              <span class="history-label">Last Requested</span>
              <span class="history-date">{{ formatDateTime(itemDetails.last_requested_dt).date || '—' }}</span>
              <span class="history-job">
                <EssentialLink
                  v-if="itemDetails.last_request_id"
                  :title="`#${itemDetails.last_request_id}`"
                  @click="routeToRequestDetail({ id: itemDetails.last_request_id })"
                  dense
                  class="q-pa-none"
                />
                <template v-else>—</template>
              </span>
            </div>
            <!-- Last Refiled -->
            <div class="history-row">
              <span class="history-label">Last Refiled</span>
              <span class="history-date">{{ formatDateTime(itemDetails.last_refiled_dt).date || '—' }}</span>
              <span class="history-job">
                <EssentialLink
                  v-if="itemDetails.last_refile_job_id"
                  :title="`#${itemDetails.last_refile_job_id}`"
                  @click="routeToRefileJob(itemDetails.last_refile_job_id)"
                  dense
                  class="q-pa-none"
                />
                <template v-else>—</template>
              </span>
            </div>
            <!-- Withdraw Job -->
            <div class="history-row">
              <span class="history-label">Withdraw Job</span>
              <span class="history-date">{{ formatDateTime(itemDetails.withdrawal_dt).date || '—' }}</span>
              <span class="history-job">
                <EssentialLink
                  v-if="itemDetails.last_withdraw_job_id"
                  :title="`#${itemDetails.last_withdraw_job_id}`"
                  @click="routeToWithdrawalJob(itemDetails.last_withdraw_job_id)"
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

    <!-- Request History Table -->
    <div class="row q-mt-lg q-mb-xs-xl q-mb-sm-none">
      <div class="col-grow q-mb-xs-md q-mb-sm-none">
        <EssentialTable
          :table-columns="itemTableColumns"
          :table-visible-columns="itemTableVisibleColumns"
          :table-data="itemRequestHistory"
          :heading-row-class="'q-mb-xs-md q-mb-md-lg'"
          :heading-rearrange-class="'q-ml-auto'"
          :enable-pagination="true"
          :pagination-total="itemRequestHistoryTotal"
          :pagination-loading="appIsLoadingData"
          @update-pagination="loadRequestHistory($event)"
          @selected-table-row="routeToRequestDetail($event)"
        >
          <template #heading-row>
            <div
              class="col-sm-5 col-md-12 col-lg-auto"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? '' : 'self-center'"
            >
              <h1 class="text-h4 text-bold">
                Request History
              </h1>
            </div>
          </template>

          <template #table-td="{ colName, value }">
            <span
              v-if="colName == 'create_dt'"
              class=""
            >
              {{ formatDateTime(value).date }}
            </span>
          </template>
        </EssentialTable>
      </div>
    </div>

    <EditNonTrayItemModal
      v-if="isEditModalVisible"
      :item-data="itemDetails"
      @success="handleEditSuccess"
      @hide="isEditModalVisible = false"
    />

    <AuditTrail
      v-if="showAuditTrailModal"
      @reset="showAuditTrailModal = false"
      :entity-type="itemDetails.container_type?.type === 'Non-Tray' ? 'non_tray_items' : 'items'"
      :entity-id="itemDetails.id"
    />
  </div>
</template>

<script setup>
import { inject, onMounted, ref, watch, computed } from 'vue'
import { Notify } from 'quasar'
import { useRouter } from 'vue-router'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { useRecordManagementStore } from '@/stores/record-management-store'
import { useGlobalStore } from '@/stores/global-store'
import { useUserStore } from '@/stores/user-store'
import { storeToRefs } from 'pinia'
import EssentialTable from '@/components/EssentialTable.vue'
import EssentialLink from '@/components/EssentialLink.vue'
import MoreOptionsMenu from '@/components/MoreOptionsMenu.vue'
import EditNonTrayItemModal from '@/components/EditNonTrayItemModal.vue'
import AuditTrail from '@/components/AuditTrail.vue'

const router = useRouter()
const { currentScreenSize } = useCurrentScreenSize()

// Store Data
const { getItemRequestHistory } = useRecordManagementStore()
const {
  itemDetails,
  itemRequestHistory,
  itemRequestHistoryTotal
} = storeToRefs(useRecordManagementStore())
const { appIsLoadingData } = storeToRefs(useGlobalStore())
const userStore = useUserStore()
const { canEditNonTrayItem } = storeToRefs(userStore)

// Edit & History Modal Logic
const isEditModalVisible = ref(false)
const showAuditTrailModal = ref(false)

const isEditHidden = computed(() => {
  if (!itemDetails.value?.id) {
    return true
  }
  const isNonTrayItem = itemDetails.value.container_type?.type === 'Non-Tray'
  const isShelved = itemDetails.value.shelf_position_id !== null
  const hasPermission = canEditNonTrayItem.value
  return !isNonTrayItem || isShelved || !hasPermission
})

const openEditModal = () => {
  isEditModalVisible.value = true
}

const handleEditSuccess = () => {
  isEditModalVisible.value = false
  Notify.create({
    type: 'positive',
    message: 'Non-Tray Item updated successfully.'
  })
}

const handleOptionMenu = (selectedOption) => {
  if (selectedOption.text === 'Edit Item') {
    openEditModal()
  }
}

// Request History Logic
const itemTableVisibleColumns = ref([
  'id',
  'external_request_id',
  'create_dt'
])
const itemTableColumns = ref([
  {
    name: 'id',
    field: 'id',
    label: 'Request ID',
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
    name: 'create_dt',
    field: 'create_dt',
    label: 'Request Date',
    align: 'left',
    sortable: true
  }
])

const formatDateTime = inject('format-date-time')
const getItemLocation = inject('get-item-location')


onMounted(() => {
  loadRequestHistory()
})
watch(() => itemDetails.value.barcode, () => {
  loadRequestHistory()
})

// Helper Methods
const renderShelfBarcode = () => {
  let barcode = ''
  if (itemDetails.value.tray && itemDetails.value.tray.shelf_position) {
    barcode = itemDetails.value.tray.shelf_position.shelf?.barcode.value
  } else if (itemDetails.value.shelf_position) {
    barcode = itemDetails.value.shelf_position.shelf.barcode.value
  }
  return barcode
}

const renderItemBuilding = () => {
  let building = ''
  if (itemDetails.value.tray && itemDetails.value.tray.shelf_position) {
    building = itemDetails.value.tray.shelf_position.location?.split('-')[0]
  } else if (itemDetails.value.shelf_position) {
    building = itemDetails.value.shelf_position.location?.split('-')[0]
  }
  return building
}

// Shelving helpers for tray vs non-tray items
const getShelvingDate = () => {
  if (itemDetails.value.tray) {
    // For tray items, use tray's shelving_job update_dt or shelved_dt as fallback
    return itemDetails.value.tray.shelving_job?.update_dt || itemDetails.value.tray.shelved_dt
  }
  // For non-tray items
  return itemDetails.value.shelving_job?.update_dt || itemDetails.value.shelved_dt
}

const getShelvingJobId = () => {
  if (itemDetails.value.tray) {
    // For tray items, use tray's shelving_job_id
    return itemDetails.value.tray.shelving_job_id
  }
  // For non-tray items
  return itemDetails.value.shelving_job_id
}


// Navigation Methods

const routeToTrayDetail = (barcode) => {
  router.push({
    name: 'record-management-tray',
    params: { barcode }
  })
}

const routeToShelfDetail = () => {
  router.push({
    name: 'record-management-shelf',
    params: {
      barcode: renderShelfBarcode()
    }
  })
}

const routeToRequestDetail = (request) => {
  router.push({
    name: 'request-details',
    params: { jobId: request.id }
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
  // Get origin from tray's shelving job (for tray items) or item's shelving job (for non-tray items)
  const origin = itemDetails.value.tray?.shelving_job?.origin || itemDetails.value.shelving_job?.origin
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

const routeToRefileJob = (jobId) => {
  router.push({
    name: 'refile',
    params: { jobId }
  })
}

const routeToWithdrawalJob = (jobId) => {
  router.push({
    name: 'withdrawal',
    params: { jobId }
  })
}

const loadRequestHistory = async (qParams) => {
  try {
    appIsLoadingData.value = true
    if (itemDetails.value && itemDetails.value.container_type?.type === 'Non-Tray') {
      await getItemRequestHistory({
        ...qParams,
        non_tray_item_barcode: itemDetails.value.barcode.value
      })
    } else {
      await getItemRequestHistory({
        ...qParams,
        item_barcode: itemDetails.value.barcode.value
      })
    }
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to load request history'
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

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-weight: 500;
}

.status-in {
  background-color: rgba($positive, 0.15);
  color: $positive;
}

.status-out {
  background-color: rgba($negative, 0.15);
  color: $negative;
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

.history-header {
  font-weight: 600;
  color: rgba(0, 0, 0, 0.6);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  padding-bottom: 0.5rem;
  margin-bottom: 0.25rem;
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