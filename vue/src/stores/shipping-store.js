
import { defineStore } from 'pinia'
import inventoryServiceApi from '@/http/InventoryService.js'

export const useShippingStore = defineStore('shipping-store', {
  state: () => ({
    shippingJobList: [],
    shippingJobListTotal: 0,
    shippingJob: {
      id: null,
      status: '',
      assigned_user: null, // User object
      assigned_user_id: null,
      created_by: null,
      create_dt: null,
      bins: []
    },
    // Current active bin for scanning UI
    currentBin: null,
    // For manifest view
    manifestData: null
  }),

  actions: {
    async getShippingJobList (qParams) {
      try {
        const res = await this.$api.get(inventoryServiceApi.shippingJobs, {
          params: qParams
        })
        this.shippingJobList = res.data.items
        this.shippingJobListTotal = res.data.total
        return res.data
      } catch (error) {
        throw error
      }
    },

    async createShippingJob (payload) {
      try {
        const res = await this.$api.post(inventoryServiceApi.shippingJobs, payload)
        this.shippingJob = res.data
        return res.data
      } catch (error) {
        throw error
      }
    },

    async getShippingJob (id) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.shippingJobs}${id}`)
        this.shippingJob = res.data
        return res.data
      } catch (error) {
        throw error
      }
    },

    async updateShippingJob (id, payload) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.shippingJobs}${id}`, payload)
        this.shippingJob = {
          ...this.shippingJob,
          ...res.data
        }
        return res.data
      } catch (error) {
        throw error
      }
    },

    async deleteShippingJob (id) {
      try {
        await this.$api.delete(`${inventoryServiceApi.shippingJobs}${id}`)
      } catch (error) {
        throw error
      }
    },

    async completeShippingJob (id) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.shippingJobs}${id}/complete`)
        this.shippingJob = res.data
        return res.data
      } catch (error) {
        throw error
      }
    },

    // --- Bin Operations ---

    async scanBin (jobId, barcode) {
      try {
        // Post to /shipping-jobs/{id}/bins?barcode=...
        const res = await this.$api.post(`${inventoryServiceApi.shippingJobs}${jobId}/bins`, null, {
          params: { barcode }
        })
        // Add or update bin in local state
        const bin = res.data
        this.updateLocalBin(bin)
        this.currentBin = bin
        return bin
      } catch (error) {
        throw error
      }
    },

    async clearBin (barcode) {
      try {
        const res = await this.$api.post(`${inventoryServiceApi.shippingJobs}bins/clear`, null, {
          params: { barcode }
        })
        return res.data
      } catch (error) {
        throw error
      }
    },

    // --- Item Operations ---

    async checkItemForShipping (jobId, barcode) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.shippingJobs}${jobId}/items/check`, {
          params: { barcode }
        })
        return res.data
      } catch (error) {
        throw error
      }
    },

    async scanItemIntoBin (jobId, binId, itemBarcode) {
      try {
        const res = await this.$api.post(`${inventoryServiceApi.shippingJobs}${jobId}/bins/${binId}/items`, null, {
          params: { item_barcode: itemBarcode }
        })
        // returns updated bin
        const bin = res.data
        this.updateLocalBin(bin)
        this.currentBin = bin

        // Optimistically update status
        if (this.shippingJob.status === 'Created') {
          this.shippingJob.status = 'Running'
        }

        return bin
      } catch (error) {
        throw error
      }
    },

    async removeItemFromBin (jobId, binId, itemId) {
      try {
        await this.$api.delete(`${inventoryServiceApi.shippingJobs}${jobId}/bins/${binId}/items/${itemId}`)

        // Optimistically remove from local state
        const binIndex = this.shippingJob.bins.findIndex(b => b.id === binId)
        if (binIndex !== -1) {
          const bin = this.shippingJob.bins[binIndex]
          bin.items = bin.items.filter(i => i.id !== itemId)
          // If current bin, update it too
          if (this.currentBin && this.currentBin.id === binId) {
            this.currentBin.items = this.currentBin.items.filter(i => i.id !== itemId)
          }
        }
      } catch (error) {
        throw error
      }
    },

    updateLocalBin (bin) {
      if (!this.shippingJob.bins) {
        this.shippingJob.bins = []
      }
      const index = this.shippingJob.bins.findIndex(b => b.id === bin.id)
      if (index !== -1) {
        this.shippingJob.bins.splice(index, 1, bin)
      } else {
        this.shippingJob.bins.push(bin)
      }
    },

    async getManifest (jobId, scope, filterId) {
      try {
        const params = { scope }
        if (filterId) {
          params.filter_id = filterId
        }

        const res = await this.$api.get(`${inventoryServiceApi.shippingJobs}${jobId}/manifest`, { params })
        this.manifestData = res.data
        return res.data
      } catch (error) {
        throw error
      }
    }
  }
})
