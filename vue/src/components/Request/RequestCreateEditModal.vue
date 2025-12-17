<template>
  <PopupModal
    ref="requestCreateModal"
    :show-actions="false"
    @reset="emit('hide')"
    aria-label="manualRequestModal"
  >
    <template #header-content="{ hideModal }">
      <q-card-section class="row items-center q-pb-none">
        <h2 class="text-h6 text-bold">
          {{ renderRequestModalTitle }}
        </h2>

        <q-btn
          icon="close"
          flat
          round
          dense
          class="q-ml-auto"
          @click="hideModal"
          aria-label="close"
        />
      </q-card-section>
    </template>
    <template #main-content>
      <q-card-section
        v-if="type == 'manual' || type == 'edit'"
        class="row"
      >
        <div class="col-12 q-mb-md">
          <div class="form-group">
            <label class="form-group-label">
              Item Barcode <span class="text-caption text-negative">(Required)</span>
            </label>
            <TextInput
              v-model="manualRequestForm.barcode"
              placeholder="Enter or Scan Item Barcode"
              @focus="allowItemBarcodeScan = true"
              @blur="allowItemBarcodeScan = false"
              :disabled="type == 'edit'"
            />
          </div>
        </div>
        <div class="col-12 q-mb-md">
          <div class="form-group">
            <label class="form-group-label">
              External Request Id <span class="text-caption text-negative">(Required)</span>
            </label>
            <TextInput
              v-model="manualRequestForm.external_request_id"
              placeholder="Enter External Request Id"
            />
          </div>
        </div>
        <div class="col-12 q-mb-md">
          <div class="form-group">
            <label class="form-group-label">
              Requestor Name
            </label>
            <TextInput
              v-model="manualRequestForm.requestor_name"
              placeholder="Enter Requestor Name"
            />
          </div>
        </div>
        <!-- TODO set this to user admin privelages -->
        <div class="col-12 q-mb-md">
          <div class="form-group">
            <label class="form-group-label">
              Priority
            </label>
            <SelectInput
              v-model="manualRequestForm.priority_id"
              :options="requestsPriorities"
              option-type="requestsPriorities"
              option-value="id"
              option-label="value"
              :placeholder="'Select Priority'"
              aria-label="prioritySelect"
            />
          </div>
        </div>
        <div class="col-12 q-mb-md">
          <div class="form-group">
            <label class="form-group-label">
              Select Request Type
            </label>
            <SelectInput
              v-model="manualRequestForm.request_type_id"
              :options="requestsTypes"
              option-type="requestsTypes"
              option-value="id"
              option-label="type"
              :placeholder="'Select Request Type'"
              aria-label="requestTypeSelect"
            />
          </div>
        </div>
        <div class="col-12">
          <div class="form-group">
            <label class="form-group-label">
              Delivery Location
            </label>
            <SelectInput
              v-model="manualRequestForm.delivery_location_id"
              :options="requestsLocations"
              option-type="requestsLocations"
              option-value="id"
              option-label="name"
              :placeholder="'Select Delivery Location'"
              aria-label="requestLocationSelect"
            />
          </div>
        </div>
      </q-card-section>
      <q-card-section
        v-else
        class="row"
      >
        <div class="col-grow">
          <p class="text-body2">
            Support files: .csv
          </p>
        </div>
        <div class="col-auto flex justify-end">
          <DownloadExcel
            class="link text-body2 text-accent"
            :data="bulkRequestTemplateData"
            type="csv"
            name="bulk-request-template.csv"
            worksheet="Bulk Requests"
            :escape-csv="false"
            aria-label="downloadRequestTemplateLink"
          >
            Click to Download Template
          </DownloadExcel>
        </div>

        <div class="col-12 q-mt-md">
          <FileUploadInput
            :allow-multiple-files="false"
            :allowed-file-types="['.csv']"
            input-class="q-py-xs-md q-px-xs-lg q-py-sm-xl q-px-sm-lg"
            @file-change="requestFile = $event"
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
          :disabled="!isRequestjobFormValid"
          @click="type == 'edit' ? editRequestJob() : createRequestJob()"
        />

        <q-space class="q-mx-xs" />

        <q-btn
          v-if="type == 'manual'"
          no-caps
          unelevated
          color="accent"
          label="Next"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          :disabled="!isRequestjobFormValid"
          @click="createRequestJob(true)"
        />

        <q-space class="q-ml-xs q-mr-lg" />

        <q-btn
          outline
          no-caps
          label="Cancel"
          class="text-body1 full-width"
          @click="hideModal"
        />
      </q-card-section>
    </template>
  </PopupModal>
</template>

<script setup>
import { ref, inject, computed, watch, onMounted } from 'vue'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { useUserStore } from '@/stores/user-store'
import { useRequestStore } from '@/stores/request-store'
import { storeToRefs } from 'pinia'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import PopupModal from '@/components/PopupModal.vue'
import TextInput from '@/components/TextInput.vue'
import SelectInput from '@/components/SelectInput.vue'
import FileUploadInput from '@/components/FileUploadInput.vue'

// Props
const mainProps = defineProps({
  type: {
    type: String,
    default: ''
  },
  requestData: {
    type: Object,
    default: () => {
      return {
        id: null
      }
    }
  }
})

// Emits
const emit = defineEmits([
  'hide',
  'changeDisplay'
])

// Composables
const { compiledBarCode } = useBarcodeScanHandler()

// Store Data
const { appActionIsLoadingData } = storeToRefs(useGlobalStore())
const { userData } = storeToRefs(useUserStore())
const {
  requestsTypes,
  requestsPriorities,
  requestsLocations
} = storeToRefs(useOptionStore())
const {
  postRequestJob,
  patchRequestJob,
  postRequestBatchJob
} = useRequestStore()

// Local Data
const requestCreateModal = ref(null)
const bulkRequestTemplateData = ref([
  {
    'Item Barcode': '',
    'External Request ID': '',
    'Requestor Name': '',
    'Priority': '',
    'Request Type': '',
    'Delivery Location': ''
  }
])
const requestFile = ref([])
const manualRequestForm = ref({
  barcode: null,
  external_request_id: null,
  requestor_name: null,
  request_type_id: null,
  delivery_location_id: null,
  priority_id: null
})
const renderRequestModalTitle = computed(() => {
  if (mainProps.type == 'manual') {
    return 'Create Manual Request'
  } else if (mainProps.type == 'edit') {
    return 'Edit Request'
  } else {
    return 'Import Request File'
  }
})
const isRequestjobFormValid = computed(() => {
  let formIsValid = false
  if (mainProps.type == 'manual' || mainProps.type == 'edit') {
    // if any value in our form is null or empty form is not valid except for priority since thats optional
    const optionalFields = [
      'requestor_name',
      'priority_id',
      'request_type_id',
      'delivery_location_id'
    ]
    formIsValid = Object.keys(manualRequestForm.value).every(key => optionalFields.includes(key) || (manualRequestForm.value[key] !== null && manualRequestForm.value[key] !== ''))
  } else {
    formIsValid = requestFile.value.length == 0 ? false : true
  }
  return formIsValid
})
const allowItemBarcodeScan = ref(false)

// Logic
const handleAlert = inject('handle-alert')
const handleCSVDownload = inject('handle-csv-download')

onMounted(() => {
  if (mainProps.type == 'edit') {
    //populate or request form with the passed in request data
    manualRequestForm.value = {
      request_type_id: mainProps.requestData.request_type_id,
      external_request_id: mainProps.requestData.external_request_id,
      requestor_name: mainProps.requestData.requestor_name,
      barcode: mainProps.requestData.non_tray_item ? mainProps.requestData.non_tray_item.barcode.value : mainProps.requestData.item.barcode.value,
      delivery_location_id: mainProps.requestData.delivery_location_id,
      priority_id: mainProps.requestData.priority_id
    }
  }
})

watch(compiledBarCode, (barcode) => {
  if (barcode !== '' && allowItemBarcodeScan.value) {
    // assigns scanned barcode to item barcode form input
    manualRequestForm.value.barcode = barcode
  }
})

const createRequestJob = async (isNext = false) => {
  try {
    appActionIsLoadingData.value = true
    let payload
    if (mainProps.type == 'manual') {
      payload = {
        request_type_id: manualRequestForm.value.request_type_id,
        external_request_id: manualRequestForm.value.external_request_id,
        requestor_name: manualRequestForm.value.requestor_name,
        barcode_value: manualRequestForm.value.barcode,
        delivery_location_id: manualRequestForm.value.delivery_location_id,
        priority_id: manualRequestForm.value.priority_id,
        requested_by_id: userData.value.user_id
      }
      await postRequestJob(payload)
      handleAlert({
        type: 'success',
        text: 'Successfully created the request.',
        autoClose: true
      })

      emit('changeDisplay', 'request_view')
    } else {
      payload = {
        file: requestFile.value[0].file,
        requested_by_id: userData.value.user_id
      }
      await postRequestBatchJob(payload)

      handleAlert({
        type: 'success',
        text: 'Successfully uploaded batch requests.',
        autoClose: true
      })

      emit('changeDisplay', 'batch_view')
    }
  } catch (error) {
    if (mainProps.type == 'manual') {
      handleAlert({
        type: 'error',
        text: error,
        autoClose: true
      })
    } else if (error.response.status == 400) {
      handleAlert({
        type: 'error',
        text: 'Batch request upload failed with errors. See downloaded error report.',
        autoClose: true
      })
      handleCSVDownload(error.response.data, 'Bulk_Request_Errors')
    } else {
      handleAlert({
        type: 'error',
        text: error,
        autoClose: true
      })
    }
  } finally {
    appActionIsLoadingData.value = false
    // If we're a manual request and clicking next, we want to reset
    // the form and keep it displayed.
    if (mainProps.type == 'manual' && isNext) {
      manualRequestForm.value = {
        request_type_id: null,
        external_request_id: null,
        requestor_name: null,
        barcode: null,
        delivery_location_id: null,
        priority_id: null
      }
    } else {
      requestCreateModal.value.hideModal()
    }
  }
}
const editRequestJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: mainProps.requestData.id,
      request_type_id: manualRequestForm.value.request_type_id,
      external_request_id: manualRequestForm.value.external_request_id,
      requestor_name: manualRequestForm.value.requestor_name,
      delivery_location_id: manualRequestForm.value.delivery_location_id,
      priority_id: manualRequestForm.value.priority_id
    }
    await patchRequestJob(payload)
    handleAlert({
      type: 'success',
      text: 'Successfully updated the request.',
      autoClose: true
    })

    emit('changeDisplay', 'request_view')
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
    requestCreateModal.value.hideModal()
  }
}
</script>

<style lang="scss" scoped>
</style>
