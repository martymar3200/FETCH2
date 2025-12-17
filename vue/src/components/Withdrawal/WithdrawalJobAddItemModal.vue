<template>
  <PopupModal
    @reset="emit('hide')"
    ref="withdrawalAddItemModal"
    aria-label="withdrawalJobItemModal"
  >
    <template #header-content="{ hideModal }">
      <q-card-section class="row items-center q-pb-none">
        <h2 class="text-h6 text-bold">
          {{ entryType == 'Scan' ? 'Scan Item' : entryType == 'Bulk' ? 'Bulk Upload Items' : 'Manual Barcode Entry' }}
        </h2>

        <q-btn
          icon="close"
          flat
          round
          dense
          class="q-ml-auto"
          @click="hideModal"
          aria-label="closeModal"
        />
      </q-card-section>
    </template>

    <template #main-content>
      <q-card-section
        v-if="entryType == 'Scan'"
        class="row q-pb-sm"
      >
        <div class="col-12">
          <BarcodeBox
            :barcode="itemBarcode ?? 'Please Scan Item'"
            :min-height="'5rem'"
          />
        </div>
        <div
          v-if="showAddAlert"
          class="col-12"
        >
          <p class="text-body2">
            Successfully added an item. Scan another barcode when ready!
          </p>
        </div>
      </q-card-section>
      <q-card-section
        v-else-if="entryType == 'Bulk'"
        class="row q-pb-sm"
      >
        <div class="col-grow">
          <p class="text-body2">
            Support files: .csv
          </p>
        </div>
        <div class="col-auto flex justify-end">
          <DownloadExcel
            class="link text-body2 text-accent"
            :data="bulkWithdrawTemplateData"
            type="csv"
            name="bulk-withdraw-template.csv"
            worksheet="Bulk Withdraw"
            :escape-csv="false"
            aria-label="downloadWithdrawTemplateLink"
          >
            Click to Download Template
          </DownloadExcel>
        </div>
        <div class="col-12 q-mt-md">
          <FileUploadInput
            :allow-multiple-files="false"
            :allowed-file-types="['.csv', '.xlsx']"
            input-class="q-py-xs-md q-px-xs-lg q-py-sm-xl q-px-sm-lg"
            @file-change="withdrawFile = $event"
          />
        </div>
      </q-card-section>
      <q-card-section
        v-else
        class="row q-pb-sm"
      >
        <div
          class="form-group"
        >
          <label class="form-group-label">
            Type Barcode
          </label>
          <TextInput
            v-model="itemBarcode"
            placeholder="Please Enter Barcode"
          />
        </div>
      </q-card-section>
    </template>

    <template #footer-content="{ hideModal }">
      <q-card-section
        class="row no-wrap justify-between items-center q-pt-sm"
      >
        <q-btn
          v-if="entryType == 'Scan'"
          no-caps
          unelevated
          color="accent"
          label="Done"
          class="text-body1 full-width"
          @click="hideModal()"
        />
        <q-btn
          v-else-if="entryType == 'Bulk'"
          no-caps
          unelevated
          color="accent"
          label="Upload Items"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          :disabled="withdrawFile.length == 0"
          @click="bulkAddItemToWithdrawJob()"
        />
        <q-btn
          v-else
          no-caps
          unelevated
          color="accent"
          label="Submit"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          :disabled="!itemBarcode"
          @click="triggerItemScan(itemBarcode)"
        />

        <q-space class="q-mx-xs" />

        <q-btn
          outline
          no-caps
          label="Cancel"
          class="text-body1 full-width"
          @click="hideModal();"
        />
      </q-card-section>
    </template>
  </PopupModal>
</template>

<script setup>
import { ref, inject, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store'
import { useWithdrawalStore } from '@/stores/withdrawal-store'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import PopupModal from '@/components/PopupModal.vue'
import BarcodeBox from '@/components/BarcodeBox.vue'
import TextInput from '@/components/TextInput.vue'
import FileUploadInput from '@/components/FileUploadInput.vue'

// Props
defineProps({
  entryType: {
    type: String,
    default: 'Scan'
  }
})

// Emits
const emit = defineEmits(['hide'])

// Compasables
const { compiledBarCode } = useBarcodeScanHandler()

// Store Data
const { appActionIsLoadingData } = storeToRefs(useGlobalStore())
const { postWithdrawJobItem, postWithdrawJobBulkItems } = useWithdrawalStore()
const { withdrawJob, withdrawJobItems } = storeToRefs(useWithdrawalStore())

// Local Data
const bulkWithdrawTemplateData = ref([
  {
    'Item Barcode': '',
    'Tray Barcode': ''
  }
])
const withdrawalAddItemModal = ref(null)
const itemBarcode = ref(null)
const showAddAlert = ref(false)
const withdrawFile = ref([])

// Logic
const handleAlert = inject('handle-alert')

watch(compiledBarCode, (barcode) => {
  if (barcode !== '') {
    triggerItemScan(barcode)
  }
})
const triggerItemScan = (barcode_value) => {
  // check if barcode already is part of the job items
  if (withdrawJobItems.value.length > 0 && (withdrawJobItems.value.some(itm => itm.barcode.value == barcode_value) || withdrawJob.value.trays.some(itm => itm.barcode.value == barcode_value))) {
    handleAlert({
      type: 'error',
      text: 'The scanned barcode already exists in this withdraw job. Please try again!',
      autoClose: true
    })
  } else {
    itemBarcode.value = barcode_value
    addItemToWithdrawJob()
  }
}

const addItemToWithdrawJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      job_id: withdrawJob.value.id,
      barcode_value: itemBarcode.value
    }
    const res = await postWithdrawJobItem(payload)

    handleAlert({
      type: 'success',
      text: 'Successfully added an item to the Withdraw Job!',
      autoClose: true
    })
    // check if errors are returned in our 200 response and display them
    if (res) {
      res.forEach(err => {
        handleAlert({
          type: 'error',
          text: `Batch withdraw upload failed for the following: ${JSON.stringify(err)}`,
          autoClose: true
        })
      })
    }

    showAddAlert.value = true
    setTimeout(() => {
      showAddAlert.value = false
    }, 2500)
  } catch (error) {
    if (error.response?.data?.errors) {
      error.response.data.errors.forEach(err => {
        handleAlert({
          type: 'error',
          text: `Batch withdraw upload failed: ${JSON.stringify(err)}`,
          autoClose: true
        })
      })
    } else {
      handleAlert({
        type: 'error',
        text: error,
        autoClose: true
      })
    }
  } finally {
    appActionIsLoadingData.value = false
    itemBarcode.value = null
    withdrawalAddItemModal.value.hideModal()
  }
}

const bulkAddItemToWithdrawJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      job_id: withdrawJob.value.id,
      file: withdrawFile.value[0].file
    }
    const res = await postWithdrawJobBulkItems(payload)

    handleAlert({
      type: 'success',
      text: 'Successfully bulk added items to the Withdraw Job!',
      autoClose: true
    })
    // check if errors are returned in our 200 response and display them
    if (res) {
      res.forEach(err => {
        handleAlert({
          type: 'error',
          text: `Batch withdraw upload failed for the following: ${JSON.stringify(err)}`,
          autoClose: true
        })
      })
    }
  } catch (error) {
    //TODO figure out how to handle error logging for the user
    if (error.response?.data?.errors) {
      error.response.data.errors.forEach(err => {
        handleAlert({
          type: 'error',
          text: `Batch withdraw upload failed: ${JSON.stringify(err)}`,
          autoClose: true
        })
      })
    } else {
      handleAlert({
        type: 'error',
        text: error,
        autoClose: true
      })
    }
  } finally {
    appActionIsLoadingData.value = false
    withdrawFile.value = []
    withdrawalAddItemModal.value.hideModal()
  }
}
</script>

<style lang="scss" scoped>
</style>