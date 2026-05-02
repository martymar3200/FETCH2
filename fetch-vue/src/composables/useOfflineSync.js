import { ref } from 'vue'
import { api } from '@/boot/axios'
import { useIndexDbHandler } from '@/composables/useIndexDbHandler.js'
import { useGlobalStore } from '@/stores/global-store'
import { useUserStore } from '@/stores/user-store'

const STORE_NAME = 'offline-operations'

// A reactive reference so the UI can quickly bind to the count of pending operations.
export const pendingOpsCount = ref(0)
let initialized = false

export function useOfflineSync () {
  const { addDataToIndexDb, getDataInIndexDb, deleteDataInIndexDb } = useIndexDbHandler()

  const initOpsStore = async () => {
    if (initialized) {
      return
    }
    const res = await getDataInIndexDb(STORE_NAME)
    if (res && res.data) {
      pendingOpsCount.value = Object.keys(res.data).length
    }
    initialized = true
  }

  // Pre-load current ops count immediately on composable load
  initOpsStore().catch(err => {
    console.error('Failed to initialize offline operations store:', err)
  })

  /**
   * Generates a unique ID for the offline operation.
   */
  const generateUUID = () => {
    return '10000000-1000-4000-8000-100000000000'.replace(/[018]/g, c =>
      (+c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> +c / 4).toString(16)
    )
  }

  /**
   * Makes an API request if online. If offline, queues an offline operation.
   * @param {Object} params
   * @param {String} params.method HTTP method (e.g., 'PATCH', 'POST')
   * @param {String} params.url API endpoint URL
   * @param {Object} params.payload Request payload
   * @param {Function} [params.optimisticUpdate] Callback to update local Pinia state immediately if offline
   * @param {Function} [params.updateSnapshot] Callback to snapshot local Pinia state to IndexDb if offline
   * @returns {Object} Resolves `{ fromServer: true, data: responseData }` or `{ fromServer: false }`.
   */
  const offlineAwareRequest = async ({ method, url, payload, optimisticUpdate, updateSnapshot }) => {
    const globalStore = useGlobalStore()

    if (globalStore.appIsOffline) {
      // Offline mode: queue operation
      const opId = generateUUID()
      const operation = {
        id: opId,
        timestamp: Date.now(),
        method,
        url,
        payload
      }

      // Execute optimistic local update
      if (typeof optimisticUpdate === 'function') {
        optimisticUpdate()
      }

      // Save operation to IDB
      await addDataToIndexDb(STORE_NAME, opId, operation)
      pendingOpsCount.value++

      // Execute local IDB snapshot for the specific job
      if (typeof updateSnapshot === 'function') {
        await updateSnapshot()
      }

      return { fromServer: false }
    } else {
      // Online mode: real API call
      try {
        const config = {
          method,
          url
        }
        if (payload) {
          config.data = payload
        }
        const res = await api(config)
        return {
          fromServer: true,
          data: res.data
        }
      } catch (error) {
        throw error
      }
    }
  }

  /**
   * Reads all pending offline operations from IndexedDB, sorted by completion timestamp.
   * @returns {Array<Object>} Sorted list of operations.
   */
  const getPendingOperations = async () => {
    const res = await getDataInIndexDb(STORE_NAME)
    if (!res || !res.data) {
      return []
    }

    const ops = Object.values(res.data)
    ops.sort((a, b) => a.timestamp - b.timestamp)
    return ops
  }

  /**
   * Syncs all pending operations sequentially. Aborts immediately on ANY error.
   */
  const syncPendingOps = async () => {
    const userStore = useUserStore()

    // 1. Verify Authentication First (prevents replaying queue with expired cookie)
    try {
      await api.get('/auth/me')
    } catch (authError) {
      // If 401, clear credentials so user gets redirected
      if (authError.response?.status === 401) {
        userStore.patchLogout(true)
        throw new Error('AUTH_EXPIRED')
      }
      // If some other error (e.g., 500, network), abort sync entirely
      throw new Error(`Auth verification failed: ${authError.message}`)
    }

    // 2. Fetch Pending Operations
    const pendingOps = await getPendingOperations()
    if (pendingOps.length === 0) {
      return
    }

    // 3. Replay Operations sequentially
    for (const op of pendingOps) {
      try {
        const config = {
          method: op.method,
          url: op.url
        }
        if (op.payload) {
          config.data = op.payload
        }

        await api(config)

        // Operation succeeded! Safe to delete from queue.
        await deleteDataInIndexDb(STORE_NAME, op.id)
        pendingOpsCount.value--

      } catch (opError) {
        // ABORT SYNC ON ANY ERROR
        // The remaining operations stay in IndexedDB precisely as requested.
        throw opError
      }
    }

    // Update count immediately to 0 when complete
    pendingOpsCount.value = 0
  }

  return {
    pendingOpsCount,
    offlineAwareRequest,
    syncPendingOps,
    getPendingOperations,
    initOpsStore
  }
}
