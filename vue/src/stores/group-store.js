import { defineStore } from 'pinia'
import inventoryServiceApi from '@/http/InventoryService.js'

export const useGroupStore = defineStore('group-store', {
  state: () => ({
    permissionsList: [],
    groupList: [],
    groupDetails: {
      id: null,
      name: null,
      permissions: [],
      users: []
    }
  }),
  actions: {
    resetGroupStore () {
      this.$reset()
    },
    resetGroupDetails () {
      this.groupDetails = {
        id: null,
        name: null,
        permissions: [],
        users: []
      }
    },
    async getPermissionsList () {
      try {
        const res = await this.$api.get(inventoryServiceApi.permissions, { params: { size: this.apiPageSizeDefault } })
        this.permissionsList = res.data.items
      } catch (error) {
        throw error
      }
    },
    async getAdminGroupList () {
      try {
        const res = await this.$api.get(inventoryServiceApi.groups, { params: { size: this.apiPageSizeDefault } })
        this.groupList = res.data.items
      } catch (error) {
        throw error
      }
    },
    async getAdminGroupPermissions (id) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.groups}${id}/permissions`)
        this.groupDetails = res.data
      } catch (error) {
        throw error
      }
    },
    async postAdminGroup (payload) {
      try {
        const res = await this.$api.post(inventoryServiceApi.groups, payload)
        this.groupDetails = res.data
        this.groupList = [
          ...this.groupList,
          res.data
        ]
      } catch (error) {
        throw error
      }
    },
    async patchAdminGroup (payload) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.groups}${payload.id}`, payload)
        // update the group in the group list as well
        const matchingGroupIndex = this.groupList.findIndex(g => g.id == payload.id)
        this.groupList[matchingGroupIndex] = res.data
      } catch (error) {
        throw error
      }
    },
    async deleteAdminGroup (id) {
      try {
        await this.$api.delete(`${inventoryServiceApi.groups}${id}`)
        this.groupList = this.groupList.filter(g => g.id !== id)
      } catch (error) {
        throw error
      }
    },
    async postAdminGroupPermission (payload) {
      try {
        const res = await this.$api.post(`${inventoryServiceApi.groups}${payload.groupId}/add_permission/${payload.permissionId}`)
        this.groupDetails = res.data
      } catch (error) {
        throw error
      }
    },
    async deleteAdminGroupPermission (payload) {
      try {
        const res = await this.$api.delete(`${inventoryServiceApi.groups}${payload.groupId}/remove_permission/${payload.permissionId}`)
        this.groupDetails = res.data
      } catch (error) {
        throw error
      }
    },
    async getAdminGroupUsers (id) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.groups}${id}/users`)
        this.groupDetails = res.data
      } catch (error) {
        throw error
      }
    },
    async postAdminGroupUser (groupId, userId) {
      try {
        const res = await this.$api.post(`${inventoryServiceApi.groups}${groupId}/add_user/${userId}`)
        this.groupDetails = res.data
      } catch (error) {
        throw error
      }
    },
    async deleteAdminGroupUser (groupId, userId) {
      try {
        const res = await this.$api.delete(`${inventoryServiceApi.groups}${groupId}/remove_user/${userId}`)
        this.groupDetails = res.data
      } catch (error) {
        throw error
      }
    }
  }
})
