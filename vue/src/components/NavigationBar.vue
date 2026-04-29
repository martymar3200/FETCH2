<template>
  <div class="nav">
    <!-- main nav -->
    <q-header
      class="nav-top bg-white text-primary bordered"
    >
      <q-toolbar class="justify-between q-py-xs">
        <!-- MODIFIED: This button now calls the global store action -->
        <BaseButton
          v-if="userData.user_id"
          color="primary"
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
        class="nav-banner bg-red-1 text-negative"
        inline-actions
        dense
      >
        <q-icon
          name="mdi-barcode-scan"
          color="negative"
          size="20px"
          class="q-mr-sm"
        />
        <span class="text-weight-medium">Barcode scanning is disabled.</span>
        <template #action>
          <BaseButton
            variant="danger"
            label="Enable Scan"
            class="text-body2"
            @click="barcodeScanAllowed = true"
          />
        </template>
      </q-banner>

      <!-- offline banner -->
      <q-banner
        v-if="showOfflineBanner"
        class="nav-banner bg-amber-1 text-warning-dark"
        inline-actions
        dense
      >
        <q-icon
          name="signal_wifi_off"
          color="warning"
          size="20px"
          class="q-mr-sm"
        />
        <span class="text-weight-medium">You are in offline mode.</span>
      </q-banner>

      <!-- online banner if user has pending api requests -->
      <q-banner
        v-if="appPendingSync && !appIsOffline"
        class="nav-banner bg-green-1 text-positive"
        inline-actions
        dense
      >
        <q-icon
          name="wifi"
          color="positive"
          size="20px"
          class="q-mr-sm"
        />
        <span class="text-weight-medium">You are back online! There are pending requests.</span>
        <template #action>
          <BaseButton
            :loading="syncInProgress == 'In Progress'"
            variant="primary"
            :label="syncInProgress == 'Complete' ? 'Sync Completed' : 'Send Requests'"
            class="text-body2"
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
      overlay
      class="nav-side bg-sidebar-theme"
    >
      <q-list
        class="nav-list"
        role="group"
        @click.capture="setMainNavDrawerOpen(false)"
      >
        <q-item
          class="q-mt-xl q-mb-lg"
          clickable
          tag="a"
          role="link"
          :to="'/'"
        >
          <q-item-section class="flex flex-center">
            <div class="nav-logo-text">
              FETCH<span>2</span>
            </div>
          </q-item-section>
        </q-item>

        <EssentialLink
          v-for="link in essentialLinks"
          :key="link.title"
          v-bind="link"
          :icon-size="'28px'"
          class="nav-list-link text-white"
          :class="isActiveLink(link) ? 'bg-accent' : ''"
        />

        <!-- admin level link -->
        <EssentialLink
          v-if="checkUserPermission('can_access_admin')"
          v-bind="adminLink"
          :icon-size="'28px'"
          class="nav-list-link-admin text-white"
          :class="isActiveLink(adminLink) ? 'bg-accent' : ''"
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
          <BaseButton
            no-caps
            unelevated
            color="negative"
            label="Yes, Ignore Requests"
            class="text-body1 full-width"
            @click="handleRouteSyncGuard(appSyncGuard.name); hideModal();"
          />
          <q-space class="q-mx-xs" />
          <BaseButton
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
import { notify } from '@/utils/notify'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store'
import { useBarcodeStore } from '@/stores/barcode-store'
import { useUserStore } from '@/stores/user-store'
import { useOfflineSync } from '@/composables/useOfflineSync.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import EssentialLink from '@/components/EssentialLink.vue'
import SearchBar from '@/components/Search/SearchBar.vue'
import PopupModal from '@/components/PopupModal.vue'
import UserLogin from '@/components/User/UserLogin.vue'
import UserMenu from '@/components/User/UserMenu.vue'
import BaseButton from '@/components/Base/BaseButton.vue'


const route = useRoute()
const router = useRouter()

// Composables
const {
  pendingOpsCount,
  syncPendingOps
} = useOfflineSync()
const syncInProgress = ref('')
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
      title: 'Shipping',
      icon: 'local_shipping',
      link: '/shipping',
      requiresPerm: 'can_access_shipping'
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

// Logic

// Auto-close the navigation drawer when the route changes
watch(route, () => {
  setMainNavDrawerOpen(false)
})


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

  // display a route guard alert if the user tries to directly navigate to a page
  if (appRouteGuard.value) {
    displayRouteGuardAlert(appRouteGuard.value.name)
  }
})

watch(appIsOffline, () => {
  if (!appIsOffline.value && pendingOpsCount.value > 0) {
    appPendingSync.value = true
  }
})

// watch the pendingOpsCount and if we detect any requests that are still pending display the online banner with requests pending action
watch(pendingOpsCount, () => {
  if (pendingOpsCount.value > 0 && !appIsOffline.value) {
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
const handleRouteSyncGuard = (pathName) => {
  // we do not delete pending operations anymore, the user just leaves the page and the ops remain in IDB
  appSyncGuard.value = null
  router.push({
    name: pathName
  })
}

const triggerBackgroundSync = async () => {
  syncInProgress.value = 'In Progress'
  try {
    await syncPendingOps()
    syncInProgress.value = 'Complete'
    // after 2.5 seconds we refresh the page so the app can fetch the latest server state unconditionally
    setTimeout(() => {
      syncInProgress.value = ''
      appPendingSync.value = false
      window.location.reload()
    }, 2500)
  } catch (error) {
    syncInProgress.value = ''
    if (error.message === 'AUTH_EXPIRED') {
      // User has been logged out and redirected by store handler, nothing to do here
      return
    }
    notify({
      type: 'negative',
      message: error.message || 'Sync failed due to an error. Remaining queue saved.'
    })
  }
}
const displayRouteGuardAlert = (pathName) => {
  notify({
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
    padding-top: 24px;

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
<style lang="scss">
/* Global styles for the navigation drawer and logo */
.nav-side {
  border-radius: 0 24px 24px 0 !important;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.2), 0 10px 10px -5px rgba(0, 0, 0, 0.1) !important;
  overflow: hidden !important;

  .q-drawer__content {
    border-radius: 0 24px 24px 0 !important;
    overflow: hidden !important;
  }
}

.nav-logo-text {
  font-size: 2.25rem;
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.05em;
  user-select: none;
  display: flex;
  align-items: center;
  justify-content: center;

  span {
    color: #1d4ed8; /* Blue 700 Accent */
    margin-left: 2px;
  }
}
</style>
