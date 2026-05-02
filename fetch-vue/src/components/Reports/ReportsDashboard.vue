<template>
  <div class="reports-dashboard">
    <!-- Page Header -->
    <div class="row q-mb-md">
      <div class="col-12">
        <h1 class="text-h4 text-bold">
          Reports & Tools
        </h1>
      </div>
    </div>

    <!-- Two-Column Card Layout (shown when no report is active) -->
    <div
      v-if="generatedTableColumns.length === 0"
      class="row q-col-gutter-lg"
    >
      <!-- Reports Column -->
      <div class="col-12 col-md-6">
        <div class="reports-section">
          <div class="section-header">
            <q-icon
              name="bar_chart"
              size="24px"
              class="q-mr-sm"
            />
            <span class="text-h6 text-bold">Reports</span>
          </div>
          <div class="cards-container">
            <div
              v-for="report in reportItems"
              :key="report.id"
              class="report-card"
              @click="selectReport(report.id)"
              role="button"
              tabindex="0"
              @keydown.enter="selectReport(report.id)"
            >
              <div class="report-card__content">
                <div class="report-card__title">
                  {{ report.label }}
                </div>
                <div class="report-card__description">
                  {{ report.description }}
                </div>
              </div>
              <q-icon
                name="chevron_right"
                size="20px"
                class="report-card__arrow"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- Tools Column -->
      <div class="col-12 col-md-6">
        <div class="reports-section">
          <div class="section-header">
            <q-icon
              name="build"
              size="24px"
              class="q-mr-sm"
            />
            <span class="text-h6 text-bold">Tools</span>
          </div>
          <div class="cards-container">
            <div
              v-for="tool in toolItems"
              :key="tool.id"
              class="report-card"
              @click="selectReport(tool.id)"
              role="button"
              tabindex="0"
              @keydown.enter="selectReport(tool.id)"
            >
              <div class="report-card__content">
                <div class="report-card__title">
                  {{ tool.label }}
                </div>
                <div class="report-card__description">
                  {{ tool.description }}
                </div>
              </div>
              <q-icon
                name="chevron_right"
                size="20px"
                class="report-card__arrow"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Active Report View (shown when a report is generated) -->
    <div
      v-show="generatedTableColumns.length > 0"
      class="report-results"
    >
      <!-- Report Controls Bar -->
      <div class="row q-mb-md items-center">
        <div class="col-auto">
          <BaseButton
            flat
            no-caps
            icon="arrow_back"
            label="Back to Reports"
            class="text-body2"
            @click="resetReport()"
          />
        </div>
        <div class="col-auto q-ml-md">
          <span class="text-h6 text-bold">{{ reportType }}</span>
        </div>
        <div class="col-grow" />
        <!-- All action buttons on one line -->
        <div class="col-auto flex items-center q-gutter-sm">
          <!-- Show/Hide Filters -->
          <BaseButton
            flat
            dense
            no-caps
            :color="showFilterRow ? 'accent' : 'grey-7'"
            :label="showFilterRow ? 'Hide Filters' : 'Show Filters'"
            :icon="showFilterRow ? 'filter_alt' : 'filter_alt_off'"
            class="text-body2"
            @click="toggleFilterRow"
          />
          <!-- Clear Filters -->
          <BaseButton
            v-if="showFilterRow"
            flat
            dense
            no-caps
            color="grey-7"
            label="Clear"
            icon="clear_all"
            class="text-body2"
            @click="clearColumnFilters"
          />
          <!-- Rearrange Columns -->
          <BaseButton
            flat
            dense
            no-caps
            color="grey-7"
            label="Rearrange"
            icon="view_column"
            icon-right="arrow_drop_down"
            class="text-body2"
          >
            <q-menu>
              <q-list
                dense
                style="min-width: 180px;"
              >
                <q-item-label
                  header
                  class="text-subtitle2"
                >
                  Show/Hide Columns
                </q-item-label>
                <q-item
                  v-for="col in generatedTableColumns"
                  :key="col.name"
                  clickable
                  @click="toggleColumnVisibility(col.name)"
                >
                  <q-item-section side>
                    <q-checkbox
                      :model-value="generatedTableVisibleColumns.includes(col.name)"
                      @update:model-value="toggleColumnVisibility(col.name)"
                      dense
                    />
                  </q-item-section>
                  <q-item-section>
                    <q-item-label>{{ col.label }}</q-item-label>
                  </q-item-section>
                </q-item>
              </q-list>
            </q-menu>
          </BaseButton>
          <!-- Redo Report -->
          <BaseButton
            v-if="reportType && reportType !== 'Item Lookup'"
            flat
            dense
            no-caps
            color="grey-7"
            icon="refresh"
            label="Redo Report"
            class="text-body2"
            @click="showReportModal = true"
          />
          <!-- Export Report -->
          <BaseButton
            flat
            dense
            no-caps
            color="grey-7"
            icon="download"
            icon-right="arrow_drop_down"
            label="Export Report"
            class="text-body2"
            :disabled="appIsOffline"
            aria-label="exportReportMenu"
            aria-haspopup="menu"
            :aria-expanded="exportReportMenuState"
          >
            <q-menu
              @show="exportReportMenuState = true"
              @hide="exportReportMenuState = false"
              aria-label="exportReportMenuList"
            >
              <q-list>
                <q-item
                  clickable
                  v-close-popup
                  @click="reportPrintTemplate.printReport()"
                  role="menuitem"
                >
                  <q-item-section>
                    <q-item-label>
                      <span>Print</span>
                    </q-item-label>
                  </q-item-section>
                </q-item>
                <q-item
                  clickable
                  v-close-popup
                  @click="downloadReport(reportType)"
                  role="menuitem"
                >
                  <q-item-section>
                    <q-item-label>
                      <span>Download CSV</span>
                    </q-item-label>
                  </q-item-section>
                </q-item>
              </q-list>
            </q-menu>
          </BaseButton>
        </div>
      </div>

      <!-- Report Table -->
      <div class="row q-mb-xs-xl q-mb-sm-none">
        <div class="col-grow q-mb-xs-md q-mb-sm-none">
          <EssentialTable
            :table-columns="generatedTableColumns"
            :table-visible-columns="generatedTableVisibleColumns"
            :table-data="filteredReportData"
            :enable-table-reorder="false"
            :hide-table-rearrange="true"
            :heading-row-class="'q-mb-xs-md q-mb-md-lg'"
            :heading-rearrange-class="'q-ml-auto'"
            :enable-pagination="true"
            :pagination-total="reportDataTotal"
            :pagination-loading="appIsLoadingData"
            @update-pagination="regenerateReport($event)"
            @selected-table-row="null"
          >


            <!-- Filter row for column filtering - works for all reports -->
            <template #header-filter-row="{ cols }">
              <q-tr
                v-if="showFilterRow"
                class="filter-row"
              >
                <q-th
                  v-for="col in cols"
                  :key="col.name"
                  class="filter-cell"
                >
                  <q-input
                    v-model="columnFilters[getColumnFilterKey(col)]"
                    dense
                    outlined
                    clearable
                    :placeholder="`${col.label}...`"
                    class="column-filter-input"
                    @click.stop
                  >
                    <template #prepend>
                      <q-icon
                        name="search"
                        size="16px"
                        color="grey-6"
                      />
                    </template>
                  </q-input>
                </q-th>
              </q-tr>
            </template>

            <template #table-td="{ colName, value }">
              <span
                v-if="colName.includes('_dt')"
                class="text-nowrap"
              >
                {{ formatDateTime(value).date }}
              </span>
              <span
                v-if="colName == 'status'"
                class="outline text-nowrap"
                :class="value == 'Created' || value == 'Completed' ? 'text-highlight' : value == 'Paused' || value == 'Running' ? 'text-highlight-warning' : 'text-highlight-negative'"
              >
                {{ value }}
              </span>
            </template>
          </EssentialTable>
        </div>
      </div>
    </div>

    <!-- Generate Report Modal -->
    <ReportsGenerateModal
      v-if="showReportModal"
      :report-type="reportType"
      :report-history="reportFormHistory"
      @hide="showReportModal = false; reportType = lastReportType;"
      @update="reportFormHistory = $event"
      @submit="generateReportTableFields($event);"
    />

    <!-- ====================================================== -->
    <!-- ============== START: ADD NEW SCANNER MODAL ========== -->
    <!-- ====================================================== -->
    <ItemDetailsScannerModal
      v-if="showScannerModal"
      @hide="showScannerModal = false; resetReport();"
    />
    <!-- ====================================================== -->
    <!-- =============== END: ADD NEW SCANNER MODAL =========== -->
    <!-- ====================================================== -->
  </div>

  <!-- print component used to handle printing the reports -->
  <ReportPrintTemplate
    ref="reportPrintTemplate"
    :report-details="{
      type: reportType,
      data: reportData,
      headers: generatedTableColumns
    }"
  />
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
import { ref, inject, onBeforeMount, computed, watch } from 'vue'
import { notify } from '@/utils/notify'
import { useRoute, useRouter } from 'vue-router'
import { useReportsStore } from '@/stores/reports-store'
import { useGlobalStore } from '@/stores/global-store'
import { storeToRefs } from 'pinia'
import EssentialTable from '@/components/EssentialTable.vue'
import ReportsGenerateModal from '@/components/Reports/ReportsGenerateModal.vue'
import ReportPrintTemplate from '@/components/Reports/ReportPrintTemplate.vue'
// ======================================================
// ========= START: IMPORT NEW SCANNER MODAL ============
// ======================================================
import ItemDetailsScannerModal from '@/components/ItemDetailsScannerModal.vue'
// ======================================================
// ========== END: IMPORT NEW SCANNER MODAL =============
// ======================================================

const route = useRoute()
const router = useRouter()

// Store Data
const { reportData, reportDataTotal } = storeToRefs(useReportsStore())
const { getReport } = useReportsStore()
const { appIsLoadingData } = storeToRefs(useGlobalStore())
const { appIsOffline } = storeToRefs(useGlobalStore())
const { downloadReport } = useReportsStore()

// Local Data
const generatedTableVisibleColumns = ref([])
const generatedTableColumns = ref([])
const showReportModal = ref(false)
// ======================================================
// =========== START: ADD NEW REF FOR SCANNER ===========
// ======================================================
const showScannerModal = ref(false)
// ======================================================
// ============ END: ADD NEW REF FOR SCANNER ============
// ======================================================
const reportFormHistory = ref(null)
const reportType = ref(null)
const lastReportType = ref(null)
const reportItems = ref([
  {
    id: 'Item Accession',
    label: 'Item Accession',
    description: 'View accessioned items by month, owner, and media type'
  },
  {
    id: 'Item in Tray',
    label: 'Item in Tray',
    description: 'Count of trays and items by size class'
  },
  {
    id: 'Non-Tray Count',
    label: 'Non-Tray Count',
    description: 'Count of non-tray items by size class'
  },
  {
    id: 'Open Locations',
    label: 'Open Locations',
    description: 'Available shelf locations with space info'
  },
  {
    id: 'Refile Discrepancy',
    label: 'Refile Discrepancy',
    description: 'Refile job errors and discrepancies'
  },
  {
    id: 'Shelving Job Discrepancy',
    label: 'Shelving Job Discrepancy',
    description: 'Shelving job errors by user and location'
  },
  {
    id: 'Shelving Move Discrepancy',
    label: 'Shelving Move Discrepancy',
    description: 'Item location move discrepancies'
  },
  {
    id: 'Shipping Bins',
    label: 'Shipping Bins',
    description: 'Active shipping bins by location and date'
  },
  {
    id: 'Total Item Retrieved',
    label: 'Total Item Retrieved',
    description: 'Retrieval counts by owner'
  },
  {
    id: 'Tray/Item Count By Aisle',
    label: 'Tray/Item Count By Aisle',
    description: 'Inventory counts organized by aisle'
  },
  {
    id: 'User Job Summary',
    label: 'User Job Summary',
    description: 'Job processing totals by user'
  },
  {
    id: 'Verification Change',
    label: 'Verification Change',
    description: 'Verification actions and changes'
  },
  {
    id: 'Verification Status',
    label: 'Verification Status',
    description: 'Verified trays and items by date'
  },
  {
    id: 'Withdrawn Items',
    label: 'Withdrawn Items',
    description: 'List of withdrawn items with locations'
  }
])
const toolItems = ref([
  {
    id: 'Item Lookup',
    label: 'Item Lookup',
    description: 'Scan or search for item details'
  }
])
const reportPrintTemplate = ref(null)
const exportReportMenuState = ref(false)

// Filter state for report results
const showFilterRow = ref(false)
const columnFilters = ref({})
const filteredReportData = computed(() => {
  if (!reportData.value || Object.keys(columnFilters.value).length === 0) {
    return reportData.value
  }
  return reportData.value.filter(row => {
    return Object.entries(columnFilters.value).every(([
      colName,
      filterValue
    ]) => {
      if (!filterValue || filterValue === '' || (Array.isArray(filterValue) && filterValue.length === 0)) {
        return true
      }
      // Find the column definition to get the proper field accessor
      const col = generatedTableColumns.value.find(c => c.name === colName)
      let cellValue = ''
      if (col) {
        if (typeof col.field === 'function') {
          cellValue = String(col.field(row) || '').toLowerCase()
        } else {
          cellValue = String(row[col.field] || '').toLowerCase()
        }
      }
      if (Array.isArray(filterValue)) {
        return filterValue.some(v => cellValue.includes(String(v).toLowerCase()))
      }
      return cellValue.includes(String(filterValue).toLowerCase())
    })
  })
})

const clearColumnFilters = () => {
  columnFilters.value = {}
}

const toggleFilterRow = () => {
  showFilterRow.value = !showFilterRow.value
  if (!showFilterRow.value) {
    clearColumnFilters()
  }
}

// Toggle column visibility for rearrange dropdown
const toggleColumnVisibility = (colName) => {
  const index = generatedTableVisibleColumns.value.indexOf(colName)
  if (index > -1) {
    // Don't allow hiding all columns - keep at least one
    if (generatedTableVisibleColumns.value.length > 1) {
      generatedTableVisibleColumns.value.splice(index, 1)
    }
  } else {
    generatedTableVisibleColumns.value.push(colName)
  }
}

// Get filter key from column - handles both string and function field types
const getColumnFilterKey = (col) => {
  // Use column name as the filter key since field can be a function
  return col.name
}

// Reports that support server-side filtering
const serverSideFilteredReports = [
  'Open Locations',
  'User Job Summary',
  'Verification Status'
]

// Debounce timer for filter changes
let filterDebounceTimer = null

// Watch column filters and trigger regenerateReport for server-side filtered reports
watch(
  columnFilters,
  () => {
    // Only trigger for reports that support server-side filtering
    if (serverSideFilteredReports.includes(reportType.value)) {
      // Clear any existing debounce timer
      if (filterDebounceTimer) {
        clearTimeout(filterDebounceTimer)
      }
      // Debounce the API call by 500ms to avoid rapid requests
      filterDebounceTimer = setTimeout(() => {
        regenerateReport({})
      }, 500)
    }
  },
  { deep: true }
)

// Logic

const formatDateTime = inject('format-date-time')
const renderItemBarcodeDisplay = inject('render-item-barcode-display')

onBeforeMount(() => {
  // when loading the dashboard if the route contains a specific report type we need to preload that table and get the report
  if (route.params.reportType) {
    reportType.value = route.params.reportType
    generateReportTableFields(route.query)
    regenerateReport(route.query)
  }
})

const resetReport = () => {
  reportType.value = null
  reportFormHistory.value = null
  generatedTableColumns.value = []
  generatedTableVisibleColumns.value = []
  showReportModal.value = false
}

// Selection handler for card clicks
const selectReport = (reportId) => {
  reportType.value = reportId
  if (reportId === 'Item Lookup') {
    showScannerModal.value = true
  } else {
    reportFormHistory.value = null
    showReportModal.value = true
  }
}

const generateReportTableFields = (qParams) => {
  lastReportType.value = reportType.value
  // creates the report table fields needed based on the selected report type
  switch (reportType.value) {
    case 'Item Accession':
      generatedTableColumns.value = [
        {
          name: 'year',
          field: 'year',
          label: 'Year',
          align: 'left',
          sortable: true
        },
        {
          name: 'month',
          field: 'month',
          label: 'Month',
          align: 'left',
          sortable: true
        },
        {
          name: 'owner',
          field: 'owner_name',
          label: 'Owner',
          align: 'left',
          sortable: true
        },
        {
          name: 'media_type',
          field: 'media_type_name',
          label: 'Media Type',
          align: 'left',
          sortable: true
        },
        {
          name: 'size_class',
          field: 'size_class_name',
          label: 'Size Class',
          align: 'left',
          sortable: true
        },
        {
          name: 'total_count',
          field: 'count',
          label: 'Total Accessioned Count',
          align: 'left',
          sortable: true
        }
      ]
      generatedTableVisibleColumns.value = [
        'year',
        'month',
        'owner',
        'media_type',
        'size_class',
        'total_count'
      ]
      break
    case 'Item in Tray':
      generatedTableColumns.value = [
        {
          name: 'size_class_short_name',
          field: 'size_class_short_name',
          label: 'Size Class',
          align: 'left',
          sortable: true
        },
        {
          name: 'tray_count',
          field: 'tray_count',
          label: 'Total Tray Count',
          align: 'left',
          sortable: true
        },
        {
          name: 'tray_item_count',
          field: 'tray_item_count',
          label: 'Total Item Count',
          align: 'left',
          sortable: true
        }
      ]
      generatedTableVisibleColumns.value = [
        'size_class_short_name',
        'tray_count',
        'tray_item_count'
      ]
      break
    case 'Non-Tray Count':
      generatedTableColumns.value = [
        {
          name: 'size_class_short_name',
          field: 'size_class_short_name',
          label: 'Size Class',
          align: 'left',
          sortable: true
        },
        {
          name: 'non_tray_item_count',
          field: 'non_tray_item_count',
          label: '# of Non-Tray Items',
          align: 'left',
          sortable: true
        }
      ]
      generatedTableVisibleColumns.value = [
        'size_class_short_name',
        'non_tray_item_count'
      ]
      break
    case 'Open Locations':
      generatedTableColumns.value = [
        {
          name: 'location',
          field: 'location',
          label: 'Shelf Location',
          align: 'left',
          sortable: true
        },
        {
          name: 'shelf_barcode',
          field: row => row.barcode?.value,
          label: 'Shelf Barcode',
          align: 'left',
          sortable: true
        },
        {
          name: 'owner',
          field: row => row.owner?.name,
          label: 'Owner',
          align: 'left',
          sortable: true
        },
        {
          name: 'size_class',
          field: row => row.shelf_type?.size_class?.short_name,
          label: 'Size Class',
          align: 'left',
          sortable: true
        },
        {
          name: 'height',
          field: 'height',
          label: 'Height',
          align: 'left',
          sortable: true
        },
        {
          name: 'width',
          field: 'width',
          label: 'Width',
          align: 'left',
          sortable: true
        },
        {
          name: 'depth',
          field: 'depth',
          label: 'Depth',
          align: 'left',
          sortable: true
        },
        {
          name: 'available_space',
          field: 'available_space',
          label: 'Available Space',
          align: 'left',
          sortable: true
        }
      ]
      generatedTableVisibleColumns.value = [
        'location',
        'shelf_barcode',
        'owner',
        'size_class',
        'height',
        'width',
        'depth',
        'available_space'
      ]
      break
    case 'Shipping Bins':
      generatedTableColumns.value = [
        {
          name: 'barcode',
          field: 'barcode',
          label: 'Bin #',
          align: 'left',
          sortable: true
        },
        {
          name: 'shipping_job_id',
          field: 'shipping_job_id',
          label: 'Shipping Job #',
          align: 'left',
          sortable: true
        },
        {
          name: 'create_dt',
          field: 'create_dt',
          label: 'Created Date',
          align: 'left',
          sortable: true
        },
        {
          name: 'item_count',
          field: 'item_count',
          label: '# Items',
          align: 'left',
          sortable: true
        },
        {
          name: 'delivery_location',
          field: row => row.delivery_location?.name,
          label: 'Delivery Location',
          align: 'left',
          sortable: true
        }
      ]
      generatedTableVisibleColumns.value = [
        'barcode',
        'shipping_job_id',
        'create_dt',
        'item_count',
        'delivery_location'
      ]
      break
    case 'Refile Discrepancy':
      generatedTableColumns.value = [
        {
          name: 'id',
          field: 'id',
          label: 'Refile Job #',
          align: 'left',
          sortable: true
        },
        {
          name: 'assigned_user',
          field: row => row.assigned_user?.name,
          label: 'Completed By',
          align: 'left',
          sortable: true
        },
        {
          name: 'complete_dt',
          field: 'complete_dt',
          label: 'Completed Date',
          align: 'left',
          sortable: true
        },
        {
          name: 'barcode',
          field: row => row.barcode?.value,
          label: 'Item Barcode',
          align: 'left',
          sortable: true
        },
        {
          name: 'tray_barcode',
          field: row => row.tray?.barcode?.value,
          label: 'Tray Barcode',
          align: 'left',
          sortable: true
        },
        {
          name: 'error',
          field: 'error',
          label: 'Error Type',
          align: 'left',
          sortable: true
        }
      ]
      generatedTableVisibleColumns.value = [
        'id',
        'assigned_user',
        'complete_dt',
        'barcode',
        'tray_barcode',
        'error'
      ]
      break
    case 'Shelving Job Discrepancy':
      generatedTableColumns.value = [
        {
          name: 'shelving_job_id',
          field: 'shelving_job_id',
          label: 'Shelving Job #',
          align: 'left',
          sortable: true
        },
        {
          name: 'assigned_user',
          field: row => row.assigned_user?.name,
          label: 'Assigned User',
          align: 'left',
          sortable: true
        },
        {
          name: 'barcode_value',
          field: row => row.tray ? row.tray?.barcode?.value : row.non_tray_item?.barcode?.value,
          label: 'Tray / Non-Tray Barcode',
          align: 'left',
          sortable: true
        },
        {
          name: 'size_class',
          field: row => row.size_class?.short_name,
          label: 'Size Class',
          align: 'left',
          sortable: true
        },
        {
          name: 'owner',
          field: row => row.owner?.name,
          label: 'Owner',
          align: 'left',
          sortable: true
        },
        {
          name: 'assigned_location',
          field: row => row?.assigned_location,
          label: 'Assigned Location',
          align: 'left',
          sortable: true
        },
        {
          name: 'pre_assigned_location',
          field: row => row.pre_assigned_location ? row.pre_assigned_location?.value : 'N/A',
          label: 'Pre-Assigned Location',
          align: 'left',
          sortable: true
        },
        {
          name: 'error',
          field: 'error',
          label: 'Error Type',
          align: 'left',
          sortable: true
        }
      ]
      generatedTableVisibleColumns.value = [
        'shelving_job_id',
        'assigned_user',
        'barcode_value',
        'size_class',
        'owner',
        'assigned_location',
        'pre_assigned_location',
        'error'
      ]
      break
    case 'Shelving Move Discrepancy':
      generatedTableColumns.value = [
        {
          name: 'update_dt',
          field: 'update_dt',
          label: 'Completed Date',
          align: 'left',
          sortable: true
        },
        {
          name: 'assigned_user',
          field: row => row.assigned_user?.name,
          label: 'Completed By',
          align: 'left',
          sortable: true
        },
        {
          name: 'container_type',
          field: row => row.item || row.tray ? 'Tray' : 'Non-Tray',
          label: 'Container Type',
          align: 'left',
          sortable: true
        },
        {
          name: 'size_class',
          field: row => row.size_class?.short_name,
          label: 'Size Class',
          align: 'left',
          sortable: true
        },
        {
          name: 'barcode_value',
          field: row => row.item ? renderItemBarcodeDisplay(row.item) : row.tray ? renderItemBarcodeDisplay(row.tray) : renderItemBarcodeDisplay(row.non_tray_item),
          label: 'Barcode',
          align: 'left',
          sortable: true
        },
        {
          name: 'original_assigned_location',
          field: 'original_assigned_location',
          label: 'Original Item Location',
          align: 'left',
          sortable: true
        },
        {
          name: 'current_assigned_location',
          field: 'current_assigned_location',
          label: 'Current Item Location',
          align: 'left',
          sortable: true
        },
        {
          name: 'error',
          field: 'error',
          label: 'Error Type',
          align: 'left',
          sortable: true
        }
      ]
      generatedTableVisibleColumns.value = [
        'update_dt',
        'assigned_user',
        'container_type',
        'size_class',
        'barcode_value',
        'original_assigned_location',
        'current_assigned_location',
        'error'
      ]
      break
    case 'Total Item Retrieved':
      generatedTableColumns.value = [
        {
          name: 'owner_name',
          field: 'owner_name',
          label: 'Owner',
          align: 'left',
          sortable: true
        },
        {
          name: 'total_item_retrieved_count',
          field: 'total_item_retrieved_count',
          label: 'Total Item Retrieval Count',
          align: 'left',
          sortable: true
        },
        {
          name: 'max_retrieved_count',
          field: 'max_retrieved_count',
          label: 'Max Retrieval Count',
          align: 'left',
          sortable: true
        }
      ]
      generatedTableVisibleColumns.value = [
        'owner_name',
        'total_item_retrieved_count',
        'max_retrieved_count'
      ]
      break
    case 'Tray/Item Count By Aisle':
      generatedTableColumns.value = [
        {
          name: 'aisle_number',
          field: 'aisle_number',
          label: 'Aisle Number',
          align: 'left',
          sortable: true
        },
        {
          name: 'shelf_count',
          field: 'shelf_count',
          label: 'Shelf Count',
          align: 'left',
          sortable: true
        },
        {
          name: 'tray_count',
          field: 'tray_count',
          label: '# of Trays',
          align: 'left',
          sortable: true
        },
        {
          name: 'item_count',
          field: 'item_count',
          label: '# of Tray Items',
          align: 'left',
          sortable: true
        },
        {
          name: 'non_tray_item_count',
          field: 'non_tray_item_count',
          label: '# of Non-Tray Items',
          align: 'left',
          sortable: true
        },
        {
          name: 'total_item_count',
          field: 'total_item_count',
          label: 'Total Items',
          align: 'left',
          sortable: true
        }
      ]
      generatedTableVisibleColumns.value = [
        'aisle_number',
        'shelf_count',
        'tray_count',
        'item_count',
        'non_tray_item_count',
        'total_item_count'
      ]
      break
    case 'User Job Summary':
      generatedTableColumns.value = [
        {
          name: 'user_name',
          field: 'user_name',
          label: 'User Name',
          align: 'left',
          sortable: true
        },
        {
          name: 'job_type',
          field: 'job_type',
          label: 'Job Type',
          align: 'left',
          sortable: true
        },
        {
          name: 'total_items_processed',
          field: 'total_items_processed',
          label: 'Total Items Processed',
          align: 'left',
          sortable: true
        }
      ]
      generatedTableVisibleColumns.value = [
        'user_name',
        'job_type',
        'total_items_processed'
      ]
      break
    case 'Verification Change':
      generatedTableColumns.value = [
        {
          name: 'workflow_id',
          field: 'workflow_id',
          label: 'Verification Job #',
          align: 'left',
          sortable: true
        },
        {
          name: 'completed_dt',
          field: 'completed_dt',
          label: 'Completed Date',
          align: 'left',
          sortable: true
        },
        {
          name: 'completed_by',
          field: 'completed_by',
          label: 'Completed By',
          align: 'left',
          sortable: true
        },
        {
          name: 'item_barcode',
          field: 'item_barcode',
          label: 'Item Barcode',
          align: 'left',
          sortable: true
        },
        {
          name: 'tray_barcode',
          field: 'tray_barcode',
          label: 'Tray Barcode',
          align: 'left',
          sortable: true
        },
        {
          name: 'action',
          field: 'action',
          label: 'Action',
          align: 'left',
          sortable: true
        }
      ]
      generatedTableVisibleColumns.value = [
        'workflow_id',
        'completed_dt',
        'completed_by',
        'item_barcode',
        'tray_barcode',
        'action'
      ]
      break
    case 'Verification Status':
      generatedTableColumns.value = [
        {
          name: 'barcode',
          field: 'barcode',
          label: 'Barcode',
          align: 'left',
          sortable: true
        },
        {
          name: 'owner',
          field: 'owner',
          label: 'Owner',
          align: 'left',
          sortable: true
        },
        {
          name: 'verification_job_id',
          field: 'verification_job_id',
          label: 'Verification Job #',
          align: 'left',
          sortable: true
        },
        {
          name: 'verification_dt',
          field: 'verification_dt',
          label: 'Verification Date',
          align: 'left',
          sortable: true
        }
      ]
      generatedTableVisibleColumns.value = [
        'barcode',
        'owner',
        'verification_job_id',
        'verification_dt'
      ]
      break
    case 'Withdrawn Items':
      generatedTableColumns.value = [
        {
          name: 'barcode',
          field: 'barcode',
          label: 'Barcode',
          align: 'left',
          sortable: true
        },
        {
          name: 'owner',
          field: 'owner',
          label: 'Owner',
          align: 'left',
          sortable: true
        },
        {
          name: 'withdrawn_location',
          field: 'withdrawn_location',
          label: 'Last Location',
          align: 'left',
          sortable: true
        },
        {
          name: 'withdrawal_dt',
          field: 'withdrawal_dt',
          label: 'Withdrawal Date',
          align: 'left',
          sortable: true
        }
      ]
      generatedTableVisibleColumns.value = [
        'barcode',
        'owner',
        'withdrawn_location',
        'withdrawal_dt'
      ]
      break
    default:
      break
  }

  // update our route with passed in query params and report type
  router.push({
    name: 'reports',
    params: {
      reportType: reportType.value
    },
    query: qParams
  })
}

// Map column filter names to API search parameter names for server-side filtering
const getServerSideFilterParams = () => {
  const filterParams = {}

  // Only include filters for reports that support server-side filtering
  if (reportType.value === 'Open Locations') {
    if (columnFilters.value.location) {
      filterParams.location_search = columnFilters.value.location
    }
    if (columnFilters.value.shelf_barcode) {
      filterParams.shelf_barcode_search = columnFilters.value.shelf_barcode
    }
    if (columnFilters.value.owner) {
      filterParams.owner_search = columnFilters.value.owner
    }
    if (columnFilters.value.size_class) {
      filterParams.size_class_search = columnFilters.value.size_class
    }
  } else if (reportType.value === 'User Job Summary') {
    if (columnFilters.value.user_name) {
      filterParams.user_name_search = columnFilters.value.user_name
    }
    if (columnFilters.value.job_type) {
      filterParams.job_type_search = columnFilters.value.job_type
    }
  } else if (reportType.value === 'Verification Status') {
    if (columnFilters.value.barcode) {
      filterParams.barcode_search = columnFilters.value.barcode
    }
    if (columnFilters.value.owner) {
      filterParams.owner_search = columnFilters.value.owner
    }
  }

  return filterParams
}

const regenerateReport = async (qParams) => {
  try {
    appIsLoadingData.value = true
    let queryParamsForm
    if (reportFormHistory.value !== null) {
      queryParamsForm = JSON.parse(JSON.stringify(reportFormHistory.value))
      // convert any form date values to iso format along with removing any empty query params
      Object.entries(queryParamsForm).forEach(([
        key,
        value
      ]) => {
        if (key.includes('_dt') && value) {
          const [
            month,
            day,
            year
          ] = queryParamsForm[key].split('/')
          if (key.includes('from')) {
          // sets from dates to begging of day
            queryParamsForm[key] = new Date(Date.UTC(year, month - 1, day, 0, 0, 0, 0)).toISOString()
          }  else {
          // sets to date to end of date
            queryParamsForm[key] = new Date(Date.UTC(year, month - 1, day, 23, 59, 59, 999)).toISOString()
          }
        } else if ((Array.isArray(value) && value.length == 0) || !value) {
          delete queryParamsForm[key]
        }
      })
    }

    // Get server-side filter params
    const serverSideFilters = getServerSideFilterParams()

    await getReport({
      ...qParams,
      ...queryParamsForm,
      ...serverSideFilters
    }, reportType.value)
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Failed to regenerate report'
    })
  } finally {
    appIsLoadingData.value = false
  }
}
</script>
<style lang="scss" scoped>
.reports-dashboard {
  // Section containers
  .reports-section {
    background: $color-white;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  }

  .section-header {
    display: flex;
    align-items: center;
    padding-bottom: 16px;
    margin-bottom: 16px;
    border-bottom: 2px solid $accent;

    .q-icon {
      color: $primary;
    }
  }

  .cards-container {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  // Card styling
  .report-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    background: $color-gray-light;
    border-radius: 8px;
    border-left: 4px solid transparent;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      background: $color-white;
      border-left-color: $accent;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
      transform: translateY(-2px);
    }

    &:focus {
      outline: 2px solid $accent;
      outline-offset: 2px;
    }

    &__content {
      flex: 1;
    }

    &__title {
      font-size: 1rem;
      font-weight: 600;
      color: $dark;
      margin-bottom: 4px;
    }

    &__description {
      font-size: 0.85rem;
      color: $color-gray-dark;
      line-height: 1.4;
    }

    &__arrow {
      color: $color-gray-dark;
      transition: transform 0.2s ease, color 0.2s ease;
    }

    &:hover &__arrow {
      color: $accent;
      transform: translateX(4px);
    }
  }

  // Active report results view
  .report-results {
    background: $color-white;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  }
}
</style>