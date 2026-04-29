<template>
  <PopupModal
    ref="bulkUpdateLocationModal"
    title="Bulk Update Shelves"
    @reset="emit('hide'); resetBulkUpdateLocationForm();"
    aria-label="bulkUpdateLocationModal"
  >
    <template #main-content>
      <q-card-section class="row items-end">
        <div class="col-grow">
          <p class="text-body2">
            Support files: .csv
          </p>
        </div>
        <div class="col-auto flex justify-end">
          <DownloadExcel
            class="link text-body2 text-accent"
            :data="bulkUpdateTemplateData"
            type="csv"
            name="bulk-location-update-template.csv"
            worksheet="Bulk Location Update"
            :escape-csv="false"
            aria-label="downloadLocationUpdateTemplateLink"
          >
            Click to Download Template
          </DownloadExcel>
        </div>

        <div class="col-12 q-mt-md">
          <FileUploadInput
            :allow-multiple-files="false"
            :allowed-file-types="['.csv']"
            input-class="q-py-xs-md q-px-xs-lg q-py-sm-xl q-px-sm-lg"
            @file-change="bulkUpdateFile = $event"
          />
        </div>
      </q-card-section>
    </template>

    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <BaseButton
          no-caps
          unelevated
          color="accent"
          label="Submit"
          class="text-body1 full-width"
          :disabled="!isBulkUpdateValid"
          :loading="appActionIsLoadingData"
          @click="submitBulkUpdateForm()"
        />

        <q-space class="q-mx-xs" />

        <BaseButton
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
import BaseButton from '@/components/Base/BaseButton.vue'
import { ref, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store'
import { notify } from '@/utils/notify'
import { useBuildingStore } from '@/stores/building-store'
import PopupModal from '@/components/PopupModal.vue'
import FileUploadInput from '@/components/FileUploadInput.vue'

// Emits
const emit = defineEmits(['hide'])

// Store Data
const { appActionIsLoadingData } = storeToRefs(useGlobalStore())
const { postBulkUpdateLocations } = useBuildingStore()

// Local Data
const bulkUpdateLocationModal = ref(null)
const bulkUpdateTemplateData = ref([
  {
    'Shelf Barcode': '',
    'Owner': '',
    'Size Class': '',
    'Container Type': '',
    'Shelf Type': '',
    'Width': '',
    'Height': '',
    'Depth': '',
    'Location Logical Order': ''
  }
])
const bulkUpdateFile = ref([])

const isBulkUpdateValid = computed(() => {
  return bulkUpdateFile.value.length > 0
})

// Logic
const resetBulkUpdateLocationForm = () => {
  bulkUpdateFile.value = []
}

const submitBulkUpdateForm = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      file: bulkUpdateFile.value[0].file
    }
    await postBulkUpdateLocations(payload)

    notify({
      type: 'positive',
      message: 'Successfully Bulk Updated Locations!'
    })
    emit('hide')
  } catch (error) {
    if (error.response?.data && Array.isArray(error.response.data)) {
      error.response.data.forEach(err => {
        notify({
          type: 'negative',
          message: `Bulk update failed: ${JSON.stringify(err)}`,
          timeout: 0,
          actions: [
            {
              icon: 'close',
              color: 'white'
            }
          ]
        })
      })
    } else {
      notify({
        type: 'negative',
        message: error.response?.data?.detail || error.message || 'Failed to bulk update locations'
      })
    }
  } finally {
    appActionIsLoadingData.value = false
  }
}
</script>
