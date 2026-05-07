<template>
  <PopupModal
    ref="reportModal"
    :show-actions="false"
    @reset="emit('hide')"
    aria-label="generateReportModal"
  >
    <template #header-content="{ hideModal }">
      <q-card-section class="modal-header row items-center q-pb-sm">
        <h2 class="text-h6 text-bold q-mb-none">
          {{ reportType }}
        </h2>

        <BaseButton
          flat
          no-caps
          dense
          color="accent"
          label="Clear Filters"
          class="q-ml-md text-caption"
          @click="clearFilters"
          aria-label="clearFilters"
        />

        <BaseButton
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
      <q-card-section class="q-pb-none">
        <!-- location based report form -->
        <div
          v-if="reportType == 'Open Locations'"
          class="row"
        >
          <div class="col-12 q-mb-md">
            <div class="form-group">
              <label class="form-group-label">
                Building
              </label>
              <SelectInput
                v-model="reportForm.building_id"
                :options="buildings"
                option-type="buildings"
                option-value="id"
                option-label="name"
                :placeholder="'Select Building'"
                @update:model-value="handleLocationFormChange('Building')"
                aria-label="buildingSelect"
              />
            </div>
          </div>
          <div class="col-12 q-mb-md">
            <div class="form-group">
              <label class="form-group-label">
                Module
              </label>
              <SelectInput
                v-model="reportForm.module_id"
                :options="modules"
                option-type="modules"
                :option-query="{
                  building_id: reportForm.building_id
                }"
                option-value="id"
                option-label="module_number"
                :placeholder="'Select Module'"
                :disabled="!reportForm.building_id"
                @update:model-value="handleLocationFormChange('Module')"
                aria-label="moduleSelect"
              />
            </div>
          </div>

          <div
            class="col-xs-12 col-sm-6 q-pr-sm-xs"
          >
            <div class="form-group">
              <label class="form-group-label">
                Aisle
              </label>
              <SelectInput
                v-model="reportForm.aisle_id"
                :options="aisles"
                option-type="aisles"
                :option-query="{
                  building_id: reportForm.building_id,
                  module_id: reportForm.module_id,
                  sort_by: 'aisle_number'
                }"
                option-value="id"
                option-label="aisle_number"
                :placeholder="'Select Aisle'"
                :disabled="!reportForm.module_id"
                @update:model-value="handleLocationFormChange('Aisle')"
                aria-label="aisleSelect"
              />
            </div>
          </div>
          <div
            class="col-xs-12 col-sm-6 q-pl-sm-xs q-mt-xs-md q-mt-sm-none"
          >
            <div class="form-group">
              <label class="form-group-label">
                Side
              </label>
              <ToggleButtonInput
                v-model="reportForm.side_id"
                :options="sides"
                option-value="id"
                option-label="side_orientation.name"
                :disabled="!reportForm.aisle_id"
                @update:model-value="handleLocationFormChange('Side')"
              />
            </div>
          </div>

          <div
            class="col-12 q-my-md"
          >
            <div class="form-group">
              <label class="form-group-label">
                Ladder
              </label>
              <SelectInput
                v-model="reportForm.ladder_id"
                :options="ladders"
                option-type="ladders"
                :option-query="{
                  building_id: reportForm.building_id,
                  module_id: reportForm.module_id,
                  aisle_id: reportForm.aisle_id,
                  side_id: reportForm.side_id,
                  sort_by: 'ladder_number'
                }"
                option-value="id"
                option-label="ladder_number"
                :placeholder="'Select Ladder'"
                :disabled="!reportForm.side_id"
                @update:model-value="handleLocationFormChange('Ladder')"
                aria-label="ladderSelect"
              />
            </div>
          </div>

          <div class="col-12 q-mb-md">
            <div class="form-group">
              <label class="form-group-label">
                Size Class
              </label>
              <SelectInput
                v-model="reportForm.size_class_id"
                :multiple="true"
                :hide-selected="false"
                :options="sizeClass"
                option-type="sizeClass"
                option-value="id"
                option-label="name"
                :placeholder="`Select Size Class`"
                :aria-label="`sizeClassSelect`"
              />
            </div>
          </div>

          <div class="col-12 q-mb-md">
            <div class="form-group">
              <label class="form-group-label">
                Owner
              </label>
              <SelectInput
                v-model="reportForm.owner_id"
                :multiple="true"
                :hide-selected="false"
                :options="owners"
                option-type="owners"
                option-value="id"
                option-label="name"
                :placeholder="`Select Owner`"
                :aria-label="`ownerSelect`"
              />
            </div>
          </div>

          <div class="col-12 q-mb-md">
            <div class="form-group">
              <label class="form-group-label">
                Height
              </label>
              <TextInput
                v-model="reportForm.height"
                :placeholder="`Enter Height`"
                :disabled="!reportForm.building_id"
                type="number"
                :aria-label="`heightInput`"
              />
            </div>
          </div>

          <div class="col-12 q-mb-md">
            <div class="form-group">
              <label class="form-group-label">
                Width
              </label>
              <TextInput
                v-model="reportForm.width"
                :placeholder="`Enter Width`"
                :disabled="!reportForm.building_id"
                type="number"
                :aria-label="`widthInput`"
              />
            </div>
          </div>

          <div class="col-12 q-mb-md">
            <div class="form-group">
              <label class="form-group-label">
                Depth
              </label>
              <TextInput
                v-model="reportForm.depth"
                :placeholder="`Enter Depth`"
                :disabled="!reportForm.building_id"
                type="number"
                :aria-label="`depthInput`"
              />
            </div>
          </div>

          <div class="col-xs-12 col-sm-8 flex items-center q-mb-sm-md">
            <p :class="currentScreenSize !== 'xs' ? 'text-h6' : 'text-body1 q-mb-xs'">
              Show Partial Shelves?
            </p>
          </div>
          <div class="col-xs-12 col-sm-4 q-mb-md">
            <div class="form-group">
              <ToggleButtonInput
                v-model="reportForm.show_partial"
                :options="[
                  {label: 'Yes', value: true},
                  {label: 'No', value: false}
                ]"
              />
            </div>
          </div>

          <div class="col-12 q-mb-md">
            <div class="form-group">
              <q-checkbox
                v-model="reportForm.include_sub_tiers"
                label="Include Owner Tiers"
                :disable="!reportForm.owner_id?.length"
                aria-label="includeSubTiersCheckbox"
              />
            </div>
          </div>
        </div>
        <!-- generalized dynamic report forms -->
        <div
          v-else
          class="row"
        >
          <template
            v-for="param in reportParams"
            :key="param.query"
          >
            <!-- wrap in a conditional check for dynamic visibility -->
            <template v-if="shouldShowParam(param)">
              <!-- single date range picker with one calendar for both dates -->
              <div
                v-if="param.query === 'from_dt'"
                class="col-12 q-mb-lg"
              >
                <div class="form-group">
                  <label class="form-group-label">
                    Date Range
                    <q-icon
                      name="info"
                      size="14px"
                      class="q-ml-xs cursor-pointer text-grey-6"
                    >
                      <q-tooltip class="text-body2">
                        Click to select a start and end date
                      </q-tooltip>
                    </q-icon>
                  </label>
                  <q-input
                    :model-value="formatDateRange(reportForm.from_dt, reportForm.to_dt)"
                    outlined
                    dense
                    readonly
                    placeholder="Select date range"
                    class="date-range-input"
                  >
                    <template #append>
                      <q-icon
                        name="event"
                        class="cursor-pointer"
                      >
                        <q-popup-proxy
                          cover
                          transition-show="scale"
                          transition-hide="scale"
                        >
                          <q-date
                            :model-value="dateRangeValue"
                            range
                            @update:model-value="updateDateRange"
                          >
                            <div class="row items-center justify-end q-gutter-sm">
                              <BaseButton
                                label="Clear"
                                flat
                                @click="clearDateRange"
                              />
                              <BaseButton
                                v-close-popup
                                label="Done"
                                color="primary"
                                flat
                              />
                            </div>
                          </q-date>
                        </q-popup-proxy>
                      </q-icon>
                    </template>
                  </q-input>
                </div>
              </div>
              <!-- skip to_dt field since it's handled in the range picker above -->
              <div
                v-else-if="param.query === 'to_dt'"
                style="display: none;"
              />
              <!-- datetime inputs -->
              <div
                v-else-if="param.type == 'datetime'"
                class="col-12 q-mb-md"
              >
                <div class="form-group">
                  <label class="form-group-label">
                    {{ param.label }}
                    <span
                      v-if="param.required"
                      class="text-caption text-negative"
                    >
                      (Required)
                    </span>
                  </label>
                  <q-input
                    v-model="reportForm[param.query]"
                    outlined
                    dense
                    mask="####-##-## ##:##"
                    :placeholder="`YYYY-MM-DD HH:mm`"
                  >
                    <template #prepend>
                      <q-icon
                        name="event"
                        class="cursor-pointer"
                      >
                        <q-popup-proxy
                          cover
                          transition-show="scale"
                          transition-hide="scale"
                        >
                          <q-date
                            v-model="reportForm[param.query]"
                            mask="YYYY-MM-DD HH:mm"
                          >
                            <div class="row items-center justify-end">
                              <BaseButton
                                v-close-popup
                                label="Close"
                                color="primary"
                                flat
                              />
                            </div>
                          </q-date>
                        </q-popup-proxy>
                      </q-icon>
                    </template>
                    <template #append>
                      <q-icon
                        name="access_time"
                        class="cursor-pointer"
                      >
                        <q-popup-proxy
                          cover
                          transition-show="scale"
                          transition-hide="scale"
                        >
                          <q-time
                            v-model="reportForm[param.query]"
                            mask="YYYY-MM-DD HH:mm"
                            format24h
                          >
                            <div class="row items-center justify-end">
                              <BaseButton
                                v-close-popup
                                label="Close"
                                color="primary"
                                flat
                              />
                            </div>
                          </q-time>
                        </q-popup-proxy>
                      </q-icon>
                    </template>
                  </q-input>
                </div>
              </div>
              <!-- text inputs -->
              <div
                v-else-if="param.type == 'text'"
                class="col-12 q-mb-md"
              >
                <div class="form-group">
                  <label class="form-group-label">
                    {{ param.label }}
                    <span
                      v-if="param.required"
                      class="text-caption text-negative"
                    >
                      (Required)
                    </span>
                  </label>
                  <TextInput
                    v-model="reportForm[param.query]"
                    :placeholder="`Enter ${param.label}`"
                    :aria-label="`${param.query}_input`"
                  />
                </div>
              </div>
              <!-- numeric inputs -->
              <div
                v-else-if="param.type == 'number'"
                class="col-12 q-mb-md"
              >
                <div class="form-group">
                  <label class="form-group-label">
                    {{ param.label }}
                    <span
                      v-if="param.required"
                      class="text-caption text-negative"
                    >
                      (Required)
                    </span>
                  </label>
                  <TextInput
                    v-model="reportForm[param.query]"
                    :placeholder="`Enter ${param.label}`"
                    :aria-label="`${param.query}_input`"
                    type="number"
                    min="1"
                  />
                </div>
              </div>
              <!-- checkbox inputs (only render if not grouped with another field) -->
              <div
                v-else-if="param.type == 'checkbox' && !param.groupWith"
                class="col-12 q-mb-md"
              >
                <div class="form-group">
                  <q-checkbox
                    v-model="reportForm[param.query]"
                    :label="param.label"
                    :disable="param.dependsOn && !reportForm[param.dependsOn]?.length"
                    :aria-label="`${param.query}_checkbox`"
                  />
                </div>
              </div>
              <!-- select inputs -->
              <div
                v-else-if="!param.type || param.type === 'select'"
                class="col-12 q-mb-lg"
              >
                <div class="form-group">
                  <label class="form-group-label">
                    {{ param.label }}
                    <q-icon
                      name="info"
                      size="14px"
                      class="q-ml-xs cursor-pointer text-grey-6"
                    >
                      <q-tooltip class="text-body2">
                        {{ param.tooltip || `Filter by ${param.label.toLowerCase()}` }}
                      </q-tooltip>
                    </q-icon>
                    <span
                      v-if="param.required"
                      class="text-caption text-negative"
                    >
                      (Required)
                    </span>
                  </label>
                  <SelectInput
                    v-model="reportForm[param.query]"
                    :multiple="param.multiple"
                    :hide-selected="!param.multiple"
                    :options="param.options"
                    :option-type="param.optionType"
                    :option-query="param.optionQuery"
                    :option-value="param.optionValue ?? 'id'"
                    :option-label="param.optionLabel ?? 'name'"
                    :placeholder="`Select ${param.label}`"
                    :disabled="param.disabled"
                    @update:model-value="param.onUpdate ? param.onUpdate() : null"
                    :aria-label="`${param.query}Select`"
                  />
                  <!-- Render any checkbox grouped with this field -->
                  <template
                    v-for="groupedParam in reportParams.filter(p => p.groupWith === param.query)"
                    :key="groupedParam.query"
                  >
                    <q-checkbox
                      v-model="reportForm[groupedParam.query]"
                      :label="groupedParam.label"
                      :disable="groupedParam.dependsOn && !reportForm[groupedParam.dependsOn]?.length"
                      :aria-label="`${groupedParam.query}_checkbox`"
                      class="q-mt-sm"
                    />
                  </template>
                </div>
              </div>
            </template>
          </template>
        </div>
      </q-card-section>
    </template>

    <template #footer-content="{ hideModal }">
      <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
        <BaseButton
          no-caps
          unelevated
          color="accent"
          label="Run Report"
          class="text-body1 full-width"
          :loading="appActionIsLoadingData"
          :disable="!isReportFormValid"
          @click="generateReport()"
        />

        <q-space class="q-mx-xs" />

        <BaseButton
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
import BaseButton from '@/components/Base/BaseButton.vue'
import { ref, onBeforeMount, computed } from 'vue'
import { notify } from '@/utils/notify'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { useBuildingStore } from '@/stores/building-store'
import { useReportsStore } from '@/stores/reports-store'
import { storeToRefs } from 'pinia'
import moment from 'moment'
import SelectInput from '@/components/SelectInput.vue'
import ToggleButtonInput from '@/components/ToggleButtonInput.vue'
import TextInput from '@/components/TextInput.vue'
import PopupModal from '@/components/PopupModal.vue'

// Props
const mainProps = defineProps({
  reportType: {
    type: String,
    default: ''
  },
  reportHistory: undefined
})

// Emits
const emit = defineEmits([
  'hide',
  'submit',
  'update'
])

// Composables
const { currentScreenSize } = useCurrentScreenSize()

// Store Data
const { appActionIsLoadingData } = storeToRefs(useGlobalStore())
const {
  buildings,
  modules,
  aisles,
  ladders,
  owners,
  sizeClass,
  mediaTypes,
  users,
  verificationJobsDropdown,
  deliveryLocations
} = storeToRefs(useOptionStore())
const {
  getSideList,
  resetBuildingStore,
  resetBuildingChildren,
  resetModuleChildren,
  resetAisleChildren,
  resetSideChildren
} = useBuildingStore()
const { sides } = storeToRefs(useBuildingStore())
const { getReport, createScheduledExport } = useReportsStore()
const { getDeliveryLocations } = useOptionStore()

// Local Data
const reportModal = ref(null)
const reportParams = ref(null)
const reportForm = ref({})
const isReportFormValid = computed( () => {
  switch (mainProps.reportType) {
    case 'Item in Tray':
    case 'Non-Tray Count':
    case 'Tray/Item Count By Aisle':
      return !!reportForm.value.building_id
    case 'Open Locations':
      return !(!reportForm.value.building_id && !(reportForm.value.owner_id?.length) && !(reportForm.value.size_class_id?.length))
    default:
      return true
  }
})

// Date range picker helpers
const dateRangeValue = computed(() => {
  if (!reportForm.value.from_dt && !reportForm.value.to_dt) {
    return null
  }
  // Convert MM/DD/YYYY to YYYY/MM/DD format for q-date
  const convertToQDateFormat = (dateStr) => {
    if (!dateStr) {
      return null
    }
    const [
      month,
      day,
      year
    ] = dateStr.split('/')
    return `${year}/${month}/${day}`
  }
  const from = convertToQDateFormat(reportForm.value.from_dt)
  const to = convertToQDateFormat(reportForm.value.to_dt)
  if (from && to) {
    return {
      from,
      to
    }
  }
  return from || to
})

const formatDateRange = (fromDt, toDt) => {
  if (!fromDt && !toDt) {
    return ''
  }
  if (fromDt && toDt) {
    return `${fromDt} → ${toDt}`
  }
  if (fromDt) {
    return `From: ${fromDt}`
  }
  return `To: ${toDt}`
}

const updateDateRange = (val) => {
  // Convert YYYY/MM/DD back to MM/DD/YYYY for form storage
  const convertFromQDateFormat = (dateStr) => {
    if (!dateStr) {
      return null
    }
    const [
      year,
      month,
      day
    ] = dateStr.split('/')
    return `${month}/${day}/${year}`
  }
  if (val && typeof val === 'object' && val.from && val.to) {
    reportForm.value.from_dt = convertFromQDateFormat(val.from)
    reportForm.value.to_dt = convertFromQDateFormat(val.to)
  } else if (val && typeof val === 'string') {
    // Single date selected - use as from date
    reportForm.value.from_dt = convertFromQDateFormat(val)
    reportForm.value.to_dt = null
  }
}

const clearDateRange = () => {
  reportForm.value.from_dt = null
  reportForm.value.to_dt = null
}

// Logic


onBeforeMount(() => {
  generateReportModal()
})

const handleLocationFormChange = async (valueType) => {
  // reset the report form depending on the edited form field type
  switch (valueType) {
    case 'Building':
      resetBuildingChildren()
      reportForm.value.module_id = null
      reportForm.value.aisle_id = null
      reportForm.value.side_id = null
      reportForm.value.ladder_id = null
      return
    case 'Module':
      resetModuleChildren()
      reportForm.value.aisle_id = null
      reportForm.value.side_id = null
      reportForm.value.ladder_id = null
      return
    case 'Aisle':
      resetAisleChildren()
      // get sides since sides are toggle buttons and not dynamically loaded from a options select input
      if (reportForm.value.aisle_id) {
        await getSideList({
          building_id: reportForm.value.building_id,
          module_id: reportForm.value.module_id,
          aisle_id: reportForm.value.aisle_id
        })
      }
      reportForm.value.side_id = null
      reportForm.value.ladder_id = null
      return
    case 'Side':
      resetSideChildren()
      reportForm.value.ladder_id = null
      return
    case 'Ladder':
      return
  }
}

// Clear all form filters
const clearFilters = () => {
  generateReportModal()
}

const generateReportModal = () => {
  // creates the report modal params needed based on the selected report type
  switch (mainProps.reportType) {
    case 'Worker Efficiency (SLA)':
      reportForm.value = {
        from_dt: null,
        to_dt: null,
        user_id: null,
        job_type: null
      }
      reportParams.value = [
        {
          query: 'from_dt',
          label: 'Analysis Date (From)'
        },
        {
          query: 'to_dt',
          label: 'Analysis Date (To)'
        },
        {
          query: 'user_id',
          multiple: true,
          label: 'Users',
          options: users,
          optionType: 'users'
        },
        {
          query: 'job_type',
          multiple: true,
          label: 'Job Types',
          options: [
            {
              label: 'Shelving',
              value: 'Shelving'
            },
            {
              label: 'Accession',
              value: 'Accession'
            },
            {
              label: 'Pick List',
              value: 'PickList'
            },
            {
              label: 'Verification',
              value: 'Verification'
            }
          ],
          optionValue: 'value',
          optionLabel: 'label'
        }
      ]
      break
    case 'Retrieval Hot Zones':
      reportForm.value = {
        from_dt: null,
        to_dt: null,
        building_id: null
      }
      reportParams.value = [
        {
          query: 'from_dt',
          label: 'Activity Date (From)'
        },
        {
          query: 'to_dt',
          label: 'Activity Date (To)'
        },
        {
          query: 'building_id',
          label: 'Building',
          options: buildings,
          optionType: 'buildings'
        }
      ]
      break
    case 'Capacity Forecast':
    case 'Capacity Forecast (Height)':
      reportForm.value = {
        building_id: null,
        owner_id: null,
        include_sub_owners: false,
        lookback_days: 90
      }
      reportParams.value = [
        {
          query: 'building_id',
          label: 'Building',
          options: buildings,
          optionType: 'buildings'
        },
        {
          query: 'owner_id',
          label: 'Owner',
          options: owners,
          optionType: 'owners'
        },
        {
          query: 'include_sub_owners',
          type: 'checkbox',
          label: 'Include Sub-Owners in Growth Hierarchy',
          dependsOn: 'owner_id'
        },
        {
          query: 'lookback_days',
          label: 'Lookback Period (Days)',
          type: 'number',
          tooltip: 'Number of days to analyze historical growth (e.g. 30, 90, 180)'
        }
      ]
      break
    case 'Item Accession':
      reportForm.value = {
        from_dt: null,
        to_dt: null,
        owner_id: null,
        media_type_id: null,
        size_class_id: null,
        include_sub_tiers: false
      }

      reportParams.value = [
        {
          query: 'from_dt',
          label: 'Accession Date (From)'
        },
        {
          query: 'to_dt',
          label: 'Accession Date (To)'
        },
        {
          query: 'owner_id',
          multiple: true,
          label: 'Owner',
          options: owners,
          optionType: 'owners'
        },
        {
          query: 'media_type_id',
          multiple: true,
          label: 'Media Type',
          options: mediaTypes,
          optionType: 'mediaTypes'
        },
        {
          query: 'size_class_id',
          multiple: true,
          label: 'Size Class',
          options: sizeClass,
          optionType: 'sizeClass'
        },
        {
          query: 'include_sub_tiers',
          type: 'checkbox',
          label: 'Include Owner Tiers',
          dependsOn: 'owner_id',
          groupWith: 'owner_id'
        }
      ]
      break
    case 'Item in Tray':
      reportForm.value = {
        building_id: null, // required
        module_id: null,
        owner_id: null,
        aisle_num_from: null,
        aisle_num_to: null,
        from_dt: null,
        to_dt: null,
        include_sub_tiers: false
      }
      reportParams.value = [
        {
          query: 'building_id',
          label: 'Building',
          options: buildings,
          optionType: 'buildings',
          onUpdate: () => handleLocationFormChange('Building'),
          required: true
        },
        {
          query: 'module_id',
          label: 'Module',
          options: modules,
          optionType: 'modules',
          optionQuery: computed(() => {
            return {
              building_id: reportForm.value.building_id
            }
          }),
          optionLabel: 'module_number',
          onUpdate: () => handleLocationFormChange('Module'),
          disabled: computed(() => !reportForm.value.building_id)
        },
        {
          query: 'owner_id',
          label: 'Owner',
          options: owners,
          optionType: 'owners',
          multiple: true
        },
        {
          type: 'number',
          query: 'aisle_num_from',
          label: 'Aisle (From)'
        },
        {
          type: 'number',
          query: 'aisle_num_to',
          label: 'Aisle (To)'
        },
        {
          query: 'from_dt',
          label: 'Date (From)'
        },
        {
          query: 'to_dt',
          label: 'Date (To)'
        },
        {
          query: 'include_sub_tiers',
          type: 'checkbox',
          label: 'Include Owner Tiers',
          dependsOn: 'owner_id',
          groupWith: 'owner_id'
        }
      ]
      break
    case 'Shipping Bins':
      // Load delivery locations
      getDeliveryLocations()

      reportForm.value = {
        from_dt: null,
        to_dt: null,
        delivery_location_id: null
      }
      reportParams.value = [
        {
          query: 'from_dt',
          label: 'Job Created Date (From)'
        },
        {
          query: 'to_dt',
          label: 'Job Created Date (To)'
        },
        {
          query: 'delivery_location_id',
          label: 'Delivery Location',
          options: deliveryLocations,
          optionValue: 'id',
          optionLabel: 'name',
          multiple: true,
          placeholder: 'Select Delivery Location'
        }
      ]
      break
    case 'Non-Tray Count':
      reportForm.value = {
        building_id: null, // required
        module_id: null,
        owner_id: null,
        aisle_num_from: null,
        aisle_num_to: null,
        from_dt: null,
        to_dt: null,
        size_class_id: null,
        include_sub_tiers: false
      }
      reportParams.value = [
        {
          query: 'building_id',
          label: 'Building',
          options: buildings,
          optionType: 'buildings',
          onUpdate: () => handleLocationFormChange('Building'),
          required: true
        },
        {
          query: 'module_id',
          label: 'Module',
          options: modules,
          optionType: 'modules',
          optionQuery: computed(() => {
            return {
              building_id: reportForm.value.building_id
            }
          }),
          optionLabel: 'module_number',
          onUpdate: () => handleLocationFormChange('Module'),
          disabled: computed(() => !reportForm.value.building_id)
        },
        {
          query: 'owner_id',
          multiple: true,
          label: 'Owner',
          options: owners,
          optionType: 'owners'
        },
        {
          type: 'number',
          query: 'aisle_num_from',
          label: 'Aisle (From)'
        },
        {
          type: 'number',
          query: 'aisle_num_to',
          label: 'Aisle (To)'
        },
        {
          query: 'from_dt',
          label: 'Date (From)'
        },
        {
          query: 'to_dt',
          label: 'Date (To)'
        },
        {
          query: 'size_class_id',
          multiple: true,
          label: 'Size Class',
          options: sizeClass,
          optionType: 'sizeClass'
        },
        {
          query: 'include_sub_tiers',
          type: 'checkbox',
          label: 'Include Owner Tiers',
          dependsOn: 'owner_id',
          groupWith: 'owner_id'
        }
      ]
      break
    case 'Open Locations':
      reportForm.value = {
        building_id: null,
        module_id: null,
        aisle_id: null,
        side_id: null,
        ladder_id: null,
        size_class_id: null,
        owner_id: null,
        height: null,
        width: null,
        depth: null,
        show_partial: true,
        include_sub_tiers: false
      }
      break
    case 'Refile Discrepancy':
      reportForm.value = {
        job_id: null,
        from_dt: null,
        to_dt: null,
        assigned_user_id: null
      }
      reportParams.value = [
        {
          type: 'text',
          query: 'job_id',
          label: 'Job Number',
          options: [],
          optionType: ''
        },
        {
          query: 'from_dt',
          label: 'Date (From)'
        },
        {
          query: 'to_dt',
          label: 'Date (To)'
        },
        {
          query: 'assigned_user_id',
          label: 'Assigned User',
          options: users,
          optionType: 'users'
        }
      ]
      break
    case 'Shelving Job Discrepancy':
      reportForm.value = {
        from_dt: null,
        to_dt: null,
        shelving_job_id: null,
        assigned_user_id: null
      }
      reportParams.value = [
        {
          query: 'from_dt',
          label: 'Date (From)'
        },
        {
          query: 'to_dt',
          label: 'Date (To)'
        },
        {
          type: 'text',
          query: 'shelving_job_id',
          label: 'Job Number'
        },
        {
          query: 'assigned_user_id',
          multiple: true,
          label: 'Assigned User',
          options: users,
          optionType: 'users'
        }
      ]
      break
    case 'Shelving Move Discrepancy':
      reportForm.value = {
        from_dt: null,
        to_dt: null,
        assigned_user_id: null
      }
      reportParams.value = [
        {
          query: 'from_dt',
          label: 'Date (From)'
        },
        {
          query: 'to_dt',
          label: 'Date (To)'
        },
        {
          query: 'assigned_user_id',
          label: 'Assigned User',
          options: users,
          optionType: 'users'
        }
      ]
      break
    case 'Total Item Retrieved':
      reportForm.value = {
        from_dt: null,
        to_dt: null,
        owner_id: null,
        include_sub_tiers: false
      }
      reportParams.value = [
        {
          query: 'from_dt',
          label: 'Date (From)'
        },
        {
          query: 'to_dt',
          label: 'Date (To)'
        },
        {
          query: 'owner_id',
          label: 'Owner',
          options: owners,
          optionType: 'owners',
          multiple: true
        },
        {
          query: 'include_sub_tiers',
          type: 'checkbox',
          label: 'Include Owner Tiers',
          dependsOn: 'owner_id',
          groupWith: 'owner_id'
        }
      ]
      break
    case 'Verification Status':
      reportForm.value = {
        from_dt: null,
        to_dt: null,
        container_type: 'Tray'
      }
      reportParams.value = [
        {
          query: 'from_dt',
          label: 'Date (From)'
        },
        {
          query: 'to_dt',
          label: 'Date (To)'
        },
        {
          query: 'container_type',
          label: 'Container Type',
          options: [
            {
              label: 'Tray',
              value: 'Tray'
            },
            {
              label: 'Non-Tray',
              value: 'Non-Tray'
            }
          ],
          optionValue: 'value',
          optionLabel: 'label',
          required: true
        }
      ]
      break
    case 'Raw Data Export':
      reportForm.value = {
        dataset: null,
        from_dt: null,
        to_dt: null,
        date_range_type: null,
        dynamic_range: null,
        is_scheduled: true, // Forced for Raw Data Export
        schedule_type: null,
        frequency: null,
        retention_days: 7,
        start_at: moment().add(1, 'minute').format('YYYY-MM-DD HH:mm')
      }
      reportParams.value = [
        {
          query: 'dataset',
          label: 'Select Dataset',
          type: 'select',
          options: [
            {
              label: 'Items Master (Full Inventory)',
              value: 'items_master'
            },
            {
              label: 'Inventory Activity (Accessions/Retrievals)',
              value: 'inventory_activity'
            },
            {
              label: 'Facility Footprint (Shelf Utilization)',
              value: 'facility_footprint'
            }
          ],
          required: true,
          optionLabel: 'label',
          optionValue: 'value'
        },
        {
          query: 'date_range_type',
          label: 'Date Range Type',
          type: 'select',
          options: [
            {
              label: 'Static (Specific Dates)',
              value: 'static'
            },
            {
              label: 'Dynamic (Rolling Window)',
              value: 'dynamic'
            }
          ],
          optionLabel: 'label',
          optionValue: 'value',
          required: true
        },
        {
          query: 'from_dt',
          label: 'Date From',
          showIf: (form) => form.date_range_type === 'static'
        },
        {
          query: 'to_dt',
          label: 'Date To',
          showIf: (form) => form.date_range_type === 'static'
        },
        {
          query: 'dynamic_range',
          label: 'Reporting Window',
          type: 'select',
          options: [
            {
              label: 'Last 24 Hours',
              value: 'last_24h'
            },
            {
              label: 'Yesterday (Full Day)',
              value: 'yesterday'
            },
            {
              label: 'Last 7 Days',
              value: 'last_7d'
            },
            {
              label: 'Last 30 Days',
              value: 'last_30d'
            }
          ],
          optionLabel: 'label',
          optionValue: 'value',
          showIf: (form) => form.date_range_type === 'dynamic',
          required: true
        },
        {
          query: 'is_scheduled',
          label: 'Schedule this export?',
          type: 'checkbox',
          showIf: () => mainProps.reportType !== 'Raw Data Export'
        },
        {
          query: 'schedule_type',
          label: 'Schedule Type',
          type: 'select',
          options: [
            {
              label: 'One-Time',
              value: 'once'
            },
            {
              label: 'Recurring',
              value: 'recurring'
            }
          ],
          showIf: 'is_scheduled',
          optionLabel: 'label',
          optionValue: 'value'
        },
        {
          query: 'frequency',
          label: 'Frequency',
          type: 'select',
          options: [
            {
              label: 'Daily',
              value: 'daily'
            },
            {
              label: 'Weekly',
              value: 'weekly'
            },
            {
              label: 'Monthly',
              value: 'monthly'
            }
          ],
          showIf: (form) => form.is_scheduled && form.schedule_type === 'recurring',
          optionLabel: 'label',
          optionValue: 'value'
        },
        {
          query: 'start_at',
          label: 'Start Date & Time',
          type: 'datetime',
          showIf: 'is_scheduled',
          required: true
        },
        {
          query: 'retention_days',
          label: 'Retention (Days to keep file)',
          type: 'number',
          showIf: 'is_scheduled',
          required: true
        }
      ]
      break
    case 'Tray/Item Count By Aisle':
      reportForm.value = {
        building_id: null, // required
        aisle_from: null,
        aisle_to: null
      }
      reportParams.value = [
        {
          query: 'building_id',
          label: 'Building',
          options: buildings,
          optionType: 'buildings',
          required: true
        },
        {
          type: 'number',
          query: 'aisle_num_from',
          label: 'Aisle (From)'
        },
        {
          type: 'number',
          query: 'aisle_num_to',
          label: 'Aisle (To)'
        }
      ]
      break
    case 'User Job Summary':
      reportForm.value = {
        from_dt: null,
        to_dt: null,
        user_id: null
      }
      reportParams.value = [
        {
          query: 'from_dt',
          label: 'Date (From)'
        },
        {
          query: 'to_dt',
          label: 'Date (To)'
        },
        {
          query: 'user_id',
          label: 'User',
          options: users,
          optionType: 'users'
        }
      ]
      break
    case 'Verification Change':
      reportForm.value = {
        workflow_id: null,
        from_dt: null,
        to_dt: null,
        completed_by_id: null
      }
      reportParams.value = [
        {
          query: 'workflow_id',
          optionLabel: 'workflow_id',
          optionValue: 'workflow_id',
          label: 'Job Number',
          options: verificationJobsDropdown,
          optionType: 'verificationJobsDropdown'
        },
        {
          query: 'from_dt',
          label: 'Date (From)'
        },
        {
          query: 'to_dt',
          label: 'Date (To)'
        },
        {
          query: 'completed_by_id',
          label: 'Assigned User',
          options: users,
          optionType: 'users'
        }
      ]
      break
    case 'Withdrawn Items':
      reportForm.value = {
        from_dt: null,
        to_dt: null,
        owner_id: null,
        container_type: null,
        include_sub_tiers: false
      }
      reportParams.value = [
        {
          query: 'from_dt',
          label: 'Withdrawal Date (From)'
        },
        {
          query: 'to_dt',
          label: 'Withdrawal Date (To)'
        },
        {
          query: 'owner_id',
          multiple: true,
          label: 'Owner',
          options: owners,
          optionType: 'owners'
        },
        {
          query: 'container_type',
          label: 'Container Type',
          options: [
            {
              label: 'All',
              value: null
            },
            {
              label: 'Tray',
              value: 'Tray'
            },
            {
              label: 'Non-Tray',
              value: 'Non-Tray'
            }
          ],
          optionValue: 'value',
          optionLabel: 'label'
        },
        {
          query: 'include_sub_tiers',
          type: 'checkbox',
          label: 'Include Owner Tiers',
          dependsOn: 'owner_id',
          groupWith: 'owner_id'
        }
      ]
      break
    default:
      break
  }

  // if report history was passed in we replace the generated form with the prop
  // ex: user hits redo report we want to prepopulate the form with their past report search so they can update it
  if (mainProps.reportHistory) {
    reportForm.value = mainProps.reportHistory
  } else {
    // reset the building related state so that we dont leak modules downard to forms that require these, ex: open location report uses module downards
    resetBuildingStore()
  }
}

const shouldShowParam = (param) => {
  if (!param.showIf) {
    return true
  }
  if (typeof param.showIf === 'string') {
    return reportForm.value[param.showIf]
  }
  if (typeof param.showIf === 'function') {
    return param.showIf(reportForm.value)
  }
  return true
}

const generateReport = async () => {
  try {
    appActionIsLoadingData.value = true
    let queryParams = JSON.parse(JSON.stringify(reportForm.value))

    // convert any form date values to iso format along with removing any empty query params
    Object.entries(queryParams).forEach(([
      key,
      value
    ]) => {
      if (key.includes('_dt') && value && typeof value === 'string' && value.includes('/')) {
        const [
          month,
          day,
          year
        ] = queryParams[key].split('/')
        if (key.includes('from')) {
          // sets from dates to begging of day
          queryParams[key] = new Date(Date.UTC(year, month - 1, day, 0, 0, 0, 0)).toISOString()
        }  else {
          // sets to date to end of date
          queryParams[key] = new Date(Date.UTC(year, month - 1, day, 23, 59, 59, 999)).toISOString()
        }
      } else if ((Array.isArray(value) && value.length == 0) || !value) {
        if (key !== 'is_scheduled') { // don't delete is_scheduled
          delete queryParams[key]
        }
      }
    })

    if (queryParams.is_scheduled) {
      // Handle scheduling
      const schedulePayload = {
        name: `${mainProps.reportType} - ${queryParams.dataset} (${moment().format('LLL')})`,
        dataset: queryParams.dataset,
        schedule_type: queryParams.schedule_type,
        frequency: queryParams.frequency,
        retention_days: queryParams.retention_days,
        start_at: moment(queryParams.start_at).utc().toISOString(),
        filters: { ...queryParams }
      }
      // Clean up filters so we don't store scheduling meta in the data filter
      delete schedulePayload.filters.is_scheduled
      delete schedulePayload.filters.schedule_type
      delete schedulePayload.filters.frequency
      delete schedulePayload.filters.retention_days
      delete schedulePayload.filters.start_at

      // If dynamic, remove the static from_dt/to_dt if they were somehow set
      if (queryParams.date_range_type === 'dynamic') {
        delete schedulePayload.filters.from_dt
        delete schedulePayload.filters.to_dt
      } else {
        delete schedulePayload.filters.dynamic_range
      }

      await createScheduledExport(schedulePayload)
      notify({
        type: 'positive',
        message: mainProps.reportType === 'Raw Data Export'
          ? 'Export started in background. Please check the Export Inbox in a few moments.'
          : 'Export scheduled successfully'
      })
    } else {
      // Normal report generation
      await getReport(queryParams, mainProps.reportType)
      //emit to main report dashboard and pass query params so we can store them in the route
      emit('submit', queryParams)
    }
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to generate report'
    })
  } finally {
    // emit the current form to the parent
    emit('update', reportForm.value)

    appActionIsLoadingData.value = false
    reportModal.value.hideModal()
  }
}
</script>

<style lang="scss" scoped>
.modal-header {
  border-bottom: 2px solid $accent;
  background: linear-gradient(180deg, rgba($accent, 0.03) 0%, transparent 100%);
  margin-bottom: 8px;
}
</style>
