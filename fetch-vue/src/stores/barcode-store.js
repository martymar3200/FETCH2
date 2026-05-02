import { defineStore } from 'pinia'
import inventoryServiceApi from '@/http/InventoryService.js'

export const useBarcodeStore = defineStore('barcode-store', {
  state: () => ({
    barcodeScanAllowed: true,
    barcodeInputDelay: process.env.VITE_ENV == 'production' || process.env.VITE_ENV == 'stage' ? .25 : 1,
    barcodeDetails: {
      id: null,
      type_id: null,
      type: {
        name: ''
      },
      value: null,
      withdrawn: false
    }
  }),
  actions: {
    resetBarcodeStore () {
      this.barcodeDetails = {
        id: null,
        type_id: null,
        type: {
          name: ''
        },
        value: null,
        withdrawn: false
      }
    },
    async verifyBarcode (barcode, type, autoAddBarcode = false) {
      try {
        this.resetBarcodeStore()
        let validationTypes = []
        if (typeof type === 'string') {
          validationTypes.push(type)
        } else {
          validationTypes = [...type]
        }

        // check if the scanned barcode exists in the system and matches the passed in type
        await this.getBarcodeDetails(barcode)
        if (this.barcodeDetails.id && validationTypes.includes(this.barcodeDetails.type.name)) {
          return 'barcode_exists'
        } else {
          throw `The scanned barcode exists but is not an "${validationTypes.length == 1 ? validationTypes.toString() : validationTypes.join('/')}" barcode! Please try again.`
        }
      } catch (error) {
        if (error.response?.status == 404 && autoAddBarcode == true) {
          // if the barcode doesnt exist then add the barcode to the system automatically if boolean param is passed
          // (ex: accession workflow creates barcodes if verify comes back with a 404)
          await this.postBarcode(barcode, type)
          return
        }
        throw error
      }
    },
    async getBarcodeDetails (barcode) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.barcodesValue}${barcode}`)
        this.barcodeDetails = res.data
        return res
      } catch (error) {
        throw error
      }
    },
    async postBarcode (barcode, type) {
      try {
        const res = await this.$api.post(inventoryServiceApi.barcodes, {
          type,
          value: barcode
        })

        this.barcodeDetails = res.data
      } catch (error) {
        throw error
      }
    },
    async patchBarcode (barcode_id, barcode_value) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.barcodes}${barcode_id}`, {
          value: barcode_value
        })

        this.barcodeDetails = res.data
      } catch (error) {
        throw error
      }
    },
    async deleteBarcode (barcode_id) {
      try {
        const res = await this.$api.delete(`${inventoryServiceApi.barcodes}${barcode_id}`)

        this.barcodeDetails = res.data
      } catch (error) {
        throw error
      }
    }
  }
})
