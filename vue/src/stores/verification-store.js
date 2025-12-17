import { defineStore } from 'pinia'
import inventoryServiceApi from '@/http/InventoryService.js'

export const useVerificationStore = defineStore('verification-store', {
  state: () => ({
    verificationJobListTotal: 0,
    verificationJobList: [],
    verificationJob: {
      id: null,
      trayed: null,
      owner: null,
      owner_id: null,
      media_type: null,
      media_type_id: null,
      non_tray_items: [],
      size_class: null,
      size_class_id: null,
      trays: [],
      status: ''
    },
    originalVerificationJob: null,
    verificationContainer: {
      id: null,
      owner: '',
      owner_id: null,
      container_type: '',
      size_class: '',
      size_class_id: null,
      media_type: '',
      media_type_id: null,
      items: []
    },
    originalVerificationContainer: null
  }),
  getters: {
    allItemsVerified: (state) => {
      if (state.verificationJob.trayed == false) {
        return state.verificationJob.non_tray_items.length == 0 || state.verificationJob.non_tray_items.some(item => !item.scanned_for_verification) ? false : true
      } else {
        // if were in a trayed job and no tray scanned yet then return true otherwise if a tray is loaded we need to check that all tray items are verified
        if (!state.verificationContainer.id) {
          return true
        } else {
          return state.verificationContainer.items.length == 0 || state.verificationContainer.items.some(item => !item.scanned_for_verification) ? false : true
        }
      }
    },
    allTraysCompleted: (state) => {
      if (state.verificationJob.trayed && state.verificationJob.trays.length > 0) {
        // filter out the active tray if there is one since that trays completion will be based on the all items verified state
        return state.verificationJob.trays.filter(t => t.id !== state.verificationContainer.id).some(t => !t.collection_verified) ? false : true
      } else {
        return true
      }
    }
  },
  actions: {
    resetVerificationStore () {
      this.$reset()
    },
    resetVerificationJob () {
      this.verificationJob = {
        id: null,
        trayed: null,
        owner: null,
        owner_id: null,
        media_type: null,
        media_type_id: null,
        non_tray_items: [],
        size_class: null,
        size_class_id: null,
        trays: [],
        status: ''
      }
      this.originalVerificationJob = null
    },
    resetVerificationContainer () {
      this.verificationContainer = {
        id: null,
        owner: '',
        owner_id: null,
        container_type: '',
        size_class: '',
        size_class_id: null,
        media_type: '',
        media_type_id: null,
        items: []
      }
    },
    async getVerificationJobByAccessionId (accessionJobId) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.verificationJobsByAccessionJobId}${accessionJobId}`)
        return res
      } catch (error) {
        throw error
      }
    },
    async getVerificationJobList (paramsObj) {
      try {
        const res = await this.$api.get(inventoryServiceApi.verificationJobs, {
          params: {
            size: this.apiPageSizeDefault,
            ...paramsObj
          }
        })
        this.verificationJobList = res.data.items

        // keep track of response total for pagination
        this.verificationJobListTotal = res.data.total
        return res
      } catch (error) {
        throw error
      }
    },
    async getVerificationJob (workflowId) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.verificationJobsWorkflow}${workflowId}`)
        this.verificationJob = res.data
        this.originalVerificationJob = { ...this.verificationJob }
      } catch (error) {
        throw error
      }
    },
    async patchVerificationJob (payload) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.verificationJobs}${payload.id}`, payload)
        this.verificationJob = res.data
        this.originalVerificationJob = { ...this.verificationJob }
      } catch (error) {
        throw error
      }
    },
    async getVerificationTray (barcode) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.traysBarcode}${barcode}`)
        this.verificationContainer = res.data
        this.originalVerificationContainer = { ...this.verificationContainer }
      } catch (error) {
        throw error
      }
    },
    async patchVerificationTray (payload) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.trays}${payload.id}`, payload)
        this.verificationContainer = {
          ...res.data,
          items: res.data.items ?? []
        }
        this.originalVerificationContainer = { ...this.verificationContainer }

        // update the tray in the verificationJob trays
        this.verificationJob.trays[this.verificationJob.trays.findIndex(tray => tray.id == payload.id)] = res.data
        this.originalVerificationJob = { ...this.verificationJob }
      } catch (error) {
        throw error
      }
    },
    async postVerificationTrayItem (payload) {
      try {
        // add the item to the items table
        const res = await this.$api.post(inventoryServiceApi.items, payload)

        //pass an update to verification job to track which user added a new tray item
        await this.$api.patch(`${inventoryServiceApi.verificationJobs}${payload.verification_job_id}/add`, {
          barcode_value: payload.barcode_value,
          user_id: payload.user_id
        })

        this.verificationContainer.items = [
          ...this.verificationContainer.items,
          res.data
        ]
        this.originalVerificationContainer = { ... this.verificationContainer }
        // reload the verification job data to get the latest items
        await this.getVerificationJob(this.verificationJob.workflow_id)
      } catch (error) {
        throw error
      }
    },
    async patchVerificationTrayItem (payload) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.items}${payload.id}`, payload)

        // remove the old item and replace it with the updated item in verificationContainer items
        const filteredItems = this.verificationContainer.items.filter(item => item.id !== payload.id)
        this.verificationContainer.items = [
          ...filteredItems,
          res.data
        ]
        this.originalVerificationContainer = { ...this.verificationContainer }
        // reload the verification job data to get the latest items
        await this.getVerificationJob(this.verificationJob.workflow_id)
      } catch (error) {
        throw error
      }
    },
    async deleteVerificationTrayItem (barcodeItemList) {
      try {
        //pass an update to verification job to track which user removed the tray item before we delete it from the system
        await Promise.all(barcodeItemList.map(item => {
          return this.$api.patch(`${inventoryServiceApi.verificationJobs}${this.verificationJob.id}/remove`, {
            barcode_value: item.barcode.value,
            user_id: item.user_id
          })
        }))

        await Promise.all(barcodeItemList.map(item => {
          return this.$api.delete(`${inventoryServiceApi.items}${item.id}`)
        }))

        // filter the deleted tray items from the verificationContainer
        const filteredItems = this.verificationContainer.items.filter(b => !barcodeItemList.map(item => item.id).includes(b.id))
        this.verificationContainer = {
          ...this.verificationContainer,
          items: filteredItems
        }
        this.originalVerificationContainer = { ...this.verificationContainer }
        // reload the verification job data to get the latest items
        await this.getVerificationJob(this.verificationJob.workflow_id)
      } catch (error) {
        throw error
      }
    },
    async getVerificationNonTrayItem (barcode) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.nonTrayItemsBarcode}${barcode}`)
        this.verificationContainer = res.data
        this.originalVerificationContainer = { ...this.verificationContainer }
      } catch (error) {
        throw error
      }
    },
    async postVerificationNonTrayItem (payload) {
      try {
        // add the item to the nontray items table
        const res = await this.$api.post(inventoryServiceApi.nonTrayItems, payload)

        //pass an update to verification job to track which user added a new tray item
        await this.$api.patch(`${inventoryServiceApi.verificationJobs}${payload.verification_job_id}/add`, {
          barcode_value: payload.barcode_value,
          user_id: payload.user_id
        })

        // set the item as the container since there is no tray container for non tray jobs
        this.verificationContainer = res.data
        this.originalVerificationContainer = { ...this.verificationContainer }

        // add the new non tray item to verificationJob non tray items
        this.verificationJob.non_tray_items = [
          ...this.verificationJob.non_tray_items,
          res.data
        ]
        this.originalverificationJob = { ...this.verificationJob }
      } catch (error) {
        throw error
      }
    },
    async patchVerificationNonTrayItem (payload) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.nonTrayItems}${payload.id}`, payload)
        this.verificationContainer = res.data
        this.originalVerificationContainer = { ...this.verificationContainer }

        // update the non tray item in verificationJob non tray items
        this.verificationJob.non_tray_items[this.verificationJob.non_tray_items.findIndex(item => item.id == payload.id)] = res.data
        this.originalVerificationJob = { ...this.verificationJob }
      } catch (error) {
        throw error
      }
    },
    async deleteVerificationNonTrayItem (barcodeItemList) {
      try {
        //pass an update to verification job to track which user removed the nontray item before we delete it from the system
        await Promise.all(barcodeItemList.map(item => {
          return this.$api.patch(`${inventoryServiceApi.verificationJobs}${this.verificationJob.id}/remove`, {
            barcode_value: item.barcode.value,
            user_id: item.user_id
          })
        }))

        await Promise.all(barcodeItemList.map(item => {
          return this.$api.delete(`${inventoryServiceApi.nonTrayItems}${item.id}`)
        }))

        // filter the deleted non tray items from the verificationJob
        this.verificationJob = {
          ...this.verificationJob,
          non_tray_items: this.verificationJob.non_tray_items.filter(b => !barcodeItemList.map(item => item.id).includes(b.id) )
        }
        this.originalVerificationJob = { ...this.verificationJob }
      } catch (error) {
        throw error
      }
    },
    async verifyTrayItem (id) {
      try {
        await this.$api.patch(`${inventoryServiceApi.items}${id}`, { scanned_for_verification: true })

        // update the verified status for the item in verificationContainer items
        this.verificationContainer.items[this.verificationContainer.items.findIndex(item => item.id == id)].scanned_for_verification = true
        this.originalVerificationContainer = { ...this.verificationContainer }
      } catch (error) {
        throw error
      }
    },
    async verifyNonTrayItem (id) {
      try {
        await this.$api.patch(`${inventoryServiceApi.nonTrayItems}${id}`, { scanned_for_verification: true })

        // update the non tray items verified state in the verificationJob data as well
        this.verificationJob.non_tray_items[this.verificationJob.non_tray_items.findIndex(item => item.id == id)].scanned_for_verification = true
      } catch (error) {
        throw error
      }
    },
    async cancelVerificationJob (id) {
      try {
        await this.$api.delete(`${inventoryServiceApi.verificationJobs}${id}`)
        this.resetVerificationJob()
      } catch (error) {
        throw error
      }
    }
  }
})
