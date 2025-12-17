import { defineStore } from 'pinia'
import inventoryServiceApi from '@/http/InventoryService.js'

export const useWithdrawalStore = defineStore('withdrawal-store', {
  state: () => ({
    withdrawJobListTotal: 0,
    withdrawJobList: [],
    withdrawJob: {
      id: null,
      item_count: 0,
      non_tray_item_count: 0,
      create_dt: null,
      last_transition: null,
      status: '',
      items: [],
      non_tray_items: [],
      trays: []
    },
    originalWithdrawJob: null
  }),
  getters: {
    withdrawJobItems: (state) => {
      let itemList = []
      if (state.withdrawJob.id) {
        itemList = itemList.concat(state.withdrawJob.items, state.withdrawJob.non_tray_items)
      }
      // return the list sorted alphnumerically
      return itemList.sort(new Intl.Collator('en', {
        numeric:true,
        sensitivity:'accent'
      }).compare)
    }
  },
  actions: {
    resetWithdrawStore () {
      this.$reset()
    },
    resetWithdrawJob () {
      this.withdrawJob = {
        id: null,
        items_count: 0,
        non_tray_item_count: 0,
        create_dt: null,
        last_transition: null,
        status: null,
        items: [],
        non_tray_items: [],
        trays: []
      }
      this.originalWithdrawJob = null
    },
    async getWithdrawJobList (qParams) {
      try {
        const res = await this.$api.get(inventoryServiceApi.withdrawJobs, {
          params: {
            size: this.apiPageSizeDefault,
            ...qParams
          }
        })
        this.withdrawJobList = res.data.items

        // keep track of response total for pagination
        this.withdrawJobListTotal = res.data.total
      } catch (error) {
        throw error
      }
    },
    async getWithdrawJob (id) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.withdrawJobs}${id}`)
        this.withdrawJob = res.data
        this.originalWithdrawJob = { ...this.withdrawJob }
      } catch (error) {
        throw error
      }
    },
    async postWithdrawJob (payload) {
      try {
        const res = await this.$api.post(inventoryServiceApi.withdrawJobs, payload)
        this.withdrawJob = res.data
        this.originalWithdrawJob = { ...this.withdrawJob }
      } catch (error) {
        throw error
      }
    },
    async patchWithdrawJob (payload) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.withdrawJobs}${payload.id}`, payload)
        this.withdrawJob = res.data
        this.originalWithdrawJob = { ...this.withdrawJob }
      } catch (error) {
        throw error
      }
    },
    async deleteWithdrawJob (jobId) {
      try {
        await this.$api.delete(`${inventoryServiceApi.withdrawJobs}${jobId}`)
        this.resetWithdrawStore()
      } catch (error) {
        throw error
      }
    },
    async deleteWithdrawJobItems (payload) {
      try {
        const res = await this.$api.delete(`${inventoryServiceApi.withdrawJobs}${this.withdrawJob.id}/remove_items`, { data: payload })
        this.withdrawJob = res.data
        this.originalWithdrawJob = { ...this.withdrawJob }
      } catch (error) {
        throw error
      }
    },
    async postWithdrawJobItem (payload) {
      try {
        const res = await this.$api.post(`${inventoryServiceApi.withdrawJobs}${this.withdrawJob.id}/add_items`, payload)
        this.withdrawJob = res.data
        this.originalWithdrawJob = { ...this.withdrawJob }

        // check if success message contains errors and return them
        if (res.data.errors && res.data.errors.length > 0) {
          return res.data.errors
        }
      } catch (error) {
        throw error
      }
    },
    async postWithdrawJobBulkItems (payload) {
      try {
        // create a formData Object and assign the file to the formData to be passed to api as 'multipart/form-data' content
        let formData = new FormData()
        formData.append('file', payload.file)
        const res = await this.$api.post(`${inventoryServiceApi.batchUpload}withdraw-jobs/${this.withdrawJob.id}`, formData)

        //refresh the withdraw job
        await this.getWithdrawJob(this.withdrawJob.id)

        // check if success message contains errors and return them
        if (res.data.errors && res.data.errors.length > 0) {
          return res.data.errors
        }
      } catch (error) {
        throw error
      }
    }
  }
})
