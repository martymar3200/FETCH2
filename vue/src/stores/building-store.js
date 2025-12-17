import { defineStore } from 'pinia'
import inventoryServiceApi from '@/http/InventoryService.js'
import { useOptionStore } from './option-store'
const optionStore = useOptionStore()

export const useBuildingStore = defineStore('building-store', {
  state: () => ({
    buildingsTotal: 0,
    sidesTotal: 0,
    shelvesTotal: 0,
    buildings: [],
    modules: [],
    aisles: [],
    sides: [
      // default state needed for toggle buttons utilizing sides
      {
        id: 1,
        side_orientation: {
          name: 'Left'
        }
      },
      {
        id: 2,
        side_orientation: {
          name: 'Right'
        }
      }
    ],
    ladders: [],
    shelves: [],
    shelvesPositions: [],
    buildingDetails: {},
    moduleDetails: {},
    aisleDetails: {},
    sideDetails: {},
    ladderDetails: {},
    shelfDetails: {}
  }),
  actions: {
    resetBuildingStore () {
      this.$reset()
    },
    resetBuildingChildren () {
      // clears state for module options downward since user will need to select an module next to populate the rest of the data
      // ex: shelving workflow - edit shelf location uses a dynamic form that loads building locations based on parent selections aisles selection derives from module ect
      this.moduleDetails = {}
      this.aisleDetails = {}
      this.sideDetails = {}
      this.ladderDetails = {}
      this.shelfDetails = {}
      this.modules = []
      this.aisles = []
      this.sides = [
        {
          id: 1,
          side_orientation: {
            name: 'Left'
          }
        },
        {
          id: 2,
          side_orientation: {
            name: 'Right'
          }
        }
      ]
      this.ladders = []
      this.shelves = []
      this.shelvesPositions = []

      // we also need to clear the relevant options utilized in select inputs via the optionStore
      optionStore.modules = []
      optionStore.aisles = []
      optionStore.ladders = []
      optionStore.shelves = []
      optionStore.shelvesPositions = []
    },
    resetModuleChildren () {
      // clears state for aisle options downward since user will need to select an aisle next to populate the rest of the data
      this.aisleDetails = {}
      this.sideDetails = {}
      this.ladderDetails = {}
      this.shelfDetails = {}
      this.aisles = []
      this.sides = [
        {
          id: 1,
          side_orientation: {
            name: 'Left'
          }
        },
        {
          id: 2,
          side_orientation: {
            name: 'Right'
          }
        }
      ]
      this.ladders = []
      this.shelves = []
      this.shelvesPositions = []

      // we also need to clear the relevant options utilized in select inputs via the optionStore
      optionStore.aisles = []
      optionStore.ladders = []
      optionStore.shelves = []
      optionStore.shelvesPositions = []
    },
    resetAisleChildren () {
      // clears state for side options downward since user will need to select an side next to populate the rest of the data
      this.sideDetails = {}
      this.ladderDetails = {}
      this.shelfDetails = {}
      this.sides = [
        {
          id: 1,
          side_orientation: {
            name: 'Left'
          }
        },
        {
          id: 2,
          side_orientation: {
            name: 'Right'
          }
        }
      ]
      this.ladders = []
      this.shelves = []
      this.shelvesPositions = []

      // we also need to clear the relevant options utilized in select inputs via the optionStore
      optionStore.ladders = []
      optionStore.shelves = []
      optionStore.shelvesPositions = []
    },
    resetSideChildren () {
      // clears state for ladder options downward since user will need to select an ladder next to populate the rest of the data
      this.ladderDetails = {}
      this.shelfDetails = {}
      this.ladders = []
      this.shelves = []
      this.shelvesPositions = []

      // we also need to clear the relevant options utilized in select inputs via the optionStore
      optionStore.ladders = []
      optionStore.shelves = []
      optionStore.shelvesPositions = []
    },
    resetLadderChildren () {
      // clears state for shelf options downward since user will need to select an shelf next to populate the rest of the data
      this.shelfDetails = {}
      this.shelves = []
      this.shelvesPositions = []

      // we also need to clear the relevant options utilized in select inputs via the optionStore
      optionStore.shelves = []
      optionStore.shelvesPositions = []
    },
    async getBuildingsList (qParams) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.buildings}`, {
          params: {
            size: this.apiPageSizeDefault,
            ...qParams
          }
        })
        this.buildings = res.data.items

        // keep track of response total for pagination
        this.buildingsTotal = res.data.total
      } catch (error) {
        throw error
      }
    },
    async getBuildingDetails (id) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.buildings}${id}`)
        this.buildingDetails = res.data
      } catch (error) {
        throw error
      }
    },
    async postBuilding (payload) {
      try {
        const res = await this.$api.post(inventoryServiceApi.buildings, payload)

        // add the newly added building to the top of the list
        this.buildings = [
          res.data,
          ...this.buildings
        ]
      } catch (error) {
        throw error
      }
    },
    async patchBuilding (payload) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.buildings}${payload.id}`, payload)

        // update the specific building with the response info
        this.buildings[this.buildings.findIndex(b => b.id == payload.id)] = res.data
      } catch (error) {
        throw error
      }
    },
    async getModuleDetails (id) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.modules}${id}`)
        this.moduleDetails = res.data
      } catch (error) {
        throw error
      }
    },
    async postModule (payload) {
      try {
        const res = await this.$api.post(inventoryServiceApi.modules, payload)

        // add the newly added module to the top of the buildingDetail module array
        this.buildingDetails.modules = [
          res.data,
          ...this.buildingDetails.modules
        ]
      } catch (error) {
        throw error
      }
    },
    async patchModule (payload) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.modules}${payload.id}`, payload)

        // update the specific module with the response info
        this.buildingDetails.modules[this.buildingDetails.modules.findIndex(m => m.id == payload.id)] = res.data
      } catch (error) {
        throw error
      }
    },
    async getAisleDetails (id) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.aisles}${id}`)
        this.aisleDetails = res.data
      } catch (error) {
        throw error
      }
    },
    async postAisle (payload) {
      try {
        const res = await this.$api.post(inventoryServiceApi.aisles, payload)

        // add the newly added aisle to the top of the moduleDetail aisle array with a manual serialized aisle_number
        this.moduleDetails.aisles = [
          {
            ...res.data,
            aisle_number: { number: payload.aisle_number }
          },
          ...this.moduleDetails.aisles
        ]

        // generate sides left and right on the newly created aisle
        await this.postSide({
          aisle_id: res.data.id,
          side_orientation_id: 1
        }),
        await this.postSide({
          aisle_id: res.data.id,
          side_orientation_id: 2
        })
      } catch (error) {
        if (error.response.status == 422 && error.response?.data?.detail.includes('No aisle_number entity')) {
          // if we get a 422 error related to the number passed in not existing create that number and re run the aisle creation
          await this.postAisleNumber(payload.aisle_number)
          await this.postAisle(payload)
        } else {
          throw error
        }
      }
    },
    async patchAisle (payload) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.aisles}${payload.id}`, payload)

        // update the specific aisle with the response info
        this.moduleDetails.aisles[this.moduleDetails.aisles.findIndex(a => a.id == payload.id)] = {
          ...res.data,
          aisle_number: { number: payload.aisle_number }
        }
      } catch (error) {
        throw error
      }
    },
    async postAisleNumber (aisleNumber) {
      try {
        // adds a new aisle number to the db to be utilized in aisle creation
        await this.$api.post(inventoryServiceApi.aislesNumbers, { number: aisleNumber })
      } catch (error) {
        throw error
      }
    },
    async getSideList (qParams) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.sides}`, {
          params: {
            size: this.apiPageSizeDefault,
            ...qParams
          }
        })
        this.sides = res.data.items

        // keep track of response total for pagination
        this.sidesTotal = res.data.total
      } catch (error) {
        throw error
      }
    },
    async getSideDetails (id) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.sides}${id}`)
        this.sideDetails = res.data
      } catch (error) {
        throw error
      }
    },
    async postSide (payload) {
      try {
        const res = await this.$api.post(inventoryServiceApi.sides, payload)
        this.sideDetails = res.data
      } catch (error) {
        throw error
      }
    },
    async getLadderDetails (id) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.ladders}${id}`)
        this.ladderDetails = res.data
      } catch (error) {
        throw error
      }
    },
    async postLadder (payload) {
      try {
        const res = await this.$api.post(inventoryServiceApi.ladders, payload)

        // add the newly added ladder to the top of the sideDetail ladders array
        this.sideDetails.ladders = [
          {
            ...res.data,
            ladder_number: { number: payload.ladder_number }
          },
          ...this.sideDetails.ladders
        ]
      } catch (error) {
        throw error
      }
    },
    async patchLadder (payload) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.ladders}${payload.id}`, payload)

        // update the specific ladder with the response info
        this.sideDetails.ladders[this.sideDetails.ladders.findIndex(l => l.id == payload.id)] = {
          ...res.data,
          ladder_number: { number: payload.ladder_number }
        }
      } catch (error) {
        throw error
      }
    },
    async getShelveList (qParams) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.shelves}`, {
          params: {
            size: this.apiPageSizeDefault,
            ...qParams
          }
        })
        this.shelves = res.data.items

        // keep track of response total for pagination
        this.shelvesTotal = res.data.total
      } catch (error) {
        throw error
      }
    },
    async getShelfDetails (id) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.shelves}${id}`)
        this.shelfDetails = res.data
      } catch (error) {
        throw error
      }
    },
    async postShelve (payload) {
      try {
        const res = await this.$api.post(inventoryServiceApi.shelves, payload)

        // add the newly added shelve to the top of the shelves list
        this.shelves = [
          {
            ...res.data,
            shelf_number: { number: payload.shelf_number }
          },
          ...this.shelves
        ]
      } catch (error) {
        throw error
      }
    },
    async patchShelve (payload) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.shelves}${payload.id}`, payload)

        // update the specific shelve with the response info
        this.shelves[this.shelves.findIndex(s => s.id == payload.id)] = {
          ...res.data,
          shelf_number: { number: payload.shelf_number }
        }
      } catch (error) {
        throw error
      }
    },
    async getShelfPositionsList (shelf_id, available = false) {
      try {
        const res = await this.$api.get(inventoryServiceApi.shelvesPositions, {
          params: {
            shelf_id,
            empty: available,
            size: this.apiPageSizeDefault
          }
        })
        this.shelvesPositions = res.data.items
      } catch (error) {
        throw error
      }
    },
    async postBulkLocation (payload) {
      try {
        // create a formData Object and assign the file to the formData to be passed to api as 'multipart/form-data' content
        let formData = new FormData()
        Object.entries(payload).forEach(entry => {
          const [
            key,
            value
          ] = entry
          formData.append(key, value)
        })
        const res = await this.$api.post(`${inventoryServiceApi.batchUploadLocationManagement}`, formData)
        return res
      } catch (error) {
        throw error
      }
    }
  }
})
