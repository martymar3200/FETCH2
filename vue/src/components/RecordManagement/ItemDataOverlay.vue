<template>
  <q-dialog
    v-model="showItemOverlay"
    :position="'right'"
    full-height
    :class="$style.overlay"
    @hide="emit('close')"
    aria-label="informationalOverlay"
  >
    <q-card class="item-content">
      <q-card-section
        v-if="currentScreenSize !== 'xs'"
        class="row items-center justify-end q-pb-none"
      >
        <q-btn
          icon="close"
          flat
          round
          dense
          aria-label="closeOverlayButton"
          v-close-popup
        />
      </q-card-section>

      <q-card-section>
        <BarcodeBox
          :barcode="renderItemBarcode()"
          :class="renderItemBarcodeColor()"
          :min-height="'12rem'"
        />
      </q-card-section>

      <!-- Item Tray/Non-Tray Detail View (not nested)-->
      <q-card-section class="column q-pt-none">
        <div class="item-details">
          <label class="item-details-label">
            Tray Barcode:
          </label>
          <p class="item-details-text">
            {{ itemData.tray ? itemData.tray.barcode.value : 'N/A' }}
          </p>
        </div>
        <div class="item-details">
          <label class="item-details-label">
            Media Type:
          </label>
          <p class="item-details-text outline">
            {{ itemData.media_type.name }}
          </p>
        </div>
        <div class="item-details">
          <label class="item-details-label">
            Size Class:
          </label>
          <p class="item-details-text">
            {{ itemData.size_class.name }}
          </p>
        </div>
        <div class="item-details">
          <label class="item-details-label">
            Status:
          </label>
          <p
            class="item-details-text outline"
            :class="itemData.status == 'Out' ? 'text-highlight-negative' : 'text-highlight'"
          >
            {{ itemData.status }}
          </p>
        </div>
        <div class="item-details">
          <label class="item-details-label">
            Owner:
          </label>
          <p class="item-details-text">
            {{ itemData.owner?.name ? itemData.owner?.name : "" }}
          </p>
        </div>
      </q-card-section>

      <q-card-section class="column q-pt-none">
        <h1 class="text-h4 q-mb-xs-sm q-mb-sm-md">
          Dates
        </h1>

        <div class="item-details">
          <label class="item-details-label">
            Accession Date:
          </label>
          <p class="item-details-text">
            {{ formatDateTime(itemData.accession_dt).date }}
          </p>
        </div>
        <div class="item-details">
          <label class="item-details-label">
            Shelved Date:
          </label>
          <p class="item-details-text">
            {{ formatDateTime(itemData.tray ? itemData.tray.shelving_job?.update_dt : itemData.shelving_job?.update_dt).date }}
          </p>
        </div>
        <div class="item-details">
          <label class="item-details-label">
            Last Requested Date:
          </label>
          <p class="item-details-text">
            {{ formatDateTime(itemData.last_requested_dt).date }}
          </p>
        </div>
        <div class="item-details">
          <label class="item-details-label">
            Last Refile Date:
          </label>
          <p class="item-details-text">
            {{ formatDateTime(itemData.last_refiled_dt).date }}
          </p>
        </div>
        <div class="item-details">
          <label class="item-details-label">
            Withdrawal Date:
          </label>
          <p class="item-details-text">
            {{ formatDateTime(itemData.withdrawal_dt).date }}
          </p>
        </div>
      </q-card-section>

      <q-card-section class="column q-pt-none">
        <h1 class="text-h4 q-mb-xs-sm q-mb-sm-md">
          Location
        </h1>

        <div class="item-details">
          <p
            v-if="renderItemBuilding(itemData)"
            class="item-details-text outline q-mr-sm"
          >
            {{ renderItemBuilding(itemData) }}
          </p>
          <p class="item-details-text outline">
            {{ getItemLocation(itemData.tray ?? itemData) }}
          </p>
        </div>
      </q-card-section>

      <q-card-section class="row items-center q-pt-sm">
        <q-btn
          outline
          class="full-width"
          color="accent"
          label="Show Item Request History"
          @click="emit('update')"
          v-close-popup
        />
      </q-card-section>

      <!-- Item Tray/Non-Tray Data View (nested)-->
      <!-- <q-card-section class="column q-pt-xs-none q-pt-sm-md">
        <h1
          class="text-h4 q-mb-xs-sm q-mb-sm-md"
        >
          {{ itemData.item ? itemData.item.title : itemData.non_tray_item.title }}
        </h1>

        <div class="item-details">
          <label class="item-details-label">
            Barcode:
          </label>
          <p class="item-details-text">
            {{ itemData.item ? itemData.item.barcode.value : itemData.non_tray_item.barcode.value }}
          </p>
        </div>

        <div class="item-details">
          <label class="item-details-label">
            Media Type:
          </label>
          <p class="item-details-text outline">
            {{ itemData.item ? itemData.item.media_type.name : itemData.non_tray_item.media_type.name }}
          </p>
        </div>

        <div
          v-if="itemData.size"
          class="item-details"
        >
          <label class="item-details-label">
            Size Class:
          </label>
          <p class="item-details-text outline">
            {{ itemData.item ? itemData.item.size_class.name : itemData.non_tray_item.size_class.name }}
          </p>
        </div>

        <div
          v-if="itemData.volume"
          class="item-details"
        >
          <label class="item-details-label">
            Volume:
          </label>
          <p class="item-details-text">
            {{ itemData.item ? itemData.item.volume : itemData.non_tray_item.volume }}
          </p>
        </div>

        <div class="item-details">
          <label class="item-details-label">
            Dimensions:
          </label>
          <p class="item-details-text">
            {{ itemData.dimensions }}
          </p>
        </div>

        <div
          v-if="itemData.condition"
          class="item-details"
        >
          <label class="item-details-label">
            Condition:
          </label>
          <p class="item-details-text text-highlight-negative">
            {{ itemData.item ? itemData.item.condition : itemData.non_tray_item.condition }}
          </p>
        </div>
      </q-card-section> -->

      <!-- <q-card-section class="column q-pt-none">
        <h1 class="text-h4 q-mb-xs-sm q-mb-sm-md">
          Owner
        </h1>

        <div class="item-details">
          <p class="item-details-text outline">
            {{ itemData.item ? itemData.item.owner.name : itemData.non_tray_item.owner.name }}
          </p>
        </div>
      </q-card-section> -->

      <!-- <q-card-section class="column q-pt-none">
        <h1 class="text-h4 q-mb-xs-sm q-mb-sm-md">
          Dates
        </h1>

        <div class="item-details">
          <label class="item-details-label">
            Accession Date:
          </label>
          <p class="item-details-text">
            {{ formatDateTime(itemData.item ? itemData.item.accession_dt : itemData.non_tray_item.accession_dt).date }}
          </p>
        </div>

        <div
          v-if="itemData.withdraw_date"
          class="item-details"
        >
          <label class="item-details-label">
            Withdrawal Date:
          </label>
          <p class="item-details-text">
            {{ formatDateTime(itemData.item ? itemData.item.withdrawal_dt : itemData.non_tray_item.withdrawal_dt).date }}
          </p>
        </div>

        <div
          v-if="itemData.arrival_date"
          class="item-details"
        >
          <label class="item-details-label">
            Arrival Date:
          </label>
          <p class="item-details-text">
            {{ formatDateTime(itemData.item ? itemData.item.accession_dt : itemData.non_tray_item.accession_dt).date }}
          </p>
        </div>
      </q-card-section> -->

      <!-- <q-card-section
        class="column q-pt-none"
      >
        <h1 class="text-h4 q-mb-xs-sm q-mb-sm-md">
          Location
        </h1>

        <div class="item-details">
          <p class="item-details-text text-highlight q-mr-sm">
            In
          </p>
          <p class="item-details-text outline q-mr-sm">
            {{ renderItemBuilding(itemData) }}
          </p>
          <p class="item-details-text outline">
            {{ itemData.item ? getItemLocation(itemData.item.tray) : getItemLocation(itemData.non_tray_item) }}
          </p>
        </div>
      </q-card-section> -->

      <q-card-section
        v-if="currentScreenSize == 'xs'"
        class="row items-center q-pt-sm"
      >
        <q-btn
          class="full-width"
          color="primary"
          label="Close"
          v-close-popup
        />
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { ref, inject } from 'vue'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import BarcodeBox from '@/components/BarcodeBox.vue'

// Props
const mainProps = defineProps({
  itemData: {
    type: Object,
    required: true
  }
})

// Emits
const emit = defineEmits([
  'update',
  'close'
])

// Compasables
const { currentScreenSize } = useCurrentScreenSize()

// Local Data
const showItemOverlay = ref(true)

// Logic
const formatDateTime = inject('format-date-time')
const getItemLocation = inject('get-item-location')

const renderItemBarcode = () => {
  let barcode = ''
  if (mainProps.itemData.barcode) {
    barcode = mainProps.itemData.barcode.value
  } else if (mainProps.itemData.item && mainProps.itemData.item.barcode) {
    barcode = mainProps.itemData.item.barcode.value
  } else if (mainProps.itemData.non_tray_item && mainProps.itemData.non_tray_item.barcode) {
    barcode = mainProps.itemData.non_tray_item.barcode.value
  }
  return barcode
}
const renderItemBarcodeColor = () => {
  let bgColor = ''
  if (mainProps.itemData.status) {
    bgColor = mainProps.itemData.status == 'Out' ? 'bg-color-pink text-negative' : 'bg-color-green-light text-positive'
  }
  return bgColor
}

const renderItemBuilding = () => {
  let building = ''
  if (mainProps.itemData.tray && mainProps.itemData.tray.shelf_position) {
    building = mainProps.itemData.tray.shelf_position.location?.split('-')[0]
  } else if (mainProps.itemData.shelf_position) {
    building = mainProps.itemData.shelf_position.location?.split('-')[0]
  }
  // else if (mainProps.itemData.item && mainProps.itemData.item.tray.shelf_position) {
  //   building = mainProps.itemData.item.tray.shelf_position.location?.split('-')[0]
  // } else if (mainProps.itemData.non_tray_item && mainProps.itemData.non_tray_item.shelf_position) {
  //   building = mainProps.itemData.non_tray_item.shelf_position.location?.split('-')[0]
  // }
  return building
}
</script>

<style lang="scss" scoped>
.item {
  &-content {
    width: 600px;

    @media (max-width: $breakpoint-sm-min) {
      width: 100vw;
      border-radius: 0;
    }
  }

  &-details {
    display: flex;
    flex-direction: row;
    flex-wrap: nowrap;
    align-items: center;
    width: 100%;
    margin-bottom: .5rem;

    @media (max-width: $breakpoint-sm-min) {
      margin-bottom: 5px;
    }

    &:last-child {
      margin-bottom: 0;
    }

    &-label {
      margin-right: 4px;
    }
  }
}
</style>

<style lang="scss" module>
.overlay {
  z-index: 6000 !important;
  :global(.q-dialog__inner) {
    padding: 0;
  }
}
</style>
