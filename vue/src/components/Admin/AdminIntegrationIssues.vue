<template>
  <div class="admin-integration-issues q-pa-xs-sm q-pa-sm-lg">
    <div class="row q-mb-lg items-center justify-between">
      <div class="col-auto">
        <h1 class="text-h4 text-bold">
          Integration Issues
        </h1>
        <p class="text-body1 q-mt-sm text-grey-7">
          Review, resolve, or retry failed synchronous API checks between FETCH2 and established ILS Integrations.
        </p>
      </div>
    </div>

    <!-- Data Table -->
    <q-table
      :rows="ilsSyncErrors"
      :columns="columns"
      row-key="id"
      class="bg-white rounded-borders shadow-1"
      :loading="loadingToggle"
      :pagination="{ rowsPerPage: 50 }"
    >
      <template #body-cell-status="props">
        <q-td :props="props">
          <q-badge :color="props.row.status === 'ACTIVE' ? 'negative' : 'positive'">
            {{ props.row.status }}
          </q-badge>
        </q-td>
      </template>

      <template #body-cell-created_at="props">
        <q-td :props="props">
          {{ new Date(props.row.created_at).toLocaleString() }}
        </q-td>
      </template>

      <template #body-cell-actions="props">
        <q-td
          :props="props"
          auto-width
        >
          <q-btn
            v-if="props.row.status === 'ACTIVE'"
            flat
            round
            color="primary"
            icon="refresh"
            size="sm"
            @click="confirmRetry(props.row)"
          >
            <q-tooltip>Retry Sync</q-tooltip>
          </q-btn>
          <q-btn
            v-if="props.row.status === 'ACTIVE'"
            flat
            round
            color="positive"
            icon="check"
            size="sm"
            @click="confirmResolve(props.row)"
          >
            <q-tooltip>Mark Resolved</q-tooltip>
          </q-btn>
        </q-td>
      </template>
    </q-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useGlobalStore } from '@/stores/global-store'
import { useIlsSyncErrorStore } from '@/stores/ils-sync-error-store'
import { storeToRefs } from 'pinia'
import { Notify, Dialog } from 'quasar'

// Stores
const globalStore = useGlobalStore()
const { appActionIsLoadingData } = storeToRefs(globalStore)
const ilsSyncErrorStore = useIlsSyncErrorStore()
const { ilsSyncErrors } = storeToRefs(ilsSyncErrorStore)

// Local Data
const loadingToggle = ref(false)

const columns = [
  {
    name: 'item_barcode',
    label: 'Item Barcode',
    field: 'item_barcode',
    align: 'left',
    sortable: true
  },
  {
    name: 'workflow_action',
    label: 'Workflow',
    field: 'workflow_action',
    align: 'left',
    sortable: true
  },
  {
    name: 'error_message',
    label: 'Error Detail',
    field: 'error_message',
    align: 'left',
    sortable: true
  },
  {
    name: 'created_at',
    label: 'Occurred',
    field: 'created_at',
    align: 'left',
    sortable: true
  },
  {
    name: 'status',
    label: 'Status',
    field: 'status',
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

onMounted(async () => {
  await loadData()
})

const loadData = async () => {
  try {
    loadingToggle.value = true
    await ilsSyncErrorStore.getIlsSyncErrors()
  } catch (err) {
    console.error(err)
    Notify.create({
      type: 'negative',
      message: 'Failed to load ILS Sync Errors'
    })
  } finally {
    loadingToggle.value = false
  }
}

const confirmResolve = (row) => {
  Dialog.create({
    title: 'Confirm Resolve',
    message: 'Mark this issue as manually resolved? This will clear it from the active queue.',
    cancel: true,
    persistent: true
  }).onOk(async () => {
    try {
      appActionIsLoadingData.value = true
      await ilsSyncErrorStore.resolveIlsSyncError(row.id)
      Notify.create({
        type: 'positive',
        message: 'Issue Resoloved'
      })
      await loadData()
    } catch (err) {
      console.error(err)
      Notify.create({
        type: 'negative',
        message: 'Error marking resolved'
      })
    } finally {
      appActionIsLoadingData.value = false
    }
  })
}

const confirmRetry = (row) => {
  Dialog.create({
    title: 'Confirm Retry',
    message: 'Re-dispatch this event to the background validation task?',
    cancel: true,
    persistent: true
  }).onOk(async () => {
    try {
      appActionIsLoadingData.value = true
      await ilsSyncErrorStore.retryIlsSyncError(row.id)
      Notify.create({
        type: 'positive',
        message: 'Validation Sync Re-Queued!'
      })
      await loadData()
    } catch (err) {
      console.error(err)
      Notify.create({
        type: 'negative',
        message: 'Retry Failed (Only works on fully built engines)'
      })
    } finally {
      appActionIsLoadingData.value = false
    }
  })
}
</script>

<style lang="scss" scoped>
.admin-integration-issues {
  max-width: 1400px;
  margin: 0 auto;
}
</style>
