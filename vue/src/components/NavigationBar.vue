<template>
  <div class="nav">
    <!-- main nav -->
    <q-header
      elevated
      class="nav-top"
    >
      <q-toolbar class="bg-secondary justify-between">
        <!-- MODIFIED: This button now calls the global store action -->
        <q-btn
          v-if="userData.user_id"
          color="white"
          flat
          dense
          icon="menu"
          aria-label="Menu Button"
          @click="setMainNavDrawerOpen(!mainNavDrawerOpen)"
        />

        <div class="nav-search">
          <SearchBar v-if="checkUserPermission('can_access_search')" />
        </div>

        <div class="nav-actions">
          <UserLogin v-if="!userData.user_id" />
          <UserMenu v-else />
        </div>
      </q-toolbar>

      <!-- barcode scan banner -->
      <q-banner
        v-if="!barcodeScanAllowed"
        class="nav-banner bg-color-gray-light text-color-black"
        inline-actions
        dense
      >
        <q-icon
          name="mdi-barcode-scan"
          color="black"
          size="25px"
          class="q-mr-sm"
        />
        Barcode scanning is <strong class="text-red">disabled</strong>.
        <template #action>
          <q-btn
            dense
            no-caps
            unelevated
            color="positive"
            label="Enable Scan"
            class="text-body1"
            @click="barcodeScanAllowed = true"
          />
        </template>
      </q-banner>

      <!-- offline banner -->
      <q-banner
        v-if="showOfflineBanner"
        class="nav-banner bg-color-gray-light text-color-black"
        inline-actions
        dense
      >
        <q-icon
          name="signal_wifi_off"
          color="negative"
          size="25px"
          class="q-mr-sm"
        />
        You are in offline mode.
      </q-banner>

      <!-- online banner if user has pending api requests -->
      <q-banner
        v-if="appPendingSync && !appIsOffline"
        class="nav-banner bg-color-gray-light text-color-black"
        inline-actions
        dense
      >
        <q-icon
          name="wifi"
          color="positive"
          size="25px"
          class="q-mr-sm"
        />
        You are back online! There are pending requests to be sent.
        <template #action>
          <q-btn
            dense
            no-caps
            unelevated
            :loading="syncInProgress == 'In Progress'"
            color="positive"
            :label="syncInProgress == 'Complete' ? 'Sync Completed' : 'Send Requests'"
            class="text-body1"
            @click="triggerBackgroundSync"
          />
        </template>
      </q-banner>
    </q-header>

    <!-- side nav -->
    <!-- MODIFIED: The v-model is now bound to the global store state -->
    <q-drawer
      v-if="userData.user_id"
      v-model="mainNavDrawerOpen"
      show-if-above
      class="nav-side bg-primary"
    >
      <q-list
        class="nav-list"
        role="group"
      >
        <q-item
          class="q-my-lg align-center"
          clickable
          tag="a"
          role="link"
          :to="'/'"
        >
          <q-item-section>
            <img
              :src="mainLogo"
              alt="FETCH LOGO"
              width="268"
              height="100"
            />
          </q-item-section>
        </q-item>

        <EssentialLink
          v-for="link in essentialLinks"
          :key="link.title"
          v-bind="link"
          :icon-size="'28px'"
          class="nav-list-link text-white"
          :class="isActiveLink(link) ? 'nav-active' : ''"
        />

        <!-- admin level link -->
        <EssentialLink
          v-if="checkUserPermission('can_access_admin')"
          v-bind="adminLink"
          :icon-size="'28px'"
          class="nav-list-link-admin text-white"
          :class="isActiveLink(adminLink) ? 'nav-active' : ''"
        />
      </q-list>
    </q-drawer>

    <!-- sync navigation guard modal-->
    <PopupModal
      v-if="appSyncGuard"
      title="Warning"
      text="You have pending requests. Are you sure you want to leave?"
      :show-actions="false"
      aria-label="navigationGuardAlert"
    >
      <template #footer-content="{ hideModal }">
        <q-card-section class="row no-wrap justify-between items-center q-pt-sm">
          <q-btn
            no-caps
            unelevated
            color="negative"
            label="Yes, Ignore Requests"
            class="text-body1 full-width"
            @click="handleRouteSyncGuard(appSyncGuard.name); hideModal();"
          />
          <q-space class="q-mx-xs" />
          <q-btn
            outline
            no-caps
            label="Cancel"
            class="text-body1 full-width"
            @click="appSyncGuard = null; hideModal();"
          />
        </q-card-section>
      </template>
    </PopupModal>
  </div>
</template>

<script setup>
import { onMounted, ref, computed, watch } from 'vue'
import { Notify } from 'quasar'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store'
import { useBarcodeStore } from '@/stores/barcode-store'
import { useUserStore } from '@/stores/user-store'
import { useBackgroundSyncHandler } from '@/composables/useBackgroundSyncHandler.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import EssentialLink from '@/components/EssentialLink.vue'
import SearchBar from '@/components/Search/SearchBar.vue'
import PopupModal from '@/components/PopupModal.vue'
import UserLogin from '@/components/User/UserLogin.vue'
import UserMenu from '@/components/User/UserMenu.vue'

const mainLogo = '/assets/FETCH-Logo.svg'
const route = useRoute()
const router = useRouter()

// Composables
const {
  bgSyncData,
  syncInProgress,
  triggerBackgroundSync,
  deleteDataInBackgroundSyncDb
} = useBackgroundSyncHandler()
const { checkUserPermission } = usePermissionHandler()

// Store Data
// MODIFIED: Import the new state and action
const globalStore = useGlobalStore()
const {
  appIsOffline,
  appPendingSync,
  appSyncGuard,
  appRouteGuard,
  mainNavDrawerOpen // new state
} = storeToRefs(globalStore)
const { setMainNavDrawerOpen } = globalStore // new action
const { barcodeScanAllowed } = storeToRefs(useBarcodeStore())
const { userData } = storeToRefs(useUserStore())

// Local Data
const essentialLinks = computed(() => {
  let navLinks = [
    {
      title: 'Accession',
      icon: 'mdi-barcode-scan',
      link: '/accession',
      requiresPerm: 'can_access_accession'
    },
    {
      title: 'Verification',
      icon: 'done_all',
      link: '/verification',
      requiresPerm: 'can_access_verification'
    },
    {
      title: 'Shelving',
      icon: 'subject',
      link: '/shelving',
      requiresPerm: 'can_access_shelving'
    },
    {
      title: 'Request',
      icon: 'manage_search',
      link: '/request',
      requiresPerm: 'can_access_request'
    },
    {
      title: 'Pick List',
      icon: 'list',
      link: '/picklist',
      requiresPerm: 'can_access_picklist'
    },
    {
      title: 'Refile',
      icon: 'format_list_numbered',
      link: '/refile',
      requiresPerm: 'can_access_refile'
    },
    {
      title: 'Withdrawal',
      icon: 'archive',
      link: '/withdrawal',
      requiresPerm: 'can_access_withdraw'
    },
    {
      title: 'Reports',
      icon: 'task',
      link: '/reports',
      requiresPerm: 'can_access_reports'
    }
  ]

  // filter out any links that have 'requirePerm' if the logged in user doesnt have that permission
  navLinks = navLinks.filter(l => {
    if (l.requiresPerm && !userData.value.permissions?.some(perm => perm === l.requiresPerm)) {
      return false
    } else {
      return true
    }
  })
  return navLinks
})
const adminLink = ref({
  title: 'Admin',
  icon: 'mdi-shield-account',
  link: '/admin'
})
// REMOVED: No longer needed, as we now use the global state
// const leftDrawerOpen = ref(false)
const showOfflineBanner = ref(false)
const refreshWhenOnline = ref(false)

// Logic


onMounted(() => {
  window.addEventListener('offline', () => {
    showOfflineBanner.value = true

    // set offline state in store
    appIsOffline.value = true
  })
  window.addEventListener('online', () => {
    showOfflineBanner.value = false
    appIsOffline.value = false
  })

  if ('serviceWorker' in navigator) {
    // listen for messages from the serviceworker scripts
    navigator.serviceWorker.addEventListener('message', async (event) => {
      if (event.data.message == 'pending sync') {
        // show online banner only if we have requests pending in queue
        appPendingSync.value = true
      } else if (event.data.message == 'refreshWhenOnline') {
        // refresh the app state incase indexDb fails to detect stored queue calls when user comes back online
        refreshWhenOnline.value = true
      } else if (event.data.message == 'sync complete') {
        // when user triggers an offline sync, we need to wait for the syncComplete message from the serviceworker queue
        syncInProgress.value = 'Complete'

        // handle any non breaking errors passed back from bgSync
        if (event.data.error && event.data.error.length > 0) {
          event.data.error.forEach(err => {
            Notify.create({
              type: 'negative',
              message: err
            })
          })

          await new Promise(resolve => setTimeout(() => {
            appPendingSync.value = false
            syncInProgress.value = ''
            resolve()
          }, 5200))
          window.location.reload()
        } else {
          setTimeout(() => {
            appPendingSync.value = false
            syncInProgress.value = ''
            window.location.reload()
          }, 2500)
        }
      } else if (event.data.message == 'sync error') {
        syncInProgress.value = ''
        syncInProgress.value = ''
        Notify.create({
          type: 'negative',
          message: event.data.error
        })
      }
    })
  }

  // display a route guard alert if the user tries to directly navigate to a page
  if (appRouteGuard.value) {
    displayRouteGuardAlert(appRouteGuard.value.name)
  }
})

// when coming back online sometimes indexDb doesnt catch queue requests, so when our background sync service worker stores api calls it sends a message to frontend to refresh the app
watch(appIsOffline, () => {
  if (appIsOffline.value == false && refreshWhenOnline.value == true) {
    window.location.reload(true)
  }
})

// watch the bgSyncData and if we detect any requests that are still pending display the online banner with requests pending action
watch(bgSyncData, () => {
  if (bgSyncData.value.length > 0 && navigator.onLine) {
    appPendingSync.value = true
  } else {
    appPendingSync.value = false
  }
})

watch(appRouteGuard, () => {
  if (appRouteGuard.value) {
    displayRouteGuardAlert(appRouteGuard.value.name)
  }
})

// REMOVED: No longer needed, as the hamburger button calls the store action directly
// const toggleLeftDrawer = () => {
//   leftDrawerOpen.value = !leftDrawerOpen.value
// }

const isActiveLink = (linkObj) => {
  if (linkObj.link !== '/' && route.path.includes(linkObj.link)) {
    return true
  } else {
    return false
  }
}
const handleRouteSyncGuard = async (pathName) => {
  // delete all requests from background queue and reset app sync status, guard and banner
  await deleteDataInBackgroundSyncDb()
  appPendingSync.value = false
  appSyncGuard.value = null
  router.push({
    name: pathName
  })
}
const displayRouteGuardAlert = (pathName) => {
  Notify.create({
    type: 'negative',
    message: `Sorry, you do not have permission to view the <b>${pathName.replaceAll('-', ' ')}</b> page!`,
    html: true
  })
  // reset the route guard in our store
  appRouteGuard.value = null
}
</script>

<style lang="scss" scoped>
.nav {
  position: relative;

  @media (max-width: $breakpoint-sm-max) {
    :deep(.q-drawer--on-top) {
      z-index: 6000;
    }
  }

  &-top {
    z-index: 6000;
  }

  &-active {
    background-color: $accent;
  }

  &-search {
    width: 60%;

    @media (max-width: $breakpoint-sm-min) {
      width: 75%;
    }
  }

  &-list {
    position: relative;
    display: flex;
    flex-flow: column nowrap;
    height: 100%;

    &-link {
      &-admin {
        margin-top: auto;
      }
    }
  }

  &-banner {
    border-top: 1px solid $secondary;
  }
}
</style>