import { register } from 'register-service-worker'
import { Notify } from 'quasar'

// The ready(), registered(), cached(), updatefound() and updated()
// events passes a ServiceWorkerRegistration instance in their arguments.
// ServiceWorkerRegistration: https://developer.mozilla.org/en-US/docs/Web/API/ServiceWorkerRegistration

register(process.env.SERVICE_WORKER_FILE, {
  // The registrationOptions object will be passed as the second argument
  // to ServiceWorkerContainer.register()
  // https://developer.mozilla.org/en-US/docs/Web/API/ServiceWorkerContainer/register#Parameter

  // registrationOptions: { scope: './' },

  ready (registration) {
    console.log('Service worker is active.', registration)
  },

  registered (registration) {
    console.log('Service worker has been registered.', registration)
  },

  cached (registration) {
    console.log('Content has been cached for offline use.', registration)
  },

  updatefound (registration) {
    console.log('New content is downloading.', registration)
  },

  updated (registration) {
    console.log('New content is available; please refresh.', registration)
    // Display Update Notification
    Notify.create({
      type: 'info',
      color: 'primary',
      textColor: 'white',
      progress: true,
      message: 'New content is available. Please click on \'Ok\' to apply changes, or \'Cancel\' and refresh the page yourself later.',
      position: 'top',
      multiline: true,
      actions: [
        {
          label: 'Cancel',
          color: 'white',
          handler: () => { /**/ }
        },
        {
          label: 'Ok',
          color: 'white',
          handler: async () => {
            // localStorage.clear()
            // clear out all the indexDB databases except workbox background sync
            console.log('indexDb has been wiped and refreshed.')
            const dbs = await window.indexedDB.databases()
            dbs.forEach(db => {
              if (db.name !== 'workbox-background-sync') {
                window.indexedDB.deleteDatabase(db.name)
              }
            })
            navigator.serviceWorker.controller.postMessage('forceRefreshServiceWorkers')
            window.location.reload(true)
          }
        }
      ],
      timeout: 0
    })
  },

  offline () {
    console.log('No internet connection found. App is running in offline mode.')
  },

  error (err) {
    console.error('Error during service worker registration:', err)
  }
})