<template>
  <div class="accession-dashboard">
    <div class="row q-mb-xs-xl q-mb-sm-none">
      <div class="col-grow q-mb-xs-md q-mb-sm-none">
        <EssentialTable
          :table-columns="accessionTableColumns"
          :table-visible-columns="accessionTableVisibleColumns"
          :filter-options="accessionTableFilters"
          :table-data="accessionJobList"
          :enable-table-reorder="false"
          :heading-row-class="'q-mb-xs-md q-mb-md-lg'"
          :heading-filter-class="currentScreenSize == 'xs' ? 'col-xs-6 q-mr-auto' : 'q-ml-auto'"
          :enable-pagination="true"
          :pagination-total="accessionJobListTotal"
          :pagination-loading="appIsLoadingData"
          @update-pagination="loadAccessionJobs($event)"
          @selected-table-row="loadAccessionJob($event.workflow_id)"
        >
          <template #heading-row>
            <div
              class="col-sm-5 col-md-12 col-lg-auto"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? '' : 'self-center'"
            >
              <h1 class="text-h4 text-bold">
                Accession Jobs
              </h1>
            </div>

            <div
              class="col-xs-grow col-sm-7 col-md-auto flex"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? 'justify-end q-mb-md' : 'order-1'"
            >
              <q-btn
                no-caps
                unelevated
                color="accent"
                label="Start Accession"
                class="btn-no-wrap text-body1 q-ml-xs-none q-ml-sm-sm"
                :disabled="appIsOffline"
                @click="startAccessionProcess"
              />
            </div>
          </template>

          <template #table-td="{ colName, value }">
            <span
              v-if="colName == 'status'"
              class="outline text-nowrap"
              :class="value == 'Created' || value == 'Completed' ? 'text-highlight' : value == 'Paused' || value == 'Running' ? 'text-highlight-warning' : 'text-highlight-negative'"
            >
              {{ value }}
            </span>
            <span
              v-if="colName == 'trayed'"
              class="text-secondary"
            >
              {{ value == true ? 'Trayed' : 'Non-Trayed' }}
            </span>
          </template>
        </EssentialTable>
      </div>
    </div>
  </div>

  <!-- start accession process modal -->
  <PopupModal
    v-if="showAccessionModal"
    ref="accessionJobModal"
    :show-actions="false"
    @reset="reset"
    aria-label="AccessionJobCreationModal"
  >
    <template #header-content="{ hideModal }">
      <q-card-section class="row items-center justify-between q-pb-none">
        <h2
          v-if="accessionJob.trayed == null"
          class="text-h6"
        >
          Start New Accession
        </h2>
        <q-btn
          v-else
          icon="chevron_left"
          name="back"
          label="Back"
          no-caps
          flat
          dense
          class="text-body1"
          @click="accessionJob.trayed = null"
        />

        <q-btn
          icon="close"
          flat
          round
          dense
          aria-label="Close"
          @click="hideModal"
        />
      </q-card-section>
    </template>

    <template #main-content>
      <!-- first step in accession job process -->
      <q-card-section
        v-if="accessionJob.trayed == null"
        class="column no-wrap items-center"
      >
        <q-btn
          outline
          no-caps
          padding="14px md"
          label="Non-Tray Accession"
          class="accession-modal-btn full-width text-body1 q-mb-md"
          @click="accessionJob.trayed = false"
        />

        <q-btn
          outline
          no-caps
          padding="14px md"
          label="Trayed Accession"
          class="accession-modal-btn full-width text-body1"
          @click="accessionJob.trayed = true"
        />
      </q-card-section>

      <!-- second step in accession job process -->
      <template v-else>
        <q-card-section class="column no-wrap items-center">
          <div class="form-group q-mb-md">
            <label class="form-group-label">
              Owner <span class="text-caption text-negative">(Required)</span>
            </label>
            <SelectInput
              v-model="accessionJob.owner"
              :options="owners"
              option-type="owners"
              option-value="id"
              option-label="name"
              :placeholder="'Select Owner'"
              aria-label="ownerSelect"
            />
          </div>

          <div
            v-if="!accessionJob.trayed"
            class="form-group q-mb-md"
          >
            <label class="form-group-label">
              Container Size <span class="text-caption">(Optional)</span>
            </label>
            <SelectInput
              v-model="accessionJob.size_class"
              :options="sizeClass"
              option-type="sizeClass"
              option-value="id"
              option-label="name"
              :placeholder="'Select Size Class'"
              aria-label="containerSizeSelect"
            />
          </div>

          <div class="form-group">
            <label class="form-group-label">
              Media Type <span class="text-caption">(Optional)</span>
            </label>
            <SelectInput
              v-model="accessionJob.media_type"
              :options="mediaTypes"
              option-type="mediaTypes"
              option-value="id"
              option-label="name"
              :placeholder="'Select Media Type'"
              aria-label="mediaTypeSelect"
            />
          </div>
        </q-card-section>
      </template>
    </template>

    <template #footer-content="{ hideModal }">
      <q-card-section
        v-if="accessionJob.trayed !== null"
        class="row no-wrap justify-between items-center q-pt-sm"
      >
        <q-btn
          no-caps
          unelevated
          color="accent"
          label="Submit"
          class="text-body1 full-width"
          :disable="!canSubmitAccessionJob"
          :loading="appActionIsLoadingData"
          @click="submitAccessionJob()"
        />

        <q-space class="q-mx-xs" />

        <q-btn
          outline
          no-caps
          label="Cancel"
          class="accession-modal-btn text-body1 full-width"
          @click="hideModal"
        />
      </q-card-section>
    </template>
  </PopupModal>
</template>

<script setup>
import { onBeforeMount, ref, inject, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user-store'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from 'src/stores/option-store'
import { useAccessionStore } from 'src/stores/accession-store'
import { storeToRefs } from 'pinia'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import EssentialTable from '@/components/EssentialTable.vue'
import SelectInput from '@/components/SelectInput.vue'
import PopupModal from '@/components/PopupModal.vue'

const router = useRouter()

// Composables
const { currentScreenSize } = useCurrentScreenSize()

// Store Data
const {
  appIsLoadingData,
  appActionIsLoadingData,
  appIsOffline
} = storeToRefs(useGlobalStore())
const {
  resetAccessionStore,
  postAccessionJob,
  getAccessionJobList,
  getAccessionJob
} = useAccessionStore()
const {
  accessionJob,
  accessionJobList,
  accessionJobListTotal
} = storeToRefs(useAccessionStore())
const {
  owners,
  mediaTypes,
  sizeClass
} = storeToRefs(useOptionStore())
const { userData } = storeToRefs(useUserStore())


// Local Data
const accessionJobModal = ref(null)
const accessionTableVisibleColumns = ref([
  'id',
  'trayed',
  'status'
])
const accessionTableColumns = ref([
  {
    name: 'id',
    field: 'workflow_id',
    label: 'Job Number',
    align: 'left',
    sortable: true
  },
  {
    name: 'trayed',
    field: 'trayed',
    label: 'Job Type',
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
const accessionTableFilters =  ref([
  {
    field: 'trayed',
    label: 'Job Type',
    options: [
      {
        text: 'Trayed',
        boolValue: true,
        value: false
      },
      {
        text: 'Non-Trayed',
        boolValue: false,
        value: false
      }
    ]
  },
  {
    field: 'status',
    label: 'Status',
    options: [
      {
        text: 'Created',
        value: true
      },
      {
        text: 'Paused',
        value: true
      },
      {
        text: 'Running',
        value: true
      },
      {
        text: 'Completed',
        value: false
      }
    ]
  }
])
const showAccessionModal = ref(false)
const canSubmitAccessionJob = computed(() => {
  if (accessionJob.value.owner !== null) {
    return true
  } else {
    return false
  }
})

// Logic
const handleAlert = inject('handle-alert')

onBeforeMount(() => {
  resetAccessionStore()
  loadAccessionJobs()

  if (currentScreenSize.value == 'xs') {
    accessionTableVisibleColumns.value = [
      'id',
      'trayed',
      'status'
    ]
  }
})

const reset = () => {
  resetAccessionStore()
  showAccessionModal.value = false
}
const startAccessionProcess = () => {
  resetAccessionStore()
  showAccessionModal.value = !showAccessionModal.value
}

const loadAccessionJobs = async (qParams) => {
  try {
    appIsLoadingData.value = true
    await getAccessionJobList({
      ...qParams,
      status: accessionTableFilters.value.find(fltr => fltr.field == 'status').options.flatMap(opt => opt.value == true ? opt.text : [])
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
const loadAccessionJob = async (workflowId) => {
  try {
    appIsLoadingData.value = true
    await getAccessionJob(workflowId)

    router.push({
      name: 'accession',
      params: {
        jobId: workflowId
      }
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
const submitAccessionJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      size_class_id: accessionJob.trayed ? undefined : accessionJob.value.size_class,
      media_type_id: accessionJob.value.media_type,
      owner_id: accessionJob.value.owner,
      status: 'Running',
      trayed: accessionJob.value.trayed,
      created_by_id: userData.value.user_id
    }

    await postAccessionJob(payload)

    router.push({
      name: 'accession',
      params: {
        jobId: accessionJob.value.workflow_id
      }
    })

    handleAlert({
      type: 'success',
      text: 'An Accession Job has successfully been created.',
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
    accessionJobModal.value.hideModal()
  }
}

</script>
<style lang="scss" scoped>
</style>
