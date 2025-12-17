<template>
  <PopupModal
    ref="editShelvingLocationModal"
    :title="`Edit Shelving Location`"
    @reset="resetLocationForm"
    aria-label="editShelvingLocationModal"
  >
    <template #main-content>
      <q-card-section
        v-if="!appIsOffline"
        class="row items-end"
      >
        <div
          class="form-group q-mb-md"
        >
          <label class="form-group-label">
            Module
          </label>
          <SelectInput
            v-model="locationForm.module_id"
            :options="modules"
            option-type="modules"
            :option-query="{
              building_id: locationForm.building_id
            }"
            option-value="id"
            option-label="module_number"
            :placeholder="'Select Module'"
            :clearable="false"
            :disabled="!locationForm.building_id"
            @update:model-value="handleLocationFormChange('Module')"
            aria-label="moduleSelect"
          />
        </div>

        <div
          class="col-xs-12 col-sm-6 q-pr-sm-xs"
        >
          <div class="form-group">
            <label class="form-group-label">
              Aisle
            </label>
            <SelectInput
              v-model="locationForm.aisle_id"
              :options="aisles"
              option-type="aisles"
              :option-query="{
                building_id: locationForm.building_id,
                module_id: locationForm.module_id,
                sort_by: 'aisle_number'
              }"
              option-value="id"
              :option-label="opt => opt.aisle_number.number"
              :placeholder="'Select Aisle'"
              :clearable="false"
              :disabled="!locationForm.module_id"
              @update:model-value="handleLocationFormChange('Aisle')"
              aria-label="aisleSelect"
            />
          </div>
        </div>
        <div
          class="col-xs-12 col-sm-6 q-pl-sm-xs q-mt-xs-md q-mt-sm-none"
        >
          <div class="form-group">
            <label class="form-group-label">
              Side
            </label>
            <ToggleButtonInput
              v-model="locationForm.side_id"
              :options="sides"
              option-value="id"
              option-label="side_orientation.name"
              :disabled="!locationForm.aisle_id"
              @update:model-value="handleLocationFormChange('Side')"
            />
          </div>
        </div>

        <div
          class="form-group q-my-md"
        >
          <label class="form-group-label">
            Ladder
          </label>
          <SelectInput
            v-model="locationForm.ladder_id"
            :options="ladders"
            option-type="ladders"
            :option-query="{
              building_id: locationForm.building_id,
              module_id: locationForm.module_id,
              aisle_id: locationForm.aisle_id,
              side_id: locationForm.side_id,
              sort_by: 'ladder_number'
            }"
            option-value="id"
            :option-label="opt => opt.ladder_number.number"
            :placeholder="'Select Ladder'"
            :clearable="false"
            :disabled="!locationForm.side_id"
            @update:model-value="handleLocationFormChange('Ladder')"
            aria-label="ladderSelect"
          />
        </div>

        <div
          class="col-xs-12 col-sm-6 q-pr-sm-xs"
        >
          <div
            class="form-group"
          >
            <label class="form-group-label">
              Shelf
            </label>
            <SelectInput
              v-model="locationForm.shelf_id"
              :options="shelves"
              option-type="shelves"
              :option-query="{
                building_id: locationForm.building_id,
                module_id: locationForm.module_id,
                aisle_id: locationForm.aisle_id,
                side_id: locationForm.side_id,
                ladder_id: locationForm.ladder_id,
                owner_id: mainProps.shelvingItem.owner.id,
                size_class_id: mainProps.shelvingItem.size_class.id,
                sort_by: 'shelf_number'
              }"
              option-value="id"
              :option-label="opt => opt.shelf_number.number"
              :placeholder="'Select Shelf'"
              :clearable="false"
              :disabled="!locationForm.ladder_id"
              @update:model-value="handleLocationFormChange('Shelf')"
              aria-label="shelfSelect"
            >
              <template #option="{ itemProps, opt }">
                <q-item v-bind="itemProps">
                  <q-item-section>
                    <q-item-label class="text-body1">
                      <span>Shelf #: {{ opt.shelf_number.number }}</span>
                      <span class="text-secondary"> - {{ `${opt.depth}in X ${opt.width}in X ${opt.height}in` }} (available capacity: {{ opt.available_space }})</span>
                    </q-item-label>
                  </q-item-section>
                </q-item>
              </template>
            </SelectInput>
          </div>
        </div>
        <div
          class="col-xs-12 col-sm-6 q-pl-sm-xs q-mt-xs-md q-mt-sm-none"
        >
          <div
            class="form-group"
          >
            <label class="form-group-label">
              Shelf Position
            </label>
            <SelectInput
              v-model="locationForm.shelf_position_id"
              :options="shelvesPositions"
              option-type="shelvesPositions"
              :option-query="{
                shelf_id: locationForm.shelf_id,
                empty: true,
                sort_by: 'shelf_position_number'
              }"
              option-value="id"
              :option-label="opt => opt.shelf_position_number.number"
              :placeholder="'Select Shelf Position'"
              :clearable="false"
              :disabled="!locationForm.shelf_id"
              aria-label="shelfPositionSelect"
            />
          </div>
        </div>
      </q-card-section>
      <q-card-section
        v-else
        class="row items-end"
      >
        <div class="col-12">
          <BarcodeBox
            :barcode="!locationForm.shelf_barcode ? 'Please Scan Shelf' : locationForm.shelf_barcode"
            :min-height="'5rem'"
          />
        </div>
        <div
          class="col-12 q-mt-xs"
        >
          <div
            class="form-group"
          >
            <label class="form-group-label">
              Shelf Position
            </label>
            <TextInput
              v-model="locationForm.shelf_position_number"
              placeholder="Enter Shelf Postion"
              type="number"
              @focus="shelvesPositionInputFocused = true"
              @blur="shelvesPositionInputFocused = false"
              :disabled="!locationForm.shelf_barcode"
            />
          </div>
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
          :disabled="!isLocationFormValid"
          @click="submitLocationForm()"
        />

        <q-space class="q-mx-xs" />

        <q-btn
          outline
          no-caps
          label="Cancel"
          class="text-body1 full-width"
          @click="hideModal()"
        />
      </q-card-section>
    </template>
  </PopupModal>
</template>

<script setup>
import { ref, inject, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { useBuildingStore } from '@/stores/building-store'
import { useShelvingStore } from '@/stores/shelving-store'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'
import { useIndexDbHandler } from '@/composables/useIndexDbHandler.js'
import PopupModal from '@/components/PopupModal.vue'
import SelectInput from '@/components/SelectInput.vue'
import TextInput from '@/components/TextInput.vue'
import BarcodeBox from '@/components/BarcodeBox.vue'
import ToggleButtonInput from '@/components/ToggleButtonInput.vue'

const route = useRoute()

// Props
const mainProps = defineProps({
  shelvingItem: {
    type: Object,
    default: () => {
      return {
        id: null,
        owner_id: null,
        size_class_id: null
      }
    }
  }
})

// Emits
const emit = defineEmits(['hide'])

// Compasables
const { compiledBarCode } = useBarcodeScanHandler()
const { addDataToIndexDb } = useIndexDbHandler()

// Store Data
const { appActionIsLoadingData, appIsOffline } = storeToRefs(useGlobalStore())
const {
  modules,
  aisles,
  ladders,
  shelves,
  shelvesPositions
} = storeToRefs(useOptionStore())
const {
  resetModuleChildren,
  resetAisleChildren,
  resetSideChildren,
  resetLadderChildren,
  getSideList
} = useBuildingStore()
const { sides } = storeToRefs(useBuildingStore())
const { postShelvingJobContainerProposedLocation, resetShelvingJobContainer } = useShelvingStore()
const { shelvingJob } = storeToRefs(useShelvingStore())

// Local Data
const editShelvingLocationModal = ref(null)
const locationForm = ref({
  id: null,
  barcode: null,
  building_id: null,
  module_id: null,
  aisle_id: null,
  side_id: 1,
  ladder_id: null,
  shelf_id: null,
  shelf_position_id: null,
  shelf_barcode: null,
  shelf_position_number: null,
  trayed: false
})
const isLocationFormValid = computed(() => {
  // validate that all needed fields are filled out in the building form
  if (appIsOffline.value) {
    return locationForm.value.shelf_position_number ?? false
  } else {
    return locationForm.value.shelf_position_id ?? false
  }
})
const shelvesPositionInputFocused = ref(false)

// Logic
const handleAlert = inject('handle-alert')

onMounted(() => {
  // set the form data if shelvingItem is passed in
  if (mainProps.shelvingItem.id) {
    const itemLocationIdList = mainProps.shelvingItem.shelf_position?.internal_location?.split('-')
    locationForm.value.id = mainProps.shelvingItem.id
    locationForm.value.barcode = mainProps.shelvingItem.barcode.value
    locationForm.value.building_id = parseInt(itemLocationIdList[0])
    locationForm.value.module_id = parseInt(itemLocationIdList[1])
    locationForm.value.aisle_id = parseInt(itemLocationIdList[2])
    locationForm.value.side_id = parseInt(itemLocationIdList[3])
    locationForm.value.ladder_id = parseInt(itemLocationIdList[4])
    locationForm.value.shelf_id = parseInt(itemLocationIdList[5])
    locationForm.value.shelf_barcode = appIsOffline.value ? null : mainProps.shelvingItem.shelf_position?.shelf?.barcode?.value ?? ''
    locationForm.value.trayed = mainProps.shelvingItem.container_type?.type == 'Tray' ? true : false
  }
})

watch(compiledBarCode, (barcode) => {
  if (barcode !== '' && appIsOffline.value && !shelvesPositionInputFocused.value) {
    locationForm.value.shelf_barcode = barcode
    locationForm.value.shelf_position_number = null
  }
})

const handleLocationFormChange = async (valueType) => {
  // reset the form depending on the edited form field type and clear any related building state as needed
  switch (valueType) {
    case 'Module':
      // clear state for aisle options downward since user needs to select an aisle next to populate the rest of the data
      resetModuleChildren()
      locationForm.value.aisle_id = null
      locationForm.value.side_id = null
      locationForm.value.ladder_id = null
      locationForm.value.shelf_id = null
      locationForm.value.shelf_position_id = null
      return
    case 'Aisle':
      resetAisleChildren()
      // get sides since sides are buttons and not dynamically loaded from a options select input
      await getSideList({
        building_id: locationForm.value.building_id,
        module_id: locationForm.value.module_id,
        aisle_id: locationForm.value.aisle_id
      })
      locationForm.value.side_id = null
      locationForm.value.ladder_id = null
      locationForm.value.shelf_id = null
      locationForm.value.shelf_position_id = null
      return
    case 'Side':
      resetSideChildren()
      locationForm.value.ladder_id = null
      locationForm.value.shelf_id = null
      locationForm.value.shelf_position_id = null
      return
    case 'Ladder':
      resetLadderChildren()
      locationForm.value.shelf_id = null
      locationForm.value.shelf_position_id = null
      return
    case 'Shelf':
      locationForm.value.shelf_position_id = null
      return
  }
}
const resetLocationForm = () => {
  locationForm.value = {
    id: null,
    barcode: null,
    building_id: null,
    module_id: null,
    aisle_id: null,
    side_id: '',
    ladder_id: null,
    shelf_id: null,
    shelf_position_id: null,
    shelf_barcode: null,
    shelf_position_number: null,
    trayed: false
  }
  emit('hide')
}
const submitLocationForm = async () => {
  try {
    appActionIsLoadingData.value = true

    // if app is offline then we only allow the user to scan the shelf and enter a position
    let payload
    if (appIsOffline.value) {
      payload = {
        job_id: route.params.jobId,
        trayed: locationForm.value.trayed,
        container_id: locationForm.value.id,
        container_barcode_value: locationForm.value.barcode,
        shelf_position_number: locationForm.value.shelf_position_number,
        shelf_barcode_value: locationForm.value.shelf_barcode
      }
    } else {
      payload = {
        job_id: route.params.jobId,
        trayed: locationForm.value.trayed,
        container_id: locationForm.value.id,
        container_barcode_value: locationForm.value.barcode,
        shelf_position_number: shelvesPositions.value.find(shelf_pos => shelf_pos.id == locationForm.value.shelf_position_id)?.shelf_position_number?.number,
        shelf_id: locationForm.value.shelf_id
      }
    }

    await postShelvingJobContainerProposedLocation(payload)

    // when offline we need to directly update the shelving status and shelving job container as the job level
    if (appIsOffline.value) {
      if (payload.trayed) {
        shelvingJob.value.trays[shelvingJob.value.trays.findIndex(container => container.id == payload.container_id)].shelf_position.shelf_position_number.number = payload.shelf_position_number
        shelvingJob.value.trays[shelvingJob.value.trays.findIndex(container => container.id == payload.container_id)].shelf_position.shelf.barcode.value = payload.shelf_barcode_value
      } else {
        shelvingJob.value.non_tray_items[shelvingJob.value.non_tray_items.findIndex(container => container.id == payload.container_id)].shelf_position.shelf_position_number.number = payload.shelf_position_number
        shelvingJob.value.non_tray_items[shelvingJob.value.non_tray_items.findIndex(container => container.id == payload.container_id)].shelf_position.shelf.barcode.value = payload.shelf_barcode_value
      }
    }

    // update the stored shelvingJob since the container will get changed at the shelvingJob level
    addDataToIndexDb('shelvingStore', 'shelvingJob', JSON.parse(JSON.stringify(shelvingJob.value)))

    handleAlert({
      type: 'success',
      text: 'The container has been updated.',
      autoClose: true
    })
    resetShelvingJobContainer()
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
    resetLocationForm()
    editShelvingLocationModal.value.hideModal()
  }
}

defineExpose({ locationForm })
</script>

<style lang="scss" scoped>
</style>