<template>
  <div class="shelving-dashboard">
    <div class="row q-mb-xs-xl q-mb-sm-none">
      <div class="col-grow q-mb-xs-md q-mb-sm-none">
        <EssentialTable
          :table-columns="shelfTableColumns"
          :table-visible-columns="shelfTableVisibleColumns"
          :filter-options="shelfTableFilters"
          :table-data="shelvingJobList"
          :enable-table-reorder="false"
          :heading-row-class="'q-mb-xs-md q-mb-md-lg'"
          :heading-filter-class="currentScreenSize == 'xs' ? 'col-xs-6 q-mr-auto' : 'q-ml-auto'"
          :enable-pagination="true"
          :pagination-total="shelvingJobListTotal"
          :pagination-loading="appIsLoadingData"
          @update-pagination="loadShelvingJobs($event)"
          @selected-table-row="loadShelvingJob($event.id, $event.origin)"
        >
          <template #heading-row>
            <div
              class="col-sm-5 col-md-12 col-lg-auto"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? '' : 'self-center'"
            >
              <h1 class="text-h4 text-bold">
                Shelving Jobs
              </h1>
            </div>

            <div
              class="col-xs-grow col-sm-7 col-md-auto flex"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? 'justify-end q-mb-md' : 'order-1'"
            >
              <q-btn
                no-caps
                unelevated
                icon-right="arrow_drop_down"
                color="accent"
                label="Create Shelving Job"
                class="text-body1 q-ml-xs-none q-ml-sm-sm"
                :disabled="appIsOffline"
                aria-label="createShelvingJobMenu"
                aria-haspopup="menu"
                :aria-expanded="shelvingJobMenuState"
              >
                <q-menu
                  @show="shelvingJobMenuState = true"
                  @hide="shelvingJobMenuState = false"
                  aria-label="requestJobMenuList"
                >
                  <q-list>
                    <q-item
                      v-if="checkUserPermission('can_create_and_execute_direct_shelving_job')"
                      clickable
                      v-close-popup
                      @click="showShelvingJobModal = 'Direct'"
                      role="menuitem"
                    >
                      <q-item-section>
                        <q-item-label>
                          <span>
                            Direct To Shelf
                          </span>
                        </q-item-label>
                      </q-item-section>
                    </q-item>
                    <q-item
                      v-if="checkUserPermission('can_create_and_execute_shelving_job')"
                      clickable
                      v-close-popup
                      @click="showShelvingJobModal = 'Verification'"
                      role="menuitem"
                    >
                      <q-item-section>
                        <q-item-label>
                          <span>
                            From Verification Job
                          </span>
                        </q-item-label>
                      </q-item-section>
                    </q-item>
                    <q-item
                      v-if="checkUserPermission('can_move_trays_and_items_shelving_locations')"
                      clickable
                      v-close-popup
                      @click="submitShelvingMove('tray-item')"
                      role="menuitem"
                    >
                      <q-item-section>
                        <q-item-label>
                          <span>
                            Move Tray Item
                          </span>
                        </q-item-label>
                      </q-item-section>
                    </q-item>
                    <q-item
                      v-if="checkUserPermission('can_move_trays_and_items_shelving_locations')"
                      clickable
                      v-close-popup
                      @click="submitShelvingMove('tray-non-tray')"
                      role="menuitem"
                    >
                      <q-item-section>
                        <q-item-label>
                          <span class="text-no-wrap">
                            Move Tray / Non-Tray
                          </span>
                        </q-item-label>
                      </q-item-section>
                    </q-item>
                  </q-list>
                </q-menu>
              </q-btn>
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
              v-else-if="colName == 'container_count'"
              class="outline text-nowrap"
            >
              {{ value }} Containers
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

    <!-- Create Shelving Job Modal -->
    <PopupModal
      v-if="showShelvingJobModal"
      ref="createShelvingJobModal"
      :show-actions="false"
      :modal-width="'600px'"
      @reset="resetCreateShelfJobModal"
      aria-label="createShelvingJobModal"
    >
      <template #header-content="{ hideModal }">
        <q-card-section class="row items-center q-pb-none">
          <h2
            class="text-h6 text-bold"
          >
            {{ showShelvingJobModal == 'Direct' ? 'Create Direct Shelving Job' : 'Create Shelving Job' }}
          </h2>

          <q-btn
            icon="close"
            flat
            round
            dense
            class="q-ml-auto"
            @click="hideModal"
            aria-label="closeModal"
          />
        </q-card-section>
      </template>
      <template #main-content>
        <q-card-section v-if="showShelvingJobModal == 'Verification'">
          <div class="row q-mb-md">
            <div class="col-xs-12 col-sm-8 flex items-center">
              <p :class="currentScreenSize !== 'xs' ? 'text-h6' : 'text-body1 q-mb-xs'">
                Assign Shelving Location?
              </p>
            </div>
            <div class="col-xs-12 col-sm-4">
              <div class="form-group">
                <ToggleButtonInput
                  v-model="shelvingJob.assignLocation"
                  :options="[
                    {label: 'Yes', value: true},
                    {label: 'No', value: false}
                  ]"
                />
              </div>
            </div>
          </div>

          <div class="row">
            <div class="col-12 q-mb-sm">
              <h3 class="text-h6 text-bold">
                Verification Job List:
              </h3>
            </div>
            <div class="col-12">
              <div class="form-group">
                <label class="form-group-label">
                  Please Select Verification Job(s)
                </label>
                <SelectInput
                  v-model="shelvingJob.verification_jobs"
                  :multiple="true"
                  :use-chips="true"
                  :hide-selected="false"
                  :force-option-type-reload="true"
                  :options="verificationJobsDropdown"
                  option-type="verificationJobsDropdown"
                  :option-query="{ unshelved: true }"
                  option-value="id"
                  option-label="workflow_id"
                  :placeholder="'Select Verification Job(s) by Number'"
                  aria-label="verificationJobSelect"
                >
                  <template #option="{ itemProps, opt, selected, toggleOption }">
                    <q-item v-bind="itemProps">
                      <q-item-section>
                        <q-item-label class="text-body1">
                          <span>Job #: {{ opt.workflow_id }}</span>
                          <span class="text-secondary"> - {{ opt.trayed ? 'Trayed' : 'Non-Tray' }} ({{ opt.trayed ? `${opt.tray_count} containers, ${opt.item_count} items` : `${opt.non_tray_item_count} items` }})</span>
                        </q-item-label>
                      </q-item-section>
                      <q-item-section side>
                        <q-checkbox
                          :model-value="selected"
                          @update:model-value="toggleOption(opt)"
                        />
                      </q-item-section>
                    </q-item>
                  </template>
                </SelectInput>
              </div>
            </div>
          </div>

          <div class="row q-mt-md">
            <div class="col-12 q-mb-sm">
              <h3 class="text-h6 text-bold">
                Please Select Shelving Locations:
              </h3>
            </div>
            <div class="col-12">
              <div
                class="form-group q-mb-md"
              >
                <label class="form-group-label">
                  Building
                </label>
                <SelectInput
                  v-model="shelvingJob.building_id"
                  :options="buildings"
                  option-type="buildings"
                  option-value="id"
                  option-label="name"
                  :clearable="false"
                  :placeholder="'Select Building'"
                  @update:model-value="handleShelvingJobFormChange('Building')"
                  aria-label="buildingSelect"
                />
              </div>

              <template v-if="shelvingJob.assignLocation">
                <div
                  class="form-group q-mb-md"
                >
                  <label class="form-group-label">
                    Module
                  </label>
                  <SelectInput
                    v-model="shelvingJob.module_id"
                    :options="modules"
                    option-type="modules"
                    :option-query="{
                      building_id: shelvingJob.building_id
                    }"
                    option-value="id"
                    option-label="module_number"
                    :placeholder="'Select Module'"
                    :disabled="!shelvingJob.building_id"
                    :clearable="false"
                    @update:model-value="handleShelvingJobFormChange('Module')"
                    aria-label="moduleSelect"
                  />
                </div>

                <div class="row">
                  <div
                    class="col-xs-12 col-sm-6 q-pr-sm-xs q-mb-md"
                  >
                    <div class="form-group">
                      <label class="form-group-label">
                        Aisle
                      </label>
                      <SelectInput
                        v-model="shelvingJob.aisle_id"
                        :options="aisles"
                        option-type="aisles"
                        :option-query="{
                          building_id: shelvingJob.building_id,
                          module_id: shelvingJob.module_id,
                          sort_by: 'aisle_number'
                        }"
                        option-value="id"
                        :option-label="opt => opt.aisle_number.number"
                        :placeholder="'Select Aisle'"
                        :disabled="!shelvingJob.module_id"
                        :clearable="false"
                        @update:model-value="handleShelvingJobFormChange('Aisle')"
                        aria-label="aisleSelect"
                      />
                    </div>
                  </div>
                  <div
                    class="col-xs-12 col-sm-6 q-pl-sm-xs q-mb-xs-md q-mb-sm-none"
                  >
                    <div class="form-group">
                      <label class="form-group-label">
                        Side
                      </label>
                      <ToggleButtonInput
                        v-model="shelvingJob.side_id"
                        :options="sides"
                        option-value="id"
                        option-label="side_orientation.name"
                        :disabled="!shelvingJob.aisle_id"
                        @update:model-value="handleShelvingJobFormChange('Side')"
                      />
                    </div>
                  </div>
                </div>

                <div
                  class="form-group"
                >
                  <label class="form-group-label">
                    Ladder
                  </label>
                  <SelectInput
                    v-model="shelvingJob.ladder_id"
                    :options="ladders"
                    option-type="ladders"
                    :option-query="{
                      building_id: shelvingJob.building_id,
                      module_id: shelvingJob.module_id,
                      aisle_id: shelvingJob.aisle_id,
                      side_id: shelvingJob.side_id,
                      sort_by: 'ladder_number'
                    }"
                    option-value="id"
                    :option-label="opt => opt.ladder_number.number"
                    :placeholder="'Select Ladder'"
                    :disabled="!shelvingJob.side_id"
                    :clearable="false"
                    @update:model-value="handleShelvingJobFormChange('Ladder')"
                    aria-label="ladderSelect"
                  />
                </div>
              </template>
            </div>
          </div>
        </q-card-section>
        <q-card-section v-else-if="showShelvingJobModal == 'Direct'">
          <div class="row">
            <div class="col-12 q-mb-sm">
              <h3 class="text-h6 text-bold">
                Please Select Shelving Location:
              </h3>
            </div>
            <div class="col-12">
              <div
                class="form-group"
              >
                <label class="form-group-label">
                  Building
                </label>
                <SelectInput
                  v-model="shelvingJob.building_id"
                  :options="buildings"
                  option-type="buildings"
                  option-value="id"
                  option-label="name"
                  :clearable="false"
                  :placeholder="'Select Building'"
                  @update:model-value="handleShelvingJobFormChange('Building')"
                  aria-label="buildingSelect"
                />
              </div>
            </div>
          </div>
        </q-card-section>
      </template>

      <template #footer-content="{ hideModal }">
        <q-card-section
          class="row no-wrap justify-between items-center q-pt-sm"
        >
          <q-btn
            v-if="showShelvingJobModal == 'Direct'"
            no-caps
            unelevated
            color="accent"
            label="Submit"
            class="text-body1 full-width"
            :disabled="!isCreateShelvingJobFormValid"
            @click="submitDirectToShelfJob(); hideModal();"
          />
          <q-btn
            v-else
            no-caps
            unelevated
            color="accent"
            label="Submit"
            class="text-body1 full-width"
            :disabled="!isCreateShelvingJobFormValid"
            @click="submitShelvingJob(); hideModal();"
          />

          <q-space class="q-mx-xs" />

          <q-btn
            outline
            no-caps
            label="Cancel"
            class="shelving-modal-btn text-body1 full-width"
            @click="hideModal"
          />
        </q-card-section>
      </template>
    </PopupModal>
  </div>
</template>

<script setup>
import { onBeforeMount, ref, inject, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useGlobalStore } from '@/stores/global-store'
import { useUserStore } from '@/stores/user-store'
import { useOptionStore } from '@/stores/option-store'
import { useBuildingStore } from '@/stores/building-store'
import { useShelvingStore } from '@/stores/shelving-store'
import { storeToRefs } from 'pinia'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import EssentialTable from '@/components/EssentialTable.vue'
import SelectInput from '@/components/SelectInput.vue'
import ToggleButtonInput from '@/components/ToggleButtonInput.vue'
import PopupModal from '@/components/PopupModal.vue'

const router = useRouter()

// Composables
const { currentScreenSize } = useCurrentScreenSize()
const { checkUserPermission } = usePermissionHandler()

// Store Data
const {
  appIsLoadingData,
  appIsOffline
} = storeToRefs(useGlobalStore())
const {
  buildings,
  modules,
  aisles,
  ladders,
  users,
  verificationJobsDropdown
} = storeToRefs(useOptionStore())
const {
  getBuildingDetails,
  getModuleDetails,
  getAisleDetails,
  getSideDetails,
  getSideList,
  resetBuildingStore,
  resetBuildingChildren,
  resetModuleChildren,
  resetAisleChildren,
  resetSideChildren
} = useBuildingStore()
const { sides } = storeToRefs(useBuildingStore())
const {
  shelvingJobList,
  shelvingJob,
  directToShelfJob,
  shelvingJobListTotal
} = storeToRefs(useShelvingStore())
const {
  resetShelvingStore,
  resetShelvingJob,
  getShelvingJobList,
  getShelvingJob,
  postShelvingJob,
  getDirectShelvingJob,
  postDirectShelvingJob
} = useShelvingStore()
const { userData } = storeToRefs(useUserStore())

// Local Data
const createShelvingJobModal = ref(null)
const shelfTableVisibleColumns = ref([
  'id',
  'container_count',
  'status',
  'user_id',
  'create_dt',
  'last_transition'
])
const shelfTableColumns = ref([
  {
    name: 'id',
    field: 'id',
    label: 'Job Number',
    align: 'left',
    sortable: true,
    order: 0
  },
  {
    name: 'container_count',
    field: row => (row.tray_count + row.non_tray_item_count),
    label: '# of Containers in Job',
    align: 'left',
    sortable: true,
    order: 1
  },
  {
    name: 'status',
    field: 'status',
    label: 'Status',
    align: 'left',
    sortable: true,
    order: 2
  },
  {
    name: 'user_id',
    field: row => row.user ? row.user.name : '',
    label: 'Assigned User',
    align: 'left',
    sortable: true,
    order: 3
  },
  {
    name: 'create_dt',
    field: 'create_dt',
    label: 'Date Added',
    align: 'left',
    sortable: true,
    order: 4
  },
  {
    name: 'last_transition',
    field: 'last_transition',
    label: 'Last Updated',
    align: 'left',
    sortable: true,
    order: 5
  }
])
const shelfTableFilters = computed(() => {
  let tablesFilters = []
  tablesFilters = [
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
    },
    {
      field: row => row.user ? row.user.name : '',
      label: 'Assigned User',
      apiField: 'assigned_user',
      options: users.value.map(usr => {
        return {
          text: usr.name,
          value: false
        }
      })
    }
  ]
  return tablesFilters
})
const shelvingJobMenuState = ref(false)
const showShelvingJobModal = ref(null)
const isCreateShelvingJobFormValid = computed(() => {
  if (showShelvingJobModal.value == 'Verification' && (shelvingJob.value.verification_jobs.length == 0 || !shelvingJob.value.building_id)) {
    return false
  } else if (showShelvingJobModal.value == 'Direct' && !shelvingJob.value.building_id) {
    return false
  } else {
    return true
  }
})

// Logic
const handleAlert = inject('handle-alert')
const formatDateTime = inject('format-date-time')

onBeforeMount(() => {
  resetShelvingStore()
  loadShelvingJobs()

  if (currentScreenSize.value == 'xs') {
    shelfTableVisibleColumns.value = [
      'id',
      'status',
      'user_id',
      'create_dt'
    ]
  }
})

const resetCreateShelfJobModal = () => {
  resetShelvingJob()
  resetBuildingStore()
  showShelvingJobModal.value = null
}
const handleShelvingJobFormChange = async (valueType) => {
  // reset the form depending on the edited form field type
  switch (valueType) {
    case 'Building':
      resetBuildingChildren()
      await getBuildingDetails(shelvingJob.value.building_id)
      shelvingJob.value.module_id = null
      shelvingJob.value.aisle_id = null
      shelvingJob.value.side_id = null
      shelvingJob.value.ladder_id = null
      return
    case 'Module':
      // clear state for aisle options downward since user needs to select an aisle next to populate the rest of the data
      resetModuleChildren()
      await getModuleDetails(shelvingJob.value.module_id)
      shelvingJob.value.aisle_id = null
      shelvingJob.value.side_id = null
      shelvingJob.value.ladder_id = null
      return
    case 'Aisle':
      resetAisleChildren()
      await getAisleDetails(shelvingJob.value.aisle_id)
      // also get sides since sides are buttons and not dynamically loaded from a options select input
      await getSideList({
        building_id: shelvingJob.value.building_id,
        module_id: shelvingJob.value.module_id,
        aisle_id: shelvingJob.value.aisle_id
      })
      shelvingJob.value.side_id = null
      shelvingJob.value.ladder_id = null
      return
    case 'Side':
      resetSideChildren()
      await getSideDetails(shelvingJob.value.side_id)
      shelvingJob.value.ladder_id = null
      return
    case 'Ladder':
      return
  }
}

const loadShelvingJobs = async (qParams) => {
  try {
    appIsLoadingData.value = true
    await getShelvingJobList({
      ...qParams,
      status: shelfTableFilters.value.find(fltr => fltr.field == 'status').options.flatMap(opt => opt.value == true ? opt.text : []),
      user_id: checkUserPermission('can_view_all_shelving_jobs') ? null : userData.value.user_id
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
const loadShelvingJob = async (jobId, type) => {
  try {
    appIsLoadingData.value = true

    if (type == 'Verification') {
      await getShelvingJob(jobId)
      router.push({
        name: 'shelving',
        params: {
          jobId
        }
      })
    } else if (type == 'Direct') {
      await getDirectShelvingJob(jobId)
      router.push({
        name: 'shelving-dts',
        params: {
          jobId
        }
      })
    }
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
const submitShelvingJob = async () => {
  try {
    appIsLoadingData.value = true
    const params = {
      module_id: shelvingJob.value.module_id,
      aisle_id: shelvingJob.value.aisle_id,
      side_id: shelvingJob.value.side_id,
      ladder_id: shelvingJob.value.ladder_id
    }
    const payload = {
      status: 'Created',
      building_id: shelvingJob.value.building_id,
      verification_jobs: shelvingJob.value.verification_jobs,
      origin: 'Verification',
      created_by_id: userData.value.user_id
    }
    await postShelvingJob(payload, params)

    // route the user to the shelving job detail page
    router.push({
      name: 'shelving',
      params: {
        jobId: shelvingJob.value.id
      }
    })

    handleAlert({
      type: 'success',
      text: 'A Shelving Job has been successfully created.',
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
    createShelvingJobModal.value.hideModal()
  }
}
const submitDirectToShelfJob = async () => {
  try {
    appIsLoadingData.value = true
    const payload = {
      status: 'Created',
      building_id: shelvingJob.value.building_id,
      user_id: userData.value.user_id,
      origin: 'Direct',
      created_by_id: userData.value.user_id
    }
    await postDirectShelvingJob(payload)
    router.push({
      name: 'shelving-dts',
      params: {
        jobId: directToShelfJob.value.id
      }
    })

    handleAlert({
      type: 'success',
      text: 'A Direct Shelving Job has been successfully created.',
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
    createShelvingJobModal.value.hideModal()
  }
}
const submitShelvingMove = async (moveType) => {
  router.push({
    name: 'shelving-move',
    params: {
      type: moveType
    }
  })
}

</script>
<style lang="scss" scoped>
.shelving {
  &-details {
    position: relative;
    display: flex;
    align-items: center;
    flex-wrap: nowrap;

    @media (max-width: $breakpoint-sm-min) {
      flex-direction: column;
      align-items: flex-start;
    }
  }
}
</style>
