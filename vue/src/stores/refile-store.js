import { defineStore } from 'pinia'
import inventoryServiceApi from '@/http/InventoryService.js'
import { useGlobalStore } from './global-store'
const globalStore = useGlobalStore()

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
      try {
        if (globalStore.appIsOffline) {
          // this will only occur when user is pausing/resuming when offline
          navigator.serviceWorker.controller.postMessage({ queueIncomingApiCall: `${inventoryServiceApi.refileJobs}${payload.id}` })
        }
        const res = await this.$api.patch(`${inventoryServiceApi.refileJobs}${payload.id}`, payload)
        this.refileJob = res.data
        this.originalRefileJob = { ...this.refileJob }
      } catch (error) {
        if (globalStore.appIsOffline) {
          return
        } else {
          throw error
        }
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
      try {
        if (globalStore.appIsOffline) {
          // this will only occur when user reverts to queue when offline
          navigator.serviceWorker.controller.postMessage({ queueIncomingApiCall: `${inventoryServiceApi.refileJobs}${this.refileJob.id}/remove_items` })
        }
        const res = await this.$api.delete(`${inventoryServiceApi.refileJobs}${this.refileJob.id}/remove_items`, { data: payload })
        this.refileJob = res.data
        this.originalRefileJob = { ...this.refileJob }
      } catch (error) {
        if (globalStore.appIsOffline) {
          return
        } else {
          throw error
        }
      }
    },
    async patchRefileJobTrayItemScanned (payload) {
      try {
        if (globalStore.appIsOffline) {
          // this will only occur when user is scanning when offline
          navigator.serviceWorker.controller.postMessage({ queueIncomingApiCall: `${inventoryServiceApi.refileJobs}${payload.job_id}/update_item/${payload.item_id}` })
        }
        // updates a refile job item and marks it as refiled
        const res = await this.$api.patch(`${inventoryServiceApi.refileJobs}${payload.job_id}/update_item/${payload.item_id}`, payload)
        this.refileJob = res.data
        this.originalRefileJob = { ...this.refileJob }
      } catch (error) {
        if (globalStore.appIsOffline) {
          return
        } else {
          throw error
        }
      }
    },
    async patchRefileJobNonTrayItemScanned (payload) {
      try {
        if (globalStore.appIsOffline) {
          // this will only occur when user is scanning when offline
          navigator.serviceWorker.controller.postMessage({ queueIncomingApiCall: `${inventoryServiceApi.refileJobs}${payload.job_id}/update_non_tray_items/${payload.non_tray_item_id}` })
        }
        // updates a refile job non tray item and marks it as refiled
        const res = await this.$api.patch(`${inventoryServiceApi.refileJobs}${payload.job_id}/update_non_tray_items/${payload.non_tray_item_id}`, payload)
        this.refileJob = res.data
        this.originalRefileJob = { ...this.refileJob }
      } catch (error) {
        if (globalStore.appIsOffline) {
          return
        } else {
          throw error
        }
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
