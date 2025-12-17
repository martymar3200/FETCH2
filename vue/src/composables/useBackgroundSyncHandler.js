import { ref, onBeforeMount } from 'vue'

export function useBackgroundSyncHandler () {

  const indexDb = ref(null)
  const bgSyncData = ref(null)
  const syncInProgress = ref('')

  function triggerBackgroundSync () {
    // send a trigger backgroundsync message to the service workers
    navigator.serviceWorker.controller.postMessage('triggerBackgroundSync')
    syncInProgress.value = 'In Progress'
  }

  async function getBackgroundSyncDb (db_name) {
    return new Promise((resolve, reject) => {

      const request = window.indexedDB.open(db_name)

      request.onupgradeneeded = () => {
        const db = request.result
        db.createObjectStore('requests', { keyPath: 'id' })
      }

      request.onsuccess = (event) => {
        resolve(event.target.result)
      }

      request.onerror = (err) => {
        console.log('Error opening db', err)
        reject('Error')
      }
    })
  }

  async function getDataInBackgroundSyncDb (dataStore) {
    return new Promise((resolve, reject) => {
      const trans = indexDb.value.transaction([dataStore], 'readonly')
      const store = trans.objectStore(dataStore)
      let data = []
      trans.oncomplete = () => {
        resolve(data)
      }

      trans.onerror = (err) => {
        console.log('Error getting data from db', err)
        reject('Error')
      }

      store.openCursor().onsuccess = (e) => {
        let cursor = e.target.result
        if (cursor) {
          data.push(cursor.value)
          cursor.continue()
        }
      }
    })
  }

  async function deleteDataInBackgroundSyncDb () {
    return new Promise((resolve, reject) => {
      const trans = indexDb.value.transaction('requests', 'readwrite')
      const store = trans.objectStore('requests')
      const storeToDelete = store.clear()

      storeToDelete.onsuccess = () => {
        return
      }

      trans.oncomplete = () => {
        resolve()
      }

      trans.onerror = (err) => {
        console.log('Error getting data from db', err)
        reject('Error')
      }
    })
  }

  onBeforeMount(async () => {
    indexDb.value = await getBackgroundSyncDb('workbox-background-sync')
    bgSyncData.value = await getDataInBackgroundSyncDb('requests')
  })

  return {
    bgSyncData,
    syncInProgress,
    triggerBackgroundSync,
    deleteDataInBackgroundSyncDb
  }
}
