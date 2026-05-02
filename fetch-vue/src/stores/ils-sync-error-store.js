import { defineStore } from 'pinia'
import apiEndpoint from '@/http/InventoryService'
import { api } from '@/boot/axios'

export const useIlsSyncErrorStore = defineStore('ilsSyncError', {
  state: () => ({
    ilsSyncErrors: []
  }),
  getters: {},
  actions: {
    async getIlsSyncErrors (params = {}) {
      const { data } = await api.get(apiEndpoint.ilsSyncErrors, { params })
      this.ilsSyncErrors = data.items || []
      return data
    },
    async resolveIlsSyncError (id) {
      const { data } = await api.patch(`${apiEndpoint.ilsSyncErrors}${id}/resolve`)
      return data
    },
    async retryIlsSyncError (id) {
      const { data } = await api.post(`${apiEndpoint.ilsSyncErrors}${id}/retry`)
      return data
    }
  }
})
