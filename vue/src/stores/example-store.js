import { defineStore } from 'pinia'
// import inventoryServiceApi from '@/http/InventoryService.js'

export const useExampleStore = defineStore('example-store', {
  state: () => ({
    stateProp: 0
  }),
  getters: {
    stateValue: (state) => state.stateProp
  },
  actions: {
    resetExampleStore () {
      this.$reset()
    },
    async incrementStateProp () {
      this.stateProp++
    }
  }
})
