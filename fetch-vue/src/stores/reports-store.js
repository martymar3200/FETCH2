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
        'Shelving Job Discrepancy': inventoryServiceApi.reportingShelvingDiscrepancies,
        'Shelving Move Discrepancy': inventoryServiceApi.reportingMoveDiscrepancies,
        'Open Locations': inventoryServiceApi.reportingOpenLocations,
        'Tray/Item Count By Aisle': inventoryServiceApi.reportingAislesItemsCount,
        'Non-Tray Count': inventoryServiceApi.reportingNonTrayItemsCount,
        'Total Item Retrieved': inventoryServiceApi.reportingRetrievalsCount,
        'Item in Tray': inventoryServiceApi.reportingTrayItemsCount,
        'User Job Summary': inventoryServiceApi.reportingUserJobsCount,
        'Verification Change': inventoryServiceApi.reportingVerificationChangesSummary,
        'Verification Status': inventoryServiceApi.reportingVerificationStatus,
        'Withdrawn Items': inventoryServiceApi.reportingWithdrawnItems,
        'Shipping Bins': inventoryServiceApi.reportingShippingBins,
        'Worker Efficiency (SLA)': inventoryServiceApi.reportingWorkerEfficiency,
        'Retrieval Hot Zones': inventoryServiceApi.reportingHotZones,
        'Capacity Forecast': inventoryServiceApi.reportingCapacityForecast,
        'Capacity Forecast (Height)': inventoryServiceApi.reportingCapacityForecastHeight,
        'Daily Pulse': inventoryServiceApi.reportingDailyPulse
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
          })
          if (reportType === 'Daily Pulse') {
            // Transform object to metric/value rows
            this.reportData = [
              {
                metric: 'Items Accessioned Today',
                value: res.data.accessioned_today
              },
              {
                metric: 'Jobs Shelved Today',
                value: res.data.shelved_today
              },
              {
                metric: 'Items Retrieved Today',
                value: res.data.retrieved_today
              },
              {
                metric: 'Items Verified Today',
                value: res.data.verified_today
              },
              {
                metric: 'New Requests Pending',
                value: res.data.pending_requests
              },
              {
                metric: 'Verification Backlog',
                value: res.data.backlog_verification_jobs
              }
            ]
            this.reportDataTotal = this.reportData.length
          } else if (reportType === 'Capacity Forecast' || reportType === 'Capacity Forecast (Height)') {
            // Capacity forecasts return a simple list, not a paginated object
            this.reportData = res.data
            this.reportDataTotal = res.data.length
          } else {
            this.reportData = res.data.items // Store the report data
            this.reportDataTotal = res.data.total // keep track of response total for pagination
          }
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
        }).filter(item => item.last_action && item.last_action.trim() !== '')
      } catch (error) {
        throw error
      }
    },
    async getEntityAuditTrailData (entityType, entityId) {
      try {
        this.auditTrailData = []
        const res = await this.$api.get(`${inventoryServiceApi.history}entity/${entityType}/${entityId}`)
        this.auditTrailData = res.data.map(item => {
          delete item.original_values
          delete item.new_values
          return item
        }).filter(item => item.last_action && item.last_action.trim() !== '')
      } catch (error) {
        throw error
      }
    },
    async getScheduledExports () {
      try {
        const res = await this.$api.get(inventoryServiceApi.reportingScheduledExports)
        return res.data
      } catch (error) {
        throw error
      }
    },
    async createScheduledExport (payload) {
      try {
        const res = await this.$api.post(inventoryServiceApi.reportingScheduledExports, payload)
        return res.data
      } catch (error) {
        throw error
      }
    },
    async deleteScheduledExport (id) {
      try {
        await this.$api.delete(`${inventoryServiceApi.reportingScheduledExports}${id}`)
      } catch (error) {
        throw error
      }
    },
    async getExportHistory () {
      try {
        const res = await this.$api.get(inventoryServiceApi.reportingExportHistory)
        return res.data
      } catch (error) {
        throw error
      }
    },
    async downloadHistoricalExport (historyItem) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.reportingExportHistory}download/${historyItem.id}`, {
          responseType: 'blob'
        })
        const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/x-gzip' }))
        const link = document.createElement('a')
        link.href = url
        link.download = historyItem.filename
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)
      } catch (error) {
        throw error
      }
    }
  }
})
