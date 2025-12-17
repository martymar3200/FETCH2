import { defineStore } from 'pinia'
import moment from 'moment'
import inventoryServiceApi from '@/http/InventoryService.js'

export const useReportsStore = defineStore('reports-store', {
  state: () => ({
    reportDataTotal: 0,
    reportData: [],
    reportQueryParams: {},
    auditTrailData: []
  }),
  actions: {
    resetReportsStore () {
      this.$reset()
    },
    generateReportEndpoint (reportType) {
      const endpointMap = {
        'Item Accession': inventoryServiceApi.reportingAccessionItems,
        'Shelving Job Discrepancy':inventoryServiceApi.reportingShelvingDiscrepancies,
        'Shelving Move Discrepancy':inventoryServiceApi.reportingMoveDiscrepancies,
        'Open Locations': inventoryServiceApi.reportingOpenLocations,
        'Tray/Item Count By Aisle': inventoryServiceApi.reportingAislesItemsCount,
        'Non-Tray Count': inventoryServiceApi.reportingNonTrayItemsCount,
        'Total Item Retrieved': inventoryServiceApi.reportingRetrievalsCount,
        'Item in Tray': inventoryServiceApi.reportingTrayItemsCount,
        'User Job Summary': inventoryServiceApi.reportingUserJobsCount,
        'Verification Change': inventoryServiceApi.reportingVerificationChangesSummary
      }

      return endpointMap[reportType] || null
    },
    async getReport (paramsObj, reportType) {
      try {
        const endpoint = this.generateReportEndpoint(reportType)
        this.reportData = []
        if (endpoint) {
          const res = await this.$api.get(endpoint, {
            params: {
              size: this.apiPageSizeDefault,
              ...paramsObj
            }
          } )
          this.reportData = res.data.items // Store the report data
          this.reportDataTotal = res.data.total // keep track of response total for pagination
          this.reportQueryParams = paramsObj // Remember the query params for download
        }
      } catch (error) {
        throw error
      }
    },
    async downloadReport (reportType) {
      try {
        const endpoint = this.generateReportEndpoint(reportType)
        if (endpoint) {
          const res = await this.$api.get(`${endpoint}download`, {
            params: { ...this.reportQueryParams },
            responseType: 'blob'
          })
          const url = window.URL.createObjectURL(new Blob([res.data], { type: 'text/csv' }))

          // Get the current date and time and format as YYYY_MM_DD_HH_MM_SS
          const formattedDate = moment().format().slice(0, 19).replace(/[-T:]/g, '_')
          const link = document.createElement('a')
          link.href = url
          link.download = `${reportType}_${formattedDate}.csv`
          document.body.appendChild(link)
          link.click()

          link.remove()
          window.URL.revokeObjectURL(url)
        }
      } catch (error) {
        throw error
      }
    },
    async getAuditTrailData (jobType, jobId) {
      try {
        this.auditTrailData = []
        const res = await this.$api.get(`${inventoryServiceApi.history}${jobType}/${jobId}`)
        this.auditTrailData = res.data.map(item => {
          delete item.original_values
          delete item.new_values
          return item
        })
          .filter(item => item.last_action && item.last_action.trim() !== '')
      } catch (error) {
        throw error
      }
    }
  }
})
