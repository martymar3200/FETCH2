<template>
  <q-layout
    ref="main"
    class="main"
    view="lHh Lpr lFf"
  >
    <NavigationBar />

    <q-page-container class="main-content">
      <BreadCrumb
        v-if="route.name !== 'home'"
        ref="breadCrumbComponent"
      />

      <router-view />

      <!-- global alert component -->

    </q-page-container>

    <!-- pwa install banner -->
    <q-banner
      v-if="showAppInstallBanner"
      class="install-banner bg-primary text-white"
      rounded
      :inline-actions="currentScreenSize == 'xs' ? false : true"
    >
      Would you like to install the FETCH app?
      <template #action>
        <BaseButton
          flat
          color="white"
          label="Yes"
          class="text-body1"
          @click="installApp"
        />
        <BaseButton
          flat
          color="white"
          label="Later"
          class="text-body1"
          @click="showAppInstallBanner = !showAppInstallBanner"
        />
        <BaseButton
          flat
          color="white"
          label="Never"
          class="text-body1"
          @click="neverShowAppInstallBanner"
        />
      </template>
    </q-banner>
  </q-layout>
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
import { onMounted, ref, provide } from 'vue'
import { useRoute } from 'vue-router'
import { LocalStorage } from 'quasar'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { useFormatters } from '@/composables/useFormatters.js'
import { useItemDisplay } from '@/composables/useItemDisplay.js'
import { useDownloadHandler } from '@/composables/useDownloadHandler.js'

import NavigationBar from '@/components/NavigationBar.vue'
import BreadCrumb from '@/components/BreadCrumb.vue'

const route = useRoute()

// Composables
const { currentScreenSize } = useCurrentScreenSize()
const { getNestedKeyPath, getUniqueListByKey, currentIsoDate, formatDateTime } = useFormatters()
const { getItemLocation, renderItemBarcodeDisplay, renderWithdrawnTrayBarcode, renderWithdrawnShelfBarcode, renderWithdrawnItemLocation } = useItemDisplay()
const { handleCSVDownload } = useDownloadHandler()
// Local Data
const breadCrumbComponent = ref(null)
const appInstallPrompt = ref(null)
const showAppInstallBanner = ref(false)
const main = ref(null)


// Logic
onMounted(() => {
  if (!LocalStorage.getItem('hideAppInstallation')) {
    // this event only gets fired on devices that support pwa installs
    window.addEventListener('beforeinstallprompt', (event) => {
      appInstallPrompt.value = event
      showAppInstallBanner.value = true
    })
  }

  // trigger a service worker / app content check every 8 hours
  if ('serviceWorker' in navigator) {
    setInterval(() => {
      checkForServiceWorkerUpdates()
    }, 28800000)
  }
})
const installApp = () => {
  showAppInstallBanner.value = false
  appInstallPrompt.value.prompt()
  appInstallPrompt.value.userChoice.then(result => {
    if (result.outcome == 'accepted') {
      neverShowAppInstallBanner()
    }
  })
}
const neverShowAppInstallBanner = () => {
  showAppInstallBanner.value = false
  LocalStorage.set('hideAppInstallation', true)
}
const checkForServiceWorkerUpdates = () => {
  navigator.serviceWorker.getRegistrations().then(async (registrations) => {
    for (let registration of registrations) {
      // update the service workers and get latest content and refresh all indexDb instances
      const dbs = await window.indexedDB.databases()
      dbs.forEach(db => {
        if (db.name !== 'workbox-background-sync') {
          window.indexedDB.deleteDatabase(db.name)
        }
      })
      await registration.update()
    }
  })
}

// Global Functions

const handlePageOffset = () => {
  // this is the global function will use to control the q-page components min-height generation
  // this is needed since we are adding breadcrumbs to all pages which the q-page components default offset only checks for the navigation bar

  // NavigationBar component height = 50px
  let headerOffset = 50

  // NavigationBar component height + BreadCrumb component height (dynamic) = offest
  if (route.name !== 'home') {
    // default element height on desktop is 49px
    let breadCrumbElementHeight = 49
    if (breadCrumbComponent.value) {
      breadCrumbElementHeight = breadCrumbComponent.value.$el.clientHeight
    }
    headerOffset = 50 + breadCrumbElementHeight
  }

  return { minHeight: `calc(100vh - ${headerOffset}px)` }
}
provide('handle-page-offset', handlePageOffset) // handlePageOffset is globally accessible via provide/inject
provide('get-nested-key-path', getNestedKeyPath)
provide('get-unique-list-by-key', getUniqueListByKey)
provide('current-iso-date', currentIsoDate)
provide('format-date-time', formatDateTime)
provide('get-item-location', getItemLocation)
provide('render-item-barcode-display', renderItemBarcodeDisplay)
provide('render-withdrawn-tray-barcode', renderWithdrawnTrayBarcode)
provide('render-withdrawn-shelf-barcode', renderWithdrawnShelfBarcode)
provide('render-withdrawn-item-location', renderWithdrawnItemLocation)
provide('handle-csv-download', handleCSVDownload)
</script>

<style lang="scss" scoped>
.main {
  &-content {
    position: relative;
  }
}
.install-banner {
  position: absolute;
  left: 50%;
  bottom: .5rem;
  width: 98%;
  transform: translateX(-50%);
  z-index: 100000;
  box-shadow: 0 0 8px 2px rgba(0, 0, 0, 0.2), 0 3px 5px rgba(0, 0, 0, 0.24);
}


</style>
