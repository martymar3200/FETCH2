import { defineStore } from 'pinia'
import inventoryServiceApi from '@/http/InventoryService.js'

export const useRecordManagementStore = defineStore('record-management-store', {
  state: () => ({
    itemDetails: {
      id: null
    },
    itemRequestHistory: [],
    itemRequestHistoryTotal: 0,
    trayDetails: {
      id: null
    },
    shelfDetails: {
      id: null
    },
    shelfContainers: [],
    shelfContainersTotal: 0
  }),
  actions: {
    resetRecordManagementStore () {
      this.$reset()
    },
    async getItemDetails (barcodeValue) {
      try {
        // since we dont know if the item detail view is a tray item vs non tray item we need to try hitting both item and non tray item detail endpoints
        const [
          resTrayItem,
          resNonTrayItem
        ] = await Promise.all([
          this.$api.get(`${inventoryServiceApi.itemsBarcode}${barcodeValue}`).catch((error) => error.response.status == '404' ? '404' : error),
          this.$api.get(`${inventoryServiceApi.nonTrayItemsBarcode}${barcodeValue}`).catch((error) => error.response.status == '404' ? '404' : error)
        ])

        if (resTrayItem !== '404') {
          this.itemDetails = resTrayItem.data
        } else if (resNonTrayItem !== '404') {
          this.itemDetails = resNonTrayItem.data
        } else {
          return
        }
      } catch (error) {
        throw error
      }
    },
    async getItemRequestHistory (qParams) {
      try {
        const res = await this.$api.get(inventoryServiceApi.requests, {
          params: {
            size: this.apiPageSizeDefault,
            ...qParams
          }
        })
        this.itemRequestHistory = res.data.items

        // keep track of response total for pagination
        this.itemRequestHistoryTotal = res.data.total
      } catch (error) {
        throw error
      }
    },
    async getTrayDetails (barcodeValue) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.traysBarcode}${barcodeValue}`)
        this.trayDetails = res.data
      } catch (error) {
        throw error
      }
    },
    async getShelfDetails (barcodeValue) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.shelvesBarcode}${barcodeValue}`)
        this.shelfDetails = res.data
      } catch (error) {
        throw error
      }
    },
    async getShelfContainers (qParams) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.shelvesBarcode}${this.shelfDetails.barcode.value}/shelved`, {
          params: {
            size: this.apiPageSizeDefault,
            ...qParams
          }
        })
        this.shelfContainers = res.data.items

        // keep track of response total for pagination
        this.shelfContainersTotal = res.data.total
      } catch (error) {
        throw error
      }
    },
    async patchTray (payload) {
      try {
        const trayId = payload.id
        const updateData = { ...payload }
        delete updateData.id

        const res = await this.$api.patch(`${inventoryServiceApi.trays}${trayId}`, updateData)

        this.trayDetails = res.data

        return res.data
      } catch (error) {
        console.error('Failed to update tray:', error)
        throw error
      }
    },

    async patchNonTrayItem (payload) {
      try {
        const itemId = payload.id
        const updateData = { ...payload }
        delete updateData.id

        const res = await this.$api.patch(`${inventoryServiceApi.nonTrayItems}${itemId}`, updateData)
        // -------------------------------------------------------
        this.itemDetails = res.data

        return res.data
      } catch (error) {
        console.error('Failed to update Non-Tray Item:', error)
        throw error
      }
    }
  }
})