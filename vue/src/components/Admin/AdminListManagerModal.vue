<template>
  <PopupModal
    ref="listModal"
    :show-actions="false"
    @reset="emit('hide')"
    aria-label="listOptionAddOrEditModal"
  >
    <template #header-content="{ hideModal }">
      <q-card-section class="row items-center q-pb-none">
        <h2 class="text-h6 text-bold">
          {{ actionType == 'Add' ? 'Add New' : 'Edit' }} {{ titleCaseListType }}
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
            v-for="field in inputFields"
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
                  v-model="inputForm[field.field]"
                  :placeholder="`Enter ${field.label}`"
                  :type="field.fieldType ?? ''"
                  :disabled="field.disabled"
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
                  v-model="inputForm[field.field]"
                  :multiple="field.allowMultiple"
                  :use-chips="field.allowMultiple"
                  :hide-selected="!field.allowMultiple"
                  :options="field.options"
                  :option-type="field.optionType"
                  option-value="id"
                  :option-label="field.field == 'container_type_id' ? 'type' : 'name'"
                  :placeholder="`Select ${field.label}`"
                  :disabled="field.disabled"
                  :clearable="!field.required"
                  @update:model-value="listType == 'shelf-type' ? updateShelfTypeSizeClass($event) : handleInputFormChange(field.field)"
                  :aria-label="`${field.field}Select`"
                />
              </div>
            </div>
          </template>

          <!-- custom shelf type max capacity inputs -->
          <q-expansion-item
            v-if="inputForm.size_classes && inputForm.size_classes.length > 0"
            class="col-12 q-mb-md"
            header-class="text-body1 q-px-xs-none q-px-sm-sm underline"
            label="Max Capacity"
          >
            <template
              v-for="sc in inputForm.size_classes"
              :key="sc.id"
            >
              <div class="row items-center q-my-md">
                <label class="col-grow">
                  {{ sc.name }}
                </label>
                <div class="col-4">
                  <TextInput
                    v-model="sc.max_capacity"
                    placeholder="Enter Capacity"
                    :aria-label="`shelf_type_max_capacity_input`"
                    type="number"
                  />
                </div>
              </div>
            </template>
          </q-expansion-item>
        </div>
      </q-card-section>
    </template>

    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <q-btn
          no-caps
          unelevated
          color="accent"
          :label="actionType == 'Add' ? `Add ${titleCaseListType}` : `Update ${titleCaseListType}`"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          :disabled="isInputFormValid"
          @click="actionType == 'Add' ? addNewListType() : updateListType()"
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
import { ref, computed, onBeforeMount, inject, toRaw } from 'vue'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { storeToRefs } from 'pinia'
import SelectInput from '@/components/SelectInput.vue'
import TextInput from '@/components/TextInput.vue'
import PopupModal from '@/components/PopupModal.vue'

// Props
const mainProps = defineProps({
  actionType: {
    type: String,
    required: true
  },
  listType: {
    type: String,
    required: true
  },
  listData: {
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
  'newListOptionAdded'
])

// Composables

// Store Data
const { appActionIsLoadingData } = storeToRefs(useGlobalStore())
const {
  sizeClass,
  shelfTypes,
  ownersTiers,
  parentOwnerOptions,
  optionsTotal
} = storeToRefs(useOptionStore())
const {
  getParentOwnerOptions,
  postSizeClass,
  patchSizeClass,
  postOwner,
  patchOwner,
  postMediaType,
  patchMediaType,
  postShelfType,
  patchShelfType,
  deleteShelfType,
  getOptions
} = useOptionStore()

// Local Data
const titleCaseListType = computed(() => {
  let title = mainProps.listType.split('-')
  title = title.map(s => s.replace(s[0], s[0].toUpperCase()))
  return title.join(' ')
})
const listModal = ref(null)
const inputFields = ref([])
const inputForm = ref({})
const inputFormOriginal = ref({})
const isInputFormValid = computed(() => {
  const optionalFields = inputFields.value.flatMap(f => !f.required ? f.field : [] )
  // filter out excess form data if its not included in the fields since we only care about defined fields
  const mainForm = Object.fromEntries(inputFields.value.flatMap(f => f.field).map(mainField => [
    mainField,
    inputForm.value[mainField]
  ]))
  // check the main form fields and see if the required fields have proper values
  return !Object.keys(mainForm).every(key => optionalFields.includes(key) || mainForm[key] !== null && mainForm[key] !== '' && mainForm[key].length !== 0)
})
const disableParentOwnerInput = computed(() => {
  // Owner tier of 1 does not have a parent owner. Disable parent owner if no tier selected or if tier 1 is selected
  if (!inputForm.value.owner_tier_id) {
    return true
  } else {
    return ownersTiers.value.find(
      (ot) => ot.id === inputForm.value.owner_tier_id
    )?.level === 1
  }
})
const parentOwnerRequired = computed(() => {
  return !disableParentOwnerInput.value
})

// Logic
const handleAlert = inject('handle-alert')

onBeforeMount(() => {
  generateListModal()
})

const generateListModal = async () => {
  // creates the modal fields needed based on the listType
  switch (mainProps.listType) {
    case 'size-class':
      inputForm.value = {
        name: mainProps.listData.name ?? '',
        short_name: mainProps.listData.short_name ?? '',
        width: mainProps.listData.width ?? '',
        depth: mainProps.listData.depth ?? '',
        height: mainProps.listData.height ?? ''
      }
      // create a copy of our input form
      inputFormOriginal.value = { ...toRaw(inputForm.value) }

      inputFields.value = [
        {
          field: 'name',
          label: 'Full Name',
          required: true
        },
        {
          field: 'short_name',
          label: 'Short Name',
          required: true
        },
        {
          field: 'width',
          label: 'Width (in)',
          fieldType: 'number',
          required: true
        },
        {
          field: 'depth',
          label: 'Depth (in)',
          fieldType: 'number',
          required: true
        },
        {
          field: 'height',
          label: 'Height (in)',
          fieldType: 'number',
          required: true
        }
      ]
      break
    case 'media-type':
      inputForm.value = {
        name: mainProps.listData.name ?? ''
      }
      // create a copy of our input form
      inputFormOriginal.value = { ...toRaw(inputForm.value) }

      inputFields.value = [
        {
          field: 'name',
          label: 'Name',
          required: true
        }
      ]
      break
    case 'owner':
      inputForm.value = {
        owner_tier_id: mainProps.listData.owner_tier_id ?? '',
        parent_owner_id: mainProps.listData.parent_owner_id ?? null,
        name: mainProps.listData.name ?? ''
      }
      // create a copy of our input form
      inputFormOriginal.value = { ...toRaw(inputForm.value) }

      inputFields.value = [
        {
          field: 'owner_tier_id',
          label: 'Owner Tier',
          options: ownersTiers,
          optionType: 'ownersTiers',
          required: true
        },
        {
          field: 'parent_owner_id',
          label: 'Parent Owner',
          options: parentOwnerOptions,
          required: parentOwnerRequired,
          disabled: disableParentOwnerInput
        },
        {
          field: 'name',
          label: 'Owner Name',
          required: true
        }
      ]

      break
    case 'shelf-type': {
      const matchingShelfTypes = shelfTypes.value.filter(s => s.type == mainProps.listData.type)
      inputForm.value = {
        type: mainProps.listData.type ?? '',
        size_class_ids: matchingShelfTypes.map(s => s.size_class_id) ?? [],
        size_classes: matchingShelfTypes.map(s => ({
          ...s.size_class,
          max_capacity: s.max_capacity,
          shelf_type_id: s.id
        })) ?? []
      }
      // create a copy of our input form
      inputFormOriginal.value = { ...toRaw(inputForm.value) }

      //TEMP loop the shelf type size class options until we get all size class data needed for the modal
      await getOptions('sizeClass', {
        size: 50,
        sort_by: 'name'
      })
      if (optionsTotal.value > 50) {
        let page = 2
        let totalPages = Math.ceil(optionsTotal.value/50)
        while (page <= totalPages) {
          await getOptions('sizeClass', {
            size: 50,
            sort_by: 'name',
            page
          }, true)
          page++
        }
      }

      inputFields.value = [
        {
          field: 'type',
          label: 'Shelf Type Name',
          required: true
        },
        {
          field: 'size_class_ids',
          label: 'Size Class',
          options: sizeClass,
          required: true,
          allowMultiple: true
        }
      ]
      break
    }
    default:
      break
  }
}

const addNewListType = async () => {
  try {
    appActionIsLoadingData.value = true
    // send api request to add a new list option by the listType
    const payload = inputForm.value
    switch (mainProps.listType) {
      case 'size-class':
        await postSizeClass(payload)
        break
      case 'media-type':
        await postMediaType(payload)
        break
      case 'shelf-type':
      // generate an individual shelf type for each size class selection
        await Promise.all(inputForm.value.size_classes.map(sizeClassObj => {
          return postShelfType({
            type: payload.type,
            size_class_id: sizeClassObj.id,
            max_capacity: sizeClassObj.max_capacity
          })
        }))
        break
      case 'owner':
        await postOwner(payload)
        break
      default:
        break
    }

    handleAlert({
      type: 'success',
      text: `Successfully added a new ${titleCaseListType.value}.`,
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
    // emit to parent that we added a new list option
    emit('newListOptionAdded')
    listModal.value.hideModal()
  }
}

const updateListType = async () => {
  try {
    appActionIsLoadingData.value = true
    // send api request to update an existing list option by the listType
    let payload = {
      id: mainProps.listData.id,
      ...inputForm.value
    }
    switch (mainProps.listType) {
      case 'size-class': {
        await patchSizeClass(payload)
        break
      }
      case 'media-type':
        await patchMediaType(payload)
        break
      case 'shelf-type': {
      //check if we removed size class selections and send updates delete the corressponding shelf type from the api
        let removedSizeClasses = []
        removedSizeClasses = inputFormOriginal.value.size_classes.filter(oSizeClass => !inputForm.value.size_class_ids.includes(oSizeClass.id))
        if (removedSizeClasses.length > 0) {
          await Promise.all(removedSizeClasses.map(async sizeClassObj => {
            const res = await deleteShelfType(sizeClassObj.shelf_type_id)
            if (res.status !== 200) {
              handleAlert({
                type: 'error',
                text: `The shelf type: "${payload.type} - ${sizeClassObj.name}" is in use and cannot be deleted.`,
                autoClose: false
              })
            }
          }))
        }

        //check if we added new size class selections and create the corressponding shelf type
        let newSizeClasses = []
        newSizeClasses = inputForm.value.size_classes.filter(curSizeClass => !inputFormOriginal.value.size_class_ids.includes(curSizeClass.id))
        if (newSizeClasses.length > 0) {
          await Promise.all(newSizeClasses.map(sizeClassObj => {
            return postShelfType({
              type: payload.type,
              size_class_id: sizeClassObj.id,
              max_capacity: sizeClassObj.max_capacity
            })
          }))
        }

        // generate an individual shelf type update for every current shelf type by size class
        let currentSizeClasses = inputForm.value.size_classes.filter(curSizeClass => inputFormOriginal.value.size_class_ids.includes(curSizeClass.id))
        await Promise.all(currentSizeClasses.map(async sizeClassObj => {
          const res = await patchShelfType({
            id: sizeClassObj.shelf_type_id,
            type: payload.type,
            size_class_id: sizeClassObj.id,
            max_capacity: sizeClassObj.max_capacity
          })
          if (res.status !== 200) {
            handleAlert({
              type: 'error',
              text: `"${payload.type} - ${sizeClassObj.name}" - ${res.response.data.detail}`,
              autoClose: false
            })
          }
        }))
        break
      }
      case 'owner': {
        await patchOwner(payload)
        break
      }
      default:
        break
    }

    handleAlert({
      type: 'success',
      text: `Successfully updated the ${titleCaseListType.value}.`,
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
    listModal.value.hideModal()
  }
}

const updateShelfTypeSizeClass = (sizeClassIdArr) => {
  // custom function for shelf types only
  // when creating/updating a shelf type we need to map the corresponding added/removed user size class input to our input form
  const selectedSizeClasses = sizeClassIdArr.map(id => {
    return {
      ...sizeClass.value.find(s => s.id == id),
      max_capacity: 1
    }
  })

  // if there are already existing size_classes that match any of the selectedSizeClasses we need to ignore updating them
  if (inputForm.value.size_classes.length > 0) {
    const removedSizeClasses = inputForm.value.size_classes.filter(sizeClassObj => !sizeClassIdArr.includes(sizeClassObj.id))
    const newSizeClasses = selectedSizeClasses.filter(sizeClassObj => !inputForm.value.size_classes.flatMap(s => s.id).includes(sizeClassObj.id))

    // remove the deleted sizeClasses
    if (removedSizeClasses) {
      inputForm.value.size_classes = inputForm.value.size_classes.filter(sizeClassObj => !removedSizeClasses.flatMap(s => s.id).includes(sizeClassObj.id))
    }
    // add the new sizeClasses
    if (newSizeClasses) {
      inputForm.value.size_classes = [
        ...inputForm.value.size_classes,
        ...newSizeClasses
      ]
    }
  } else {
    inputForm.value.size_classes = selectedSizeClasses
  }
}

const handleInputFormChange = async (field) => {
  switch (mainProps.listType) {
    case 'owner':
      if (field === 'owner_tier_id') {
      // Get the parent owners for the currently selected tier
        inputForm.value.parent_owner_id = null
        let currentTier = ownersTiers.value.find( (ot) => ot.id == inputForm.value.owner_tier_id)
        if (currentTier?.level > 1) {
          await getParentOwnerOptions({ owner_tier_id: ownersTiers.value.find( (ot) => ot.level === currentTier.level - 1)?.id })
        }
      }
      break
    default:
      return
  }
}
</script>

<style lang="scss" scoped>
</style>
