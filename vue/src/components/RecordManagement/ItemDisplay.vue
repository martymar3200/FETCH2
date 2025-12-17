<template>
  <div class="item">
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

    <!-- Item details sections -->
    <div class="row">
      <!-- ... (All your item details sections are unchanged) ... -->
      <div class="col-xs-12 col-lg-4 q-pr-xs-none q-pr-lg-md q-pb-xs-md q-pb-lg-none">
        <BarcodeBox
          :barcode="itemDetails.barcode.value"
          :class="itemDetails.status == 'Out' ? 'bg-color-pink text-negative' : 'bg-color-green-light text-positive'"
          class="q-py-xs-sm q-py-sm-md"
        />
      </div>
      <template v-if="currentScreenSize !== 'xs'">
        <div class="col-sm-4 col-lg-3">
          <div class="column no-wrap">
            <div class="item-details">
              <label class="item-details-label text-h6">
                Tray Barcode
              </label>
              <EssentialLink
                :title="itemDetails.tray ? itemDetails.tray.barcode.value : 'N/A'"
                @click="routeToTrayDetail(itemDetails.tray.barcode.value)"
                :disabled="!itemDetails.tray"
                dense
                class="item-details-text q-pa-none"
              />
            </div>
            <div class="item-details">
              <label class="item-details-label text-h6">
                Shelf Barcode
              </label>
              <EssentialLink
                :title="renderShelfBarcode()"
                @click="routeToShelfDetail()"
                :disabled="!renderShelfBarcode()"
                dense
                class="item-details-text q-pa-none"
              />
            </div>
            <div class="item-details">
              <label class="item-details-label text-h6">
                Owner
              </label>
              <p class="item-details-text">
                {{ itemDetails.owner?.name ? itemDetails.owner?.name : "" }}
              </p>
            </div>
            <div class="item-details">
              <label class="item-details-label text-h6">
                Status
              </label>
              <p
                class="item-details-text outline"
                :class="itemDetails.status == 'Out' ? 'text-highlight-negative' : 'text-highlight' "
              >
                {{ itemDetails.status }}
              </p>
            </div>
          </div>
        </div>
        <div
          class="col-sm-4 col-lg-3"
        >
          <div class="column no-wrap">
            <div class="item-details">
              <label class="item-details-label text-h6">
                Media Type
              </label>
              <p class="item-details-text text-highlight outline">
                {{ itemDetails.media_type.name }}
              </p>
            </div>
            <div class="item-details">
              <label class="item-details-label text-h6">
                Size Class
              </label>
              <p class="item-details-text text-highlight outline">
                {{ itemDetails.size_class.name }}
              </p>
            </div>
            <div class="item-details">
              <label class="item-details-label text-h6">
                Accession Date
              </label>
              <p class="item-details-text">
                {{ formatDateTime(itemDetails.accession_dt).date }}
              </p>
            </div>
            <div class="item-details">
              <label class="item-details-label text-h6">
                Shelved Date:
              </label>
              <p class="item-details-text">
                {{ formatDateTime(itemDetails.tray ? itemDetails.tray.shelving_job?.update_dt : itemDetails.shelving_job?.update_dt).date }}
              </p>
            </div>
          </div>
        </div>
        <div class="col-sm-4 col-lg-2">
          <div class="column no-wrap">
            <div class="item-details">
              <label class="item-details-label text-h6">
                Last Requested Date:
              </label>
              <p class="item-details-text">
                {{ formatDateTime(itemDetails.last_requested_dt).date }}
              </p>
            </div>
            <div class="item-details">
              <label class="item-details-label text-h6">
                Last Refile Date:
              </label>
              <p class="item-details-text">
                {{ formatDateTime(itemDetails.last_refiled_dt).date }}
              </p>
            </div>
            <div class="item-details">
              <label class="item-details-label text-h6">
                Withdrawal Date
              </label>
              <p class="item-details-text">
                {{ formatDateTime(itemDetails.withdrawal_dt).date }}
              </p>
            </div>
            <div class="item-details">
              <label class="item-details-label text-h6">
                Location
              </label>
              <p
                v-if="renderItemBuilding()"
                class="item-details-text outline q-mr-sm"
              >
                {{ renderItemBuilding() }}
              </p>
              <p class="item-details-text outline">
                {{ getItemLocation(itemDetails.tray ?? itemDetails) }}
              </p>
            </div>
          </div>
        </div>
      </template>
      <template v-else>
        <!-- Mobile view code is unchanged -->
      </template>
    </div>

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
  </div>
</template>

<script setup>
import { inject, onMounted, ref, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { useRecordManagementStore } from '@/stores/record-management-store'
import { useGlobalStore } from '@/stores/global-store'
import { useUserStore } from '@/stores/user-store'
import { storeToRefs } from 'pinia'
import BarcodeBox from '@/components/BarcodeBox.vue'
import EssentialTable from '@/components/EssentialTable.vue'
import EssentialLink from '@/components/EssentialLink.vue'
import MoreOptionsMenu from '@/components/MoreOptionsMenu.vue'
import EditNonTrayItemModal from '@/components/EditNonTrayItemModal.vue'

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

// New Edit Logic
const isEditModalVisible = ref(false)

// ======================================================
// ========= START: COMPUTED PROPERTY WITH LOGGING ======
// ======================================================
const isEditHidden = computed(() => {
  // console.log statements can be removed now, but I'll leave them for verification
  console.clear()
  console.log('--- Checking "Edit Item" Button Visibility ---')

  if (!itemDetails.value?.id) {
    console.log('Result: HIDDEN (Reason: Item details not loaded)')
    return true
  }

  // We now check the container_type property, which is the reliable way.
  const isNonTrayItem = itemDetails.value.container_type?.type === 'Non-Tray'
  // ======================================================

  const isShelved = itemDetails.value.shelf_position_id !== null
  const hasPermission = canEditNonTrayItem.value

  console.log(`1. Is it a Non-Tray Item?            --> ${isNonTrayItem}`)
  console.log(`2. Is it already shelved?              --> ${isShelved}`)
  console.log(`3. Does the user have permission?    --> ${hasPermission}`)
  console.log('User Permissions List:', userStore.userData.permissions)

  const finalDecision = !isNonTrayItem || isShelved || !hasPermission
  console.log(`%cFinal decision (isEditHidden): ${finalDecision}`, 'font-weight: bold;')
  console.log('-------------------------------------------')

  return finalDecision
})
// ======================================================
// ========== END: COMPUTED PROPERTY WITH LOGGING =======
// ======================================================

const openEditModal = () => {
  isEditModalVisible.value = true
}

const handleEditSuccess = () => {
  isEditModalVisible.value = false
  handleAlert({
    type: 'success',
    text: 'Non-Tray Item updated successfully.',
    autoClose: true
  })
}

const handleOptionMenu = (selectedOption) => {
  if (selectedOption.text === 'Edit Item') {
    openEditModal()
  }
}

// Request History Logic (Unchanged)
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
const handleAlert = inject('handle-alert')

onMounted(() => {
  loadRequestHistory()
})
watch(() => itemDetails.value.barcode, () => {
  loadRequestHistory()
})

// Other component methods (unchanged)
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
/* styles are unchanged */
</style>