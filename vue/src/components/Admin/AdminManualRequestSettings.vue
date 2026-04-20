<template>
  <div class="row">
    <div class="col-12 col-md-6 col-lg-4">
      <q-card
        flat
        bordered
      >
        <q-card-section>
          <div class="text-h6">
            Manual Request Settings
          </div>
          <div class="text-subtitle2 q-mb-md">
            Select which fields should be strictly required when users are creating manual requests.
          </div>

          <div class="column q-gutter-y-sm">
            <q-checkbox
              v-model="requiredFields"
              val="requestor_name"
              label="Requestor Name"
            />
            <q-checkbox
              v-model="requiredFields"
              val="priority_id"
              label="Priority"
            />
            <q-checkbox
              v-model="requiredFields"
              val="request_type_id"
              label="Request Type"
            />
            <q-checkbox
              v-model="requiredFields"
              val="delivery_location_id"
              label="Delivery Location"
            />
          </div>
        </q-card-section>

        <q-card-actions align="right">
          <BaseButton
            label="Save Settings"
            color="accent"
            :loading="appActionIsLoadingData"
            @click="saveSettings"
          />
        </q-card-actions>
      </q-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Notify } from 'quasar'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import BaseButton from '@/components/Base/BaseButton.vue'

// Store
const optionStore = useOptionStore()
const { appActionIsLoadingData } = storeToRefs(useGlobalStore())

// State
const requiredFields = ref([])
const isExistingSetting = ref(false)
const settingKey = 'manual_request_required_fields'

onMounted(async () => {
  try {
    const setting = await optionStore.getSystemSetting(settingKey)
    if (setting && setting.value) {
      isExistingSetting.value = true
      requiredFields.value = JSON.parse(setting.value)
    }
  } catch (e) {
    // Setting might not exist yet, default to empty array
    requiredFields.value = []
  }
})

const saveSettings = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      key: settingKey,
      value: JSON.stringify(requiredFields.value),
      is_active: true
    }

    if (isExistingSetting.value) {
      await optionStore.patchSystemSetting(settingKey, payload)
    } else {
      await optionStore.postSystemSetting(payload)
      isExistingSetting.value = true
    }

    Notify.create({
      type: 'positive',
      message: 'Successfully updated manual request settings.'
    })
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error?.response?.data?.detail || error.message || 'Failed to save settings'
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}
</script>

<style lang="scss" scoped>
</style>
