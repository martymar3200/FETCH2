<template>
  <InfoDisplayLayout class="direct-shelving-job">
    <template #number-box-content>
      <h1
        class="info-display-details-label text-h4 text-bold"
      >
        {{ route.params.type == 'tray-item' ? 'Tray Barcode:' : 'Shelf Barcode:' }}
      </h1>
      <p class="info-display-number-box text-h4 q-pa-md">
        {{ renderBarcodeDisplay }}
      </p>
    </template>

    <template #details-content>
      <div class="col-xs-6 col-sm-6 col-md-grow">
        <div class="info-display-details q-mb-xs-none q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-lg">
          <label
            class="info-display-details-label-2 text-h6 text-bold"
          >
            Owner:
          </label>
          <p class="text-body1">
            {{ moveShelfJob.owner.name }}
          </p>
        </div>
      </div>

      <div class="col-xs-6 col-sm-6 col-md-grow">
        <div class="info-display-details q-mb-xs-none q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-lg">
          <label
            class="info-display-details-label-2 text-h6 text-bold"
          >
            Size Class:
          </label>
          <p
            class="text-body1"
            :class="moveShelfJob.size_class.name ? 'outline' : null"
          >
            {{ moveShelfJob.size_class.name }}
          </p>
        </div>
      </div>

      <div class="col-xs-6 col-sm-6 col-md-grow">
        <div class="info-display-details q-mb-xs-md q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-lg">
          <label
            class="info-display-details-label-2 text-h6 text-bold"
          >
            Assigned User:
          </label>
          <p class="text-body1">
            {{ moveShelfJob.user.name }}
          </p>
        </div>
      </div>

      <div class="col-xs-6 col-sm-grow">
        <div class="info-display-details q-mb-xs-none q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-sm">
          <label
            class="info-display-details-label-2 text-h6 text-bold"
          >
            Date Transferred:
          </label>
          <p class="text-body1">
            {{ formatDateTime(moveShelfJob.move_dt).date }}
          </p>
        </div>
      </div>

      <div
        v-if="currentScreenSize !== 'xs'"
        class="col-sm-12 col-md-12 col-lg-3 q-ml-auto"
      >
        <div class="info-display-details-action q-mt-sm-sm q-mt-md-md">
          <q-btn
            no-caps
            unelevated
            color="positive"
            label="Complete Transfer"
            class="btn-no-wrap text-body1 q-mr-sm"
            :disabled="appIsOffline || appPendingSync || moveShelfJob.containers.length == 0 || !checkUserPermission('can_move_trays_and_items_shelving_locations')"
            :loading="appActionIsLoadingData"
            @click="route.params.type == 'tray-non-tray' ? completeMoveShelfLocations() : completeMoveTrayItem()"
          />
          <q-btn
            v-if="!moveShelfJob.shelf_barcode && !moveShelfJob.tray_barcode"
            no-caps
            unelevated
            color="negative"
            label="Cancel Transfer"
            :disabled="!checkUserPermission('can_move_trays_and_items_shelving_locations')"
            class="btn-no-wrap text-body1"
            @click="cancelTransfer()"
          />
          <q-btn
            v-else
            no-caps
            unelevated
            color="accent"
            :label="route.params.type == 'tray-non-tray' ? 'Scan New Shelf' : 'Scan New Tray'"
            :disabled="!checkUserPermission('can_move_trays_and_items_shelving_locations')"
            class="btn-no-wrap text-body1 q-mr-sm"
            @click="clearMoveDetails()"
          />
        </div>
      </div>
      <MobileActionBar
        v-else-if="currentScreenSize == 'xs'"
        button-one-color="positive"
        button-one-label="Complete Transfer"
        :button-one-outline="false"
        :button-one-disabled="appIsOffline || appPendingSync || moveShelfJob.containers.length == 0 || !checkUserPermission('can_move_trays_and_items_shelving_locations')"
        @button-one-click="route.params.type == 'tray-non-tray' ? completeMoveShelfLocations() : completeMoveTrayItem()"
        button-two-color="negative"
        :button-two-label="!moveShelfJob.shelf_barcode && !moveShelfJob.tray_barcode? 'Cancel Transfer' : moveShelfJob.shelf_barcode ? 'Scan New Shelf' : 'Scan New Tray'"
        :button-two-outline="false"
        :button-two-disabled="!checkUserPermission('can_move_trays_and_items_shelving_locations')"
        :button-two-loading="appActionIsLoadingData"
        @button-two-click="!moveShelfJob.shelf_barcode && !moveShelfJob.tray_barcode ? cancelTransfer() : clearMoveDetails()"
      />
    </template>

    <template #table-content>
      <EssentialTable
        v-if="moveShelfJob.shelf_barcode || moveShelfJob.tray_barcode || moveShelfJob.containers.length > 0"
        :table-columns="route.params.type == 'tray-non-tray' ? moveTableColumns : moveItemTableColumns"
        :table-visible-columns="route.params.type == 'tray-non-tray' ? moveTableVisibleColumns: moveItemTableVisibleColumns"
        :table-data="moveShelfJob.containers"
        :heading-row-class="'q-mb-lg q-px-xs-sm q-px-sm-md'"
        :highlight-row-class="'bg-color-green-light'"
        :highlight-row-key="'scanned_for_transfer'"
        :highlight-row-value="true"
      >
        <template #heading-row>
          <div
            class="col-xs-12 col-sm-grow q-mr-auto"
          >
            <h2 class="text-h4 text-bold">
              {{ route.params.type == 'tray-non-tray' ? 'Transfer:' : 'Scan Items to Tray:' }}
            </h2>
          </div>
        </template>

        <template #table-td="{ props, colName, value }">
          <span
            v-if="colName == 'actions'"
          >
            <MoreOptionsMenu
              :options="[{ text: 'Remove From Transfer' }]"
              class=""
              @click="handleOptionMenu($event, props.row)"
            />
          </span>
          <span
            v-if="colName == 'verified'"
            class="text-bold text-nowrap"
            :class="value == true ? 'text-positive' : ''"
          >
            {{ value == true ? 'Ready To Transfer' : '' }}
            <q-icon
              v-if="value == true"
              name="mdi-check-circle"
              color="positive"
              size="25px"
              class="text-bold q-ml-xs"
            />
          </span>
        </template>
      </EssentialTable>
    </template>
  </InfoDisplayLayout>

  <!-- scan container modal -->
  <PopupModal
    v-if="showScanContainerModal"
    ref="scanContainerModal"
    title="Place Container(s)"
    @hide="showScanContainerModal = false; clearScannedContainer();"
    aria-label="scanContainerModal"
  >
    <template #main-content>
      <q-card-section class="row q-pb-sm">
        <div class="col-12">
          <BarcodeBox
            :barcode="!scannedContainer.barcode.value ? 'Please Scan Container' : scannedContainer.barcode.value "
            :min-height="'5rem'"
          />
        </div>
        <div class="col-12">
          <p class="text-body2 text-negative q-pt-xs">
            {{ scannedContainer.error }}
          </p>
        </div>
      </q-card-section>

      <q-card-section class="row q-pb-sm">
        <div
          class="form-group"
        >
          <label class="form-group-label">
            Shelf Position
          </label>
          <TextInput
            v-model="scannedContainer.shelf_position_number"
            placeholder="Enter Shelf Postion"
            :disabled="!scannedContainer.barcode.value"
            type="number"
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
          label="Confirm"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          :disabled="!scannedContainer.shelf_position_number"
          @click="addTransferContainerShelfLocation()"
        />

        <q-space class="q-mx-xs" />

        <q-btn
          outline
          no-caps
          label="Cancel"
          class="text-body1 full-width"
          @click="hideModal()"
        />
      </q-card-section>
    </template>
  </PopupModal>

  <!-- scan container note -->
  <PopupModal
    v-if="showScanContainerNoteModal"
    title="Be Aware"
    :show-actions="false"
    aria-label="scanContainerAlert"
    @hide="showScanContainerNoteModal = false"
  >
    <template #main-content>
      <q-card-section class="row q-pb-sm">
        <div class="col-12">
          <p class="text-body1">
            {{ `${ route.params.type == 'tray-non-tray' ? 'Scan the shelf to begin the transfer process.' : 'Scan the tray to begin the transfer process.'} (the process can be done offline)` }}
          </p>
        </div>
      </q-card-section>

      <q-card-section class="row q-pb-sm">
        <div
          class="form-group"
        >
          <q-checkbox
            left-label
            v-model="toggleDisableScanContainerNote"
            label="Do not show this message again?"
            class="text-body2"
          />
        </div>
      </q-card-section>
    </template>

    <template #footer-content="{ hideModal }">
      <q-card-section
        class="row no-wrap justify-between items-center q-pt-sm"
      >
        <q-btn
          outline
          no-caps
          color="accent"
          label="Confirm"
          class="text-body1 full-width"
          @click="toggleDisableScanContainerNote ? neverShowContainerNote() : null; hideModal();"
        />
      </q-card-section>
    </template>
  </PopupModal>
</template>

<script setup>
import moment from 'moment'
import { ref, inject, onBeforeMount, onMounted, watch, nextTick, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useGlobalStore } from '@/stores/global-store'
import { useUserStore } from '@/stores/user-store'
import { useBarcodeStore } from '@/stores/barcode-store'
import { useShelvingStore } from '@/stores/shelving-store'
import { storeToRefs } from 'pinia'
import { useQuasar } from 'quasar'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import { useIndexDbHandler } from '@/composables/useIndexDbHandler.js'
import InfoDisplayLayout from '@/components/InfoDisplayLayout.vue'
import EssentialTable from '@/components/EssentialTable.vue'
import MobileActionBar from '@/components/MobileActionBar.vue'
import PopupModal from '@/components/PopupModal.vue'
import BarcodeBox from '@/components/BarcodeBox.vue'
import TextInput from '@/components/TextInput.vue'
import MoreOptionsMenu from '@/components/MoreOptionsMenu.vue'

const router = useRouter()
const route = useRoute()

// Compasables
const $q = useQuasar()
const { currentScreenSize } = useCurrentScreenSize()
const { compiledBarCode } = useBarcodeScanHandler()
const {
  addDataToIndexDb,
  getDataInIndexDb,
  deleteDataInIndexDb
} = useIndexDbHandler()
const { checkUserPermission } = usePermissionHandler()

// Store Data
const {
  appIsLoadingData,
  appActionIsLoadingData,
  appIsOffline,
  appPendingSync
} = storeToRefs(useGlobalStore())
const { userData } = storeToRefs(useUserStore())
const { verifyBarcode, getBarcodeDetails } = useBarcodeStore()
const { barcodeDetails } = storeToRefs(useBarcodeStore())
const {
  getShelfByBarcode,
  getShelvingTrayContainerDetails,
  getShelvingTrayItemDetails,
  getShelvingNonTrayItemDetails,
  postMoveTrayLocation,
  postMoveNonTrayLocation,
  postMoveTrayItemLocation,
  resetShelvingStore
} = useShelvingStore()
const {
  moveShelfJob
} = storeToRefs(useShelvingStore())

// Local Data
const moveTableColumns = ref([
  {
    name: 'actions',
    field: 'actions',
    label: '',
    align: 'center',
    sortable: false,
    required: true
  },
  {
    name: 'barcode',
    field: row => row.barcode.value,
    label: 'Barcode',
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
    name: 'location',
    field: row => getItemLocation(row),
    label: 'Location',
    align: 'left',
    sortable: true
  },
  {
    name: 'verified',
    field: 'scanned_for_transfer',
    label: '',
    align: 'center',
    sortable: false,
    required: true
  }
])
const moveTableVisibleColumns = ref([
  'actions',
  'barcode',
  'owner',
  'size_class',
  'location',
  'verified'
])
const moveItemTableColumns = ref([
  {
    name: 'actions',
    field: 'actions',
    label: '',
    align: 'center',
    sortable: false,
    required: true
  },
  {
    name: 'barcode',
    field: row => row.barcode.value,
    label: 'Item Barcode',
    align: 'left',
    sortable: true
  },
  {
    name: 'tray_barcode',
    field: 'tray_barcode_value',
    label: 'Tray Barcode',
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
    name: 'verified',
    field: 'scanned_for_transfer',
    label: '',
    align: 'center',
    sortable: false,
    required: true
  }
])
const moveItemTableVisibleColumns = ref([
  'actions',
  'barcode',
  'tray_barcode',
  'owner',
  'verified'
])
const showScanContainerNoteModal = ref(false)
const toggleDisableScanContainerNote = ref(false)
const scanContainerModal = ref(null)
const showScanContainerModal = ref(false)
const scannedContainer = ref({
  barcode: {
    value: null
  },
  shelf_position_number: ''
})
const renderBarcodeDisplay = computed(() => {
  if (moveShelfJob.value.shelf_barcode) {
    return moveShelfJob.value.shelf_barcode
  } else if (moveShelfJob.value.tray_barcode) {
    return moveShelfJob.value.tray_barcode
  } else if (route.params.type == 'tray-item') {
    return 'Please Scan Tray'
  } else if (route.params.type == 'tray-non-tray') {
    return 'Please Scan Shelf'
  } else {
    return ''
  }
})

// Logic
const formatDateTime = inject('format-date-time')
const getItemLocation = inject('get-item-location')
const handleAlert = inject('handle-alert')

onBeforeMount(() => {
  if (currentScreenSize.value == 'xs') {
    moveTableVisibleColumns.value = [
      'actions',
      'barcode',
      'size_class',
      'location',
      'verified'
    ]
  }
})

onMounted(async () => {
  if (!$q.localStorage.getItem('hideMoveWorkflowNote')) {
    showScanContainerNoteModal.value = true
  }

  // when user is online and loads a job we store the current move shelf data in indexdb for reference offline
  if (!appIsOffline.value) {
    await nextTick()
    addDataToIndexDb('shelvingStore', 'moveShelfJob', JSON.parse(JSON.stringify(moveShelfJob.value)))
  } else {
    // get saved move shelf data if were offline and page was reloaded/refreshed
    const res = await getDataInIndexDb('shelvingStore')
    moveShelfJob.value = res.data.moveShelfJob
  }
})

const neverShowContainerNote = () => {
  showScanContainerNoteModal.value = false
  $q.localStorage.set('hideMoveWorkflowNote', true)
}

const handleOptionMenu = (action, rowData) => {
  switch (action.text) {
    case 'Remove From Transfer':
      removeTransferContainer(rowData.barcode.value)
      return
  }
}

watch(compiledBarCode, (barcode) => {
  if (barcode !== '' && route.params.type == 'tray-non-tray' && !moveShelfJob.value.shelf_barcode) {
    // user is scanning a shelf barcode
    triggerShelfScan(barcode)
  } else if (barcode !== '' && route.params.type == 'tray-item' && !moveShelfJob.value.tray_barcode) {
    // user is scanning a tray barcode
    triggerTrayScan(barcode)
  } else if (barcode !== '' && !scannedContainer.value.barcode.value) {
    // user has a shelf or tray scanned and is scanning containers to move
    triggerContainerScan(barcode)
  }
})
const triggerShelfScan = async (barcode_value) => {
  try {
    if (!checkUserPermission('can_move_trays_and_items_shelving_locations')) {
      handleAlert({
        type: 'error',
        text: 'Sorry you do not have permission to move trays or items!',
        autoClose: true
      })
      return
    }

    // if user is online send a get request to get the scanned shelfs data
    if (!appIsOffline.value) {
      appIsLoadingData.value = true
      const res = await getShelfByBarcode(barcode_value)
      moveShelfJob.value = {
        ...moveShelfJob.value,
        ...res.data,
        available_space: res.data.available_space,
        shelf_barcode: res.data.barcode.value,
        owner: res.data.owner,
        size_class: res.data.shelf_type.size_class,
        user: userData.value
      }
    } else {
      // else if offline assign the shelf barcode directly to the job
      moveShelfJob.value.shelf_barcode = barcode_value
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
const triggerTrayScan = async (barcode_value) => {
  try {
    if (!checkUserPermission('can_move_trays_and_items_shelving_locations')) {
      handleAlert({
        type: 'error',
        text: 'Sorry you do not have permission to move trays or items!',
        autoClose: true
      })
      return
    }

    // if user is online send a get request to get the scanned trays data
    if (!appIsOffline.value) {
      appIsLoadingData.value = true
      const res = await getShelvingTrayContainerDetails(barcode_value)
      moveShelfJob.value = {
        ...moveShelfJob.value,
        ...res.data,
        tray_barcode: res.data.barcode.value,
        owner: res.data.owner,
        size_class: res.data.size_class,
        user: userData.value
      }
    } else {
      // else if offline assign the tray barcode directly to the job
      moveShelfJob.value.tray_barcode = barcode_value
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
const triggerContainerScan = async (barcode_value) => {
  try {
    // check if the scanned barcode is in the containers data and that the barcode hasnt been transferred already
    if (moveShelfJob.value.containers.some(c => c.barcode.value == barcode_value && c.scanned_for_transfer)) {
      handleAlert({
        type: 'error',
        text: 'The scanned container has already been added to be transferred.',
        autoClose: true
      })
      return
    }

    // if online verify the scanned container exists and its the correct type and get the scanned containers data
    if (!appIsOffline.value) {
      appIsLoadingData.value = true
      appActionIsLoadingData.value = true
      if (route.params.type == 'tray-non-tray') {
        await verifyBarcode(barcode_value, [
          'Item',
          'Tray'
        ])
      } else {
        await verifyBarcode(barcode_value, 'Item')
      }

      // assign the scanned tray or non tray to our scannedContainer data
      let res
      if (barcodeDetails.value.type.name == 'Item' && route.params.type == 'tray-item') {
        res = await getShelvingTrayItemDetails(barcode_value)
        scannedContainer.value = res.data
      } else if (barcodeDetails.value.type.name == 'Item'  && route.params.type == 'tray-non-tray') {
        res = await getShelvingNonTrayItemDetails(barcode_value)
        scannedContainer.value = res.data
      } else if (barcodeDetails.value.type.name == 'Tray' && route.params.type == 'tray-non-tray') {
        res = await getShelvingTrayContainerDetails(barcode_value)
        scannedContainer.value = res.data
      }
    } else {
      // assign the scanned barcode directly if offline
      scannedContainer.value.barcode.value = barcode_value
    }

    // depending on move type either add the transfer item or verify if needed
    if (route.params.type == 'tray-item') {
      verifyAndAddTransferTrayItem()
    } else if (route.params.type == 'tray-non-tray') {
      showScanContainerModal.value = true
      verifyTransferContainerShelfLocation()
    }
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appIsLoadingData.value = false
    appActionIsLoadingData.value = false
  }
}
const verifyTransferContainerShelfLocation = () => {
  // skip validation when offline
  if (appIsOffline.value) {
    return
  }

  // when online check if the scanned container meets transfer requirments
  if (!scannedContainer.value.shelf_position?.location) {
    clearScannedContainer()
    scannedContainer.value.error = 'Container has not been assigned a shelf position. Please try again!'
  } else if (scannedContainer.value.size_class?.name !== moveShelfJob.value.size_class.name || scannedContainer.value.owner?.name !== moveShelfJob.value.owner.name) {
    clearScannedContainer()
    scannedContainer.value.error = 'Container does not match owner or size class of the scanned shelf. Please try again!'
  } else if (moveShelfJob.value.available_space == 0) {
    clearScannedContainer()
    scannedContainer.value.error = 'Container move exceeds available shelf capacity. Please try again!'
  }

  setTimeout(() => {
    scannedContainer.value.error = ''
  }, 5000)
}
const addTransferContainerShelfLocation = () => {
  const containerPendingTransfer = {
    barcode: scannedContainer.value.barcode,
    owner: scannedContainer.value.owner,
    size_class: scannedContainer.value.size_class,
    new_shelf: appIsOffline.value ? moveShelfJob.value.shelf_barcode : moveShelfJob.value.shelf_number?.number,
    new_shelf_position: scannedContainer.value.shelf_position_number,
    scanned_for_transfer: true,
    shelf_barcode_value: moveShelfJob.value.shelf_barcode,
    container_type: scannedContainer.value.container_type,
    shelf_position: moveShelfJob.value.shelf_positions?.find(sp => sp.shelf_position_number.number == scannedContainer.value.shelf_position_number) ?? { location: ' - - - - - - ' }
  }

  // if offline allow transfer directly to the moveShelfJob containers
  if (appIsOffline.value) {
    // since were offline we dont have access to proper shelf position location so we display barcode and number instead
    containerPendingTransfer.shelf_position = {
      location: ` - - - - -${containerPendingTransfer.new_shelf}-${containerPendingTransfer.new_shelf_position}`
    }
  }

  // add the new container to the moveShelfJob Containers
  moveShelfJob.value.containers = [
    ...moveShelfJob.value.containers,
    containerPendingTransfer
  ]

  // store the current movejob state in indexdb for reference offline
  addDataToIndexDb('shelvingStore', 'moveShelfJob', JSON.parse(JSON.stringify(moveShelfJob.value)))
  scanContainerModal.value.hideModal()
}
const verifyAndAddTransferTrayItem = () => {
  // if online check that the scanned tray item has been accessioned before
  if (!appIsOffline.value && !scannedContainer.value.scanned_for_verification) {
    handleAlert({
      type: 'error',
      text: 'The scanned item has not been verified. Please try again!',
      autoClose: true
    })
    clearScannedContainer()
    return
  } else if (!appIsOffline.value && !scannedContainer.value.tray.shelf_position_id) {
    handleAlert({
      type: 'error',
      text: 'The scanned item has not been previously shelved. Please try again!',
      autoClose: true
    })
    clearScannedContainer()
    return
  }

  const trayItemPendingTransfer = {
    barcode: scannedContainer.value.barcode,
    owner: scannedContainer.value.owner,
    scanned_for_transfer: true,
    tray_barcode_value: moveShelfJob.value.tray_barcode
  }

  // add the new container to the moveShelfJob Containers
  moveShelfJob.value.containers = [
    ...moveShelfJob.value.containers,
    trayItemPendingTransfer
  ]

  clearScannedContainer()

  // store the current movejob state in indexdb for reference offline
  addDataToIndexDb('shelvingStore', 'moveShelfJob', JSON.parse(JSON.stringify(moveShelfJob.value)))
}
const removeTransferContainer = (barcode_value) => {
  // find the barcode in the moveJob containers and remove it
  moveShelfJob.value.containers = moveShelfJob.value.containers.filter(c => c.barcode.value !== barcode_value)

  // store the current movejob state in indexdb for reference offline
  addDataToIndexDb('shelvingStore', 'moveShelfJob', JSON.parse(JSON.stringify(moveShelfJob.value)))
}
const completeMoveShelfLocations = async () => {
  try {
    appActionIsLoadingData.value = true
    // check if any container is missing a type association (this likely only occurs when user comes back online after moving containers offline)
    if (moveShelfJob.value.containers.some(container => !container.container_type)) {
      // verify all container barcodes and get their types
      await Promise.all(
        moveShelfJob.value.containers.map(async (container) => {
          const res = await getBarcodeDetails(container.barcode.value)
          if (res.data.type.name == 'Tray') {
            container.container_type = {
              type: 'Tray'
            }
          } else if (res.data.type.name == 'Item') {
            container.container_type = {
              type: 'Non-Tray'
            }
          }
        })
      )
    }

    const responses = await Promise.all(
      moveShelfJob.value.containers.map((container) => {
        if (container.container_type.type == 'Tray') {
          const payload = {
            tray_barcode_value: container.barcode.value,
            shelf_barcode_value: container.shelf_barcode_value,
            shelf_position_number: parseInt(container.new_shelf_position),
            assigned_user_id: userData.value.user_id
          }
          return postMoveTrayLocation(payload)
        } else if (container.container_type.type == 'Non-Tray') {
          const payload = {
            non_tray_barcode_value: container.barcode.value,
            shelf_barcode_value: container.shelf_barcode_value,
            shelf_position_number: parseInt(container.new_shelf_position),
            assigned_user_id: userData.value.user_id
          }
          return postMoveNonTrayLocation(payload)
        }
      })
    )

    // loop through the responses and display the transfer success/failures and link the move descrepency report if needed
    const successfulMoves = []
    const capturedMoveErrors = []
    for (const res of responses) {
      if (res.status == 200) {
        successfulMoves.push(res)
      } else {
        capturedMoveErrors.push(res)
      }
    }

    // display all success/errors under a single alert
    if (successfulMoves.length > 0) {
      handleAlert({
        type: 'success',
        text: `${successfulMoves.length} containers successfully transferred.`,
        autoClose: true
      })
    }
    if (capturedMoveErrors.length > 0) {
      handleAlert({
        type: 'error',
        text: `Failed to move ${capturedMoveErrors.length} container(s). Please see move discrepancy <a href='/reports/Shelving%20Move%20Discrepancy?from_dt=${moment().format('YYYY-MM-DDT00:00:00.000') + 'Z'}&to_dt=${moment().format('YYYY-MM-DDT23:59:59.999') + 'Z'}&assigned_user_id=${userData.value.user_id}' tabindex='0'>report</a>`,
        autoClose: false
      })
    }

    // set transffered date
    moveShelfJob.value.move_dt = new Date()
    setTimeout(() => {
      router.push({
        name: 'shelving',
        params: {
          jobId: null
        }
      })
    }, 1000)
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    deleteDataInIndexDb('shelvingStore', 'moveShelfJob')
    appActionIsLoadingData.value = false
  }
}
const completeMoveTrayItem = async () => {
  try {
    appActionIsLoadingData.value = true

    const responses = await Promise.all(
      moveShelfJob.value.containers.map((container) => {
        const payload = {
          tray_barcode_value: container.tray_barcode_value,
          item_barcode_value: container.barcode.value,
          assigned_user_id: userData.value.user_id
        }
        return postMoveTrayItemLocation(payload)
      })
    )

    // loop through the responses and display the transfer success/failures and link the move descrepency report if needed
    const successfulMoves = []
    const capturedMoveErrors = []
    for (const res of responses) {
      if (res.status == 200) {
        successfulMoves.push(res)
      } else {
        capturedMoveErrors.push(res)
      }
    }

    // display all success/errors under a single alert
    if (successfulMoves.length > 0) {
      handleAlert({
        type: 'success',
        text: `${successfulMoves.length} containers successfully transferred.`,
        autoClose: true
      })
    }
    if (capturedMoveErrors.length > 0) {
      handleAlert({
        type: 'error',
        text: `Failed to move ${capturedMoveErrors.length} container(s). Please see move discrepancy <a href='/reports/Shelving%20Move%20Discrepancy?from_dt=${moment().format('YYYY-MM-DDT00:00:00.000') + 'Z'}&to_dt=${moment().format('YYYY-MM-DDT23:59:59.999') + 'Z'}&assigned_user_id=${userData.value.user_id}' tabindex='0'>report</a>`,
        autoClose: false
      })
    }

    // set transffered date
    moveShelfJob.value.move_dt = new Date()
    setTimeout(() => {
      router.push({
        name: 'shelving',
        params: {
          jobId: null
        }
      })
    }, 1000)
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    deleteDataInIndexDb('shelvingStore', 'moveShelfJob')
    appActionIsLoadingData.value = false
  }
}

const clearMoveDetails = () => {
  // clears out any scanned shelf or tray data from the moveShelfJob data
  moveShelfJob.value.shelf_barcode = ''
  moveShelfJob.value.tray_barcode = ''
  moveShelfJob.value.owner.id = null
  moveShelfJob.value.owner.name = ''
  moveShelfJob.value.size_class_id = null
  moveShelfJob.value.size_class.name = ''
  moveShelfJob.value.available_space = null

  // store the current shelving job state in indexdb for reference offline whenever job is executed
  addDataToIndexDb('shelvingStore', 'moveShelfJob', JSON.parse(JSON.stringify(moveShelfJob.value)))
}
const clearScannedContainer = () => {
  scannedContainer.value = {
    barcode: {
      value: null
    },
    shelf_position_number: ''
  }
}
const cancelTransfer = () => {
  resetShelvingStore()

  // remove the move job state from indexdb
  deleteDataInIndexDb('shelvingStore', 'moveShelfJob')

  router.push({
    name: 'shelving',
    params: {
      jobId: null
    }
  })
}
</script>

<style lang="scss" scoped>
</style>
