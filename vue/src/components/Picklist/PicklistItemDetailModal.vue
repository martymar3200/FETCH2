<template>
  <PopupModal
    @reset="resetPicklistItem(); emit('hide');"
    aria-label="picklistJobItemDetailModal"
  >
    <template #header-content="{ hideModal }">
      <q-card-section class="row items-center q-pb-none">
        <h2
          class="text-h6 text-bold"
        >
          Item Details
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
      <q-card-section class="row q-pb-sm">
        <div class="col-12">
          <BarcodeBox
            :barcode="renderItemBarcodeDisplay(picklistItem.item ? picklistItem.item : picklistItem.non_tray_item)"
            :min-height="'5rem'"
          />
        </div>
      </q-card-section>

      <q-card-section
        class="row q-pb-none"
      >
        <div class="col-6">
          <div class="container-details">
            <label class="text-body1 text-bold">
              Item Barcode:
            </label>
            <p class="text-body1">
              {{ renderItemBarcodeDisplay(picklistItem.item ? picklistItem.item : picklistItem.non_tray_item) }}
            </p>
          </div>
        </div>
        <div class="col-6">
          <div
            v-if="picklistItem.item?.tray"
            class="container-details"
          >
            <label class="text-body1 text-bold">
              Tray Barcode:
            </label>
            <p class="text-body1">
              {{ picklistItem?.item?.tray?.barcode?.value }}
            </p>
          </div>
        </div>
        <div class="col-6">
          <div class="container-details">
            <label class="text-body1 text-bold">
              Module:
            </label>
            <p class="text-body1">
              {{ picklistItem.item ? picklistItem.item?.tray?.shelf_position?.location?.split('-')[1] : picklistItem.non_tray_item?.shelf_position?.location?.split('-')[1] }}
            </p>
          </div>
        </div>
        <div class="col-6">
          <div class="container-details">
            <label class="text-body1 text-bold">
              Aisle:
            </label>
            <p class="text-body1">
              {{ picklistItem.item ? picklistItem.item?.tray?.shelf_position?.location?.split('-')[2] : picklistItem.non_tray_item?.shelf_position?.location?.split('-')[2] }}
            </p>
          </div>
        </div>
        <div class="col-6">
          <div class="container-details">
            <label class="text-body1 text-bold">
              Side:
            </label>
            <p class="text-body1">
              {{ picklistItem.item ? picklistItem.item?.tray?.shelf_position?.location?.split('-')[3] : picklistItem.non_tray_item?.shelf_position?.location?.split('-')[3] }}
            </p>
          </div>
        </div>
        <div class="col-6">
          <div class="container-details">
            <label class="text-body1 text-bold">
              Ladder:
            </label>
            <p class="text-body1">
              {{ picklistItem.item ? picklistItem.item?.tray?.shelf_position?.location?.split('-')[4] : picklistItem.non_tray_item?.shelf_position?.location?.split('-')[4] }}
            </p>
          </div>
        </div>
        <div class="col-6">
          <div class="container-details">
            <label class="text-body1 text-bold">
              Shelf:
            </label>
            <p class="text-body1">
              {{ picklistItem.item ? picklistItem.item?.tray?.shelf_position?.location?.split('-')[5] : picklistItem.non_tray_item?.shelf_position?.location?.split('-')[5] }}
            </p>
          </div>
        </div>
        <div class="col-6">
          <div class="container-details">
            <label class="text-body1 text-bold">
              Shelf Position:
            </label>
            <p class="text-body1">
              {{ picklistItem.item ? picklistItem.item?.tray?.shelf_position?.location?.split('-')[6] : picklistItem.non_tray_item?.shelf_position?.location?.split('-')[6] }}
            </p>
          </div>
        </div>
      </q-card-section>
    </template>

    <template #footer-content="{ hideModal }">
      <q-card-section class="column items-center q-pt-sm">
        <q-btn
          no-caps
          unelevated
          outline
          color="accent"
          label="Cancel"
          class="text-body1 full-width"
          @click="hideModal"
        />
      </q-card-section>
    </template>
  </PopupModal>
</template>

<script setup>
import { inject, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { usePicklistStore } from '@/stores/picklist-store'
import PopupModal from '@/components/PopupModal.vue'
import BarcodeBox from '@/components/BarcodeBox.vue'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'

// Emits
const emit = defineEmits(['hide'])

// Compasables
const { compiledBarCode } = useBarcodeScanHandler()

// Store Data
const {
  resetPicklistItem
} = usePicklistStore()
const {
  picklistItem
} = storeToRefs(usePicklistStore())

// Logic
const renderItemBarcodeDisplay = inject('render-item-barcode-display')
watch(compiledBarCode, () => {
  emit('hide')
})
</script>

<style lang="scss" scoped>
.container {
  &-details {
    display: flex;
    flex-flow: row wrap;
    width: 100%;
    padding-bottom: 8px;

    label {
      margin-right: .5rem;
    }
  }
}
</style>
