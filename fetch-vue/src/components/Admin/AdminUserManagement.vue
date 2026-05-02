<template>
  <div class="user-management q-pa-xs-sm q-pa-sm-lg">
    <div class="row q-mb-xs-xl q-mb-sm-none">
      <div class="col-grow q-mb-xs-md q-mb-sm-none">
        <EssentialTable
          ref="userTableComponent"
          :table-columns="tableColumns"
          :table-visible-columns="tableVisibleColumns"
          :table-data="userList"
          :heading-row-class="'q-mb-xs-md q-mb-md-lg'"
          :enable-pagination="true"
          :pagination-total="userListTotal"
          :pagination-loading="appIsLoadingData"
          :hide-table-rearrange="true"
          @update-pagination="loadUsers($event)"
          @selected-table-row="openUserModal('Edit', $event)"
        >
          <template #heading-row>
            <div
              class="col-sm-5 col-md-12 col-lg-auto"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? '' : 'self-center'"
            >
              <h1 class="text-h4 text-bold">
                User Management
              </h1>
            </div>

            <div class="col-grow" />

            <div
              class="col-auto flex items-center"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? 'justify-end q-mb-md' : ''"
            >
              <BaseButton
                no-caps
                unelevated
                color="accent"
                icon="add_circle"
                label="Create"
                class="btn-no-wrap text-body1 btn-modern"
                @click="openUserModal('Create')"
              />
            </div>
          </template>

          <template #table-td="{ colName, value, props }">
            <span v-if="colName == 'actions'">
              <BaseButton
                flat
                round
                dense
                icon="delete"
                color="negative"
                @click.stop="confirmDeleteUser(props.row)"
              >
                <q-tooltip>Delete User</q-tooltip>
              </BaseButton>
            </span>
            <span v-else>
              {{ value }}
            </span>
          </template>
        </EssentialTable>
      </div>
    </div>

    <!-- Create/Edit User Modal -->
    <PopupModal
      v-if="showUserModal"
      :title="`${userModalMode} User`"
      :show-actions="false"
      @reset="closeUserModal"
    >
      <template #main-content>
        <q-card-section class="column q-gutter-y-md">
          <div class="form-group">
            <label class="form-group-label">First Name</label>
            <TextInput
              v-model="userForm.first_name"
              placeholder="First Name"
              :rules="[val => !!val || 'Field is required']"
            />
          </div>
          <div class="form-group">
            <label class="form-group-label">Last Name</label>
            <TextInput
              v-model="userForm.last_name"
              placeholder="Last Name"
              :rules="[val => !!val || 'Field is required']"
            />
          </div>
          <div class="form-group">
            <label class="form-group-label">Email</label>
            <TextInput
              v-model="userForm.email"
              placeholder="Email Address"
              :rules="[val => !!val || 'Field is required', val => /.+@.+\..+/.test(val) || 'Invalid email format']"
            />
          </div>
        </q-card-section>
      </template>

      <template #footer-content>
        <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
          <BaseButton
            no-caps
            unelevated
            color="accent"
            :label="userModalMode === 'Create' ? 'Create' : 'Save'"
            class="text-body1 full-width"
            :loading="appActionIsLoadingData"
            @click="submitUserForm"
          />
          <q-space class="q-mx-xs" />
          <BaseButton
            outline
            no-caps
            label="Cancel"
            class="text-body1 full-width"
            @click="closeUserModal"
          />
        </q-card-section>
      </template>
    </PopupModal>

    <!-- Delete Confirmation Modal -->
    <PopupModal
      v-if="showDeleteModal"
      title="Delete User"
      :text="`Are you sure you want to delete user ${userToDelete?.first_name} ${userToDelete?.last_name}?`"
      :show-actions="false"
      @reset="showDeleteModal = false"
    >
      <template #footer-content>
        <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
          <BaseButton
            no-caps
            unelevated
            color="negative"
            label="Delete"
            class="text-body1 full-width"
            :loading="appActionIsLoadingData"
            @click="executeDeleteUser"
          />
          <q-space class="q-mx-xs" />
          <BaseButton
            outline
            no-caps
            label="Cancel"
            class="text-body1 full-width"
            @click="showDeleteModal = false"
          />
        </q-card-section>
      </template>
    </PopupModal>
  </div>
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
import { ref, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store'
import { useAdminUserStore } from '@/stores/admin-user-store'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { notify } from '@/utils/notify'
import EssentialTable from '@/components/EssentialTable.vue'
import PopupModal from '@/components/PopupModal.vue'
import TextInput from '@/components/TextInput.vue'

// Composables
const { currentScreenSize } = useCurrentScreenSize()

// Store Data
const { appIsLoadingData, appActionIsLoadingData } = storeToRefs(useGlobalStore())
const userStore = useAdminUserStore()
const { userList, userListTotal } = storeToRefs(userStore)

// Local Data
const userTableComponent = ref(null)
const showUserModal = ref(false)
const userModalMode = ref('Create')
const showDeleteModal = ref(false)
const userToDelete = ref(null)
const userForm = ref({
  id: null,
  first_name: '',
  last_name: '',
  email: ''
})

const tableVisibleColumns = ref([
  'first_name',
  'last_name',
  'email',
  'actions'
])

const tableColumns = ref([
  {
    name: 'first_name',
    field: 'first_name',
    label: 'First Name',
    align: 'left',
    sortable: true
  },
  {
    name: 'last_name',
    field: 'last_name',
    label: 'Last Name',
    align: 'left',
    sortable: true
  },
  {
    name: 'email',
    field: 'email',
    label: 'Email',
    align: 'left',
    sortable: true
  },
  {
    name: 'actions',
    field: 'actions',
    label: 'Actions',
    align: 'center',
    sortable: false
  }
])

// Logic
onMounted(() => {
  loadUsers()
})

const loadUsers = async (qParams = {}) => {
  try {
    appIsLoadingData.value = true
    await userStore.getUsers(qParams)
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to load users'
    })
  } finally {
    appIsLoadingData.value = false
  }
}

const openUserModal = (mode, userData = null) => {
  userModalMode.value = mode
  if (mode === 'Edit' && userData) {
    userForm.value = { ...userData }
  } else {
    userForm.value = {
      id: null,
      first_name: '',
      last_name: '',
      email: ''
    }
  }
  showUserModal.value = true
}

const closeUserModal = () => {
  showUserModal.value = false
  userForm.value = {
    id: null,
    first_name: '',
    last_name: '',
    email: ''
  }
}

const submitUserForm = async () => {
  if (!userForm.value.first_name || !userForm.value.last_name || !userForm.value.email) {
    notify({
      type: 'warning',
      message: 'Please fill in all required fields'
    })
    return
  }

  try {
    appActionIsLoadingData.value = true
    if (userModalMode.value === 'Create') {
      await userStore.createUser(userForm.value)
      notify({
        type: 'positive',
        message: 'User created successfully'
      })
    } else {
      await userStore.updateUser(userForm.value.id, userForm.value)
      notify({
        type: 'positive',
        message: 'User updated successfully'
      })
    }
    loadUsers()
    closeUserModal()
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || `Failed to ${userModalMode.value.toLowerCase()} user`
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}

const confirmDeleteUser = (user) => {
  userToDelete.value = user
  showDeleteModal.value = true
}

const executeDeleteUser = async () => {
  if (!userToDelete.value) {
    return
  }

  try {
    appActionIsLoadingData.value = true
    await userStore.deleteUser(userToDelete.value.id)
    notify({
      type: 'positive',
      message: 'User deleted successfully'
    })
    loadUsers()
    showDeleteModal.value = false
    userToDelete.value = null
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to delete user'
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}
</script>

<style lang="scss" scoped>
.user-management {
  height: 100%;
}
</style>
