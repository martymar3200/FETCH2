<template>
  <q-page padding>
    <div class="row q-mb-lg">
      <div class="col-12">
        <h1 class="text-h4 text-bold">
          User Settings
        </h1>
      </div>
    </div>

    <div class="row">
      <div class="col-12 col-md-8 col-lg-6">
        <q-card
          flat
          bordered
        >
          <q-card-section>
            <div class="text-h6">
              Personal Information
            </div>
          </q-card-section>

          <q-separator />

          <q-card-section>
            <div class="row q-col-gutter-y-md">
              <div class="col-12 col-sm-6">
                <div class="text-caption text-grey-7">
                  First Name
                </div>
                <div class="text-body1 text-weight-medium">
                  {{ userData.first_name }}
                </div>
              </div>

              <div class="col-12 col-sm-6">
                <div class="text-caption text-grey-7">
                  Last Name
                </div>
                <div class="text-body1 text-weight-medium">
                  {{ userData.last_name }}
                </div>
              </div>

              <div class="col-12 col-sm-6">
                <div class="text-caption text-grey-7">
                  Email Address
                </div>
                <div class="text-body1 text-weight-medium">
                  {{ userData.email }}
                </div>
              </div>

              <div class="col-12">
                <q-separator class="q-my-md" />
                <div class="text-h6 q-mb-md">
                  Preferences
                </div>
              </div>

              <div class="col-12 col-sm-6">
                <div class="form-group">
                  <label class="form-group-label q-mb-xs">
                    Default Building Location
                  </label>
                  <div class="text-caption text-grey-7 q-mb-sm">
                    This building will be auto-selected for you in creation modals (e.g., Pick Lists, Jobs).
                  </div>
                  <SelectInput
                    v-model="defaultBuildingId"
                    :options="buildingsList"
                    option-type="buildings"
                    option-value="id"
                    option-label="name"
                    placeholder="Select Default Building"
                    aria-label="defaultBuildingSelect"
                    @update:model-value="onBuildingChange"
                    :loading="saving"
                  />
                </div>
              </div>
            </div>
          </q-card-section>
        </q-card>
      </div>
    </div>
  </q-page>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user-store'
import { useOptionStore } from '@/stores/option-store'
import { storeToRefs } from 'pinia'
import SelectInput from '@/components/SelectInput.vue'
import { notify } from '@/utils/notify'

const userStore = useUserStore()
const { userData } = storeToRefs(userStore)

const optionStore = useOptionStore()
const { buildings } = storeToRefs(optionStore)

const buildingsList = ref([])
const defaultBuildingId = ref(null)
const saving = ref(false)

onMounted(async () => {
  // Load buildings for the dropdown if not already loaded
  if (buildings.value.length === 0) {
    await optionStore.getOptions('buildings')
  }
  buildingsList.value = buildings.value

  // Set initial value from user profile
  defaultBuildingId.value = userData.value.default_building_id || null
})

const onBuildingChange = async (newVal) => {
  saving.value = true
  try {
    await userStore.updateUserProfile(userData.value.user_id, {
      default_building_id: newVal
    })
    notify({
      type: 'positive',
      message: 'Default building updated successfully'
    })
  } catch (error) {
    notify({
      type: 'negative',
      message: 'Failed to update default building'
    })
    // Revert on failure
    defaultBuildingId.value = userData.value.default_building_id || null
    console.error(error)
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
</style>
