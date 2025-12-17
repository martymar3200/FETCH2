<template>
  <div class="withdrawal-dashboard">
    <div class="row q-mb-xs-xl q-mb-sm-none">
      <div class="col-grow q-mb-xs-md q-mb-sm-none">
        <EssentialTable
          :table-columns="withdrawTableColumns"
          :table-visible-columns="withdrawTableVisibleColumns"
          :filter-options="withdrawTableFilters"
          :table-data="withdrawJobList"
          :enable-table-reorder="false"
          :heading-row-class="'q-mb-xs-md q-mb-md-lg'"
          :heading-filter-class="currentScreenSize == 'xs' ? 'col-xs-6 q-mr-auto' : 'q-ml-auto'"
          :enable-pagination="true"
          :pagination-total="withdrawJobListTotal"
          :pagination-loading="appIsLoadingData"
          @update-pagination="loadWithdrawJobs($event)"
          @selected-table-row="loadWithdrawJob($event.id)"
        >
          <template #heading-row>
            <div
              class="col-sm-5 col-md-12 col-lg-auto"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? '' : 'self-center'"
            >
              <h1 class="text-h4 text-bold">
                Withdraw Jobs
              </h1>
            </div>

            <div
              class="col-xs-grow col-sm-7 col-md-auto flex"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? 'justify-end q-mb-md' : 'order-1'"
            >
              <q-btn
                no-caps
                unelevated
                color="accent"
                label="Create Withdraw Job"
                class="btn-no-wrap text-body1 q-ml-xs-none q-ml-sm-sm"
                :disabled="appIsOffline"
                @click="createWithdrawJob"
              />
            </div>
          </template>

          <template #table-td="{ colName, value }">
            <span
              v-if="colName == 'status'"
              class="outline text-nowrap"
              :class="value == 'Created' || value == 'Completed' ? 'text-highlight' : value == 'Paused' || value == 'Running' ? 'text-highlight-warning' : 'text-highlight-negative'"
            >
              {{ value }}
            </span>
            <span
              v-else-if="colName == 'item_count'"
              class="outline text-nowrap"
            >
              {{ value }} Items
            </span>
            <span v-else-if="colName == 'create_dt'">
              {{ formatDateTime(value).date }}
            </span>
            <span v-else-if="colName == 'last_transition'">
              {{ formatDateTime(value).date }}
            </span>
          </template>
        </EssentialTable>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onBeforeMount, ref, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useGlobalStore } from '@/stores/global-store'
import { useWithdrawalStore } from '@/stores/withdrawal-store'
import { useUserStore } from '@/stores/user-store'
import { storeToRefs } from 'pinia'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import EssentialTable from '@/components/EssentialTable.vue'

const router = useRouter()

// Composables
const { currentScreenSize } = useCurrentScreenSize()

// Store Data
const {
  appIsLoadingData,
  appIsOffline
} = storeToRefs(useGlobalStore())
const {
  withdrawJobList,
  withdrawJobListTotal,
  withdrawJob
} = storeToRefs(useWithdrawalStore())
const {
  resetWithdrawStore,
  getWithdrawJobList,
  getWithdrawJob,
  postWithdrawJob
} = useWithdrawalStore()
const { userData } = storeToRefs(useUserStore())

// Local Data
const withdrawTableVisibleColumns = ref([
  'id',
  'item_count',
  'status',
  'create_dt'
])
const withdrawTableColumns = ref([
  {
    name: 'id',
    field: 'id',
    label: 'Job ID #',
    align: 'left',
    sortable: true
  },
  {
    name: 'item_count',
    field: row => row.item_count + row.non_tray_item_count,
    label: '# of Items',
    align: 'left',
    sortable: true
  },
  {
    name: 'status',
    field: 'status',
    label: 'Status',
    align: 'left',
    sortable: true
  },
  {
    name: 'create_dt',
    field: 'create_dt',
    label: 'Date Created',
    align: 'left',
    sortable: true
  }
])
const withdrawTableFilters =  ref([
  {
    field: 'status',
    label: 'Status',
    options: [
      {
        text: 'Created',
        value: true
      },
      {
        text: 'Paused',
        value: true
      },
      {
        text: 'Running',
        value: true
      },
      {
        text: 'Completed',
        value: false
      }
    ]
  }
])

// Logic
const handleAlert = inject('handle-alert')
const formatDateTime = inject('format-date-time')

onBeforeMount(() => {
  resetWithdrawStore()
  loadWithdrawJobs()

  if (currentScreenSize.value == 'xs') {
    withdrawTableVisibleColumns.value = [
      'id',
      'item_count',
      'create_dt'
    ]
  }
})

const loadWithdrawJobs = async (qParams) => {
  try {
    appIsLoadingData.value = true
    await getWithdrawJobList({
      ...qParams,
      status: withdrawTableFilters.value.find(fltr => fltr.field == 'status').options.flatMap(opt => opt.value == true ? opt.text : [])
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
const loadWithdrawJob = async (jobId) => {
  try {
    appIsLoadingData.value = true

    await getWithdrawJob(jobId)
    router.push({
      name: 'withdrawal',
      params: {
        jobId
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
const createWithdrawJob = async () => {
  try {
    appIsLoadingData.value = true
    const payload = {
      created_by_id: userData.value.user_id
    }
    await postWithdrawJob(payload)

    // route the user to the withdrawal job detail page
    router.push({
      name: 'withdrawal',
      params: {
        jobId: withdrawJob.value.id
      }
    })

    handleAlert({
      type: 'success',
      text: 'A Withdraw Job has been successfully created.',
      autoClose: true
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

</script>
<style lang="scss" scoped>
</style>
