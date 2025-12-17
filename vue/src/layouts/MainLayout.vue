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
      <transition-group
        name="alert-notification"
        tag="div"
        class="alert-notification"
        :style="calcAlertWidthPlusScrollOffset"
      >
        <AlertPopup
          v-for="(item, i) in alerts"
          :key="i"
          :alert-type="item.type"
          :alert-text="item.text"
          :persistent="item.persistent"
          :auto-close="item.autoClose"
          @reset="clearAlerts(item.timestamp, true)"
        />
      </transition-group>
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
        <q-btn
          flat
          color="white"
          label="Yes"
          class="text-body1"
          @click="installApp"
        />
        <q-btn
          flat
          color="white"
          label="Later"
          class="text-body1"
          @click="showAppInstallBanner = !showAppInstallBanner"
        />
        <q-btn
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
import { onMounted, ref, provide, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useQuasar } from 'quasar'
import moment from 'moment'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { useAlertPopup } from '@/composables/useAlertPopup'
import AlertPopup from '@/components/AlertPopup.vue'
import NavigationBar from '@/components/NavigationBar.vue'
import BreadCrumb from '@/components/BreadCrumb.vue'

const route = useRoute()

// Composables
const $q = useQuasar()
const { currentScreenSize } = useCurrentScreenSize()
const { alerts, handleAlert, clearAlerts } = useAlertPopup()

// Local Data
const breadCrumbComponent = ref(null)
const appInstallPrompt = ref(null)
const showAppInstallBanner = ref(false)
const main = ref(null)
const calcAlertWidthPlusScrollOffset = computed(() => {
  // get the offset of the main qlayout prop and assign the calculated width for the alerts container
  // get the scroll position of the main qlayout prop and assign the calculated top spacing for the alerts container
  if (main.value) {
    return `width: calc(100% - ${main.value.$.provides._q_l_.left.offset}px); top: ${main.value.$.provides._q_l_.scroll.value.position + 50}px`
  } else {
    return `width: calc(100% - ${300}px);`
  }
})

// Logic
onMounted(() => {
  if (!$q.localStorage.getItem('hideAppInstallation')) {
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
  $q.localStorage.set('hideAppInstallation', true)
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
provide('handle-alert', handleAlert) // handleAlert is globally accessible via provide/inject
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
const getNestedKeyPath = (obj, path) => {
  if (typeof path === 'string') {
    path = path.replace('?', '').split('.')
  }

  if (path.length === 0) {
    return obj
  }
  return getNestedKeyPath(obj[path[0]], path.slice(1))
}
provide('get-nested-key-path', getNestedKeyPath)
const getUniqueListByKey = (arr, key) => {
  // removes duplicate objects from provided array using specified key
  return arr.filter((obj1, i, array) =>
    array.findIndex(obj2 => (obj2[key] == obj1[key])) == i
  )
}
provide('get-uniqure-list-by-key', getUniqueListByKey)
const currentIsoDate = () => {
  const timezoneAwareDateIso = moment().format()
  return timezoneAwareDateIso
}
provide('current-iso-date', currentIsoDate)
const formatDateTime = (dateTime) => {
  if (!dateTime) {
    return {
      date: '',
      time: '',
      dateTime: ''
    }
  }

  //check if the passed in dateTime has missing timezone offset or Z in the ISO string add the z if not
  if (dateTime && /([zZ]|([+-]\d{2}:?\d{2}))$/.test(dateTime) == false) {
    dateTime + 'Z'
  }

  const localTimeFormat = new Date(dateTime).toLocaleString()
  const splitDateTime = localTimeFormat.split(',')
  return {
    date: splitDateTime[0],
    time: splitDateTime[1],
    dateTime: localTimeFormat
  }
}
provide('format-date-time', formatDateTime)
const getItemLocation = (itemData) => {
  let module = ''
  let aisle = ''
  let side = ''
  let ladder = ''
  let shelf = ''
  let shelfPosition = ''
  if (itemData && itemData.shelf_position && itemData.shelf_position.location) {
    const itemLocationValues = itemData.shelf_position.location.split('-')
    module = itemLocationValues[1]
    aisle = itemLocationValues[2]
    side = itemLocationValues[3]
    ladder = itemLocationValues[4]
    shelf = itemLocationValues[5]
    shelfPosition = itemLocationValues[6]
  }

  return `${module}-${aisle}-${side == 'Right' ? 'R' : side == 'Left' ? 'L' : side}-${ladder}-${shelf}-${shelfPosition}`.replace('undefined-', '')
}
provide('get-item-location', getItemLocation)
const audioAlert = () => {
  const beep = new AudioContext()

  let oscillatorNode = beep.createOscillator()
  let gainNode = beep.createGain()
  oscillatorNode.connect(gainNode)

  // Set the oscillator frequency in hertz
  oscillatorNode.frequency.value = 280

  // Set the type of oscillator
  oscillatorNode.type= 'square'
  gainNode.connect(beep.destination)

  // Set the gain to the volume
  gainNode.gain.value = 100 * 0.01

  // Start audio with the desired duration
  oscillatorNode.start(beep.currentTime)
  oscillatorNode.stop(beep.currentTime + 250 * 0.001)
}
provide('audio-alert', audioAlert)
const renderItemBarcodeDisplay = (itemData) => {
  // check if item data object contains barcode.value, or withdrawn_barcode.value field
  if (typeof itemData == 'object' && itemData) {
    return itemData.withdrawn_barcode?.value ?? itemData.barcode.value
  } else {
    return ''
  }
}
provide('render-item-barcode-display', renderItemBarcodeDisplay)
const renderWithdrawnTrayBarcode = (itemData) => {
  // The withdrawn_loc_bcodes are in the form xxxx-yyyy or xxxx
  // Where xxxx is the shelf barcode and yyyy is the tray
  const barcodes = itemData?.withdrawn_loc_bcodes.split('-')
  return barcodes[1] ?? ''
}
provide('render-withdrawn-tray-barcode', renderWithdrawnTrayBarcode)
const renderWithdrawnShelfBarcode = (itemData) => {
  // The withdrawn_loc_bcodes are in the form xxxx-yyyy or xxxx
  // Where xxxx is the shelf barcode and yyyy is the tray
  const barcodes = itemData?.withdrawn_loc_bcodes.split('-')
  return barcodes[0] ?? ''
}
provide('render-withdrawn-shelf-barcode', renderWithdrawnShelfBarcode)
const renderWithdrawnItemLocation = (itemData) => {
  return itemData.status === 'Withdrawn' ? itemData?.withdrawn_location : (itemData.tray ? itemData?.tray?.shelf_position?.location : itemData?.shelf_position?.location)
}
provide('render-withdrawn-item-location', renderWithdrawnItemLocation)
const handleCSVDownload = (fileData, fileName) => {
  const url = window.URL.createObjectURL(new Blob([fileData], { type: 'text/csv' }))

  // Get the current date and time and format as YYYY_MM_DD_HH_MM_SS
  const formattedDate = moment().format().slice(0, 19).replace(/[-T:]/g, '_')
  const link = document.createElement('a')
  link.href = url
  link.download = `${fileName}_${formattedDate}.csv`
  document.body.appendChild(link)
  link.click()

  link.remove()
  window.URL.revokeObjectURL(url)
}
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

.alert-notification {
  position: absolute;
  top: 50px;
  display: flex;
  flex-direction: column-reverse;
  gap: 0.8rem;
  z-index: 7000;

  &-enter-active {
    animation: alert-fade-in 0.5s ease-in-out;
  }

  &-leave-active {
    animation: alert-fade-in 0.5s ease-in-out reverse;
  }
}

@keyframes alert-fade-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
</style>
