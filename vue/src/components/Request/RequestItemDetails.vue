<template>
  <div class="request">
    <div class="row">
      <div class="col-12 flex no-wrap items-center q-mb-xs-md q-mb-sm-lg">
        <MoreOptionsMenu
          :options="[
            { text: 'Edit Request', disabled: requestJob.status == 'InProgress' || requestJob.status == 'Completed'},
            { text: 'Delete Request', optionClass: 'text-negative', disabled: requestJob.status == 'Completed', hidden: !checkUserPermission('can_delete_request')},
          ]"
          class="q-mr-sm"
          @click="handleOptionMenu"
        />
        <h1 class="text-h4 text-bold">
          Request Item: {{ requestJob.id }}
        </h1>
      </div>
    </div>

    <div class="row">
      <div class="col-xs-12 col-lg-4 q-pr-xs-none q-pr-lg-md q-pb-xs-md q-pb-lg-none">
        <BarcodeBox
          :barcode="renderItemBarcodeDisplay(requestJob.item ? requestJob.item : requestJob.non_tray_item)"
          :class="renderRequestItemStatus == 'In' ? 'bg-color-green-light text-positive' : 'bg-color-pink text-negative'"
          class="q-py-xs-sm q-py-sm-md"
        />
      </div>
      <div class="col-xs-6 col-sm-4 col-lg-3">
        <div class="column no-wrap">
          <div class="request-details">
            <label class="request-details-label text-h6">
              Item Barcode
            </label>
            <EssentialLink
              :title="renderItemBarcodeDisplay(requestJob.item ? requestJob.item : requestJob.non_tray_item)"
              @click="routeToItemDetail(renderItemBarcodeDisplay(requestJob.item ? requestJob.item : requestJob.non_tray_item))"
              dense
              class="request-details-text q-pa-none"
            />
          </div>

          <div class="request-details">
            <label class="request-details-label text-h6">
              Request ID
            </label>
            <p class="request-details-text">
              {{ requestJob.id }}
            </p>
          </div>

          <div class="request-details">
            <label class="request-details-label text-h6">
              External Request ID
            </label>
            <p class="request-details-text">
              {{ requestJob.external_request_id }}
            </p>
          </div>

          <!-- TODO change request status to use new status field at the top level of the requestJob data once api has that setup-->
          <div class="request-details">
            <label class="request-details-label text-h6">
              Request Status
            </label>
            <p
              class="request-details-text outline"
              :class="requestJob.status == 'Completed' ? 'text-highlight' : requestJob.status == 'InProgress' ? 'text-highlight-warning' : null"
            >
              {{ requestJob.status }}
            </p>
          </div>
        </div>
      </div>
      <div class="col-xs-6 col-sm-4 col-lg-3">
        <div class="column no-wrap">
          <div class="request-details">
            <label class="request-details-label text-h6">
              Request Type
            </label>
            <p
              class="request-details-text"
            >
              {{ requestJob.request_type ? requestJob.request_type.type : '' }}
            </p>
          </div>

          <div class="request-details">
            <label class="request-details-label text-h6">
              Priority
            </label>
            <p
              class="request-details-text"
              :class="requestJob.priority ? 'outline' : null"
            >
              {{ requestJob.priority ? requestJob.priority.value : '' }}
            </p>
          </div>

          <div class="request-details">
            <label class="request-details-label text-h6">
              Requested Date
            </label>
            <p class="request-details-text">
              {{ formatDateTime(requestJob.create_dt).date }}
            </p>
          </div>

          <div class="request-details">
            <label class="request-details-label text-h6">
              Requestor Name
            </label>
            <p class="request-details-text">
              {{ requestJob.requestor_name }}
            </p>
          </div>
        </div>
      </div>
      <div class="col-xs-6 col-sm-4 col-lg-2">
        <div class="column no-wrap">
          <div class="request-details">
            <label class="request-details-label text-h6">
              Delivery Location:
            </label>
            <p class="request-details-text">
              {{ requestJob.delivery_location ? requestJob.delivery_location.name : '' }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Request Creation Modal -->
  <RequestCreateEditModal
    v-if="showEditRequestModal"
    :type="'edit'"
    :request-data="requestJob"
    @hide="showEditRequestModal = false"
  />

  <!-- confirmation modal -->
  <PopupModal
    v-if="showConfirmationModal"
    ref="confirmationModal"
    :title="'Delete'"
    :text="'Are you sure you want to delete the Request?'"
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
          label="Delete Request"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="deleteRequest()"
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
import { ref, inject, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import { useGlobalStore } from '@/stores/global-store'
import { useRequestStore } from '@/stores/request-store'
import { storeToRefs } from 'pinia'
import BarcodeBox from '@/components/BarcodeBox.vue'
import EssentialLink from '@/components/EssentialLink.vue'
import MoreOptionsMenu from '@/components/MoreOptionsMenu.vue'
import RequestCreateEditModal from '@/components/Request/RequestCreateEditModal.vue'
import PopupModal from '@/components/PopupModal.vue'

const router = useRouter()

// Composables
const { checkUserPermission } = usePermissionHandler()

// Store Data
const { requestJob } = storeToRefs(useRequestStore())
const { appActionIsLoadingData } = storeToRefs(useGlobalStore())
const { deleteRequestJob } = useRequestStore()

// Local Data
const renderRequestItemStatus = computed(() => {
  if (requestJob.value.item && (requestJob.value.item.status == 'Out' || requestJob.value.item.status == 'Withdrawn')) {
    return 'Out'
  } else if (requestJob.value.non_tray_item && (requestJob.value.non_tray_item.status == 'Out' || requestJob.value.non_tray_item.status == 'Withdrawn')) {
    return 'Out'
  } else {
    return 'In'
  }
})
const showEditRequestModal = ref(false)
const showConfirmationModal = ref(false)

// Logic
const handleAlert = inject('handle-alert')
const formatDateTime = inject('format-date-time')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')

const routeToItemDetail = (barcode) => {
  router.push({
    name: 'record-management-items',
    params: {
      barcode
    }
  })
}

const handleOptionMenu = (option) => {
  if (option.text == 'Edit Request') {
    showEditRequestModal.value = true
  } else if (option.text == 'Delete Request') {
    showConfirmationModal.value = true
  }
}

const deleteRequest = async () => {
  try {
    appActionIsLoadingData.value = true
    await deleteRequestJob(requestJob.value.id)

    handleAlert({
      type: 'success',
      text: 'The request has been deleted.',
      autoClose: true
    })
    appActionIsLoadingData.value = false

    await nextTick()

    router.push({
      name: 'request',
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

</script>

<style lang="scss" scoped>
.request {
  &-details {
    position: relative;
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;
    width: 100%;
    margin-bottom: 1rem;

    @media (max-width: $breakpoint-sm-min) {
      flex-direction: column;
      align-items: flex-start;
      margin-bottom: 8px;
    }

    &-label {
      width: 100%;

      @media (max-width: $breakpoint-sm-min) {
        width: initial;
        margin-right: 4px;
      }
    }

    &-text {
      min-width: 1px;
      min-height: 28px; // this offsets any text with outline/highlight classes

      @media (max-width: $breakpoint-sm-min) {
        min-height: 25px;
      }
    }
  }
}
</style>
