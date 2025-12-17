<template>
  <!-- This structure EXACTLY mirrors your working EditTrayModal -->
  <PopupModal
    ref="editModal"
    :show-actions="false"
    @reset="emit('hide')"
    @hide="resetForm"
    aria-label="editNonTrayItemModal"
  >
    <!-- Header -->
    <template #header-content="{ hideModal }">
      <q-card-section class="row items-center q-pb-none">
        <h2 class="text-h6 text-bold">
          Edit Non-Tray Item
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

    <!-- Main Content (The Form) -->
    <template #main-content>
      <q-card-section class="q-pb-none">
        <p v-if="itemData">
          Editing Item Barcode: <strong>{{ itemData.barcode?.value || 'N/A' }}</strong>
        </p>

        <!-- Owner Dropdown -->
        <div class="form-group q-mb-md">
          <label class="form-group-label">Owner</label>
          <SelectInput
            v-model="form.owner_id"
            :options="owners"
            option-value="id"
            option-label="name"
            option-type="owners"
            placeholder="Select an Owner"
            :clearable="false"
          />
        </div>

        <!-- Size Class Dropdown -->
        <div class="form-group q-mb-md">
          <label class="form-group-label">Size Class</label>
          <SelectInput
            v-model="form.size_class_id"
            :options="sizeClass"
            option-value="id"
            option-label="name"
            option-type="sizeClass"
            placeholder="Select a Size Class"
            :clearable="false"
          />
        </div>

        <!-- Media Type Dropdown -->
        <div class="form-group q-mb-md">
          <label class="form-group-label">Media Type</label>
          <SelectInput
            v-model="form.media_type_id"
            :options="mediaTypes"
            option-value="id"
            option-label="name"
            option-type="mediaTypes"
            placeholder="Select a Media Type"
            :clearable="false"
          />
        </div>

        <!-- Error Message Display -->
        <div
          v-if="errorMessage"
          class="text-negative q-mt-md"
        >
          <q-icon
            name="warning"
            class="q-mr-sm"
          />
          {{ errorMessage }}
        </div>
      </q-card-section>
    </template>

    <!-- Footer (Action Buttons) -->
    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <q-btn
          no-caps
          unelevated
          color="accent"
          label="Save Changes"
          class="text-body1 full-width"
          :loading="isLoading"
          @click="submitEdit"
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
import { ref, watch, onMounted } from 'vue'
import { useRecordManagementStore } from '@/stores/record-management-store'
import { useOptionStore } from '@/stores/option-store'
import { storeToRefs } from 'pinia'
import PopupModal from '@/components/PopupModal.vue'
import SelectInput from '@/components/SelectInput.vue'

const props = defineProps({
  itemData: {
    type: Object,
    default: () => null
  }
})

const emit = defineEmits([
  'hide',
  'success'
])

// Store setup
const recordManagementStore = useRecordManagementStore()
const optionStore = useOptionStore()
const { owners, sizeClass, mediaTypes } = storeToRefs(optionStore)
const isLoading = ref(false)
const errorMessage = ref('')

// Form state
const form = ref({
  owner_id: null,
  size_class_id: null,
  media_type_id: null
})

const editModal = ref(null)

// Watcher to pre-populate the form
watch(() => props.itemData, async (newItem) => {
  if (newItem && newItem.id) {
    form.value.owner_id = newItem.owner_id
    form.value.size_class_id = newItem.size_class_id
    form.value.media_type_id = newItem.media_type_id

    if (newItem.owner_id && !optionStore.owners.some(o => o.id === newItem.owner_id)) {
      await optionStore.getExactOptionById('owners', newItem.owner_id, true)
    }
    if (newItem.size_class_id && !optionStore.sizeClass.some(s => s.id === newItem.size_class_id)) {
      await optionStore.getExactOptionById('sizeClass', newItem.size_class_id, true)
    }
    if (newItem.media_type_id && !optionStore.mediaTypes.some(m => m.id === newItem.media_type_id)) {
      await optionStore.getExactOptionById('mediaTypes', newItem.media_type_id, true)
    }
  }
}, { immediate: true })

// Fetch full lists for dropdowns
onMounted(() => {
  optionStore.getOptions('owners', { size: 100 })
  optionStore.getOptions('sizeClass', { size: 100 })
  optionStore.getOptions('mediaTypes', { size: 100 })
})

const submitEdit = async () => {
  if (!props.itemData) {
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    // =================== THE NEW PAYLOAD ==================
    // ======================================================
    // This will trigger the audit logic in the back-end.
    const payload = {
      id: props.itemData.id,
      owner_id: form.value.owner_id,
      size_class_id: form.value.size_class_id,
      media_type_id: form.value.media_type_id,
      verification_job_id: props.itemData.verification_job_id // <-- CRITICAL ADDITION
    }
    // ======================================================

    await recordManagementStore.patchNonTrayItem(payload)

    emit('success')
    if (editModal.value) {
      editModal.value.hideModal()
    }
  } catch (error) {
    if (error.response?.data?.detail) {
      errorMessage.value = error.response.data.detail
    } else {
      errorMessage.value = 'An unexpected error occurred while saving the item.'
    }
  } finally {
    isLoading.value = false
  }
}

const resetForm = () => {
  errorMessage.value = ''
  if (props.itemData) {
    form.value.owner_id = props.itemData.owner_id
    form.value.size_class_id = props.itemData.size_class_id
    form.value.media_type_id = props.itemData.media_type_id
  }
}

defineExpose({
  showModal: () => {
    if (editModal.value) {
      editModal.value.showModal()
    }
  }
})
</script>