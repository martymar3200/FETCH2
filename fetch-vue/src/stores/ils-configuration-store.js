import { defineStore } from 'pinia'
import apiEndpoint from '@/http/InventoryService'
import { api } from '@/boot/axios'

export const useIlsConfigurationStore = defineStore('ilsConfiguration', {
  state: () => ({
    ilsConfigurations: []
  }),
  getters: {},
  actions: {
    async getIlsConfigurations () {
      const { data } = await api.get(apiEndpoint.ilsConfigurations)
      this.ilsConfigurations = data
      return data
    },
    async getIlsConfigurationById (id) {
      const { data } = await api.get(`${apiEndpoint.ilsConfigurations}${id}`)
      return data
    },
    async postIlsConfiguration (payload) {
      const { data } = await api.post(apiEndpoint.ilsConfigurations, payload)
      return data
    },
    async patchIlsConfiguration (id, payload) {
      const { data } = await api.patch(`${apiEndpoint.ilsConfigurations}${id}`, payload)
      return data
    },
    async deleteIlsConfiguration (id) {
      const { data } = await api.delete(`${apiEndpoint.ilsConfigurations}${id}`)
      return data
    },
    async syncIlsRequests (payload) {
      const { data } = await api.post(apiEndpoint.ilsConfigurationsSyncRequests, { config_ids: payload })
      return data
    }
  }
})
