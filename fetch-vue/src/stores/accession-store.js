import { defineStore } from 'pinia'
import inventoryServiceApi from '@/http/InventoryService.js'

export const useAccessionStore = defineStore('accession-store', {
  state: () => ({
    accessionJobListTotal: 0,
    accessionJobList: [],
    accessionJob: {
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
    accessionContainer: {
      id: null,
      owner: '',
      container_type: '',
      size_class: '',
      media_type: '',
      items: []
    },
    originalAccessionJob: null,
    originalAccessionContainer: null
  }),
  getters: {
    allItemsVerified: (state) => {
      if (state.accessionJob.trayed == false) {
        return state.accessionJob.non_tray_items.length == 0 || state.accessionJob.non_tray_items.some(item => item.scanned_for_accession == false) ? false : true
      } else {
        return state.accessionContainer.items.length !== 0
      }
    }
  },
  actions: {
    resetAccessionStore () {
      this.accessionJob = {
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
      this.accessionContainer = {
        id: null,
        owner: '',
        container_type: '',
        size_class: '',
        media_type: '',
        items: []
      }
      this.originalAccessionJob = null
      this.originalAccessionContainer = null
    },
    resetAccessionContainer () {
      this.accessionContainer = {
        id: null,
        owner: '',
        container_type: '',
        size_class: '',
        media_type: '',
        items: []
      }
    },
    async getAccessionJobList (qParams) {
      try {
        const res = await this.$api.get(inventoryServiceApi.accessionJobs, {
          params: {
            size: this.apiPageSizeDefault,
            ...qParams
          }
        })
        this.accessionJobList = res.data.items

        // keep track of response total for pagination
        this.accessionJobListTotal = res.data.total
      } catch (error) {
        throw error
      }
    },
    async getAccessionJob (workflowId) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.accessionJobsWorkflow}${workflowId}`)
        this.accessionJob = { ...res.data }
        this.originalAccessionJob = { ...res.data }
      } catch (error) {
        throw error
      }
    },
    async postAccessionJob (payload) {
      try {
        const res = await this.$api.post(inventoryServiceApi.accessionJobs, payload)

        this.accessionJob = { ...res.data }
        this.originalAccessionJob = { ...res.data }
      } catch (error) {
        throw error
      }
    },
    async patchAccessionJob (payload) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.accessionJobs}${payload.id}`, payload)
        this.accessionJob = { ...res.data }
        this.originalAccessionJob = { ...res.data }
      } catch (error) {
        throw error
      }
    },
    async deleteAccessionJob (jobId) {
      try {
        await this.$api.delete(`${inventoryServiceApi.accessionJobs}${jobId}`)
        this.resetAccessionStore()
      } catch (error) {
        throw error
      }
    },
    async getAccessionTray (barcode) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.traysBarcode}${barcode}`)
        this.accessionContainer = {
          ...res.data,
          items: res.data.items ?? []
        }
        this.originalAccessionContainer = {
          ...res.data,
          items: res.data.items ?? []
        }
      } catch (error) {
        throw error
      }
    },
    async postAccessionTray (payload) {
      try {
        const res = await this.$api.post(inventoryServiceApi.trays, payload)

        this.accessionContainer = {
          ...res.data,
          items: res.data.items ?? []
        }
        this.originalAccessionContainer = {
          ...res.data,
          items: res.data.items ?? []
        }

        // add the new tray to accessionJob trays
        this.accessionJob.trays = [
          ...this.accessionJob.trays,
          res.data
        ]
        this.originalAccessionJob.trays = [
          ...this.originalAccessionJob.trays,
          res.data
        ]
      } catch (error) {
        throw error
      }
    },
    async patchAccessionTray (payload) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.trays}${payload.id}`, payload)
        this.accessionContainer = {
          ...res.data,
          items: res.data.items ?? []
        }
        this.originalAccessionContainer = {
          ...res.data,
          items: res.data.items ?? []
        }
      } catch (error) {
        throw error
      }
    },
    async deleteAccessionTray (trayId) {
      try {
        await this.$api.delete(`${inventoryServiceApi.trays}${trayId}`)
        this.resetAccessionContainer()

        // remove the deleted tray from the accessionJob data
        this.accessionJob.trays = this.accessionJob.trays.filter(tray => tray.id !== trayId)
      } catch (error) {
        throw error
      }
    },
    async postAccessionTrayItem (payload) {
      try {
        const res = await this.$api.post(inventoryServiceApi.items, payload)

        this.accessionContainer.items = [
          ...this.accessionContainer.items,
          res.data
        ]
        this.originalAccessionContainer = { ... this.accessionContainer }

        // reload the accession job data to get the latest items
        await this.getAccessionJob(this.accessionJob.workflow_id)
      } catch (error) {
        throw error
      }
    },
    async patchAccessionTrayItem (payload) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.items}${payload.id}`, payload)

        // remove the old item and replace it with the upated item in accessionContainer items
        const filteredItems = this.accessionContainer.items.filter(item => item.id !== payload.id)
        this.accessionContainer.items = [
          ...filteredItems,
          res.data
        ]
        this.originalAccessionContainer = { ...this.accessionContainer }
        // reload the accession job data to get the latest items
        await this.getAccessionJob(this.accessionJob.workflow_id)
      } catch (error) {
        throw error
      }
    },
    async deleteAccessionTrayItem (barcodeList) {
      try {
        await Promise.all(barcodeList.map(barcode => {
          return this.$api.delete(`${inventoryServiceApi.items}${barcode}`)
        }))

        // filter the deleted tray items from the accessionContainer
        const filteredItems = this.accessionContainer.items.filter(b => !barcodeList.includes(b.id))
        this.accessionContainer = {
          ...this.accessionContainer,
          items: filteredItems
        }
        this.originalAccessionContainer = { ...this.accessionContainer }
        // reload the accession job data to get the latest items
        await this.getAccessionJob(this.accessionJob.workflow_id)
      } catch (error) {
        throw error
      }
    },
    async getAccessionNonTrayItem (barcode) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.nonTrayItemsBarcode}${barcode}`)

        this.accessionContainer = res.data
        this.originalAccessionContainer = { ...this.accessionContainer }
      } catch (error) {
        throw error
      }
    },
    async postAccessionNonTrayItem (payload) {
      try {
        const res = await this.$api.post(inventoryServiceApi.nonTrayItems, payload)

        // set the item as the container since there is no tray container for non tray jobs
        this.accessionContainer = res.data
        this.originalAccessionContainer = { ...this.accessionContainer }

        // add the new non tray item to accessionJob non tray items
        this.accessionJob.non_tray_items = [
          ...this.accessionJob.non_tray_items,
          res.data
        ]
        this.originalAccessionJob.non_tray_items = [
          ...this.originalAccessionJob.non_tray_items,
          res.data
        ]
      } catch (error) {
        throw error
      }
    },
    async patchAccessionNonTrayItem (payload) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.nonTrayItems}${payload.id}`, payload)

        // set the item as the container since there is no tray container for non tray jobs
        this.accessionContainer = res.data
        this.originalAccessionContainer = { ...this.accessionContainer }

        // update the non tray item in accessionJob non tray items
        this.accessionJob.non_tray_items[this.accessionJob.non_tray_items.findIndex(item => item.id == payload.id)] = res.data
        this.originalAccessionJob = { ...this.accessionJob }
      } catch (error) {
        throw error
      }
    },
    async deleteAccessionNonTrayItem (barcodeList) {
      try {
        await Promise.all(barcodeList.map(barcode => {
          return this.$api.delete(`${inventoryServiceApi.nonTrayItems}${barcode}`)
        }))

        // filter the deleted non tray items from the accessionJob
        this.accessionJob = {
          ...this.accessionJob,
          non_tray_items: this.accessionJob.non_tray_items.filter(b => !barcodeList.includes(b.id) )
        }
        this.originalAccessionJob = { ...this.accessionJob }

        // reset the container since the non tray item data is wiped
        this.resetAccessionContainer()
      } catch (error) {
        throw error
      }
    }
  }
})
