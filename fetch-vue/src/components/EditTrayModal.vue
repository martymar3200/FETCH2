<template>
  <PopupModal
    ref="editModal"
    @submit="submitEdit"
    @hide="resetForm"
  >
    <template #header-content="{ hideModal }">
      <q-card-section class="row items-center q-pb-none">
        <h2 class="text-h6 text-bold">
          Edit Tray
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
      <q-card-section>
        <p v-if="trayData">
          Editing Tray Barcode: <strong>{{ trayData.barcode?.value || 'N/A' }}</strong>
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

    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <BaseButton
          variant="primary"
          label="Save Changes"
          class="text-body1 full-width"
          :loading="isLoading"
          @click="submitEdit"
        />
        <q-space class="q-mx-xs" />
        <BaseButton
          variant="outline"
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
import BaseButton from '@/components/Base/BaseButton.vue'

const props = defineProps({
  trayData: {
    type: Object,
    default: () => null
  },
  verificationJobId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['success'])

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

// ======================================================================
// =================== START: THE ROBUST WATCHER ========================
// ======================================================================

// This watcher now ensures the options for the current values are loaded
// before the user can see the form.
watch(() => props.trayData, async (newTray) => {
  if (newTray && newTray.id) {
    // 1. Set the form values immediately.
    form.value.owner_id = newTray.owner_id
    form.value.size_class_id = newTray.size_class_id
    form.value.media_type_id = newTray.media_type_id

    // 2. Check if each option exists. If not, fetch it specifically.
    // The `combineOptions = true` parameter in getExactOptionById is crucial.
    if (newTray.owner_id && !optionStore.owners.some(o => o.id === newTray.owner_id)) {
      await optionStore.getExactOptionById('owners', newTray.owner_id, true)
    }
    if (newTray.size_class_id && !optionStore.sizeClass.some(s => s.id === newTray.size_class_id)) {
      await optionStore.getExactOptionById('sizeClass', newTray.size_class_id, true)
    }
    if (newTray.media_type_id && !optionStore.mediaTypes.some(m => m.id === newTray.media_type_id)) {
      await optionStore.getExactOptionById('mediaTypes', newTray.media_type_id, true)
    }
  }
}, { immediate: true }) // 'immediate' runs this once on component creation.

// ======================================================================
// ==================== END: THE ROBUST WATCHER =========================
// ======================================================================


// onMounted still fetches the full lists in the background for the user
// to choose from if they want to change the value.
onMounted(() => {
  optionStore.getOptions('owners', { size: 100 })
  optionStore.getOptions('sizeClass', { size: 100 })
  optionStore.getOptions('mediaTypes', { size: 100 })
})

const submitEdit = async () => {
  if (!props.trayData) {
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    const payload = {
      id: props.trayData.id,
      ...form.value
    }

    if (props.verificationJobId) {
      payload.verification_job_id = props.verificationJobId
    }

    await recordManagementStore.patchTray(payload)

    emit('success')
    editModal.value.hideModal()
  } catch (error) {
    if (error.response?.data?.detail) {
      errorMessage.value = error.response.data.detail
    } else {
      errorMessage.value = 'An unexpected error occurred while saving the tray.'
    }
  } finally {
    isLoading.value = false
  }
}

// Reset the form when the modal is hidden
const resetForm = () => {
  errorMessage.value = ''
  if (props.trayData) {
    form.value.owner_id = props.trayData.owner_id
    form.value.size_class_id = props.trayData.size_class_id
    form.value.media_type_id = props.trayData.media_type_id
  }
}

defineExpose({
  showModal: () => editModal.value.showModal()
})
</script>