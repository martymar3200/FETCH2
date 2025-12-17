<template>
  <InfoDisplayLayout class="shelving-job">
    <template #number-box-content>
      <div class="flex q-mb-xs">
        <MoreOptionsMenu
          :options="[
            {
              text: 'Edit',
              hidden: !checkUserPermission(
                'can_assign_and_reassign_shelving_job'
              ),
              disabled:
                appIsOffline ||
                editJob ||
                shelvingJob.status == 'Paused' ||
                shelvingJob.status == 'Completed',
            },
            { text: 'Print Job' },
            { text: 'View History' }
          ]"
          class="q-mr-xs"
          @click="handleOptionMenu"
        />
        <h1
          id="jobNumber"
          class="info-display-details-label text-h4"
        >
          Job Number:
        </h1>
      </div>
      <p class="info-display-number-box text-h4">
        {{ shelvingJob.id }}
      </p>
    </template>

    <template #details-content>
      <div class="col-xs-6 col-sm-6 col-md-grow">
        <div
          class="info-display-details q-mb-xs-md q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-lg"
        >
          <label class="info-display-details-label-2 text-h6">
            Building:
          </label>
          <p class="text-body1">
            {{ shelvingJob.building?.name }}
          </p>
        </div>
      </div>

      <div class="col-xs-6 col-sm-6 col-md-grow">
        <div
          class="info-display-details q-mb-xs-md q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-lg"
        >
          <label class="info-display-details-label-2 text-h6">
            Assigned User:
          </label>
          <p
            v-if="!editJob"
            class="text-body1"
          >
            {{ shelvingJob.user?.name }}
          </p>
          <SelectInput
            v-else
            v-model="shelvingJob.user_id"
            :options="users"
            option-type="users"
            option-value="id"
            option-label="name"
            aria-label="userSelect"
          >
            <template #no-option>
              <q-item>
                <q-item-section class="text-italic text-grey">
                  No Users Found
                </q-item-section>
              </q-item>
            </template>
          </SelectInput>
        </div>
      </div>

      <div class="col-xs-6 col-sm-6 col-md-grow">
        <div
          class="info-display-details q-mb-xs-none q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-lg"
        >
          <label class="info-display-details-label-2 text-h6">
            Date Created:
          </label>
          <p class="text-body1">
            {{ formatDateTime(shelvingJob.create_dt).date }}
          </p>
        </div>
      </div>

      <div class="col-xs-6 col-sm-auto col-md-auto q-mr-auto">
        <div
          class="info-display-details q-mb-xs-none q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-md-sm"
        >
          <label class="info-display-details-label-2 text-h6"> Status: </label>
          <p
            class="text-body1"
            :class="
              shelvingJob.status == 'Created' ||
                shelvingJob.status == 'Completed'
                ? 'outline text-highlight'
                : shelvingJob.status == 'Paused' ||
                  shelvingJob.status == 'Running'
                  ? 'outline text-highlight-warning'
                  : null
            "
          >
            {{ shelvingJob.status }}
          </p>
        </div>
      </div>

      <div
        v-if="currentScreenSize !== 'xs'"
        class="col-sm-12 col-md-12 col-lg-3 q-ml-auto"
      >
        <div
          v-if="editJob"
          class="info-display-details-action q-mt-sm-sm q-mt-md-md"
        >
          <q-btn
            no-caps
            unelevated
            color="accent"
            label="Save Edits"
            class="btn-no-wrap text-body1 q-mr-sm"
            :loading="appActionIsLoadingData"
            @click="updateShelvingJob"
          />
          <q-btn
            no-caps
            unelevated
            outline
            color="accent"
            label="Cancel"
            class="btn-no-wrap text-body1"
            @click="cancelShelvingJobEdits"
          />
        </div>
        <div
          v-else-if="shelvingJob.status !== 'Completed'"
          class="info-display-details-action q-mt-sm-sm q-mt-md-md"
        >
          <q-btn
            v-if="shelvingJob.status !== 'Created'"
            no-caps
            unelevated
            outline
            color="accent"
            :icon="shelvingJob.status !== 'Paused' ? 'mdi-pause' : 'mdi-play'"
            :label="shelvingJob.status == 'Paused' ? 'Resume Job' : 'Pause Job'"
            :disabled="appPendingSync"
            class="btn-no-wrap text-body1 q-mr-sm"
            @click="
              shelvingJob.status == 'Paused'
                ? updateShelvingJobStatus('Running')
                : updateShelvingJobStatus('Paused')
            "
          />
          <q-btn
            no-caps
            unelevated
            color="positive"
            :label="
              shelvingJob.status == 'Created' ? 'Execute Job' : 'Complete Job'
            "
            class="btn-no-wrap text-body1"
            :disabled="
              appIsOffline ||
                appPendingSync ||
                !checkUserPermission('can_create_and_execute_shelving_job') ||
                shelvingJob.status == 'Paused' ||
                !allContainersShelved
            "
            :loading="appActionIsLoadingData"
            @click="
              shelvingJob.status == 'Created'
                ? executeShelvingJob()
                : (showCompleteJobModal = true)
            "
          />
        </div>
      </div>
      <MobileActionBar
        v-else-if="currentScreenSize == 'xs' && editJob"
        button-one-color="accent"
        :button-one-label="'Save Edits'"
        :button-one-outline="false"
        :button-one-loading="appActionIsLoadingData"
        @button-one-click="updateShelvingJob"
        button-two-color="accent"
        :button-two-label="'Cancel'"
        :button-two-outline="true"
        @button-two-click="cancelShelvingJobEdits"
      />
      <MobileActionBar
        v-else-if="shelvingJob.status !== 'Completed'"
        button-one-color="accent"
        :button-one-icon="
          shelvingJob.status !== 'Paused' ? 'mdi-pause' : 'mdi-play'
        "
        :button-one-label="
          shelvingJob.status == 'Paused' ? 'Resume Job' : 'Pause Job'
        "
        :button-one-outline="true"
        :button-one-disabled="appPendingSync || shelvingJob.status == 'Created'"
        @button-one-click="
          shelvingJob.status == 'Paused'
            ? updateShelvingJobStatus('Running')
            : updateShelvingJobStatus('Paused')
        "
        button-two-color="positive"
        :button-two-label="
          shelvingJob.status == 'Created' ? 'Execute Job' : 'Complete Job'
        "
        :button-two-outline="false"
        :button-two-disabled="
          appIsOffline ||
            appPendingSync ||
            shelvingJob.status == 'Paused' ||
            !allContainersShelved
        "
        :button-two-loading="appActionIsLoadingData"
        @button-two-click="
          shelvingJob.status == 'Created'
            ? executeShelvingJob()
            : (showCompleteJobModal = true)
        "
      />
    </template>

    <template #table-content>
      <EssentialTable
        :table-columns="shelfTableColumns"
        :table-visible-columns="shelfTableVisibleColumns"
        :filter-options="shelfTableFilters"
        :table-data="shelvingJobContainers"
        :row-key="row => row.barcode.value"
        :hide-table-rearrange="false"
        :heading-row-class="'justify-end q-mb-lg q-px-xs-sm q-px-sm-md'"
        :heading-filter-class="
          currentScreenSize == 'xs' ? 'col-xs-6 q-mr-auto' : 'q-ml-auto'
        "
        :highlight-row-class="'bg-color-green-light'"
        :highlight-row-key="'scanned_for_shelving'"
        :highlight-row-value="true"
      >
        <template #heading-row>
          <div class="col-xs-12 col-sm-grow q-mr-auto">
            <h2 class="text-h4 text-bold">
              Containers in Job:
            </h2>
          </div>
        </template>

        <template #table-td="{ colName, props, value }">
          <span v-if="colName == 'actions'">
            <MoreOptionsMenu
              :options="[
                {
                  text: 'Edit Location',
                  disabled:
                    shelvingJob.status == 'Paused' ||
                    shelvingJob.status == 'Completed',
                },
              ]"
              class=""
              @click="handleOptionMenu($event, props.row)"
            />
          </span>
          <span v-if="colName == 'side'">
            {{ value.slice(0, 1) }}
          </span>
          <span
            v-if="colName == 'verified'"
            class="text-bold text-nowrap"
            :class="value == true ? 'text-positive' : ''"
          >
            {{ value == true ? "Shelved" : "" }}
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

  <!-- Edit Location Form Modal -->
  <ShelvingJobDetailsEditLocationModal
    v-if="showShelvingLocationModal"
    ref="locationModalComponent"
    :shelving-item="selectedShelvingItem"
    @hide="
      showShelvingLocationModal = false;
      selectedShelvingItem = null;
    "
  />

  <!-- scan container note -->
  <PopupModal
    v-if="showScanContainerNote"
    title="Be Aware"
    text="Scan the containers to begin the shelving process. (the process can be done offline)"
    :show-actions="false"
    aria-label="scanContainerAlert"
  >
    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <q-btn
          outline
          no-caps
          color="accent"
          label="Confirm"
          class="text-body1 full-width"
          @click="hideModal"
        />
      </q-card-section>
    </template>
  </PopupModal>

  <!-- scan container modal -->
  <ShelvingJobDetailsScanContainerModal
    v-if="showScanContainerModal"
    @hide="showScanContainerModal = false"
  />

  <!-- complete job modal -->
  <PopupModal
    v-if="showCompleteJobModal"
    ref="confirmationModal"
    :title="'Confirm'"
    text="Are you sure you want to complete the job?"
    :show-actions="false"
    @reset="showCompleteJobModal = false"
    aria-label="confirmationModal"
  >
    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <q-btn
          no-caps
          unelevated
          color="accent"
          label="Complete & Print"
          class="btn-no-wrap text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="
            completeShelvingJob(true);
          "
        />

        <q-space class="q-mx-xs" />

        <q-btn
          no-caps
          unelevated
          color="accent"
          label="Complete"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="
            completeShelvingJob(false);
          "
        />

        <q-space
          v-if="currentScreenSize !== 'xs'"
          class="q-mx-lg"
        />

        <q-btn
          v-if="currentScreenSize !== 'xs'"
          outline
          no-caps
          label="Cancel"
          class="text-body1 full-width"
          @click="hideModal"
        />
      </q-card-section>
    </template>
  </PopupModal>

  <!-- print component: shelving job report -->
  <ShelvingBatchSheet
    ref="batchSheetComponent"
    :shelving-job-details="shelvingJob"
  />

  <!-- audit trail modal -->
  <AuditTrail
    v-if="showAuditTrailModal"
    ref="historyModal"
    @reset="showAuditTrailModal = null"
    :job-type="showAuditTrailModal"
    :job-id="shelvingJob.id"
  />
</template>

<script setup>
import { ref, computed, inject, onBeforeMount, toRaw, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useGlobalStore } from '@/stores/global-store'
import { useUserStore } from '@/stores/user-store'
import { useShelvingStore } from '@/stores/shelving-store'
import { useBuildingStore } from '@/stores/building-store'
import { useOptionStore } from '@/stores/option-store'
import { storeToRefs } from 'pinia'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import { useIndexDbHandler } from '@/composables/useIndexDbHandler.js'
import InfoDisplayLayout from '@/components/InfoDisplayLayout.vue'
import MoreOptionsMenu from '@/components/MoreOptionsMenu.vue'
import EssentialTable from '@/components/EssentialTable.vue'
import SelectInput from '@/components/SelectInput.vue'
import MobileActionBar from '@/components/MobileActionBar.vue'
import PopupModal from '@/components/PopupModal.vue'
import ShelvingBatchSheet from '@/components/Shelving/ShelvingBatchSheet.vue'
import ShelvingJobDetailsEditLocationModal from '@/components/Shelving/ShelvingJobDetailsEditLocationModal.vue'
import ShelvingJobDetailsScanContainerModal from '@/components/Shelving/ShelvingJobDetailsScanContainerModal.vue'
import AuditTrail from '@/components/AuditTrail.vue'

const router = useRouter()
const route = useRoute()

// Compasables
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
  appPendingSync,
  appIsOffline
} = storeToRefs(useGlobalStore())
const { userData } = storeToRefs(useUserStore())
const {
  patchShelvingJob,
  getShelvingJobContainer
} = useShelvingStore()
const {
  shelvingJob,
  originalShelvingJob,
  shelvingJobContainers,
  allContainersShelved
} = storeToRefs(useShelvingStore())
const { users } = storeToRefs(useOptionStore())
const { getSideList } = useBuildingStore()

// Local Data
const batchSheetComponent = ref(null)
const locationModalComponent = ref(null)
const confirmationModal = ref(null)
const editJob = ref(false)
const selectedShelvingItem = ref(null)
const shelfTableColumns = ref([
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
    field: (row) => renderItemBarcodeDisplay(row),
    label: 'Barcode',
    align: 'left',
    sortable: true
  },
  {
    name: 'owner',
    field: (row) => row.owner?.name,
    label: 'Owner',
    align: 'left',
    sortable: true
  },
  {
    name: 'size_class',
    field: (row) => row.size_class?.name,
    label: 'Size Class',
    align: 'left',
    sortable: true
  },
  {
    name: 'location',
    field: (row) => getItemLocation(row),
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
  'actions',
  'barcode',
  'owner',
  'size_class',
  'location',
  'verified'
])
const shelfTableFilters = computed(() => {
  let tablesFilters = []
  if (shelvingJobContainers.value.length > 0) {
    tablesFilters = [
      {
        field: (row) => row.owner?.name,
        label: 'Owner',
        // render options based on the passed in table data
        // loop through all containers and return customized data set for table filtering and remove the duplicates
        options: getUniqueListByKey(shelvingJobContainers.value.map(c => {
          return {
            text: c.owner.name,
            value: false
          }
        }), 'text')
      },
      {
        field: (row) => row.size_class?.name,
        label: 'Size Class',
        options: getUniqueListByKey(shelvingJobContainers.value.map(c => {
          return {
            text: c.size_class.name,
            value: false
          }
        }), 'text')
      }
    ]
  }
  return tablesFilters
})
const showShelvingLocationModal = ref(false)
const showScanContainerNote = ref(false)
const showScanContainerModal = ref(false)
const showCompleteJobModal = ref(false)
const historyModal = ref(null)
const showAuditTrailModal = ref(false)

// Logic
const currentIsoDate = inject('current-iso-date')
const formatDateTime = inject('format-date-time')
const getItemLocation = inject('get-item-location')
const handleAlert = inject('handle-alert')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')
const getUniqueListByKey = inject('get-uniqure-list-by-key')

onBeforeMount(() => {
  if (currentScreenSize.value == 'xs') {
    shelfTableVisibleColumns.value = [
      'actions',
      'barcode',
      'size_class',
      'location',
      'verified'
    ]
  }
})

onMounted(async () => {
  // when user is online and loads a job we store the current shelving job data and original in indexdb for reference offline
  if (!appIsOffline.value) {
    addDataToIndexDb(
      'shelvingStore',
      'shelvingJob',
      JSON.parse(JSON.stringify(shelvingJob.value))
    )
    addDataToIndexDb(
      'shelvingStore',
      'originalShelvingJob',
      JSON.parse(JSON.stringify(originalShelvingJob.value))
    )
  } else {
    // get saved shelving job data if were offline and page was reloaded/refreshed
    const res = await getDataInIndexDb('shelvingStore')
    shelvingJob.value = res.data.shelvingJob
    originalShelvingJob.value = res.data.originalShelvingJob
  }
})

watch(compiledBarCode, (barcode) => {
  if (
    barcode !== '' &&
    shelvingJob.value.status == 'Running' &&
    !showShelvingLocationModal.value &&
    !showScanContainerModal.value
  ) {
    // only allow scans if the shelving job is in a running state
    triggerContainerScan(barcode)
  }
})
const triggerContainerScan = async (barcode_value) => {
  // check if the scanned barcode is in the containers data and that the barcode hasnt been shelved already
  if (
    !shelvingJobContainers.value.some((c) => c.barcode.value == barcode_value)
  ) {
    handleAlert({
      type: 'error',
      text: 'The scanned container does not exist in this shelving job. Please try again.',
      autoClose: true
    })
    return
  } else if (
    shelvingJobContainers.value.some(
      (c) => c.barcode.value == barcode_value && c.scanned_for_shelving
    )
  ) {
    handleAlert({
      type: 'error',
      text: 'The scanned container has already been marked as shelved.',
      autoClose: true
    })
    return
  } else {
    // load the matching containers info directly from the shelvingJob data
    appIsLoadingData.value = true
    await getShelvingJobContainer(barcode_value)
    appIsLoadingData.value = false
    showScanContainerModal.value = true
  }
}

const handleOptionMenu = async (action, rowData) => {
  switch (action.text) {
    case 'Edit Location':
      try {
        const itemLocationIdList = rowData.shelf_position?.internal_location?.split('-')
        if (!appIsOffline.value) {
          appIsLoadingData.value = true
          if (itemLocationIdList) {
            await getSideList({
              building_id: shelvingJob.value.building_id,
              module_id: itemLocationIdList[1],
              aisle_id: itemLocationIdList[2]
            })
          }
        }

        // set the passed in rowData as the selected shelvingItem
        selectedShelvingItem.value = rowData
      } catch (error) {
        handleAlert({
          type: 'error',
          text: error,
          autoClose: true
        })
      } finally {
        appIsLoadingData.value = false
        showShelvingLocationModal.value = true
      }

      return
    case 'Edit':
      editJob.value = true
      return
    case 'Print Job':
      batchSheetComponent.value.printBatchReport()
      return
    case 'View History':
      showAuditTrailModal.value = 'shelving_jobs'
      return
  }
}

const cancelShelvingJobEdits = () => {
  shelvingJob.value = { ...toRaw(originalShelvingJob.value) }
  editJob.value = false
}
const executeShelvingJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: route.params.jobId,
      status: 'Running',
      user_id: shelvingJob.value.user_id
        ? shelvingJob.value.user_id
        : userData.value.user_id,
      run_timestamp: currentIsoDate()
    }
    await patchShelvingJob(payload)

    // store the current shelving job data in indexdb for reference offline whenever job is executed
    addDataToIndexDb(
      'shelvingStore',
      'shelvingJob',
      JSON.parse(JSON.stringify(shelvingJob.value))
    )
    addDataToIndexDb(
      'shelvingStore',
      'originalShelvingJob',
      JSON.parse(JSON.stringify(originalShelvingJob.value))
    )

    handleAlert({
      type: 'success',
      text: 'Shelving Job Successfully Started',
      autoClose: true
    })
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
    showScanContainerNote.value = true
  }
}
const updateShelvingJobStatus = async (status) => {
  try {
    appIsLoadingData.value = true
    const payload = {
      id: route.params.jobId,
      status,
      run_timestamp: currentIsoDate()
    }

    await patchShelvingJob(payload)

    if (appIsOffline.value) {
      // when offline we update the status directly
      shelvingJob.value.status = payload.status
      originalShelvingJob.value.status = payload.status
    }

    // store the current shelving job data in indexdb for reference offline whenever job is executed
    addDataToIndexDb(
      'shelvingStore',
      'shelvingJob',
      JSON.parse(JSON.stringify(shelvingJob.value))
    )
    addDataToIndexDb(
      'shelvingStore',
      'originalShelvingJob',
      JSON.parse(JSON.stringify(originalShelvingJob.value))
    )

    handleAlert({
      type: 'success',
      text: `Job Status has been updated to: ${status}`,
      autoClose: true
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
const updateShelvingJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: route.params.jobId,
      user_id: shelvingJob.value.user_id
    }
    await patchShelvingJob(payload)

    handleAlert({
      type: 'success',
      text: 'The job has been updated.',
      autoClose: true
    })
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
    editJob.value = false
  }
}
const completeShelvingJob = async (printBool) => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: route.params.jobId,
      status: 'Completed',
      run_timestamp: currentIsoDate()
    }
    await patchShelvingJob(payload)

    if (printBool) {
      batchSheetComponent.value.printBatchReport()
    }
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
    deleteDataInIndexDb('shelvingStore', 'shelvingJob')
    deleteDataInIndexDb('shelvingStore', 'originalShelvingJob')
    appActionIsLoadingData.value = false
    confirmationModal.value.hideModal()
  }
}
</script>

<style lang="scss" scoped></style>
