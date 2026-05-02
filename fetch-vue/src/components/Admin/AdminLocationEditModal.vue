<template>
  <PopupModal
    ref="locationModal"
    :show-actions="false"
    @reset="emit('hide')"
    aria-label="locationEditModal"
  >
    <template #header-content="{ hideModal }">
      <q-card-section class="row items-center q-pb-none">
        <h2 class="text-h6 text-bold">
          {{ actionType === 'Add' ? 'Add New' : actionType === 'Insert' ? 'Insert & Shift' : 'Edit' }} {{ singularLevel }}
        </h2>

        <BaseButton
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
            <!-- Text / Number inputs -->
            <div
              v-if="!field.options"
              class="q-mb-md"
              :class="field.field === 'width' || field.field === 'height' ? 'col-xs-12 col-sm-4' : field.field === 'depth' ? 'col-xs-12 col-sm-4 q-px-sm-sm' : 'col-12'"
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

            <!-- Select inputs -->
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
                  :option-label="field.field === 'container_type_id' || field.field === 'shelf_type_id' ? 'type' : 'name'"
                  :placeholder="`Select ${field.label}`"
                  :disabled="field.disabled"
                  :clearable="!field.required"
                  @update:model-value="handleFieldChange(field.label)"
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
        <BaseButton
          no-caps
          unelevated
          color="accent"
          :label="actionType === 'Add' ? `Add ${singularLevel}` : actionType === 'Insert' ? `Insert ${singularLevel}` : `Update ${singularLevel}`"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          :disabled="!isFormValid"
          @click="actionType === 'Add' ? addEntity() : actionType === 'Insert' ? insertEntity() : updateEntity()"
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
import { ref, computed, onBeforeMount } from 'vue'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { useBuildingStore } from '@/stores/building-store'
import { notify } from '@/utils/notify'
import { storeToRefs } from 'pinia'
import SelectInput from '@/components/SelectInput.vue'
import TextInput from '@/components/TextInput.vue'
import PopupModal from '@/components/PopupModal.vue'

// Props
const props = defineProps({
  actionType: {
    type: String,
    required: true
  },
  locationLevel: {
    type: String,
    required: true
  },
  locationData: {
    type: Object,
    default: () => ({})
  },
  parentIds: {
    type: Object,
    default: () => ({})
  }
})

// Emits
const emit = defineEmits([
  'hide',
  'saved'
])

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
  patchShelve,
  postInsertShelve
} = useBuildingStore()


// Local Data
const locationModal = ref(null)
const locationFields = ref([])
const locationForm = ref({})

const singularLevel = computed(() => {
  const map = {
    Buildings: 'Building',
    Modules: 'Module',
    Aisles: 'Aisle',
    Ladders: 'Ladder',
    Shelves: 'Shelf'
  }
  return map[props.locationLevel] || props.locationLevel
})

const isFormValid = computed(() => {
  const optionalFields = locationFields.value.flatMap(f => !f.required ? f.field : [])
  return Object.keys(locationForm.value).every(key =>
    optionalFields.includes(key) || (locationForm.value[key] !== null && locationForm.value[key] !== '')
  )
})

const disableShelfType = computed(() => !locationForm.value.size_class_id)

const filteredShelfTypes = computed(() => {
  if (shelfTypes.value.length > 0) {
    return shelfTypes.value.filter(st => st.size_class_id == locationForm.value.size_class_id)
  }
  return []
})

// Logic

onBeforeMount(() => {
  generateForm()
})

const handleFieldChange = (labelType) => {
  if (labelType === 'Container Size' && locationForm.value.shelf_type_id) {
    locationForm.value.shelf_type_id = null
  }
}

const generateForm = () => {
  switch (props.locationLevel) {
    case 'Buildings':
      locationForm.value = {
        name: props.locationData.name ?? ''
      }
      locationFields.value = [
        {
          field: 'name',
          label: 'Building Name',
          required: true
        }
      ]
      break

    case 'Modules':
      locationForm.value = {
        building_id: props.parentIds.building_id,
        module_number: props.locationData.module_number ?? ''
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

    case 'Aisles':
      locationForm.value = {
        module_id: props.parentIds.module_id,
        sort_priority: props.locationData.sort_priority ?? null,
        aisle_number: props.locationData.aisle_number ?? ''
      }
      locationFields.value = [
        {
          field: 'aisle_number',
          label: 'Aisle Number',
          fieldType: 'number',
          required: true,
          disabled: props.actionType === 'Edit'
        },
        {
          field: 'sort_priority',
          fieldType: 'number',
          label: 'Aisle Priority'
        }
      ]
      break

    case 'Ladders':
      locationForm.value = {
        side_id: props.parentIds.side_id,
        sort_priority: props.locationData.sort_priority ?? null,
        ladder_number: props.locationData.ladder_number ?? ''
      }
      locationFields.value = [
        {
          field: 'ladder_number',
          label: 'Ladder Number',
          fieldType: 'number',
          required: true,
          disabled: props.actionType === 'Edit'
        },
        {
          field: 'sort_priority',
          fieldType: 'number',
          label: 'Ladder Priority'
        }
      ]
      break

    case 'Shelves':
      locationForm.value = {
        ladder_id: props.parentIds.ladder_id,
        owner_id: props.locationData.owner?.id ?? null,
        size_class_id: props.locationData.shelf_type?.size_class_id ?? null,
        shelf_type_id: props.locationData.shelf_type?.id ?? null,
        container_type_id: props.locationData.container_type?.id ?? null,
        width: props.locationData.width ?? '',
        depth: props.locationData.depth ?? '',
        height: props.locationData.height ?? '',
        barcode_value: props.locationData.barcode?.value ?? '',
        sort_priority: props.locationData.sort_priority ?? null,
        shelf_number: props.locationData.shelf_number ?? ''
      }
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
          disabled: props.actionType === 'Edit' && props.locationData.barcode?.value ? true : false,
          required: true
        },
        {
          field: 'shelf_number',
          label: 'Shelf Number',
          fieldType: 'number',
          disabled: props.actionType === 'Edit',
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
  }
}

const addEntity = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = locationForm.value

    switch (props.locationLevel) {
      case 'Buildings':
        await postBuilding(payload)
        break
      case 'Modules':
        await postModule(payload)
        break
      case 'Aisles':
        await postAisle(payload)
        break
      case 'Ladders':
        await postLadder(payload)
        break
      case 'Shelves':
        await postShelve(payload)
        break
    }

    notify({
      type: 'positive',
      message: `Successfully added a new ${singularLevel.value}`
    })

    emit('saved')
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || `Failed to add ${singularLevel.value}`
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}

const insertEntity = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = locationForm.value

    // insert only applies to Shelves
    if (props.locationLevel === 'Shelves') {
      await postInsertShelve(payload)
    }

    notify({
      type: 'positive',
      message: `Successfully inserted a new ${singularLevel.value}`
    })

    emit('saved')
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || `Failed to insert ${singularLevel.value}`
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}

const updateEntity = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: props.locationData.id,
      ...locationForm.value
    }

    switch (props.locationLevel) {
      case 'Buildings':
        await patchBuilding(payload)
        break
      case 'Modules':
        await patchModule(payload)
        break
      case 'Aisles':
        await patchAisle(payload)
        break
      case 'Ladders':
        await patchLadder(payload)
        break
      case 'Shelves':
        if (payload.sort_priority === '') {
          payload.sort_priority = null
        }
        await patchShelve(payload)
        break
    }

    notify({
      type: 'positive',
      message: `Successfully updated the ${singularLevel.value}`
    })

    emit('saved')
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || `Failed to update ${singularLevel.value}`
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}
</script>

<style lang="scss" scoped>
</style>
