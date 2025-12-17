<template>
  <div class="admin-groups q-pa-xs-sm q-pa-sm-lg">
    <div class="row q-mb-xs-md q-mb-sm-lg">
      <div class="col-12">
        <h1 class="text-h4 text-bold">
          Groups &amp; Permissions
        </h1>
      </div>
    </div>

    <div class="row">
      <div
        v-for="group in groupList"
        :key="group.id"
        class="col-xs-12 col-sm-4 col-md-3 q-pa-xs-xs q-pa-lg-sm q-pa-xl-md"
      >
        <q-card
          flat
          bordered
          class="admin-groups-card"
        >
          <q-card-section class="admin-groups-card-details q-pa-md">
            <MoreOptionsMenu
              :options="[
                { text: 'Edit Permissions' },
                { text: 'Add/Edit User(s) in Group' },
                { text: 'Rename Group Name' },
                { text: 'Delete Group', optionClass: 'text-negative' }
              ]"
              class="q-mr-sm"
              @click="handleOptionMenu($event, group)"
            />
            <p class="text-h5 text-bold">
              {{ group.name }}
            </p>
          </q-card-section>
        </q-card>
      </div>

      <div class="col-xs-12 col-sm-4 col-md-3 q-pa-xs-xs q-pa-lg-sm q-pa-xl-md">
        <q-btn
          no-caps
          unelevated
          outline
          icon="add"
          label="Add New Group"
          align="left"
          class="admin-groups-card admin-groups-card-dashed text-h5 text-bold"
          @click="showAddGroupModal = true"
        />
      </div>
    </div>
  </div>

  <!-- new group modal -->
  <PopupModal
    v-if="showAddGroupModal"
    ref="addGroupModal"
    :title="'Add New Group'"
    @reset="resetAddGroupModal"
    aria-label="newGroupModal"
  >
    <template #main-content>
      <q-card-section class="column no-wrap items-center">
        <div class="form-group">
          <label class="form-group-label">
            Group Name
          </label>
          <TextInput
            v-model="groupDetails.name"
            placeholder="Enter Group Name"
          />
        </div>
      </q-card-section>
    </template>

    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <q-btn
          no-caps
          unelevated
          color="accent"
          label="Submit"
          class="text-body1 full-width"
          :disabled="!groupDetails.name"
          :loading="appActionIsLoadingData"
          @click="createAdminGroup()"
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

  <!-- edit group modal -->
  <PopupModal
    v-if="showEditGroupModal"
    ref="editGroupModal"
    :title="'Rename Group'"
    @reset="showEditGroupModal = false; renameGroupInput = ''; selectedGroup = null;"
    aria-label="renameGroupModal"
  >
    <template #main-content>
      <q-card-section class="column no-wrap items-center">
        <div class="form-group">
          <TextInput
            v-model="renameGroupInput"
            placeholder="Enter Group Name"
            aria-label="renameGroupTextInput"
          />
        </div>
      </q-card-section>
    </template>

    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <q-btn
          no-caps
          unelevated
          color="accent"
          label="Save Changes"
          class="text-body1 full-width"
          :disabled="!renameGroupInput || renameGroupInput == selectedGroup.name"
          :loading="appActionIsLoadingData"
          @click="updateAdminGroup()"
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

  <!-- add/edit group user modal -->
  <PopupModal
    v-if="showGroupUserModal"
    ref="groupUsersModal"
    :title="'Add / Edit User(s)'"
    :modal-width="'600px'"
    @reset="showGroupUserModal = false; addUserInput = null; selectedGroup = null;"
    aria-label="groupUsersModal"
  >
    <template #main-content>
      <q-card-section class="row wrap items-center">
        <div class="col-12 form-group q-mb-md">
          <SelectInput
            v-model="addUserInput"
            :multiple="true"
            :use-chips="true"
            :hide-selected="false"
            :options="users"
            option-type="users"
            option-value="id"
            option-label="name"
            :placeholder="'Select User To Add'"
            aria-label="multiUserSelect"
          />
        </div>

        <div
          v-if="!groupDetails.users"
          class="col-12"
        >
          No users currently in this group.
        </div>
        <template v-else>
          <div class="col-12">
            <div class="row admin-groups-users">
              <div
                v-for="user in groupDetails.users"
                :key="user.id"
                class="col-xs-6 col-sm-4 col-md-3"
              >
                <div class="admin-groups-users-chip q-pa-xs">
                  <q-btn
                    dense
                    outline
                    no-caps
                    class="text-body1 full-width q-pl-sm"
                    @click="showConfirmationModal = {
                      type: 'deleteUser',
                      text: `Do you wish to delete ${user.name} from the group?`
                    }; selectedGroupUserId = user.id"
                  >
                    <span class="text-left">
                      {{ user.first_name }} {{ user.last_name }}
                    </span>
                    <q-icon
                      name="close"
                      size="22px"
                      class="q-ml-auto"
                      aria-label="removeUserIcon"
                    />
                  </q-btn>
                </div>
              </div>
            </div>
          </div>
        </template>
      </q-card-section>
    </template>

    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <q-btn
          no-caps
          unelevated
          color="accent"
          label="Add User(s)"
          class="text-body1 full-width"
          :disabled="!addUserInput"
          :loading="appActionIsLoadingData"
          @click="addAdminGroupUser()"
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

  <!-- confirmation modal -->
  <PopupModal
    v-if="showConfirmationModal !== null"
    ref="confirmationModal"
    :title="'Confirm'"
    :text="showConfirmationModal.text"
    :show-actions="false"
    @reset="showConfirmationModal = null; selectedGroupUserId = null;"
    aria-label="confirmationModal"
  >
    <template #footer-content="{ hideModal }">
      <q-card-section
        v-if="showConfirmationModal.type == 'deleteUser'"
        class="row no-wrap justify-between items-center q-pt-sm"
      >
        <q-btn
          no-caps
          unelevated
          color="negative"
          label="Delete User"
          class="btn-no-wrap text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="removeAdminGroupUser"
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
      <q-card-section
        v-else-if="showConfirmationModal.type == 'deleteGroup'"
        class="row no-wrap justify-between items-center q-pt-sm"
      >
        <q-btn
          no-caps
          unelevated
          color="negative"
          label="Delete Group"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="removeAdminGroup"
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
import { ref, inject, onBeforeMount } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { useGroupStore } from '@/stores/group-store'
import { useUserStore } from '@/stores/user-store'
import MoreOptionsMenu from '@/components/MoreOptionsMenu.vue'
import PopupModal from '@/components/PopupModal.vue'
import TextInput from '@/components/TextInput.vue'
import SelectInput from '@/components/SelectInput.vue'

const router = useRouter()

// Store Data
const { appActionIsLoadingData, appIsLoadingData } = storeToRefs(useGlobalStore())
const { users } = storeToRefs(useOptionStore())
const { groupList, groupDetails } = storeToRefs(useGroupStore())
const {
  resetGroupStore,
  resetGroupDetails,
  getAdminGroupList,
  getAdminGroupPermissions,
  getAdminGroupUsers,
  postAdminGroup,
  patchAdminGroup,
  deleteAdminGroup,
  postAdminGroupUser,
  deleteAdminGroupUser
} = useGroupStore()
const { getUserPermissions } = useUserStore()
const { userData } = storeToRefs(useUserStore())

// Local Data
const renameGroupInput = ref('')
const addUserInput = ref(null)
const selectedGroup = ref(null)
const selectedGroupUserId = ref(null)
const addGroupModal = ref(null)
const showAddGroupModal = ref(false)
const editGroupModal = ref(null)
const showEditGroupModal = ref(false)
const groupUsersModal = ref(null)
const showGroupUserModal = ref(false)
const confirmationModal = ref(null)
const showConfirmationModal = ref(null)

// Logic
const handleAlert = inject('handle-alert')

onBeforeMount(() => {
  resetGroupStore()
  loadAdminGroups()
})

const handleOptionMenu = (option, groupData) => {
  if (option.text == 'Edit Permissions') {
    selectedGroup.value = groupData
    loadAdminGroupPermissions()
  } else if (option.text == 'Add/Edit User(s) in Group') {
    selectedGroup.value = groupData
    loadAdminGroupUsers()
  } else if (option.text == 'Rename Group Name') {
    showEditGroupModal.value = true
    selectedGroup.value = groupData
    renameGroupInput.value = groupData.name
  } else if (option.text == 'Delete Group') {
    selectedGroup.value = groupData
    showConfirmationModal.value = {
      type: 'deleteGroup',
      text: 'Are you sure you want to delete this group?'
    }
  }
}
const resetAddGroupModal = () => {
  showAddGroupModal.value = false
  resetGroupDetails()
}

const loadAdminGroups = async () => {
  try {
    appIsLoadingData.value = true
    await getAdminGroupList()
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
const loadAdminGroupPermissions = async () => {
  try {
    appIsLoadingData.value = true
    await getAdminGroupPermissions(selectedGroup.value.id)

    router.push({
      name: 'admin-groups',
      params: {
        groupId: selectedGroup.value.id
      }
    })
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
const loadAdminGroupUsers = async () => {
  try {
    appIsLoadingData.value = true
    await getAdminGroupUsers(selectedGroup.value.id)
    showGroupUserModal.value = true
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
const createAdminGroup = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      name: groupDetails.value.name
    }
    await postAdminGroup(payload)

    handleAlert({
      type: 'success',
      text: `The ${groupDetails.value.name} group has been created.`,
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
    addGroupModal.value.hideModal()
  }
}
const updateAdminGroup = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      id: selectedGroup.value.id,
      name: renameGroupInput.value
    }
    await patchAdminGroup(payload)

    handleAlert({
      type: 'success',
      text: 'The groups name has been updated.',
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
    editGroupModal.value.hideModal()
  }
}
const removeAdminGroup = async () => {
  try {
    appActionIsLoadingData.value = true
    await deleteAdminGroup(selectedGroup.value.id)

    handleAlert({
      type: 'success',
      text: 'The group has been successfully deleted.',
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
    selectedGroup.value = null
    confirmationModal.value.hideModal()
  }
}
const addAdminGroupUser = async () => {
  try {
    appActionIsLoadingData.value = true
    await Promise.all(addUserInput.value.map(usr => postAdminGroupUser(selectedGroup.value.id, usr)))

    handleAlert({
      type: 'success',
      text: 'The group users have been updated.',
      autoClose: true
    })

    // check if the signed in user exists in an edited group and reload permissions on the user if change to group is detected
    if (groupDetails.value.users.some(usr => usr.id == userData.value.user_id)) {
      loadUserPermissions()
    }
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
    addUserInput.value = null
  }
}
const removeAdminGroupUser = async () => {
  try {
    appActionIsLoadingData.value = true
    await deleteAdminGroupUser(selectedGroup.value.id, selectedGroupUserId.value)

    handleAlert({
      type: 'success',
      text: 'The user has been successfully deleted from the group.',
      autoClose: true
    })

    // check if the delete user is the signed user and reload their permissions
    if (selectedGroupUserId.value == userData.value.user_id) {
      loadUserPermissions()
    }
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
    confirmationModal.value.hideModal()
  }
}

const loadUserPermissions = async () => {
  try {
    await getUserPermissions()
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  }
}
</script>

<style lang="scss" scoped>
.admin-groups {
  &-card {
    position: relative;
    display: flex;
    width: 100%;
    height: 100%;
    min-height: 70px;
    border-color: $secondary;
    border-radius: 4px;
    transition: 0.3s ease;

    &-dashed {
      border-style: dashed;
      border-width: 2px;
      border-color: $color-black;

      &::before {
        border: none;
      }

      &:hover:not(:disabled) {
        color: $accent;
        border-color: $accent;
        cursor: pointer;
      }

      :deep(.q-icon) {
        font-size: 25px;
        font-weight: 700;
        margin-left: 0;
      }
    }

    &-details {
      display: flex;
      flex-flow: row nowrap;
      align-items: center;
      width: 100%;
    }
  }

  &-users {
    position: relative;
    left: -4px;
    width: calc(100% + 8px);

    &-chip {
      display: flex;
      width: 100%;
      height: 100%;

      :deep(.q-btn__content) {
        flex-wrap: nowrap;
      }
    }
  }
}
</style>