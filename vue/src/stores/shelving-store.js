import { defineStore } from 'pinia'
import inventoryServiceApi from '@/http/InventoryService.js'
import { useOfflineSync } from '@/composables/useOfflineSync.js'
import { useIndexDbHandler } from '@/composables/useIndexDbHandler.js'
export const useShelvingStore = defineStore('shelving-store', {
  state: () => ({
    shelvingJobListTotal: 0,
    shelvingJobList: [],
    shelvingJob: {
      type: null,
      assignLocation: false,
      id: null,
      aisle_id: null,
      user: {
        name: ''
      },
      user_id: null,
      building_id: null,
      building: null,
      create_dt: null,
      containers: [],
      ladder_id: null,
      module_id: null,
      side_id: null,
      status: '',
      trays: [],
      non_tray_items: [],
      verification_jobs: [],
      // Merged Direct Fields
      shelf_barcode: {
        value: ''
      },
      owner: {
        name: ''
      },
      owner_id: null,
      size_class: {
        name: ''
      },
      size_class_id: null,
      nextAvailablePosition: null
    },
    originalShelvingJob: null,
    shelvingJobContainer: {
      id: null,
      barcode: {
        value: ''
      },
      owner: {
        name: ''
      },
      owner_id: null,
      size_class: {
        name: ''
      },
      size_class_id: null,
      module_id: null,
      aisle_id: null,
      side_id: null,
      ladder_id: null,
      shelf_id: null,
      shelf_barcode: {
        value: ''
      },
      shelf_position_id: null,
      shelf_position: {
        location: ' - - - - - - ',
        shelf_position_number: {
          number: ''
        }
      },
      scanned_for_shelving: false
    }

  }),
  getters: {
    shelvingJobContainers: (state) => {
      let containerList = []
      if (state.shelvingJob.id) {
        containerList = containerList.concat(state.shelvingJob.trays || [], state.shelvingJob.non_tray_items || [])
      }
      // return the list first sorted alphnumerically then sorted by scanned boolean
      const sortBoolOrder = {
        false: 1,
        null: 2,
        true: 3
      }
      return containerList.sort(new Intl.Collator('en', {
        numeric: true,
        sensitivity: 'accent'
      }).compare).sort((a, b) => sortBoolOrder[a.scanned_for_shelving] - sortBoolOrder[b.scanned_for_shelving])
    },
    allContainersShelved: (state) => {
      // Unified logic: if we have containers, they must all be scanned.
      // For created/empty jobs (Direct start), it's false until populated (or true if considered 'done' when empty? Logic was: length==0 -> true in Direct)
      // Old Direct logic: return state.shelvingJobContainers.length == 0 || state.shelvingJobContainers.some(c => !c.scanned_for_shelving) ? false : true
      // This meant: if length 0, true (can complete). If some not scanned, false.
      // Let's preserve that.

      const containers = state.shelvingJobContainers
      if (containers.length === 0) {
        return true
      }
      return !containers.some(c => !c.scanned_for_shelving)
    }
  },
  actions: {
    resetShelvingStore () {
      this.$reset()
    },
    resetShelvingJob () {
      this.shelvingJob = {
        type: null,
        assignLocation: false,
        id: null,
        aisle_id: null,
        user: {
          name: ''
        },
        assigned_user_id: null,
        building_id: null,
        building: null,
        create_dt: null,
        containers: [],
        ladder_id: null,
        module_id: null,
        side_id: null,
        status: '',
        verification_jobs: [],
        trays: [],
        non_tray_items: [],
        shelf_barcode: { value: '' },
        owner: { name: '' },
        owner_id: null,
        size_class: { name: '' },
        size_class_id: null,
        nextAvailablePosition: null
      }
      this.originalShelvingJob = null
    },
    resetShelvingJobContainer () {
      this.shelvingJobContainer = {
        id: null,
        barcode: {
          value: ''
        },
        owner: {
          name: ''
        },
        owner_id: null,
        size_class: {
          name: ''
        },
        size_class_id: null,
        module_id: null,
        aisle_id: null,
        side_id: null,
        ladder_id: null,
        shelf_id: null,
        shelf_barcode: {
          value: ''
        },
        shelf_position_id: null,
        shelf_position: {
          location: ' - - - - - - ',
          shelf_position_number: {
            number: ''
          }
        },
        scanned_for_shelving: false
      }
    },
    async getShelfByBarcode (barcode_value) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.shelvesBarcode}${barcode_value}`)
        if (this.shelvingJob.id) {
          this.shelvingJob = {
            ...this.shelvingJob,
            shelf_barcode: res.data.barcode,
            owner: res.data.owner,
            size_class: res.data.shelf_type.size_class
          }
          // Also fetch next available position
          try {
            const nextPosRes = await this.$api.get(`${inventoryServiceApi.shelvesBarcode}${barcode_value}/next-position`)
            this.shelvingJob.nextAvailablePosition = nextPosRes.data.next_available_position
          } catch (nextPosError) {
            // If next position fetch fails, continue without it
            console.warn('Failed to fetch next available position:', nextPosError)
            this.shelvingJob.nextAvailablePosition = null
          }
        }
        return res
      } catch (error) {
        throw error
      }
    },
    async getShelvingJobList (qParams) {
      try {
        const res = await this.$api.get(inventoryServiceApi.shelvingJobs, {
          params: {
            size: this.apiPageSizeDefault,
            ...qParams
          }
        })
        this.shelvingJobList = res.data.items

        // keep track of response total for pagination
        this.shelvingJobListTotal = res.data.total
      } catch (error) {
        throw error
      }
    },
    async getShelvingJob (id) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.shelvingJobs}${id}`)
        this.shelvingJob = res.data
        this.originalShelvingJob = { ...this.shelvingJob }
      } catch (error) {
        throw error
      }
    },
    async postShelvingJob (payload, qParams) {
      try {
        const res = await this.$api.post(inventoryServiceApi.shelvingJobs, payload, { params: qParams })
        this.shelvingJob = res.data
        this.originalShelvingJob = { ...this.shelvingJob }
      } catch (error) {
        throw error
      }
    },
    async patchShelvingJob (payload) {
      const { offlineAwareRequest } = useOfflineSync()
      const { addDataToIndexDb } = useIndexDbHandler()
      try {
        const res = await offlineAwareRequest({
          method: 'PATCH',
          url: `${inventoryServiceApi.shelvingJobs}${payload.id}`,
          payload,
          optimisticUpdate: () => {
            if (this.shelvingJob && payload.status) {
              this.shelvingJob.status = payload.status
              this.originalShelvingJob.status = payload.status
            }
          },
          updateSnapshot: async () => {
            await addDataToIndexDb('shelvingStore', 'shelvingJob', JSON.parse(JSON.stringify(this.shelvingJob)))
            await addDataToIndexDb('shelvingStore', 'originalShelvingJob', JSON.parse(JSON.stringify(this.originalShelvingJob)))
          }
        })
        if (res.fromServer) {
          this.shelvingJob = res.data
          this.originalShelvingJob = { ...res.data }
        }
      } catch (error) {
        throw error
      }
    },

    /**
     * Create a new shelving job (unified endpoint)
     * @param {Object} payload - Must include { origin: 'Direct' | 'List', building_id, ... }
     * @returns {Object} Created job data
     */
    async createShelvingJob (payload) {
      try {
        const res = await this.$api.post(inventoryServiceApi.shelvingJobs, payload)
        this.shelvingJob = res.data
        this.originalShelvingJob = { ...this.shelvingJob }
        return res.data
      } catch (error) {
        throw error
      }
    },

    getShelvingJobContainer (barcode_value) {
      // find the container with the matching barcode_value and set the data as the shelvingJobContainer
      this.shelvingJobContainer = this.shelvingJobContainers.find(container => container.barcode.value == barcode_value)
    },
    async postShelvingJobContainer (payload) {
      const { offlineAwareRequest } = useOfflineSync()
      const { addDataToIndexDb } = useIndexDbHandler()
      try {
        const res = await offlineAwareRequest({
          method: 'POST',
          url: `${inventoryServiceApi.shelvingJobs}${payload.job_id}/reassign-container-location`,
          payload,
          optimisticUpdate: () => {
            this.shelvingJobContainer.shelf_position_id = payload.shelf_position_id
            if (payload.shelf_position_number && this.shelvingJobContainer.shelf_position?.shelf_position_number) {
              this.shelvingJobContainer.shelf_position.shelf_position_number.number = payload.shelf_position_number
            }

            // Helper to update or add container
            const updateOrAddContainer = (listName) => {
              if (this.shelvingJob[listName]) {
                const index = this.shelvingJob[listName].findIndex(container => container.id == payload.container_id)
                if (index !== -1) {
                  // Update existing
                  const item = this.shelvingJob[listName][index] = this.shelvingJobContainer
                  // move to bottom
                  this.shelvingJob[listName].splice(index, 1)
                  this.shelvingJob[listName].push(item)
                } else {
                  // Add new (Direct workflow)
                  this.shelvingJob[listName] = [
                    ...this.shelvingJob[listName],
                    this.shelvingJobContainer
                  ]
                }
              } else {
                this.shelvingJob[listName] = [this.shelvingJobContainer]
              }
            }

            // update the container at the shelving job level as well
            if (payload.trayed || this.shelvingJobContainer.container_type?.type == 'Tray') {
              updateOrAddContainer('trays')
            } else {
              updateOrAddContainer('non_tray_items')
            }
            this.originalShelvingJob = { ...this.shelvingJob }
          },
          updateSnapshot: async () => {
            await addDataToIndexDb('shelvingStore', 'shelvingJob', JSON.parse(JSON.stringify(this.shelvingJob)))
            await addDataToIndexDb('shelvingStore', 'originalShelvingJob', JSON.parse(JSON.stringify(this.originalShelvingJob)))
          }
        })

        if (res.fromServer) {
          this.shelvingJobContainer = {
            ...this.shelvingJobContainer,
            ...res.data
          }
          if (res.data.next_available_position !== undefined) {
            this.shelvingJob.nextAvailablePosition = res.data.next_available_position
          }
        }
      } catch (error) {
        throw error
      }
    },

    async postShelvingJobContainerProposedLocation (payload) {
      const { offlineAwareRequest } = useOfflineSync()
      const { addDataToIndexDb } = useIndexDbHandler()
      try {
        const res = await offlineAwareRequest({
          method: 'POST',
          url: `${inventoryServiceApi.shelvingJobs}${payload.job_id}/reassign-proposed-location`,
          payload,
          optimisticUpdate: () => {
            this.shelvingJobContainer.shelf_position_id = payload.shelf_position_id
            this.shelvingJobContainer.shelf_position.shelf_position_number.number = payload.shelf_position_number
            if (payload.trayed) {
              const trayItemIndex = this.shelvingJob.trays.findIndex(container => container.id == payload.container_id)
              const trayItemByIndex = this.shelvingJob.trays[trayItemIndex] = this.shelvingJobContainer
              this.shelvingJob.trays.splice(trayItemIndex, 1)
              this.shelvingJob.trays.push(trayItemByIndex)
            } else {
              const nonTrayItemIndex = this.shelvingJob.non_tray_items.findIndex(container => container.id == payload.container_id)
              const nonTrayItemByIndex = this.shelvingJob.non_tray_items[nonTrayItemIndex] = this.shelvingJobContainer
              this.shelvingJob.non_tray_items.splice(nonTrayItemIndex, 1)
              this.shelvingJob.non_tray_items.push(nonTrayItemByIndex)
            }
            this.originalShelvingJob = { ...this.shelvingJob }
          },
          updateSnapshot: async () => {
            await addDataToIndexDb('shelvingStore', 'shelvingJob', JSON.parse(JSON.stringify(this.shelvingJob)))
            await addDataToIndexDb('shelvingStore', 'originalShelvingJob', JSON.parse(JSON.stringify(this.originalShelvingJob)))
          }
        })
        if (res.fromServer) {
          this.shelvingJobContainer = {
            ...this.shelvingJobContainer,
            ...res.data
          }
        }
      } catch (error) {
        throw error
      }
    },
    async getShelvingTrayContainerDetails (barcode_value) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.traysBarcode}${barcode_value}`)
        return res
      } catch (error) {
        throw error
      }
    },
    async getShelvingTrayItemDetails (barcode_value) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.itemsBarcode}${barcode_value}`)
        return res
      } catch (error) {
        throw error
      }
    },
    async getShelvingNonTrayItemDetails (barcode_value) {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.nonTrayItemsBarcode}${barcode_value}`)
        return res
      } catch (error) {
        throw error
      }
    },


    // ==============================================================================
    // SHELVE BY LIST ACTIONS
    // ==============================================================================

    /**
     * Create a new Shelve by List job
     * @deprecated Use createShelvingJob with origin='List' instead
     */
    async createShelveByListJob (payload) {
      // Backward compatibility - delegate to unified action
      // payload already has necessary fields, just add origin if missing (though backend handles it)
      return this.createShelvingJob({
        ...payload,
        origin: 'List'
      })
    },

    /**
     * Get containers in a Shelve by List job
     */
    async getShelveByListContainers (jobId, status = null) {
      try {
        const params = status ? { status } : {}
        const res = await this.$api.get(`${inventoryServiceApi.shelvingJobs}${jobId}/containers`, { params })
        return res.data
      } catch (error) {
        throw error
      }
    },

    /**
     * Add a container to a Shelve by List job by barcode
     */
    async addContainerToShelveByList (jobId, containerBarcode) {
      try {
        const res = await this.$api.post(`${inventoryServiceApi.shelvingJobs}${jobId}/containers`, {
          container_barcode: containerBarcode
        })
        return res.data
      } catch (error) {
        throw error
      }
    },

    /**
     * Remove a container from a Shelve by List job
     */
    async removeContainerFromShelveByList (jobId, containerId) {
      try {
        await this.$api.delete(`${inventoryServiceApi.shelvingJobs}${jobId}/containers/${containerId}`)
        return true
      } catch (error) {
        throw error
      }
    },

    /**
     * Cancel a Shelve by List job (only if not started)
     */
    async cancelShelveByListJob (jobId) {
      try {
        const res = await this.$api.post(`${inventoryServiceApi.shelvingJobs}${jobId}/cancel`)
        return res.data
      } catch (error) {
        throw error
      }
    },

    /**
     * Run pre-assignment on a Shelve by List job
     */
    async runPreAssignment (jobId, preAssignInput) {
      try {
        const res = await this.$api.post(`${inventoryServiceApi.shelvingJobs}${jobId}/pre-assign`, preAssignInput)
        return res.data
      } catch (error) {
        throw error
      }
    },

    /**
     * Override a container's proposed location
     */
    async overrideContainerLocation (jobId, containerId, overrideInput) {
      try {
        const res = await this.$api.patch(
          `${inventoryServiceApi.shelvingJobs}${jobId}/containers/${containerId}/override`,
          overrideInput
        )
        return res.data
      } catch (error) {
        throw error
      }
    },

    /**
     * Scan a container during Shelve by List execution
     */
    async scanContainerForShelveByList (jobId, containerBarcode) {
      try {
        const res = await this.$api.post(
          `${inventoryServiceApi.shelvingJobs}${jobId}/scan-container`,
          null,
          { params: { container_barcode: containerBarcode } }
        )
        return res.data
      } catch (error) {
        throw error
      }
    },

    /**
     * Confirm a container has been shelved (supports offline sync)
     */
    async confirmContainerShelved (jobId, confirmation) {
      const { offlineAwareRequest } = useOfflineSync()
      try {
        const res = await offlineAwareRequest({
          method: 'POST',
          url: `${inventoryServiceApi.shelvingJobs}${jobId}/confirm-shelve`,
          payload: confirmation,
          optimisticUpdate: () => {
            // Leave snapshot and logic to component as it currently does.
          }
        })
        if (!res.fromServer) {
          return {
            status: 'queued_offline',
            ...confirmation
          }
        }
        return res.data
      } catch (error) {
        throw error
      }
    }
  }
})

