<template>
  <InfoDisplayLayout class="direct-shelving-job">
    <template #number-box-content>
      <h1
        id="jobNumber"
        class="info-display-details-label text-h4 text-bold"
      >
        Shelf Number:
      </h1>
      <p class="info-display-number-box text-h4 q-pa-md">
        {{ !directToShelfJob.shelf_barcode.value ? 'Please Scan Shelf' : directToShelfJob.shelf_barcode.value }}
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
            {{ directToShelfJob.owner.name }}
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
            :class="directToShelfJob.size_class.name ? 'outline' : null"
          >
            {{ directToShelfJob.size_class.name }}
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
            {{ !directToShelfJob.user ? userData.name : directToShelfJob.user.name }}
          </p>
        </div>
      </div>

      <div class="col-xs-6 col-sm-grow">
        <div class="info-display-details q-mb-xs-none q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-sm">
          <label
            class="info-display-details-label-2 text-h6 text-bold"
          >
            Date Created:
          </label>
          <p class="text-body1">
            {{ formatDateTime(directToShelfJob.create_dt).date }}
          </p>
        </div>
      </div>

      <div
        v-if="currentScreenSize !== 'xs' && directToShelfJob.status !== 'Completed'"
        class="col-sm-12 col-md-12 col-lg-3 q-ml-auto"
      >
        <div class="info-display-details-action q-mt-sm-sm q-mt-md-md">
          <q-btn
            no-caps
            unelevated
            color="accent"
            label="Scan New Shelf"
            :disabled="!directToShelfJob.shelf_barcode.value || !checkUserPermission('can_create_and_execute_direct_shelving_job')"
            class="btn-no-wrap text-body1 q-mr-sm"
            @click="clearShelfDetails()"
          />
          <q-btn
            no-caps
            unelevated
            color="positive"
            label="Complete Job"
            class="btn-no-wrap text-body1"
            :disabled="appIsOffline || appPendingSync || !allContainersShelved || !checkUserPermission('can_create_and_execute_direct_shelving_job')"
            :loading="appActionIsLoadingData"
            @click="completeDirectToShelfJob()"
          />
        </div>
      </div>
      <MobileActionBar
        v-else-if="currentScreenSize == 'xs' && directToShelfJob.status !== 'Completed'"
        button-one-color="accent"
        button-one-label="Scan New Shelf"
        :button-one-outline="false"
        :button-one-disabled="!directToShelfJob.shelf_barcode.value || !checkUserPermission('can_create_and_execute_direct_shelving_job')"
        @button-one-click="clearShelfDetails()"
        button-two-color="positive"
        button-two-label="Complete Job"
        :button-two-outline="false"
        :button-two-disabled="appIsOffline || appPendingSync || !allContainersShelved || !checkUserPermission('can_create_and_execute_direct_shelving_job')"
        :button-two-loading="appActionIsLoadingData"
        @button-two-click="completeDirectToShelfJob()"
      />
    </template>

    <template #table-content>
      <EssentialTable
        :table-columns="shelfTableColumns"
        :table-visible-columns="shelfTableVisibleColumns"
        :table-data="shelvingJobContainers"
        :heading-row-class="'justify-end q-mb-lg q-px-xs-sm q-px-sm-md'"
        :highlight-row-class="'bg-color-green-light'"
        :highlight-row-key="'scanned_for_shelving'"
        :highlight-row-value="true"
      >
        <template #heading-row>
          <div
            class="col-xs-12 col-sm-grow q-mr-auto"
          >
            <h2 class="text-h4 text-bold">
              Containers in Job:
            </h2>
          </div>
        </template>

        <template #table-td="{ colName, value }">
          <span v-if="colName == 'side'">
            {{ value ? value.slice(0, 1) : '' }}
          </span>
          <span
            v-if="colName == 'verified'"
            class="text-bold text-nowrap"
            :class="value == true ? 'text-positive' : ''"
          >
            {{ value == true ? 'Shelved' : '' }}
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

  <!-- dts scan container modal -->
  <PopupModal
    v-if="showScanContainerModal"
    ref="scanContainerModal"
    title="Container Location"
    @reset="showScanContainerModal = false; resetShelvingJobContainer();"
    aria-label="scanContainerModal"
  >
    <template #main-content>
      <q-card-section class="row q-pb-sm">
        <div class="col-12">
          <BarcodeBox
            :barcode="renderItemBarcodeDisplay(shelvingJobContainer)"
            :min-height="'5rem'"
          />
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
            v-model="shelvingJobContainer.shelf_position_number"
            placeholder="Enter Shelf Postion"
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
          label="Submit"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          :disabled="!shelvingJobContainer.shelf_position_number"
          @click="assignContainerLocation();"
        />

        <q-space class="q-mx-xs" />

        <q-btn
          outline
          no-caps
          label="Cancel"
          class="text-body1 full-width"
          @click="resetShelvingJobContainer(); hideModal();"
        />
      </q-card-section>
    </template>
  </PopupModal>
</template>

<script setup>
import { ref, inject, onBeforeMount, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useGlobalStore } from '@/stores/global-store'
import { useUserStore } from '@/stores/user-store'
import { useShelvingStore } from '@/stores/shelving-store'
import { storeToRefs } from 'pinia'
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

const router = useRouter()

// Compasables
const { currentScreenSize } = useCurrentScreenSize()
const { compiledBarCode } = useBarcodeScanHandler()
const {
  addDataToIndexDb,
  getDataInIndexDb,
  deleteDataInIndexDb
} = useIndexDbHandler()
const { checkUserPermission } = usePermissionHandler()

// // Store Data
const {
  appIsLoadingData,
  appActionIsLoadingData,
  appIsOffline,
  appPendingSync
} = storeToRefs(useGlobalStore())
const { userData } = storeToRefs(useUserStore())
const {
  getShelfByBarcode,
  patchDirectShelvingJob,
  postDirectShelvingJobContainer,
  resetShelvingJobContainer
} = useShelvingStore()
const {
  directToShelfJob,
  shelvingJobContainers,
  shelvingJobContainer,
  allContainersShelved
} = storeToRefs(useShelvingStore())

// Local Data
const scanContainerModal = ref(null)
const shelfTableColumns = ref([
  {
    name: 'barcode',
    field: row => renderItemBarcodeDisplay(row),
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
    label: 'Item Location',
    align: 'left',
    sortable: true
  },
  {
    name: 'verified',
    field: 'scanned_for_shelving',
    label: '',
    align: 'center',
    sortable: false,
    required: true
  }
])
const shelfTableVisibleColumns = ref([
  'barcode',
  'owner',
  'size_class',
  'location',
  'verified'
])
const showScanContainerModal = ref(false)

// Logic
const currentIsoDate = inject('current-iso-date')
const formatDateTime = inject('format-date-time')
const getItemLocation = inject('get-item-location')
const handleAlert = inject('handle-alert')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')

onBeforeMount(() => {
  if (currentScreenSize.value == 'xs') {
    shelfTableVisibleColumns.value = [
      'barcode',
      'size_class',
      'location',
      'verified'
    ]
  }
})

onMounted(async () => {
  // when user is online and loads a job we store the current shelving job data in indexdb for reference offline
  if (!appIsOffline.value && !appPendingSync.value) {
    await nextTick()
    addDataToIndexDb('shelvingStore', 'directToShelfJob', JSON.parse(JSON.stringify(directToShelfJob.value)))
  } else {
    // get saved shelving job data if were offline and page was reloaded/refreshed
    const res = await getDataInIndexDb('shelvingStore')
    directToShelfJob.value = res.data.directToShelfJob
  }
})

watch(compiledBarCode, (barcode) => {
  if (barcode !== '' && !directToShelfJob.value.shelf_barcode.value && directToShelfJob.value.status !== 'Completed') {
    // user is scanning a shelf barcode
    triggerShelfScan(barcode)
  } else if (barcode !== '' && !shelvingJobContainer.value.barcode.value && directToShelfJob.value.status !== 'Completed') {
    // user has a shelf scanned and is scanning containers to place on a shelf
    triggerContainerScan(barcode)
  }
})
const triggerShelfScan = async (barcode_value) => {
  try {
    if (!checkUserPermission('can_create_and_execute_direct_shelving_job')) {
      handleAlert({
        type: 'error',
        text: 'Sorry you do not have permission to execute direct to shelf jobs!',
        autoClose: true
      })
      return
    }

    // if user is online send a get request to get the scanned shelfs data
    if (!appIsOffline.value) {
      appIsLoadingData.value = true
      await getShelfByBarcode(barcode_value)
    } else {
      // else if offline assign the shelf barcode directly to the job
      directToShelfJob.value.shelf_barcode.value = barcode_value
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
const triggerContainerScan = (barcode_value) => {
  // check if the scanned barcode is in the containers data and that the barcode hasnt been shelved already
  if (shelvingJobContainers.value.some(c => c.barcode.value == barcode_value && c.scanned_for_shelving)) {
    handleAlert({
      type: 'error',
      text: 'The scanned container has already been marked as shelved.',
      autoClose: true
    })
    return
  } else {
    shelvingJobContainer.value.barcode.value = barcode_value
    showScanContainerModal.value = true
  }
}
const assignContainerLocation = async () => {
  try {
    appActionIsLoadingData.value = true

    const payload = {
      job_id: directToShelfJob.value.id,
      container_barcode_value: shelvingJobContainer.value.barcode.value,
      shelf_barcode_value: directToShelfJob.value.shelf_barcode.value,
      shelf_position_number: parseInt(shelvingJobContainer.value.shelf_position_number),
      shelved_dt: currentIsoDate(),
      scanned_for_shelving: true
    }
    await postDirectShelvingJobContainer(payload)

    // if offline assign the shelve directly to the directToShelf containers as a tray temporarily since we wont know what the scanned container type is
    if (appIsOffline.value) {
      directToShelfJob.value.trays = [
        ...directToShelfJob.value.trays,
        {
          ...shelvingJobContainer.value,
          barcode: {
            value: shelvingJobContainer.value.barcode.value
          },
          shelf_position: {
            location: ` - - - - -${directToShelfJob.value.shelf_barcode.value}-${shelvingJobContainer.value.shelf_position_number}`
          },
          scanned_for_shelving: true
        }
      ]
    }

    // store the current shelving job state in indexdb for reference offline whenever job is executed
    addDataToIndexDb('shelvingStore', 'directToShelfJob', JSON.parse(JSON.stringify(directToShelfJob.value)))

    handleAlert({
      type: 'success',
      text: 'The container has been updated.',
      autoClose: true
    })
    showScanContainerModal.value = false
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
    resetShelvingJobContainer()
    scanContainerModal.value.hideModal()
  }
}
const completeDirectToShelfJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: directToShelfJob.value.id,
      status: 'Completed',
      run_timestamp: currentIsoDate()
    }
    await patchDirectShelvingJob(payload)

    handleAlert({
      type: 'success',
      text: 'The Shelving Job has been completed.',
      autoClose: true
    })

    router.push({
      name: 'shelving',
      params: {
        jobId: null
      }
    })
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    deleteDataInIndexDb('shelvingStore', 'directToShelfJob')
    appActionIsLoadingData.value = false
  }
}

const clearShelfDetails = () => {
  // clears out any scanned shelf data from the directToShelf data
  directToShelfJob.value.shelf_barcode.value = ''
  directToShelfJob.value.owner.id = null
  directToShelfJob.value.owner.name = ''
  directToShelfJob.value.size_class_id = null
  directToShelfJob.value.size_class.name = ''

  // store the current shelving job state in indexdb for reference offline whenever job is executed
  addDataToIndexDb('shelvingStore', 'directToShelfJob', JSON.parse(JSON.stringify(directToShelfJob.value)))
}
</script>

<style lang="scss" scoped>
</style>
