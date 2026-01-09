<template>
  <div class="admin-child-owner-shelving q-pa-xs-sm q-pa-sm-lg">
    <div class="row q-mb-lg">
      <div class="col-12">
        <h1 class="text-h4 text-bold">
          Child Owner Shelving
        </h1>
        <p class="text-body1 q-mt-sm text-grey-7">
          Configure whether child owner items can be shelved on parent owner shelves.
        </p>
      </div>
    </div>

    <div class="row">
      <div class="col-xs-12 col-sm-6 col-md-5">
        <div class="q-pa-md bg-white rounded-borders shadow-1">
          <label class="text-h6 text-bold q-mb-md block">
            Allow Child Owner Shelving
          </label>

          <q-toggle
            v-model="allowChildOwnerShelving"
            :label="allowChildOwnerShelving ? 'Enabled' : 'Disabled'"
            color="primary"
            size="lg"
            @update:model-value="updateSetting"
          />

          <div class="q-mt-lg">
            <div class="text-body2 text-grey-7">
              <q-icon
                name="info"
                class="q-mr-xs"
              />
              <span v-if="allowChildOwnerShelving">
                Child owner items CAN be shelved on parent owner shelves.
                The shelf owner will remain as the parent owner.
              </span>
              <span v-else>
                Child owner items CANNOT be shelved on parent owner shelves.
                An exact owner match is required (unless shelf is Unassigned).
              </span>
            </div>
          </div>

          <div class="q-mt-md text-caption text-grey-6">
            <strong>Note:</strong> Parent owner items cannot be shelved on child owner shelves regardless of this setting.
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { storeToRefs } from 'pinia'

// Store Data
const { appIsLoadingData, appActionIsLoadingData } = storeToRefs(useGlobalStore())
const { getSystemSetting, postSystemSetting, patchSystemSetting } = useOptionStore()

// Local Data
const allowChildOwnerShelving = ref(false)
const settingExists = ref(false)

// Logic
const handleAlert = inject('handle-alert')

onMounted(async () => {
  await loadCurrentSetting()
})

const loadCurrentSetting = async () => {
  try {
    appIsLoadingData.value = true
    const data = await getSystemSetting('allow_child_owner_shelving')
    allowChildOwnerShelving.value = data.value === 'true'
    settingExists.value = true
  } catch (error) {
    // If setting doesn't exist, use default and mark as not existing
    console.warn('Setting not found, will create on first update')
    allowChildOwnerShelving.value = false
    settingExists.value = false
  } finally {
    appIsLoadingData.value = false
  }
}

const updateSetting = async (newValue) => {
  try {
    appActionIsLoadingData.value = true
    const valueStr = newValue ? 'true' : 'false'

    if (settingExists.value) {
      // Setting exists, update it
      await patchSystemSetting('allow_child_owner_shelving', {
        value: valueStr
      })
    } else {
      // Setting doesn't exist, create it
      await postSystemSetting({
        key: 'allow_child_owner_shelving',
        value: valueStr,
        description: 'Allow child owner items to be shelved on parent owner shelves'
      })
      settingExists.value = true
    }

    handleAlert({
      type: 'success',
      text: 'Child owner shelving setting updated successfully.',
      autoClose: true
    })
  } catch (error) {
    console.error('Update error:', error)
    handleAlert({
      type: 'error',
      text: 'Failed to update child owner shelving setting.',
      autoClose: true
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
