
<template>
  <div class="admin-shipping-settings q-pa-xs-sm q-pa-sm-lg">
    <div class="row q-mb-lg">
      <div class="col-12">
        <h1 class="text-h4 text-bold">
          Shipping Module Settings
        </h1>
        <p class="text-body1 q-mt-sm text-grey-7">
          Enable or disable the Shipping Module workflow.
        </p>
      </div>
    </div>

    <div class="row">
      <div class="col-xs-12 col-sm-6 col-md-4">
        <div class="q-pa-md bg-white rounded-borders shadow-1">
          <label class="text-h6 text-bold q-mb-md block">
            Shipping Module Status
          </label>

          <q-toggle
            v-model="isEnabled"
            :label="isEnabled ? 'Enabled' : 'Disabled'"
            color="positive"
            class="q-mb-md"
            left-label
            size="lg"
            @update:model-value="updateSetting"
          />

          <div class="q-mt-lg">
            <div class="text-body2 text-grey-7">
              <q-icon
                name="info"
                class="q-mr-xs"
              />
              <span v-if="isEnabled">
                Shipping workflow is active. Items from Pick Lists will be set to "Retrieved" and require a Shipping Job to move to "Out".
              </span>
              <span v-else>
                Shipping workflow is disabled. Items from Pick Lists will be set directly to "Out" upon completion.
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { storeToRefs } from 'pinia'
import { notify } from '@/utils/notify'

// Store Data
const { appIsLoadingData, appActionIsLoadingData } = storeToRefs(useGlobalStore())
const { getSystemSetting, postSystemSetting, patchSystemSetting } = useOptionStore()

// Local Data
const isEnabled = ref(false)
const settingExists = ref(false)
const settingName = 'shipping_module_enabled'

// Logic

onMounted(async () => {
  await loadCurrentSetting()
})

const loadCurrentSetting = async () => {
  try {
    appIsLoadingData.value = true
    const data = await getSystemSetting(settingName)
    isEnabled.value = data.value === 'true'
    settingExists.value = true
  } catch (error) {
    // If setting doesn't exist, use default (false)
    isEnabled.value = false
    settingExists.value = false
  } finally {
    appIsLoadingData.value = false
  }
}

const updateSetting = async (newValue) => {
  try {
    appActionIsLoadingData.value = true
    const strValue = String(newValue)

    if (settingExists.value) {
      // Setting exists, update it
      await patchSystemSetting(settingName, {
        value: strValue
      })
    } else {
      // Setting doesn't exist, create it
      await postSystemSetting({
        key: settingName,
        value: strValue,
        description: 'Enable or disable the Shipping Module workflow.'
      })
      settingExists.value = true
    }

    notify({
      type: 'positive',
      message: `Shipping Module ${newValue ? 'Enabled' : 'Disabled'} Successfully.`
    })
  } catch (error) {
    console.error('Update error:', error)
    notify({
      type: 'negative',
      message: 'Failed to update setting.'
    })
    // Revert to previous value
    await loadCurrentSetting()
  } finally {
    appActionIsLoadingData.value = false
  }
}
</script>

<style lang="scss" scoped>
</style>
