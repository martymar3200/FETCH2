<template>
  <!-- MODIFIED: Updated column classes to fit the new 3-column layout -->
  <div
    class="col-12 col-md-3 verification-container-info"
  >
    <div class="row">
      <!-- REMOVED: The entire header block with MoreOptionsMenu and Job # was cut from here -->

      <div class="col-xs-12 col-sm-6 col-md-12 col-lg-12 q-mb-xs-md q-mb-sm-none q-mb-lg-lg">
        <BarcodeBox
          :barcode="!verificationContainer.id ? 'Please Scan Tray' : verificationContainer.barcode?.value"
          class="q-mb-md-xl q-mb-lg-none"
        />
      </div>

      <div
        class="col-xs-12 col-sm-6 col-md-12 col-lg-12"
      >
        <div class="row">
          <div class="col-xs-6 col-sm-12 q-mb-xs-sm q-mb-lg-lg verification-container-info-details">
            <label class="text-h6 q-mb-xs">
              Owner
            </label>
            <p
              v-if="!editMode"
              class="outline"
            >
              {{ verificationJob.owner?.name }}
            </p>
            <SelectInput
              v-else-if="editMode && verificationContainer.id == null"
              v-model="verificationJob.owner_id"
              :options="owners"
              option-type="owners"
              option-value="id"
              option-label="name"
              :clearable="false"
              aria-label="ownerSelect"
            />
          </div>

          <div class="col-xs-6 col-sm-12 q-mb-xs-none q-mb-sm-sm q-mb-lg-lg verification-container-info-details">
            <label class="text-h6 q-mb-xs">
              Container Type
            </label>
            <p
              class="q-my-auto"
            >
              Trayed
            </p>
          </div>

          <div
            v-if="verificationContainer.id"
            class="col-xs-6 col-sm-12 q-mb-xs-none q-mb-sm-sm q-mb-lg-lg verification-container-info-details"
          >
            <label class="text-h6 q-mb-xs">
              Container Size
            </label>
            <p
              v-if="!editMode"
              class="outline"
            >
              {{ verificationContainer.size_class?.name }}
            </p>
            <SelectInput
              v-else
              v-model="verificationContainer.size_class_id"
              :options="sizeClass"
              option-type="sizeClass"
              option-value="id"
              option-label="name"
              :clearable="false"
              aria-label="sizeClassSelect"
            />
          </div>

          <div class="col-xs-6 col-sm-12 verification-container-info-details">
            <label class="text-h6 q-mb-xs">
              Media Type
            </label>
            <p
              v-if="!editMode"
              class="outline text-highlight"
            >
              {{ !verificationContainer.id ? verificationJob.media_type?.name : verificationContainer.media_type?.name }}
            </p>
            <template v-else>
              <SelectInput
                v-if="!verificationContainer.id"
                v-model="verificationJob.media_type_id"
                :options="mediaTypes"
                option-type="mediaTypes"
                option-value="id"
                option-label="name"
                :clearable="false"
                aria-label="mediaTypeSelect"
              />
              <SelectInput
                v-else
                v-model="verificationContainer.media_type_id"
                :options="mediaTypes"
                option-type="mediaTypes"
                option-value="id"
                option-label="name"
                :clearable="false"
                aria-label="mediaTypeSelect"
              />
            </template>
          </div>
        </div>

        <div
          v-if="currentScreenSize !== 'xs' && editMode"
          class="row q-pl-sm-md q-pl-lg-none q-mt-md-sm"
        >
          <q-space class="divider q-my-sm" />

          <div class="col-6 q-pr-xs-xs">
            <q-btn
              no-caps
              unelevated
              color="accent"
              label="Save Edits"
              class="full-width text-body1"
              :loading="appActionIsLoadingData"
              @click="!verificationContainer.id ? updateTrayJob() : updateTrayContainer()"
              :disabled="verificationJob.status == 'Paused'"
            />
          </div>
          <div class="col-6 q-pl-xs-xs">
            <q-btn
              no-caps
              unelevated
              outline
              color="accent"
              label="Cancel"
              class="full-width text-body1"
              @click="cancelTrayEdit()"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- mobile compressed container info -->
    <VerificationMobileInfo
      v-if="currentScreenSize == 'xs'"
      @handle-option-menu="handleOptionMenu"
    />

    <!-- mobile actions menu -->
    <MobileActionBar
      v-if="currentScreenSize == 'xs' && editMode"
      button-one-color="accent"
      button-one-label="Save Edits"
      :button-one-outline="false"
      :button-one-loading="appActionIsLoadingData"
      @button-one-click="!verificationContainer.id ? updateTrayJob() : updateTrayContainer()"
      button-two-color="accent"
      button-two-label="Cancel"
      :button-two-outline="true"
      @button-two-click="cancelTrayEdit()"
    />

    <!-- audit trail modal -->
    <AuditTrail
      v-if="showAuditTrailModal"
      ref="historyModal"
      @reset="showAuditTrailModal = null"
      :job-type="showAuditTrailModal"
      :job-id="verificationJob.id"
    />
  </div>
  <!-- confirmation modal -->
  <PopupModal
    v-if="showConfirmationModal"
    ref="confirmationModal"
    :title="'Cancel'"
    :text="'Are you sure you want to cancel the Verification Job?'"
    :show-actions="false"
    @reset="showConfirmationModal = null"
    aria-label="confirmationModal"
  >
    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <q-btn
          no-caps
          unelevated
          color="negative"
          label="Cancel Verification"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="cancelVerification()"
        />
        <q-space class="q-mx-xs" />
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
import { ref, toRaw, watch, inject, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store'
import { useVerificationStore } from '@/stores/verification-store'
import { useOptionStore } from '@/stores/option-store'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import BarcodeBox from '@/components/BarcodeBox.vue'
import SelectInput from '@/components/SelectInput.vue'
import VerificationMobileInfo from '@/components/Verification/VerificationMobileInfo.vue'
import MobileActionBar from '@/components/MobileActionBar.vue'
import AuditTrail from '@/components/AuditTrail.vue'
import PopupModal from '@/components/PopupModal.vue'

const router = useRouter()
const route = useRoute()

// Composables
const { compiledBarCode } = useBarcodeScanHandler()
const { currentScreenSize } = useCurrentScreenSize()

// Store Data
const { appIsLoadingData, appActionIsLoadingData } = storeToRefs(useGlobalStore())
const {
  owners,
  sizeClass,
  mediaTypes
} = storeToRefs(useOptionStore())
const {
  getOwner,
  getSizeClass,
  getMediaType
} = useOptionStore()
const {
  patchVerificationJob,
  getVerificationTray,
  patchVerificationTray,
  cancelVerificationJob
} = useVerificationStore()
const {
  verificationJob,
  verificationContainer,
  originalVerificationContainer,
  originalVerificationJob
} = storeToRefs(useVerificationStore())

// Local Data
const editMode = ref(false)
const historyModal = ref(null)
const showAuditTrailModal = ref(false)
const showConfirmationModal = ref(false)

// Logic
const handleAlert = inject('handle-alert')

watch(route, () => {
  if (!route.params.containerId) {
    editMode.value = false
  }
})

watch(compiledBarCode, (barcode_value) => {
  if (barcode_value !== '' && !verificationContainer.value.id && verificationJob.value.status !== 'Paused') {
    handleTrayScan(barcode_value)
  }
})
const handleTrayScan = async (barcode_value) => {
  try {
    if (verificationJob.value.trays && !verificationJob.value.trays.some(tray => tray.barcode.value == barcode_value)) {
      handleAlert({
        type: 'error',
        text: `The scanned tray ${barcode_value} doesnt exist on this verification job. Please scan a tray that is associated to this job.`,
        persistent: true
      })
      return
    } else {
      appIsLoadingData.value = true
      await getVerificationTray(barcode_value)

      if (!verificationContainer.value.scanned_for_verification && verificationJob.value.status !== 'Completed') {
        await patchVerificationTray({
          id: verificationContainer.value.id,
          scanned_for_verification: true
        })
      }

      if (verificationJob.value.status !== 'Running' && verificationJob.value.status !== 'Completed') {
        await updateTrayJob()
      }

      if (verificationContainer.value.id) {
        router.push({
          name: 'verification-container',
          params: {
            jobId: verificationJob.value.workflow_id,
            containerId: verificationContainer.value.barcode.value
          }
        })
      }
    }
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      persistent: true
    })
  } finally {
    appIsLoadingData.value = false
  }
}

const cancelVerification = async () => {
  try {
    appActionIsLoadingData.value = true
    await cancelVerificationJob(verificationJob.value.id)

    handleAlert({
      type: 'success',
      text: 'Verification Job canceled',
      autoClose: true
    })
    appActionIsLoadingData.value = false

    await nextTick()

    router.push({
      name: 'verification',
      params: {
        jobId: null
      }
    })

  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      persistent: true
    })
    appActionIsLoadingData.value = false
  }
}

const loadOptionData = async () => {
  try {
    appIsLoadingData.value = true
    if (!verificationContainer.value.id) {
      await Promise.all([
        getOwner(verificationJob.value.owner_id),
        getMediaType(verificationJob.value.media_type_id)
      ])
    } else {
      await Promise.all([
        getSizeClass(verificationContainer.value.size_class_id),
        getMediaType(verificationContainer.value.media_type_id)
      ])
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

const cancelTrayEdit = () => {
  if (!verificationContainer.value.id) {
    verificationJob.value = { ...toRaw(originalVerificationJob.value) }
  } else {
    verificationContainer.value = { ...toRaw(originalVerificationContainer.value) }
  }

  editMode.value = false
}
const updateTrayJob = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: verificationJob.value.id,
      owner_id: verificationJob.value.owner_id,
      media_type_id: verificationJob.value.media_type_id,
      status: 'Running'
    }

    await patchVerificationJob(payload)

    handleAlert({
      type: 'success',
      text: 'The job has been updated.',
      autoClose: true
    })
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      persistent: true
    })
  } finally {
    appActionIsLoadingData.value = false
    editMode.value = false
  }
}
const updateTrayContainer = async () => {
  try {
    appActionIsLoadingData.value = true

    const payload = {
      ...verificationContainer.value
    }
    await patchVerificationTray(payload)

    handleAlert({
      type: 'success',
      text: 'Verification Container Has Been Updated',
      autoClose: true
    })
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      persistent: true
    })
  } finally {
    appActionIsLoadingData.value = false
    editMode.value = false
  }
}

defineExpose({
  editMode,
  showConfirmationModal,
  showAuditTrailModal,
  loadOptionData
})
</script>

<style lang="scss" scoped>
.verification-container {
  width: 100%;
  height: auto;

  &-info {
    border-right: 1px solid;
    border-color: $secondary;
    padding: 3rem;
    transition: all .4s ease-in-out;

    @media (max-width: $breakpoint-md-max) {
      border-right: none;
      padding: 1.5rem;
      padding-bottom: 0;
    }

    @media (max-width: $breakpoint-sm-min) {
      padding: 1rem;
      padding-bottom: 0;
    }

    &-details {
      display: flex;
      flex-flow: column nowrap;
      align-items: center;

      @media (max-width: $breakpoint-md-max) {
        align-items: flex-start;
        padding-left: 1rem;
      }

      @media (max-width: $breakpoint-sm-min) {
        align-items: flex-start;

        &:nth-child(odd) {
          padding-left: 0;
          padding-right: 4px;
        }

        &:nth-child(even) {
          padding-left: 4px;
          padding-right: 0;
        }
      }

      & label {
        position: relative;

        .q-btn {
          position: absolute;
          padding: .4rem;
        }
      }
    }
  }
}
</style>