<template>
  <div class="admin-ils-configuration q-pa-xs-sm q-pa-sm-lg">
    <div class="row q-mb-lg items-center justify-between">
      <div class="col-auto">
        <h1 class="text-h4 text-bold">
          ILS Integrations
        </h1>
        <p class="text-body1 q-mt-sm text-grey-7">
          Manage remote API connections, credentials, and webhook subscriptions for Integrated Library Systems like FOLIO and ALMA.
        </p>
      </div>
      <div class="col-auto">
        <BaseButton
          no-caps
          unelevated
          color="accent"
          icon="add_circle"
          label="Add ILS Configuration"
          class="btn-no-wrap text-body1 btn-modern"
          @click="openDialog()"
        />
      </div>
    </div>

    <!-- Data Table -->
    <q-table
      :rows="ilsConfigurations"
      :columns="columns"
      row-key="id"
      class="bg-white rounded-borders shadow-1"
      :loading="loadingToggle"
    >
      <template #body-cell-is_active="props">
        <q-td :props="props">
          <q-badge :color="props.row.is_active ? 'positive' : 'negative'">
            {{ props.row.is_active ? 'Active' : 'Inactive' }}
          </q-badge>
        </q-td>
      </template>

      <template #body-cell-adapter_type="props">
        <q-td :props="props">
          <q-chip
            size="sm"
            outline
          >
            {{ props.row.adapter_type }}
          </q-chip>
        </q-td>
      </template>

      <template #body-cell-actions="props">
        <q-td
          :props="props"
          auto-width
        >
          <BaseButton
            flat
            round
            dense
            color="primary"
            icon="edit"
            size="sm"
            @click="openDialog(props.row)"
          >
            <q-tooltip>Edit Configuration</q-tooltip>
          </BaseButton>
          <BaseButton
            flat
            round
            dense
            color="negative"
            icon="delete"
            size="sm"
            @click="confirmDelete(props.row)"
          >
            <q-tooltip>Delete Configuration</q-tooltip>
          </BaseButton>
        </q-td>
      </template>
    </q-table>

    <!-- Create/Edit Dialog -->
    <q-dialog
      v-model="showDialog"
      persistent
      maximized
    >
      <q-card class="column bg-grey-2">
        <q-toolbar class="bg-primary text-white">
          <q-toolbar-title>
            <span class="text-weight-bold">{{ editingId ? 'Edit' : 'Create' }} ILS Configuration</span>
          </q-toolbar-title>
          <BaseButton
            flat
            round
            dense
            icon="close"
            v-close-popup
          />
        </q-toolbar>

        <q-card-section class="col q-pt-lg bg-white q-ma-md rounded-borders shadow-1 scroll">
          <q-form
            ref="ilsForm"
            @submit="onSubmit"
            class="q-gutter-md row"
          >

            <div class="col-12 text-h6 q-pb-none">
              General Details
            </div>

            <div class="col-xs-12 col-sm-6">
              <q-input
                v-model="formData.name"
                label="Configuration Name *"
                outlined
                hide-bottom-space
                :rules="[val => !!val || 'Name is required']"
              />
            </div>

            <div class="col-xs-12 col-sm-6">
              <q-select
                v-model="formData.adapter_type"
                :options="adapterTypes"
                label="Adapter Type *"
                outlined
                hide-bottom-space
                :rules="[val => !!val || 'Adapter Type is required']"
              />
            </div>

            <div class="col-12">
              <q-toggle
                v-model="formData.is_active"
                label="Integration Active"
                color="positive"
                left-label
              />
            </div>

            <div class="col-12 text-h6 q-pt-md">
              API Connection
            </div>

            <div class="col-12">
              <q-input
                v-model="formData.base_url"
                label="Base URL *"
                outlined
                hide-bottom-space
                :rules="[val => !!val || 'Base URL is required']"
              />
            </div>

            <div class="col-xs-12 col-md-4">
              <q-input
                v-model="formData.tenant_id"
                label="Tenant ID *"
                outlined
                hide-bottom-space
                :rules="[val => !!val || 'Tenant ID is required']"
              />
            </div>

            <div class="col-xs-12 col-md-4">
              <q-input
                v-model="formData.auth_client_id"
                label="Client ID *"
                outlined
                hide-bottom-space
                :rules="[val => !!val || 'Client ID is required']"
              />
            </div>

            <div class="col-xs-12 col-md-4">
              <q-input
                v-model="formData.auth_client_secret"
                label="Client Secret *"
                type="password"
                outlined
                hide-bottom-space
                :rules="[val => !!val || 'Client Secret is required']"
              />
            </div>

            <div class="col-xs-12 col-md-6">
              <q-input
                v-model="formData.auth_token_url"
                label="Token URL (Optional)"
                outlined
                hide-bottom-space
                hint="Used if the typical auth route differs from base connection"
              />
            </div>

            <div class="col-xs-12 col-md-6">
              <q-input
                v-model="formData.ils_service_point_id"
                label="Service Point ID (FOLIO)"
                outlined
                hide-bottom-space
                hint="Required for Eureka check-ins and pulled requests"
              />
            </div>

            <div class="col-12 text-h6 q-pt-md">
              Mapping Defaults
            </div>

            <div class="col-xs-12 col-md-4">
              <q-input
                v-model="formData.expected_shelved_status"
                label="Expected Shelved Status *"
                outlined
                hide-bottom-space
                hint="The exact ILS status string when FETCH2 verifies shelving"
                :rules="[val => !!val || 'Required']"
              />
            </div>

            <div class="col-xs-12 col-md-4">
              <q-input
                v-model="formData.expected_refile_status"
                label="Expected Refile Status *"
                outlined
                hide-bottom-space
                hint="The exact ILS status string when items are queued for refile"
                :rules="[val => !!val || 'Required']"
              />
            </div>

            <div class="col-xs-12 col-md-4">
              <q-input
                v-model="formData.expected_picklist_status"
                label="Expected Picklist Status *"
                outlined
                hide-bottom-space
                hint="The exact ILS status string when FETCH2 completes a picklist"
                :rules="[val => !!val || 'Required']"
              />
            </div>

            <div class="col-12 text-h6 q-pt-md">
              Feature / Hook Toggles
            </div>
            <div class="col-12 row q-gutter-x-lg">
              <q-checkbox
                v-model="formData.enable_accession_hook"
                label="Enable Accession Sync"
              />
              <q-checkbox
                v-model="formData.enable_shelving_hook"
                label="Enable Shelving Sync"
              />
              <q-checkbox
                v-model="formData.enable_refile_hook"
                label="Enable Refile Sync"
              />
              <q-checkbox
                v-model="formData.enable_picklist_hook"
                label="Enable Picklist Sync"
              />
              <q-checkbox
                v-model="formData.enable_requests_hook"
                label="Enable Requests Sync"
              />
              <q-checkbox
                v-model="formData.enable_jit_metadata_hook"
                label="Enable Custom Meta JIT Pull (SLOW!)"
                color="orange"
              />
            </div>

          </q-form>
        </q-card-section>

        <q-separator />

        <q-card-actions
          align="right"
          class="bg-white text-teal q-pa-md shadow-up-1 z-top"
        >
          <BaseButton
            outline
            no-caps
            label="Cancel"
            v-close-popup
          />
          <q-space class="q-mx-xs" />
          <BaseButton
            no-caps
            unelevated
            label="Save Configuration"
            color="primary"
            @click="submitForm"
            :loading="appActionIsLoadingData"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import BaseButton from '@/components/Base/BaseButton.vue'
import { useGlobalStore } from '@/stores/global-store'
import { useIlsConfigurationStore } from '@/stores/ils-configuration-store'
import { storeToRefs } from 'pinia'
import { Notify, Dialog } from 'quasar'

// Stores
const globalStore = useGlobalStore()
const { appActionIsLoadingData } = storeToRefs(globalStore)
const ilsStore = useIlsConfigurationStore()
const { ilsConfigurations } = storeToRefs(ilsStore)

// Local Data
const loadingToggle = ref(false)
const showDialog = ref(false)
const editingId = ref(null)
const ilsForm = ref(null)

const adapterTypes = [
  'FOLIO',
  'ALMA',
  'CUSTOM_MIDDLEWARE'
]

const columns = [
  {
    name: 'name',
    label: 'Name',
    field: 'name',
    align: 'left',
    sortable: true
  },
  {
    name: 'adapter_type',
    label: 'Adapter',
    field: 'adapter_type',
    align: 'left',
    sortable: true
  },
  {
    name: 'tenant_id',
    label: 'Tenant',
    field: 'tenant_id',
    align: 'left',
    sortable: true
  },
  {
    name: 'is_active',
    label: 'Status',
    field: 'is_active',
    align: 'center',
    sortable: true
  },
  {
    name: 'actions',
    label: 'Actions',
    field: 'actions',
    align: 'right'
  }
]

const initialData = {
  name: '',
  adapter_type: 'FOLIO',
  base_url: '',
  tenant_id: '',
  auth_client_id: '',
  auth_client_secret: '',
  auth_token_url: null,
  ils_service_point_id: '',
  expected_shelved_status: 'Available',
  expected_refile_status: 'In Transit',
  expected_picklist_status: 'In Transit',
  is_active: false,
  enable_accession_hook: false,
  enable_shelving_hook: false,
  enable_refile_hook: false,
  enable_picklist_hook: false,
  enable_requests_hook: false,
  enable_jit_metadata_hook: false
}

const formData = ref({ ...initialData })

onMounted(async () => {
  await loadData()
})

const loadData = async () => {
  try {
    loadingToggle.value = true
    await ilsStore.getIlsConfigurations()
  } catch (err) {
    console.error(err)
    Notify.create({
      type: 'negative',
      message: 'Failed to load ILS Configurations'
    })
  } finally {
    loadingToggle.value = false
  }
}

const openDialog = (row = null) => {
  if (row) {
    editingId.value = row.id
    // Simple deep copy without proxy observers
    formData.value = JSON.parse(JSON.stringify(row))
  } else {
    editingId.value = null
    formData.value = { ...initialData }
  }
  showDialog.value = true
}

const submitForm = () => {
  ilsForm.value.validate().then(success => {
    if (success) {
      onSubmit()
    }
  })
}

const onSubmit = async () => {
  try {
    appActionIsLoadingData.value = true

    // Cleanup empty strings for optionals
    const payload = { ...formData.value }
    if (payload.auth_token_url === '') {
      payload.auth_token_url = null
    }
    if (payload.ils_service_point_id === '') {
      payload.ils_service_point_id = null
    }

    if (editingId.value) {
      await ilsStore.patchIlsConfiguration(editingId.value, payload)
      Notify.create({
        type: 'positive',
        message: 'Configuration Updated'
      })
    } else {
      await ilsStore.postIlsConfiguration(payload)
      Notify.create({
        type: 'positive',
        message: 'Configuration Created'
      })
    }
    showDialog.value = false
    await loadData()
  } catch (err) {
    console.error(err)
    Notify.create({
      type: 'negative',
      message: err?.response?.data?.detail || 'An error occurred'
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}

const confirmDelete = (row) => {
  Dialog.create({
    title: 'Confirm Delete',
    message: `Are you sure you want to delete ${row.name}? This may break existing connections.`,
    cancel: true,
    persistent: true
  }).onOk(async () => {
    try {
      appActionIsLoadingData.value = true
      await ilsStore.deleteIlsConfiguration(row.id)
      Notify.create({
        type: 'positive',
        message: 'Configuration Deleted'
      })
      await loadData()
    } catch (err) {
      console.error(err)
      Notify.create({
        type: 'negative',
        message: err?.response?.data?.detail || 'Deletion failed (Check active Owner assignments)'
      })
    } finally {
      appActionIsLoadingData.value = false
    }
  })
}
</script>

<style lang="scss" scoped>
.admin-ils-configuration {
  max-width: 1200px;
  margin: 0 auto;
}
</style>
