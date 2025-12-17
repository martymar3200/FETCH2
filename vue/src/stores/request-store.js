import { defineStore } from 'pinia'
import inventoryServiceApi from '@/http/InventoryService.js'

export const useRequestStore = defineStore('request-store', {
  state: () => ({
    requestJobListTotal: 0,
    requestJobList: [],
    requestJob: {
      id: null,
      accession_dt: null,
      arrival_dt: null,
      barcode: {
        value: null
      },
      building: {
        name: ''
      },
      create_dt: null,
      condition: null,
      delivery_location: null,
      dimensions: null,
      external_request_id: null,
      item_location: null,
      media_type: {
        name: ''
      },
      owner: {
        name: ''
      },
      priority: null,
      requestor_name: null,
      status: null,
      size_class: {
        name: ''
      },
      type: null,
      withdrawal_dt: null
    },
    requestBatchJob: {
      id: null,
      import_source: null,
      request_count: null,
      status: null,
      uploaded_by: null,
      import_dt: null,
      items: []
    }
  }),
  actions: {
    resetRequestStore () {
      this.$reset()
    },
    resetRequestJob () {
      this.requestJob = {
        id: null,
        accession_dt: null,
        arrival_dt: null,
        barcode: {
          value: null
        },
        building: {
          name: ''
        },
        create_dt: null,
        condition: null,
        delivery_location: null,
        dimensions: null,
        external_request_id: null,
        item_location: null,
        media_type: {
          name: ''
        },
        owner: {
          name: ''
        },
        priority: null,
        requestor_name: null,
        status: null,
        size_class: {
          name: ''
        },
        type: null,
        withdrawal_dt: null
      }
    },
    async getRequestJobList (paramsObj) {
      try {
        const res = await this.$api.get(inventoryServiceApi.requests, {
          params: {
            size: this.apiPageSizeDefault,
            ...paramsObj
          }
        })
        this.requestJobList = res.data.items

        // keep track of response total for pagination
        this.requestJobListTotal = res.data.total
      } catch (error) {
        throw error
      }
    },
    async getRequestJob (id) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.requests}${id}`)
        this.requestJob = res.data
      } catch (error) {
        throw error
      }
    },
    async postRequestJob (payload) {
      try {
        await this.$api.post(inventoryServiceApi.requests, payload)
        // refresh the requestJobList using request view filter since this endpoint only triggers from the request view tab
        await this.getRequestJobList({ queue: true })
      } catch (error) {
        throw error
      }
    },
    async patchRequestJob (payload) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.requests}${payload.id}`, payload)
        this.requestJob = res.data
        // refresh the requestJobList using request view filter since this endpoint only triggers from the request view tab
        await this.getRequestJobList({ queue: true })
      } catch (error) {
        throw error
      }
    },
    async deleteRequestJob (id) {
      try {
        await this.$api.delete(`${inventoryServiceApi.requests}${id}`)
        this.resetRequestJob()
      } catch (error) {
        throw error
      }
    },
    async getRequestBatchJobList (qParams) {
      try {
        const res = await this.$api.get(inventoryServiceApi.batchUpload, {
          params: {
            ...qParams,
            batch_upload_type: 'request'
          }
        })
        this.requestJobList = res.data.items

        // keep track of response total for pagination
        this.requestJobListTotal = res.data.total
      } catch (error) {
        throw error
      }
    },
    async getRequestBatchJob (id) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.batchUpload}${id}`)
        this.requestBatchJob  = res.data
      } catch (error) {
        throw error
      }
    },
    async postRequestBatchJob (payload) {
      try {
        // create a formData Object and assign the file to the formData to be passed to api as 'multipart/form-data' content
        let formData = new FormData()
        formData.append('file', payload.file)
        formData.append('requested_by_id', payload.requested_by_id)
        await this.$api.post(`${inventoryServiceApi.batchUpload}request`, formData)

        // reload batch request data
        await this.getRequestBatchJobList()
      } catch (error) {
        throw error
      }
    }
  }
})
