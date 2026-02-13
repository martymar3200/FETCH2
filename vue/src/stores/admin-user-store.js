import { defineStore } from 'pinia'
import inventoryServiceApi from '@/http/InventoryService.js'

export const useAdminUserStore = defineStore('admin-user-store', {
  state: () => ({
    userList: [],
    userListTotal: 0,
    selectedUser: {}
  }),

  actions: {
    async getUsers (qParams = {}) {
      try {
        const res = await this.$api.get(inventoryServiceApi.users, {
          params: qParams
        })
        this.userList = res.data.items
        this.userListTotal = res.data.total
        return res
      } catch (error) {
        throw error
      }
    },

    async createUser (payload) {
      try {
        const res = await this.$api.post(inventoryServiceApi.users, payload)
        return res.data
      } catch (error) {
        throw error
      }
    },

    async updateUser (id, payload) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.users}${id}`, payload)
        return res.data
      } catch (error) {
        throw error
      }
    },

    async deleteUser (id) {
      try {
        await this.$api.delete(`${inventoryServiceApi.users}${id}`)
      } catch (error) {
        throw error
      }
    },

    async getUser (id) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.users}${id}`)
        this.selectedUser = res.data
        return res.data
      } catch (error) {
        throw error
      }
    }
  }
})
