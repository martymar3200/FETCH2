/* eslint-env serviceworker */

/*
 * This file (which will be your service worker)
 * is picked up by the build system ONLY if
 * quasar.config.js > pwa > workboxMode is set to "injectManifest"
 */

import { clientsClaim } from 'workbox-core'
import { precacheAndRoute, cleanupOutdatedCaches } from 'workbox-precaching'
import { registerRoute } from 'workbox-routing'
import { NetworkFirst } from 'workbox-strategies'
import { Queue } from 'workbox-background-sync'

self.skipWaiting()
clientsClaim()

console.log('Custom service worker active')

// Use with precache injection (caches all local files needed to run app offline)
precacheAndRoute(self.__WB_MANIFEST)

// Custom manual sync function for Queue class that can be triggered from the vue client
let manualSyncErrorLog = []
Queue.prototype.manualSync = async function () {
  let entry
  while ((entry = await this.shiftRequest())) {
    try {
      const res = await fetch(entry.request.clone())
      const resdata = await res.json()

      // 422 are considered passing for some reason but we want to send an error message for these
      if (res.status == '422') {
        console.log('Queued Request Failed', entry.request, resdata)
        manualSyncErrorLog.push(resdata.detail ?? 'some error occured that doesnt break the proccess')
      } else {
        // Data successfully synchronized send response data back to client
        console.log('Queued Request Successfully Sent', res, resdata)
        const clients = await self.clients.matchAll()
        for (const client of clients) {
          client.postMessage({
            url: res.url,
            response: resdata
          })
        }
      }
    } catch (error) {
      // Handle synchronization errors
      console.log('Queued Request Failed', entry.request, error)

      // Put the failed request back in the queue if its a breaking error otherwise log the error responses to send back to the clinet
      // manualSyncErrorLog.push(`some error occured that doesnt break the proccess ${error}`)
      if (error.response.status == 500) {
        await this.unshiftRequest(entry)
        throw error
      } else {
        manualSyncErrorLog.push(error.response.detail ?? 'some error occured that doesnt break the proccess')
      }
    }
  }
}

// Queue (stores offline api requests)
const offlineQueue = new Queue('offlineQueue', {
  // Cancel the auto replayRequest call when user comes back online since we want to manaully trigger this
  onSync: async () => {
    // Send message to client to notify there are pending requests in queue
    const clients = await self.clients.matchAll()
    for (const client of clients) {
      client.postMessage({ message: 'pending sync' })
    }
  }
})
// Automated queue system
// const offlineQueue = new Queue('offlineQueue', {
//   onSync: async ({ queue }) => {
//     console.log('test onsync', queue)
//     let entry
//     while ((entry = await queue.shiftRequest())) {
//       try {
//         const res = await fetch(entry.request).then(response => response.json())
//         // Data successfully synchronized send response data back to client
//         console.log('Queued Request Successfully Sent', res)
//         const clients = await self.clients.matchAll()
//         for (const client of clients) {
//           client.postMessage(res)
//         }
//       } catch (error) {
//         // Handle synchronization errors
//         console.log('Queued Request Failed', entry.request, error)

//         // Put the failed request back in the queue
//         await queue.unshiftRequest(entry)
//         throw error
//       }
//     }
//   }
// })

// Listen for messages from frontend and trigger something in our service worker file
self.addEventListener('message', async (event) => {
  // if we get a replayRequest (triggerBackgroundSync) message from frontend, trigger sync on the offline queue
  if (event.data === 'triggerBackgroundSync') {
    try {
      await offlineQueue.manualSync()

      // Send message to client to notify sync is complete
      const clients = await self.clients.matchAll()
      for (const client of clients) {
        client.postMessage({
          message: 'sync complete',
          error: manualSyncErrorLog
        })
        manualSyncErrorLog = []
      }
    } catch (error) {
      // Send message to client to notify sync is failed
      const clients = await self.clients.matchAll()
      for (const client of clients) {
        client.postMessage({
          message: 'sync error',
          error
        })
      }
    }
  } else if (typeof event.data == 'object' && event.data.queueIncomingApiCall) {
    // application is offline and we need to queue the passed in api request url
    clientApiCallUrl = event.data.queueIncomingApiCall
  } else if (event.data === 'forceRefreshServiceWorkers') {
    self.skipWaiting()
  }
})

// if api call fails due to absense of network connection client will send that request url in a message to be stored in the offline queue using clientApiCallUrl
let clientApiCallUrl = '/url-recieved-from-client'
self.addEventListener('fetch', (event) => {
  if (event.request.url.startsWith(process.env.VITE_INV_SERVCE_API) && event.request.url.includes(clientApiCallUrl)) {
    if (!self.navigator.onLine) {
      const promiseChain = fetch(event.request.clone()).catch(async () => {
        console.log('Request failed to send no internet available storing in queue')
        // send message to frontend to refresh once back online so that background sync can reload properly and send pending request
        const clients = await self.clients.matchAll()
        for (const client of clients) {
          client.postMessage({ message: 'refreshWhenOnline' })
        }

        return offlineQueue.pushRequest({ request: event.request })
      })

      event.waitUntil(promiseChain)
    }
  }

  // test scenerio setup for test page owner tiers
  if (event.request.url.startsWith(process.env.VITE_INV_SERVCE_API) && event.request.url.includes('/owners/tiers')) {
    if (!self.navigator.onLine) {
      const promiseChain = fetch(event.request.clone()).catch(() => {
        console.log('Request failed to send no internet available storing in queue')
        return offlineQueue.pushRequest({ request: event.request })
      })

      event.waitUntil(promiseChain)
    }
  }
})

// Caches all other api specific request data except the /tiers endpoint
registerRoute(
  ({ url }) => {
    return url.href.startsWith(process.env.VITE_INV_SERVCE_API) && !url.href.includes('/tiers')
  },
  new NetworkFirst()
)

cleanupOutdatedCaches()