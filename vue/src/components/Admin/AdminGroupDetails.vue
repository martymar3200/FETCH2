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
            name="accession"
            label="Accession"
            class="admin-group-details-tablist-tab"
          />
          <q-tab
            name="verification"
            label="Verification"
            class="admin-group-details-tablist-tab"
          />
          <q-tab
            name="shelving"
            label="Shelving"
            class="admin-group-details-tablist-tab"
          />
          <q-tab
            name="request"
            label="Request"
            class="admin-group-details-tablist-tab"
          />
          <q-tab
            name="picklist"
            label="Picklist"
            class="admin-group-details-tablist-tab"
          />
          <q-tab
            name="refile"
            label="Refile"
            class="admin-group-details-tablist-tab"
          />
          <q-tab
            name="withdraw"
            label="Withdraw"
            class="admin-group-details-tablist-tab"
          />
          <q-tab
            name="reporting"
            label="Reporting"
            class="admin-group-details-tablist-tab"
          />
          <q-tab
            name="admin"
            label="Admin"
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
          <q-tab-panel name="accession">
            <div
              v-if="!isStageOrProd"
              class="row q-mb-xs-md q-mb-lg-lg"
            >
              <q-btn
                no-caps
                unelevated
                color="accent"
                label="Enable All Accession Permissions"
                class="btn-no-wrap text-body1"
                @click="toggleAllPermissions('accession')"
              />
            </div>
            <div
              v-for="permission in permissionsList.filter(p => p.name.includes('accession'))"
              :key="permission.id"
              class="row q-mb-xs-md q-mb-lg-lg"
            >
              <div class="col-xs-12 col-sm-8 col-md-10 flex items-center">
                <p :class="currentScreenSize !== 'xs' ? 'text-h6' : 'text-body1 q-mb-xs'">
                  {{ renderPermissionName(permission.name) }}
                </p>
              </div>
              <div class="col-xs-12 col-sm-4 col-md-2">
                <div class="form-group">
                  <ToggleButtonInput
                    :model-value="renderPermissionValue(permission)"
                    @update:model-value="$event == true ? addAdminGroupPermission(permission.id) : removeAdminGroupPermission(permission.id)"
                    :options="[
                      {label: 'Yes', value: true},
                      {label: 'No', value: false}
                    ]"
                  />
                </div>
              </div>
            </div>
          </q-tab-panel>

          <q-tab-panel name="verification">
            <div
              v-if="!isStageOrProd"
              class="row q-mb-xs-md q-mb-lg-lg"
            >
              <q-btn
                no-caps
                unelevated
                color="accent"
                label="Enable All Verification Permissions"
                class="btn-no-wrap text-body1"
                @click="toggleAllPermissions('verification')"
              />
            </div>
            <div
              v-for="permission in permissionsList.filter(p => p.name.includes('verification'))"
              :key="permission.id"
              class="row q-mb-xs-md q-mb-lg-lg"
            >
              <div class="col-xs-12 col-sm-8 col-md-10 flex items-center">
                <p :class="currentScreenSize !== 'xs' ? 'text-h6' : 'text-body1 q-mb-xs'">
                  {{ renderPermissionName(permission.name) }}
                </p>
              </div>
              <div class="col-xs-12 col-sm-4 col-md-2">
                <div class="form-group">
                  <ToggleButtonInput
                    :model-value="renderPermissionValue(permission)"
                    @update:model-value="$event == true ? addAdminGroupPermission(permission.id) : removeAdminGroupPermission(permission.id)"
                    :options="[
                      {label: 'Yes', value: true},
                      {label: 'No', value: false}
                    ]"
                  />
                </div>
              </div>
            </div>
          </q-tab-panel>

          <q-tab-panel name="shelving">
            <div
              v-if="!isStageOrProd"
              class="row q-mb-xs-md q-mb-lg-lg"
            >
              <q-btn
                no-caps
                unelevated
                color="accent"
                label="Enable All Shelving Permissions"
                class="btn-no-wrap text-body1"
                @click="toggleAllPermissions('shelving')"
              />
            </div>
            <div
              v-for="permission in permissionsList.filter(p => p.name.includes('shelving'))"
              :key="permission.id"
              class="row q-mb-xs-md q-mb-lg-lg"
            >
              <div class="col-xs-12 col-sm-8 col-md-10 flex items-center">
                <p :class="currentScreenSize !== 'xs' ? 'text-h6' : 'text-body1 q-mb-xs'">
                  {{ renderPermissionName(permission.name) }}
                </p>
              </div>
              <div class="col-xs-12 col-sm-4 col-md-2">
                <div class="form-group">
                  <ToggleButtonInput
                    :model-value="renderPermissionValue(permission)"
                    @update:model-value="$event == true ? addAdminGroupPermission(permission.id) : removeAdminGroupPermission(permission.id)"
                    :options="[
                      {label: 'Yes', value: true},
                      {label: 'No', value: false}
                    ]"
                  />
                </div>
              </div>
            </div>
          </q-tab-panel>

          <q-tab-panel name="request">
            <div
              v-if="!isStageOrProd"
              class="row q-mb-xs-md q-mb-lg-lg"
            >
              <q-btn
                no-caps
                unelevated
                color="accent"
                label="Enable All Request Permissions"
                class="btn-no-wrap text-body1"
                @click="toggleAllPermissions('request')"
              />
            </div>
            <div
              v-for="permission in permissionsList.filter(p => p.name.includes('request'))"
              :key="permission.id"
              class="row q-mb-xs-md q-mb-lg-lg"
            >
              <div class="col-xs-12 col-sm-8 col-md-10 flex items-center">
                <p :class="currentScreenSize !== 'xs' ? 'text-h6' : 'text-body1 q-mb-xs'">
                  {{ renderPermissionName(permission.name) }}
                </p>
              </div>
              <div class="col-xs-12 col-sm-4 col-md-2">
                <div class="form-group">
                  <ToggleButtonInput
                    :model-value="renderPermissionValue(permission)"
                    @update:model-value="$event == true ? addAdminGroupPermission(permission.id) : removeAdminGroupPermission(permission.id)"
                    :options="[
                      {label: 'Yes', value: true},
                      {label: 'No', value: false}
                    ]"
                  />
                </div>
              </div>
            </div>
          </q-tab-panel>

          <q-tab-panel name="picklist">
            <div
              v-if="!isStageOrProd"
              class="row q-mb-xs-md q-mb-lg-lg"
            >
              <q-btn
                no-caps
                unelevated
                color="accent"
                label="Enable All Picklist Permissions"
                class="btn-no-wrap text-body1"
                @click="toggleAllPermissions('picklist')"
              />
            </div>
            <div
              v-for="permission in permissionsList.filter(p => p.name.includes('picklist'))"
              :key="permission.id"
              class="row q-mb-xs-md q-mb-lg-lg"
            >
              <div class="col-xs-12 col-sm-8 col-md-10 flex items-center">
                <p :class="currentScreenSize !== 'xs' ? 'text-h6' : 'text-body1 q-mb-xs'">
                  {{ renderPermissionName(permission.name) }}
                </p>
              </div>
              <div class="col-xs-12 col-sm-4 col-md-2">
                <div class="form-group">
                  <ToggleButtonInput
                    :model-value="renderPermissionValue(permission)"
                    @update:model-value="$event == true ? addAdminGroupPermission(permission.id) : removeAdminGroupPermission(permission.id)"
                    :options="[
                      {label: 'Yes', value: true},
                      {label: 'No', value: false}
                    ]"
                  />
                </div>
              </div>
            </div>
          </q-tab-panel>

          <q-tab-panel name="refile">
            <div
              v-if="!isStageOrProd"
              class="row q-mb-xs-md q-mb-lg-lg"
            >
              <q-btn
                no-caps
                unelevated
                color="accent"
                label="Enable All Refile Permissions"
                class="btn-no-wrap text-body1"
                @click="toggleAllPermissions('refile')"
              />
            </div>
            <div
              v-for="permission in permissionsList.filter(p => p.name.includes('refile'))"
              :key="permission.id"
              class="row q-mb-xs-md q-mb-lg-lg"
            >
              <div class="col-xs-12 col-sm-8 col-md-10 flex items-center">
                <p :class="currentScreenSize !== 'xs' ? 'text-h6' : 'text-body1 q-mb-xs'">
                  {{ renderPermissionName(permission.name) }}
                </p>
              </div>
              <div class="col-xs-12 col-sm-4 col-md-2">
                <div class="form-group">
                  <ToggleButtonInput
                    :model-value="renderPermissionValue(permission)"
                    @update:model-value="$event == true ? addAdminGroupPermission(permission.id) : removeAdminGroupPermission(permission.id)"
                    :options="[
                      {label: 'Yes', value: true},
                      {label: 'No', value: false}
                    ]"
                  />
                </div>
              </div>
            </div>
          </q-tab-panel>

          <q-tab-panel name="withdraw">
            <div
              v-if="!isStageOrProd"
              class="row q-mb-xs-md q-mb-lg-lg"
            >
              <q-btn
                no-caps
                unelevated
                color="accent"
                label="Enable All Withdraw Permissions"
                class="btn-no-wrap text-body1"
                @click="toggleAllPermissions('withdraw')"
              />
            </div>
            <div
              v-for="permission in permissionsList.filter(p => p.name.includes('withdraw'))"
              :key="permission.id"
              class="row q-mb-xs-md q-mb-lg-lg"
            >
              <div class="col-xs-12 col-sm-8 col-md-10 flex items-center">
                <p :class="currentScreenSize !== 'xs' ? 'text-h6' : 'text-body1 q-mb-xs'">
                  {{ renderPermissionName(permission.name) }}
                </p>
              </div>
              <div class="col-xs-12 col-sm-4 col-md-2">
                <div class="form-group">
                  <ToggleButtonInput
                    :model-value="renderPermissionValue(permission)"
                    @update:model-value="$event == true ? addAdminGroupPermission(permission.id) : removeAdminGroupPermission(permission.id)"
                    :options="[
                      {label: 'Yes', value: true},
                      {label: 'No', value: false}
                    ]"
                  />
                </div>
              </div>
            </div>
          </q-tab-panel>

          <q-tab-panel name="reporting">
            <div
              v-if="!isStageOrProd"
              class="row q-mb-xs-md q-mb-lg-lg"
            >
              <q-btn
                no-caps
                unelevated
                color="accent"
                label="Enable All Reports Permissions"
                class="btn-no-wrap text-body1"
                @click="toggleAllPermissions('reports'); toggleAllPermissions('search'); toggleAllPermissions('detail');"
              />
            </div>
            <div
              v-for="permission in permissionsList.filter(p => p.name.includes('reports') || p.name.includes('search') || p.name.includes('detail'))"
              :key="permission.id"
              class="row q-mb-xs-md q-mb-lg-lg"
            >
              <div class="col-xs-12 col-sm-8 col-md-10 flex items-center">
                <p :class="currentScreenSize !== 'xs' ? 'text-h6' : 'text-body1 q-mb-xs'">
                  {{ renderPermissionName(permission.name) }}
                </p>
              </div>
              <div class="col-xs-12 col-sm-4 col-md-2">
                <div class="form-group">
                  <ToggleButtonInput
                    :model-value="renderPermissionValue(permission)"
                    @update:model-value="$event == true ? addAdminGroupPermission(permission.id) : removeAdminGroupPermission(permission.id)"
                    :options="[
                      {label: 'Yes', value: true},
                      {label: 'No', value: false}
                    ]"
                  />
                </div>
              </div>
            </div>
          </q-tab-panel>

          <q-tab-panel name="admin">
            <div
              v-if="!isStageOrProd"
              class="row q-mb-xs-md q-mb-lg-lg"
            >
              <q-btn
                no-caps
                unelevated
                color="accent"
                label="Enable All Admin Permissions"
                class="btn-no-wrap text-body1"
                @click="toggleAllPermissions('admin'); toggleAllPermissions('manage');"
              />
            </div>
            <div
              v-for="permission in permissionsList.filter(p => p.name.includes('admin') || p.name.includes('manage') || p.name === 'can_edit_tray'|| p.name === 'can_edit_non_tray_item')"
              :key="permission.id"
              class="row q-mb-xs-md q-mb-lg-lg"
            >
              <div class="col-xs-12 col-sm-8 col-md-10 flex items-center">
                <p :class="currentScreenSize !== 'xs' ? 'text-h6' : 'text-body1 q-mb-xs'">
                  {{ renderPermissionName(permission.name) }}
                </p>
              </div>
              <div class="col-xs-12 col-sm-4 col-md-2">
                <div class="form-group">
                  <ToggleButtonInput
                    :model-value="renderPermissionValue(permission)"
                    @update:model-value="$event == true ? addAdminGroupPermission(permission.id) : removeAdminGroupPermission(permission.id)"
                    :options="[
                      {label: 'Yes', value: true},
                      {label: 'No', value: false}
                    ]"
                  />
                </div>
              </div>
            </div>
          </q-tab-panel>
        </q-tab-panels>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onBeforeMount, computed } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store'
import { useGroupStore } from '@/stores/group-store'
import { useUserStore } from '@/stores/user-store'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import ToggleButtonInput from '@/components/ToggleButtonInput.vue'

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
const activeTab = ref('accession')
const isStageOrProd = computed(() => {
  return process.env.VITE_ENV == 'production' || process.env.VITE_ENV == 'stage'
})

// Logic
const handleAlert = inject('handle-alert')

onBeforeMount(() => {
  loadAdminGroupPermissions()
})

const renderPermissionValue = (permissionData) => {
  // check if the passed in permission exists in our group
  if (groupDetails.value.permissions.some(perm => perm.id == permissionData.id)) {
    return true
  } else {
    return false
  }
}
const renderPermissionName = (permissionName) => {
  // removes all _ characters and replace the first letter in each word with to uppercase (regex)
  // ex: some_permission_here = Some Permission Here
  return permissionName.replaceAll('_', ' ').replace(/(^\w{1})|(\s{1}\w{1})/g, match => match.toUpperCase())
}

const toggleAllPermissions = (groupName) => {
  const permissionsByGroup = permissionsList.value.filter(p => p.name.includes(groupName))
  // add all the permissions by the passed in groupName
  // ex passing in 'accession' will set all permissions related to accession to be enabled
  permissionsByGroup.forEach(p => {
    if (!groupDetails.value.permissions.some(ep => ep.name == p.name)) {
      addAdminGroupPermission(p.id)
    }
  })
}

const loadAdminGroupPermissions = async () => {
  try {
    appIsLoadingData.value = true
    // get all permissions first
    await getPermissionsList()

    // get the group based permissions to compare against
    await getAdminGroupPermissions(route.params.groupId)
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
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
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
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
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
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