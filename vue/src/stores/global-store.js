import { defineStore } from 'pinia'

export const useGlobalStore = defineStore('global-store', {
  state: () => ({
    appIsOffline: false,
    appIsLoadingData: false,
    appActionIsLoadingData: false,
    appTableIsLoadingData: false,
    appPendingSync: false,
    appSyncGuard: null,
    appRouteGuard: null,
    pageInitLoading: false,
    // State to control the main navigation drawer's visibility. Default to closed.
    mainNavDrawerOpen: false
  }),
  // NEW: Action to allow any component to control the drawer's state.
  actions: {
    setMainNavDrawerOpen (isOpen) {
      this.mainNavDrawerOpen = isOpen
    }
  }
})