<template>
  <div class="admin-shelf-position-direction q-pa-xs-sm q-pa-sm-lg">
    <div class="row q-mb-lg">
      <div class="col-12">
        <h1 class="text-h4 text-bold">
          Shelf Position Direction
        </h1>
        <p class="text-body1 q-mt-sm text-grey-7">
          Configure the direction for auto-assigning shelf positions during direct-to-shelf shelving.
        </p>
      </div>
    </div>

    <div class="row">
      <div class="col-xs-12 col-sm-6 col-md-4">
        <div class="q-pa-md bg-white rounded-borders shadow-1">
          <label class="text-h6 text-bold q-mb-md block">
            Auto-Assign Direction
          </label>

          <q-option-group
            v-model="selectedDirection"
            :options="directionOptions"
            color="primary"
            class="q-mb-md"
            @update:model-value="updateDirection"
          />

          <div class="q-mt-lg">
            <div class="text-body2 text-grey-7">
              <q-icon
                name="info"
                class="q-mr-xs"
              />
              <span v-if="selectedDirection === 'low_to_high'">
                Positions will be suggested starting from 1, then 2, 3, etc.
              </span>
              <span v-else>
                Positions will be suggested starting from the highest available (e.g., 15, 14, 13...).
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
const selectedDirection = ref('low_to_high')
const settingExists = ref(false)
const directionOptions = [
  {
    label: 'Low to High (1, 2, 3...)',
    value: 'low_to_high'
  },
  {
    label: 'High to Low (...3, 2, 1)',
    value: 'high_to_low'
  }
]

// Logic


onMounted(async () => {
  await loadCurrentSetting()
})

const loadCurrentSetting = async () => {
  try {
    appIsLoadingData.value = true
    const data = await getSystemSetting('shelf_position_auto_assign_direction')
    selectedDirection.value = data.value
    settingExists.value = true
  } catch (error) {
    // If setting doesn't exist, use default and mark as not existing
    console.warn('Setting not found, will create on first update')
    selectedDirection.value = 'low_to_high'
    settingExists.value = false
  } finally {
    appIsLoadingData.value = false
  }
}

const updateDirection = async (newValue) => {
  try {
    appActionIsLoadingData.value = true

    if (settingExists.value) {
      // Setting exists, update it
      await patchSystemSetting('shelf_position_auto_assign_direction', {
        value: newValue
      })
    } else {
      // Setting doesn't exist, create it
      await postSystemSetting({
        key: 'shelf_position_auto_assign_direction',
        value: newValue,
        description: 'Direction for auto-assigning shelf positions during shelving. Options: low_to_high, high_to_low'
      })
      settingExists.value = true
    }

    notify({
      type: 'positive',
      message: 'Shelf position direction updated successfully.'
    })
  } catch (error) {
    console.error('Update error:', error)
    notify({
      type: 'negative',
      message: 'Failed to update shelf position direction.'
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
