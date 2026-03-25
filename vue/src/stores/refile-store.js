import { defineStore } from 'pinia'
import inventoryServiceApi from '@/http/InventoryService.js'
import { useOfflineSync } from '@/composables/useOfflineSync.js'
import { useIndexDbHandler } from '@/composables/useIndexDbHandler.js'
export const useRefileStore = defineStore('refile-store', {
  state: () => ({
    refileJobListTotal: 0,
    refileQueueListTotal: 0,
    refileJobList: [],
    refileQueueList: [],
    refileJob: {
      id: null,
      refile_job_items: []
    },
    originalRefileJob: null,
    refileItem: {
      id: null,
      barcode: {
        value: null
      },
      owner: null
    }
  }),
  getters: {
    allItemsRefiled: (state) => {
      if (state.refileJob.id && state.refileJob.status !== 'Created') {
        // if were in a running refile job, we check that items exist and none of the items are pending refile state
        return (state.refileJob.refile_job_items && state.refileJob.refile_job_items.length == 0) || state.refileJob.refile_job_items?.some(itm => itm.status == 'Out') ? false : true
      } else {
        return true
      }
    }
  },
  actions: {
    resetRefileStore () {
      this.$reset()
    },
    resetRefileItem () {
      this.refileItem = {
        id: null,
        barcode: {
          value: null
        },
        owner: null
      }
    },
    async getRefileJobList (qParams) {
      try {
        const res = await this.$api.get(inventoryServiceApi.refileJobs, {
          params: {
            size: this.apiPageSizeDefault,
            ...qParams
          }
        })
        this.refileJobList = res.data.items

        // keep track of response total for pagination
        this.refileJobListTotal = res.data.total
      } catch (error) {
        throw error
      }
    },
    async getRefileQueueList (qParams) {
      try {
        const res = await this.$api.get(inventoryServiceApi.refileQueue, {
          params: {
            size: this.apiPageSizeDefault,
            ...qParams
          }
        })
        this.refileQueueList = res.data.items

        // keep track of response total for pagination
        this.refileQueueListTotal = res.data.total
      } catch (error) {
        throw error
      }
    },
    async getRefileJob (id) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.refileJobs}${id}`)
        this.refileJob = res.data
        this.originalRefileJob = { ...this.refileJob }
      } catch (error) {
        throw error
      }
    },
    async postRefileJob (payload) {
      try {
        const res = await this.$api.post(inventoryServiceApi.refileJobs, payload)
        this.refileJob = res.data
        this.originalRefileJob = { ...this.refileJob }
      } catch (error) {
        throw error
      }
    },
    async patchRefileJob (payload) {
      const { offlineAwareRequest } = useOfflineSync()
      const { addDataToIndexDb } = useIndexDbHandler()
      try {
        const res = await offlineAwareRequest({
          method: 'PATCH',
          url: `${inventoryServiceApi.refileJobs}${payload.id}`,
          payload,
          optimisticUpdate: () => {
            if (this.refileJob && payload.status) {
              this.refileJob.status = payload.status
              this.originalRefileJob.status = payload.status
            }
          },
          updateSnapshot: async () => {
            await addDataToIndexDb('refileStore', 'refileJob', JSON.parse(JSON.stringify(this.refileJob)))
            await addDataToIndexDb('refileStore', 'originalRefileJob', JSON.parse(JSON.stringify(this.originalRefileJob)))
          }
        })
        if (res.fromServer) {
          this.refileJob = res.data
          this.originalRefileJob = { ...this.refileJob }
        }
      } catch (error) {
        throw error
      }
    },
    async deleteRefileJob (jobId) {
      try {
        await this.$api.delete(`${inventoryServiceApi.refileJobs}${jobId}`)
        this.resetRefileStore()
      } catch (error) {
        throw error
      }
    },
    getRefileJobItem (barcode_value) {
      // find the item with the matching barcode_value and set the data as the refileItem
      this.refileItem = this.refileJob.refile_job_items.find(itm => itm.barcode.value == barcode_value)
    },
    async postRefileJobItem (payload) {
      try {
        // used to add queue items to a refile Job
        const res = await this.$api.post(`${inventoryServiceApi.refileJobs}${payload.id}/add_items`, payload)
        this.refileJob = res.data
        this.originalRefileJob = { ...this.refileJob }
      } catch (error) {
        throw error
      }
    },
    async deleteRefileJobItems (payload) {
      const { offlineAwareRequest } = useOfflineSync()
      const { addDataToIndexDb } = useIndexDbHandler()
      try {
        const res = await offlineAwareRequest({
          method: 'DELETE',
          url: `${inventoryServiceApi.refileJobs}${this.refileJob.id}/remove_items`,
          payload,
          optimisticUpdate: () => {
            if (payload.barcode_values) {
              this.refileJob.refile_job_items = this.refileJob.refile_job_items.filter(itm => !payload.barcode_values.includes(itm.barcode?.value))
            }
            this.originalRefileJob = { ...this.refileJob }
          },
          updateSnapshot: async () => {
            await addDataToIndexDb('refileStore', 'refileJob', JSON.parse(JSON.stringify(this.refileJob)))
            await addDataToIndexDb('refileStore', 'originalRefileJob', JSON.parse(JSON.stringify(this.originalRefileJob)))
          }
        })
        if (res.fromServer) {
          this.refileJob = res.data
          this.originalRefileJob = { ...this.refileJob }
        }
      } catch (error) {
        throw error
      }
    },
    async patchRefileJobTrayItemScanned (payload) {
      const { offlineAwareRequest } = useOfflineSync()
      const { addDataToIndexDb } = useIndexDbHandler()
      try {
        const res = await offlineAwareRequest({
          method: 'PATCH',
          url: `${inventoryServiceApi.refileJobs}${payload.job_id}/update_item/${payload.item_id}`,
          payload,
          optimisticUpdate: () => {
            const index = this.refileJob.refile_job_items.findIndex(itm => itm.item_id == payload.item_id || itm.id == payload.item_id)
            if (index !== -1) {
              const item = this.refileJob.refile_job_items[index]
              item.status = 'In'
              // Optional: move it to the bottom of the list for visual consistency
              this.refileJob.refile_job_items.splice(index, 1)
              this.refileJob.refile_job_items.push(item)
            }
            this.originalRefileJob = { ...this.refileJob }
          },
          updateSnapshot: async () => {
            await addDataToIndexDb('refileStore', 'refileJob', JSON.parse(JSON.stringify(this.refileJob)))
            await addDataToIndexDb('refileStore', 'originalRefileJob', JSON.parse(JSON.stringify(this.originalRefileJob)))
          }
        })
        if (res.fromServer) {
          this.refileJob = res.data
          this.originalRefileJob = { ...this.refileJob }
        }
      } catch (error) {
        throw error
      }
    },
    async patchRefileJobNonTrayItemScanned (payload) {
      const { offlineAwareRequest } = useOfflineSync()
      const { addDataToIndexDb } = useIndexDbHandler()
      try {
        const res = await offlineAwareRequest({
          method: 'PATCH',
          url: `${inventoryServiceApi.refileJobs}${payload.job_id}/update_non_tray_items/${payload.non_tray_item_id}`,
          payload,
          optimisticUpdate: () => {
            const index = this.refileJob.refile_job_items.findIndex(itm => itm.non_tray_item_id == payload.non_tray_item_id || itm.id == payload.non_tray_item_id)
            if (index !== -1) {
              const item = this.refileJob.refile_job_items[index]
              item.status = 'In'
              this.refileJob.refile_job_items.splice(index, 1)
              this.refileJob.refile_job_items.push(item)
            }
            this.originalRefileJob = { ...this.refileJob }
          },
          updateSnapshot: async () => {
            await addDataToIndexDb('refileStore', 'refileJob', JSON.parse(JSON.stringify(this.refileJob)))
            await addDataToIndexDb('refileStore', 'originalRefileJob', JSON.parse(JSON.stringify(this.originalRefileJob)))
          }
        })
        if (res.fromServer) {
          this.refileJob = res.data
          this.originalRefileJob = { ...this.refileJob }
        }
      } catch (error) {
        throw error
      }
    },
    async postRefileQueueItem (payload) {
      try {
        const res = await this.$api.patch(inventoryServiceApi.refileQueue, payload)
        this.refileItem = res.data.item ? res.data.item : res.data.non_tray_item
      } catch (error) {
        throw error
      }
    }
  }
})
