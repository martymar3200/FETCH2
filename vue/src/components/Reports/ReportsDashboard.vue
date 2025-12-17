<template>
  <div class="reports-dashboard">
    <div class="row q-mb-xs">
      <div class="col-12">
        <h1 class="text-h4 text-bold q-mb-sm">
          Reports
        </h1>
      </div>
    </div>

    <div class="row q-mb-xs-lg q-mb-sm-md">
      <div class="col-auto">
        <SelectInput
          v-model="reportType"
          :options="reportOptions"
          :clearable="true"
          :placeholder="'Select Report or Tool'"
          @clear="resetReport()"
          @update:model-value="handleSelection"
          aria-label="reportSelect"
        />
      </div>
      <div class="col-auto">
        <q-btn
          v-if="reportType && reportType !== 'Item Lookup'"
          no-caps
          unelevated
          color="accent"
          label="Redo Report"
          class="text-body1 full-height q-ml-xs-xs q-ml-sm-sm"
          @click="showReportModal = true"
        />
      </div>
    </div>

    <div
      v-show="generatedTableColumns.length > 0"
      class="row q-mb-xs-xl q-mb-sm-none"
    >
      <div class="col-grow q-mb-xs-md q-mb-sm-none">
        <EssentialTable
          :table-columns="generatedTableColumns"
          :table-visible-columns="generatedTableVisibleColumns"
          :table-data="reportData"
          :enable-table-reorder="false"
          :heading-row-class="'q-mb-xs-md q-mb-md-lg'"
          :heading-rearrange-class="'q-ml-auto'"
          :enable-pagination="true"
          :pagination-total="reportDataTotal"
          :pagination-loading="appIsLoadingData"
          @update-pagination="regenerateReport($event)"
          @selected-table-row="null"
        >
          <template #heading-row>
            <div
              class="col-xs-12 col-sm-12 col-md-auto flex"
              :class="currentScreenSize == 'sm' || currentScreenSize == 'xs' ? 'justify-end q-mb-md' : 'order-1'"
            >
              <q-btn
                no-caps
                unelevated
                icon-right="arrow_drop_down"
                color="accent"
                label="Export Report"
                class="text-body1 q-ml-xs-none q-ml-sm-sm"
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
                          <span>
                            Print
                          </span>
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
                          <span>
                            Download CSV
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
import { ref, inject, onBeforeMount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { useReportsStore } from '@/stores/reports-store'
import { useGlobalStore } from '@/stores/global-store'
import { storeToRefs } from 'pinia'
import EssentialTable from '@/components/EssentialTable.vue'
import SelectInput from '@/components/SelectInput.vue'
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

// Composables
const { currentScreenSize } = useCurrentScreenSize()

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
const reportOptions =  ref([
  'Item Accession',
  'Item in Tray',
  'Non-Tray Count',
  'Open Locations',
  'Refile Discrepancy',
  'Shelving Job Discrepancy',
  'Shelving Move Discrepancy',
  'Total Item Retrieved',
  'Tray/Item Count By Aisle',
  'User Job Summary',
  'Verification Change',
  // ======================================================
  // ========= START: ADD NEW OPTION TO DROPDOWN ==========
  // ======================================================
  'Item Lookup'
  // ======================================================
  // ========== END: ADD NEW OPTION TO DROPDOWN ===========
  // ======================================================
])
const reportPrintTemplate = ref(null)
const exportReportMenuState = ref (false)

// Logic
const handleAlert = inject('handle-alert')
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

// ======================================================
// ====== START: CREATE NEW SELECTION HANDLER ===========
// ======================================================
const handleSelection = (selectedValue) => {
  // This new function will decide which modal to open
  if (selectedValue === 'Item Lookup') {
    showScannerModal.value = true
  } else if (selectedValue) {
    // If it's any other report, open the standard report modal
    reportFormHistory.value = null
    showReportModal.value = true
  }
}
// ======================================================
// ======= END: CREATE NEW SELECTION HANDLER ============
// ======================================================

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
        'owner',
        'size_class',
        'height',
        'width',
        'depth',
        'available_space'
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

    await getReport({
      ...qParams,
      ...queryParamsForm
    }, reportType.value)
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