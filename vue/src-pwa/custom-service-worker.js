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

self.skipWaiting()
clientsClaim()

console.log('Custom service worker active')

// Use with precache injection (caches all local files needed to run app offline)
precacheAndRoute(self.__WB_MANIFEST)

// Listen for messages from frontend
self.addEventListener('message', async (event) => {
  if (event.data === 'forceRefreshServiceWorkers') {
    self.skipWaiting()
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