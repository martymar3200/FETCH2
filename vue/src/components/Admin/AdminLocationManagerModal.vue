<template>
  <PopupModal
    ref="locationModal"
    :show-actions="false"
    @reset="emit('hide')"
    aria-label="locationAddOrEditModal"
  >
    <template #header-content="{ hideModal }">
      <q-card-section class="row items-center q-pb-none">
        <h2 class="text-h6 text-bold">
          {{ actionType == 'Add' ? 'Add New' : 'Edit' }} {{ titleCaseLocationType }}
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
      <q-card-section class="q-pb-none">
        <div class="row">
          <template
            v-for="field in locationFields"
            :key="field.field"
          >
            <!-- text inputs -->
            <div
              v-if="!field.options"
              class="q-mb-md"
              :class="field.field == 'width' || field.field == 'height' ? 'col-xs-12 col-sm-4' : field.field == 'depth' ? 'col-xs-12 col-sm-4 q-px-sm-sm' : 'col-12'"
            >
              <div class="form-group">
                <label class="form-group-label">
                  {{ field.label }}
                  <span
                    v-if="field.required"
                    class="text-caption text-negative"
                  >
                    (Required)
                  </span>
                </label>
                <TextInput
                  v-model="locationForm[field.field]"
                  :placeholder="`Enter ${field.label}`"
                  :disabled="field.disabled"
                  :type="field.fieldType ?? ''"
                  :aria-label="`${field.field}_input`"
                />
              </div>
            </div>
            <!-- select inputs -->
            <div
              v-else
              class="col-12 q-mb-md"
            >
              <div class="form-group">
                <label class="form-group-label">
                  {{ field.label }}
                  <span
                    v-if="field.required"
                    class="text-caption text-negative"
                  >
                    (Required)
                  </span>
                </label>
                <SelectInput
                  v-model="locationForm[field.field]"
                  :options="field.options"
                  :option-type="field.optionType"
                  option-value="id"
                  :option-label="field.field == 'container_type_id' || field.field == 'shelf_type_id' ? 'type' : 'name'"
                  :placeholder="`Select ${field.label}`"
                  :disabled="field.disabled"
                  :clearable="!field.required"
                  @update:model-value="handleLocationFormChange(field.label)"
                  :aria-label="`${field.field}Select`"
                />
              </div>
            </div>
          </template>
        </div>
      </q-card-section>
    </template>

    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <q-btn
          no-caps
          unelevated
          color="accent"
          :label="actionType == 'Add' ? `Add ${titleCaseLocationType}` : `Update ${titleCaseLocationType}`"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          :disabled="isLocationFormValid"
          @click="actionType == 'Add' ? addNewLocationType() : updateLocationType()"
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
import { ref, computed, onBeforeMount, inject } from 'vue'
import { useRoute } from 'vue-router'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { useBuildingStore } from '@/stores/building-store'
import { useBarcodeStore } from '@/stores/barcode-store'
import { storeToRefs } from 'pinia'
import SelectInput from '@/components/SelectInput.vue'
import TextInput from '@/components/TextInput.vue'
import PopupModal from '@/components/PopupModal.vue'

const route = useRoute()

// Props
const mainProps = defineProps({
  actionType: {
    type: String,
    required: true
  },
  locationType: {
    type: String,
    required: true
  },
  locationData: {
    type: Object,
    default: () => {
      return {}
    }
  }
})

// Emits
const emit = defineEmits([
  'hide',
  'submit',
  'newLocationAdded'
])

// Composables

// Store Data
const { appActionIsLoadingData } = storeToRefs(useGlobalStore())
const {
  owners,
  sizeClass,
  shelfTypes,
  containerTypes
} = storeToRefs(useOptionStore())
const {
  postBuilding,
  patchBuilding,
  postModule,
  patchModule,
  postAisle,
  patchAisle,
  postLadder,
  patchLadder,
  postShelve,
  patchShelve
} = useBuildingStore()
const { verifyBarcode } = useBarcodeStore()
const { barcodeDetails } = storeToRefs(useBarcodeStore())

// Local Data
const titleCaseLocationType = computed(() => {
  return mainProps.locationType.replace(mainProps.locationType[0], mainProps.locationType[0].toUpperCase()).slice(0, -1)
})
const locationModal = ref(null)
const locationFields = ref(null)
const locationForm = ref({})
const isLocationFormValid = computed(() => {
  const optionalFields = locationFields.value.flatMap(f => !f.required ? f.field : [] )
  return !Object.keys(locationForm.value).every(key => optionalFields.includes(key) || locationForm.value[key] !== null && locationForm.value[key] !== '')
})
const disableShelfType = computed(() => {
  return !locationForm.value.size_class_id ? true : false
})
const filteredShelfTypes =  computed(() => {
  let shelfTypesBySizeClass = []
  if (shelfTypes.value.length > 0) {
    shelfTypesBySizeClass = shelfTypes.value.filter(st => st.size_class_id == locationForm.value.size_class_id)
  }
  return shelfTypesBySizeClass
})

// Logic
const handleAlert = inject('handle-alert')

onBeforeMount(() => {
  generateLocationModal()
})

const handleLocationFormChange = async (labelType) => {
  // reset the form depending on the edited form field type
  switch (labelType) {
    case 'Container Size':
      if (locationForm.value.shelf_type_id) {
        locationForm.value.shelf_type_id = null
      }
      return
  }
}

const generateLocationModal = () => {
  // creates the modal fields needed based on the locationType
  switch (mainProps.locationType) {
    case 'buildings':
      locationForm.value = {
        name: mainProps.locationData.name ?? ''
      }
      locationFields.value = [
        {
          field: 'name',
          label: 'Building Name',
          required: true
        }
      ]
      break
    case 'modules':
      locationForm.value = {
        building_id: route.params.buildingId,
        module_number: mainProps.locationData.module_number ?? ''
      }
      locationFields.value = [
        {
          field: 'module_number',
          label: 'Module Number',
          fieldType: 'number',
          required: true
        }
      ]
      break
    case 'aisles':
      locationForm.value = {
        module_id: route.params.moduleId,
        sort_priority: mainProps.locationData.sort_priority ?? null,
        aisle_number: mainProps.locationData.aisle_number?.number ?? ''
      }
      locationFields.value = [
        {
          field: 'aisle_number',
          label: 'Aisle Number',
          fieldType: 'number',
          required: true,
          disabled: mainProps.actionType == 'Edit'
        },
        {
          field: 'sort_priority',
          fieldType: 'number',
          label: 'Aisle Priority'
        }
      ]
      break
    case 'ladders':
      locationForm.value = {
        side_id: route.params.sideId,
        sort_priority: mainProps.locationData.sort_priority ?? null,
        ladder_number: mainProps.locationData.ladder_number?.number ?? ''
      }
      locationFields.value = [
        {
          field: 'ladder_number',
          label: 'Ladder Number',
          fieldType: 'number',
          required: true,
          disabled: mainProps.actionType == 'Edit'
        },
        {
          field: 'sort_priority',
          fieldType: 'number',
          label: 'Ladder Priority'
        }
      ]
      break
    case 'shelves':
      locationForm.value = {
        ladder_id: route.params.ladderId,
        owner_id: mainProps.locationData.owner?.id ?? null,
        size_class_id: mainProps.locationData.shelf_type?.size_class_id ?? null,
        shelf_type_id: mainProps.locationData.shelf_type?.id ?? null,
        container_type_id: mainProps.locationData.container_type?.id ?? null,
        width: mainProps.locationData.width ?? '',
        depth: mainProps.locationData.depth ?? '',
        height: mainProps.locationData.height ?? '',
        barcode_value: mainProps.locationData.barcode?.value ?? '',
        sort_priority: mainProps.locationData.sort_priority ?? null,
        shelf_number: mainProps.locationData.shelf_number?.number ?? ''
      },
      locationFields.value = [
        {
          field: 'owner_id',
          label: 'Owner',
          options: owners,
          optionType: 'owners',
          required: true
        },
        {
          field: 'size_class_id',
          label: 'Container Size',
          options: sizeClass,
          optionType: 'sizeClass',
          required: true
        },
        {
          field: 'shelf_type_id',
          label: 'Shelf Type',
          options: filteredShelfTypes,
          optionType: 'shelfTypes',
          required: true,
          disabled: disableShelfType
        },
        {
          field: 'container_type_id',
          label: 'Container Type',
          options: containerTypes,
          optionType: 'containerTypes',
          required: true
        },
        {
          field: 'barcode_value',
          label: 'Shelf Barcode',
          disabled: mainProps.actionType == 'Edit' && mainProps.locationData.barcode?.value ? true : false,
          required: true
        },
        {
          field: 'shelf_number',
          label: 'Shelf Number',
          fieldType: 'number',
          disabled: mainProps.actionType == 'Edit',
          required: true
        },
        {
          field: 'sort_priority',
          fieldType: 'number',
          label: 'Shelf Priority'
        },
        {
          field: 'width',
          fieldType: 'number',
          label: 'Width (in)',
          required: true
        },
        {
          field: 'depth',
          fieldType: 'number',
          label: 'Depth (in)',
          required: true
        },
        {
          field: 'height',
          fieldType: 'number',
          label: 'Height (in)',
          required: true
        }
      ]
      break
    default:
      break
  }
}

const addNewLocationType = async () => {
  try {
    appActionIsLoadingData.value = true
    // send api request to add a new location by the locationType
    const payload = locationForm.value
    switch (mainProps.locationType) {
      case 'buildings':
        await postBuilding(payload)
        break
      case 'modules':
        await postModule(payload)
        break
      case 'aisles':
        await postAisle(payload)
        break
      case 'ladders':
        await postLadder(payload)
        break
      case 'shelves':
        // if payload includes a shelf barcode validate it and create the shelf barcode
        if (payload.barcode_value !== '') {
          const res = await verifyBarcode(payload.barcode_value, 'Shelf', true)
          if (res == 'barcode_exists') {
          // if the inputed shelf barcode exists throw an error since shelf barcode has to be new when adding new shelves
            handleAlert({
              type: 'error',
              text: 'The shelf barcode inputed already exists. Please try again.',
              autoClose: true
            })
            return
          }
          payload.barcode_id = barcodeDetails.value.id
        }
        await postShelve(payload)
        break
      default:
        break
    }

    handleAlert({
      type: 'success',
      text: `Successfully Added A New ${titleCaseLocationType.value}`,
      autoClose: true
    })
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    // emit to parent that we added a new location option
    emit('newLocationAdded')

    appActionIsLoadingData.value = false
    locationModal.value.hideModal()
  }
}

const updateLocationType = async () => {
  try {
    appActionIsLoadingData.value = true
    // send api request to add a new location by the locationType
    let payload = {
      id: mainProps.locationData.id,
      ...locationForm.value
    }
    switch (mainProps.locationType) {
      case 'buildings':
        await patchBuilding(payload)
        break
      case 'modules':
        await patchModule(payload)
        break
      case 'aisles':
        await patchAisle(payload)
        break
      case 'ladders':
        await patchLadder(payload)
        break
      case 'shelves':
        // if payload includes a shelf barcode validate it and create the shelf barcode (this only occurs when a user bulk uploads a shelf without a barcode)
        if (!mainProps.locationData.barcode?.value) {
          const res = await verifyBarcode(payload.barcode_value, 'Shelf', true)
          if (res == 'barcode_exists') {
            // if the inputed shelf barcode exists throw an error since shelf barcode has to be new when adding new shelves
            handleAlert({
              type: 'error',
              text: 'The shelf barcode inputed already exists. Please try again.',
              autoClose: true
            })
            return
          }
          payload.barcode_id = barcodeDetails.value.id
        }

        // convert empty payload value for sort priority to be null since backend expects int values only
        if (payload.sort_priority == '') {
          payload.sort_priority = null
        }
        await patchShelve(payload)
        break
      default:
        break
    }

    handleAlert({
      type: 'success',
      text: `Successfully Updated The ${titleCaseLocationType.value}`,
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
    locationModal.value.hideModal()
  }
}
</script>

<style lang="scss" scoped>
</style>
