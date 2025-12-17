import { defineStore } from 'pinia'
import inventoryServiceApi from '@/http/InventoryService.js'
import { useGlobalStore } from './global-store'
const globalStore = useGlobalStore()

export const usePicklistStore = defineStore('picklist-store', {
  state: () => ({
    picklistJobListTotal: 0,
    picklistJobList: [],
    picklistJob: {
      id: null,
      building: null,
      user: null,
      user_id: null,
      create_dt: null,
      status: null,
      requests: []
    },
    originalPicklistJob: null,
    picklistItem: {
      id: null,
      barcode: {
        value: null
      }
    }
  }),
  getters: {
    picklistItems: (state) => {
      let items = []
      if (state.picklistJob.requests.length > 0) {
        items = state.picklistJob.requests.map(rq => {
          return {
            ...rq,
            status: rq.item ? rq.item.status : rq.non_tray_item.status
          }
        })
      }
      return items
    },
    allItemsRetrieved: (state) => {
      if (state.picklistJob.id && state.picklistJob.status !== 'Created') {
        // when a picklist job is active we can keep track of if all requested items have been pulled/retrieved using status
        return state.picklistItems.length == 0 || state.picklistItems.some(itm => itm.status == 'PickList') ? false : true
      } else {
        return true
      }
    }
  },
  actions: {
    resetPicklistStore () {
      this.$reset()
    },
    async getPicklistJobList (qParams) {
      try {
        const res = await this.$api.get(inventoryServiceApi.picklists, {
          params: {
            size: this.apiPageSizeDefault,
            ...qParams
          }
        })
        this.picklistJobList = res.data.items

        // keep track of response total for pagination
        this.picklistJobListTotal = res.data.total
      } catch (error) {
        throw error
      }
    },
    async getPicklistJob (id) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.picklists}${id}`)
        this.picklistJob = res.data
        this.originalPicklistJob = { ...this.picklistJob }
      } catch (error) {
        throw error
      }
    },
    async postPicklistJob (payload) {
      try {
        const res = await this.$api.post(inventoryServiceApi.picklists, payload)
        this.picklistJob = res.data
        this.originalPicklistJob = { ...this.picklistJob }
      } catch (error) {
        throw error
      }
    },
    async patchPicklistJob (payload) {
      try {
        if (globalStore.appIsOffline) {
          // this will only occur when user is pausing/resuming when offline
          navigator.serviceWorker.controller.postMessage({ queueIncomingApiCall: `${inventoryServiceApi.picklists}${payload.id}` })
        }
        const res = await this.$api.patch(`${inventoryServiceApi.picklists}${payload.id}`, payload)
        this.picklistJob = res.data
        this.originalPicklistJob = { ...this.picklistJob }
      } catch (error) {
        if (globalStore.appIsOffline) {
          return
        } else {
          throw error
        }
      }
    },
    async deletePicklistJob (jobId) {
      try {
        await this.$api.delete(`${inventoryServiceApi.picklists}${jobId}`)
        this.resetPicklistStore()
      } catch (error) {
        throw error
      }
    },
    resetPicklistItem () {
      this.picklistItem = {
        id: null,
        barcode: {
          value: null
        }
      }
    },
    async getPicklistJobItem (itemId) {
      //find the item with the matching barcode_value and set the data as the picklistJobItem
      this.picklistItem = this.picklistJob.requests.find(picklistRequest => picklistRequest?.item?.barcode.value == itemId || picklistRequest.non_tray_item?.barcode.value == itemId)
    },
    async patchPicklistJobItem (payload) {
      try {
        // used to add request items to a picklist
        const res = await this.$api.patch(`${inventoryServiceApi.picklists}${payload.id}/add_request`, payload)
        this.picklistJob = res.data
        this.originalPicklistJob = { ...this.picklistJob }
      } catch (error) {
        throw error
      }
    },
    async patchPicklistJobItemScanned (payload) {
      try {
        if (globalStore.appIsOffline) {
          // this will only occur when user is scanning when offline
          navigator.serviceWorker.controller.postMessage({ queueIncomingApiCall: `${inventoryServiceApi.picklists}${payload.id}/update_request/${payload.request_id}` })
        }
        // updates a request item and marks it as retrieved
        const res = await this.$api.patch(`${inventoryServiceApi.picklists}${payload.id}/update_request/${payload.request_id}`, payload)
        this.picklistJob = res.data
        this.originalPicklistJob = { ...this.picklistJob }
      } catch (error) {
        if (globalStore.appIsOffline) {
          return
        } else {
          throw error
        }
      }
    },
    async deletePicklistJobItem (itemId) {
      try {
        if (globalStore.appIsOffline) {
          // this will only occur when user reverts to queue when offline
          navigator.serviceWorker.controller.postMessage({ queueIncomingApiCall: `${inventoryServiceApi.picklists}${this.picklistJob.id}/remove_request/${itemId}` })
        }
        const res = await this.$api.delete(`${inventoryServiceApi.picklists}${this.picklistJob.id}/remove_request/${itemId}`)
        this.picklistJob = res.data
        this.originalPicklistJob = { ...this.picklistJob }
      } catch (error) {
        if (globalStore.appIsOffline) {
          return
        } else {
          throw error
        }
      }
    }
  }
})
