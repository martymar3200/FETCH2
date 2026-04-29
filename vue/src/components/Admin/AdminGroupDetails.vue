<template>
  <div class="admin-group-details">
    <div class="row q-mt-xs-sm q-mt-sm-md">
      <div class="col-12">
        <!-- group tabs -->
        <q-tabs
          v-model="activeTab"
          :outside-arrows="currentScreenSize !== 'xs'"
          mobile-arrows
          class="admin-group-details-tablist"
          active-color="accent"
          indicator-color="accent"
          align="justify"
        >
          <q-tab
            name="roles"
            label="Functional Roles (Bundles)"
            class="admin-group-details-tablist-tab"
          />

        </q-tabs>

        <!-- group tabs - content -->
        <q-tab-panels
          v-model="activeTab"
          animated
          transition-prev="slide-right"
          transition-next="slide-right"
          class="admin-group-details-tabpanels q-pa-xs-md q-pa-sm-lg"
        >
          <!-- NEW BUNDLES VIEW -->
          <q-tab-panel name="roles">
            <div class="text-body1 q-mb-md">
              Toggle these functional bundles to automatically assign the necessary granular permissions.
            </div>
            <div
              v-for="bundle in PERMISSION_BUNDLES"
              :key="bundle.name"
              class="row q-mb-xs-md q-mb-lg-lg items-center"
              style="border-bottom: 1px solid #eee; padding-bottom: 15px;"
            >
              <div class="col-xs-12 col-sm-8 col-md-10">
                <p class="text-h6 q-mb-xs">
                  {{ bundle.name }}
                </p>
                <div class="text-caption text-grey-8">
                  {{ bundle.description }}
                </div>
              </div>
              <div class="col-xs-12 col-sm-4 col-md-2">
                <ToggleButtonInput
                  :model-value="getBundleState(bundle)"
                  @update:model-value="toggleBundle(bundle, $event)"
                  :options="[
                    {label: 'On', value: true},
                    {label: 'Off', value: false}
                  ]"
                />
              </div>
            </div>
          </q-tab-panel>

          <!-- LEGACY VIEW -->

        </q-tab-panels>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onBeforeMount } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { notify } from '@/utils/notify'
import { useGlobalStore } from '@/stores/global-store'
import { useGroupStore } from '@/stores/group-store'
import { useUserStore } from '@/stores/user-store'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import ToggleButtonInput from '@/components/ToggleButtonInput.vue'
import { PERMISSION_BUNDLES } from '@/constants/permission_bundles.js'

const route = useRoute()

// Compasables
const { currentScreenSize } = useCurrentScreenSize()

// Store Data
const { appActionIsLoadingData, appIsLoadingData } = storeToRefs(useGlobalStore())
const { permissionsList, groupDetails } = storeToRefs(useGroupStore())
const {
  getPermissionsList,
  getAdminGroupPermissions,
  postAdminGroupPermission,
  deleteAdminGroupPermission
} = useGroupStore()
const { getUserPermissions } = useUserStore()

// Local Data
const activeTab = ref('roles')


// Logic


onBeforeMount(() => {
  loadAdminGroupPermissions()
})



const getBundleState = (bundle) => {
  // Check if all permissions in bundle are present
  // 1. Filter bundle.permissions that EXIST in permissionsList (avoid errors if seed data missing)
  const validPermissions = bundle.permissions.filter(bp => permissionsList.value.some(p => p.name === bp))

  if (validPermissions.length === 0) {
    return false
  }

  // 2. Check if all of them are in groupDetails.permissions
  const allPresent = validPermissions.every(bp => groupDetails.value.permissions.some(gp => gp.name === bp))

  return allPresent
}

const toggleBundle = async (bundle, turnOn) => {
  // 1. Get IDs of permissions in bundle
  const validPermissions = permissionsList.value.filter(p => bundle.permissions.includes(p.name))

  if (turnOn) {
    // Add all
    const toAdd = validPermissions.filter(p => !groupDetails.value.permissions.some(gp => gp.id === p.id))
    // Parallelize for speed
    await Promise.all(toAdd.map(p => addAdminGroupPermission(p.id)))
  } else {
    // Remove all
    const toRemove = validPermissions.filter(p => groupDetails.value.permissions.some(gp => gp.id === p.id))
    await Promise.all(toRemove.map(p => removeAdminGroupPermission(p.id)))
  }
}

const loadAdminGroupPermissions = async () => {
  try {
    appIsLoadingData.value = true
    // get all permissions first
    await getPermissionsList()

    // get the group based permissions to compare against
    await getAdminGroupPermissions(route.params.groupId)
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to load group permissions'
    })
  } finally {
    appIsLoadingData.value = false
  }
}
const addAdminGroupPermission = async (permissionId) => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      groupId: groupDetails.value.id,
      permissionId
    }
    await postAdminGroupPermission(payload)

    // reload user permissions
    await getUserPermissions()
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to update admin group permission'
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}
const removeAdminGroupPermission = async (permissionId) => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      groupId: groupDetails.value.id,
      permissionId
    }
    await deleteAdminGroupPermission(payload)

    // reload user permissions
    await getUserPermissions()
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to remove admin group permission'
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}
</script>

<style lang="scss" scoped>
.admin-group-details {
  &-tablist {
    &-tab {
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        width: 100%;
        border-bottom: 1px solid transparent;
      }

      :deep(.q-tab__label) {
        // copy of .text-h5 styling + font weight bold
        font-size: 1.5rem;
        font-weight: 700;
        line-height: 2rem;
        letter-spacing: normal;
      }

      &.q-tab--inactive::after {
        border-color: $color-gray;
      }
    }
  }

  &-tabpanels {
    :deep(.q-tab-panel) {
      padding: 0;
    }
  }
}
</style>