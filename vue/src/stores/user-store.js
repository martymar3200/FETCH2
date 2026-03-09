import { defineStore } from 'pinia'
import inventoryServiceApi from '@/http/InventoryService.js'

export const useUserStore = defineStore('user-store', {
  state: () => ({
    userData: JSON.parse(localStorage.getItem('user')) || {
      user_id: null,
      username: '',
      first_name: '',
      last_name: '',
      permissions: []
    }
  }),

  // ======================================================================
  // ================== START: ADD THIS NEW GETTERS SECTION ===============
  // ======================================================================
  getters: {
    /**
     * It checks if the 'can_edit_tray' permission string exists in the user's permission list.
     * @param {object} state The store's state.
     * @returns {boolean} True if the user has the permission, otherwise false.
     */
    canEditTray: (state) => {
      return state.userData.permissions.includes('can_edit_tray')
    },
    canEditNonTrayItem: (state) => {
      return state.userData.permissions.includes('can_edit_non_tray_item')
    }
  },
  // ======================================================================
  // =================== END: ADD THIS NEW GETTERS SECTION ================
  // ======================================================================

  actions: {
    resetUserStore () {
      localStorage.removeItem('user')
      this.$reset()
    },
    async patchLogin (payload, type) {
      try {
        if (type == 'Internal') {
          // Internal login sets the HttpOnly cookie directly from the backend response now
          await this.$api.post(inventoryServiceApi.authLegacyLogin, payload)
        }

        // At this point, whether Internal or SSO, the browser should have the HttpOnly cookie.
        // We verify the session by fetching the current user profile.
        // Wait, FETCH2 doesn't have an `/auth/me` endpoint. We need to construct a robust way to get the user ID.
        // The most secure approach if we don't have the ID is to hit an endpoint that returns the profile.
        // Let's assume there is an `/auth/me` or `/users/me` being added, or we have to add it.
        // For now, if the backend expects us to know the ID, we need the backend to return basic profile info
        // on login, or we create a `/users/me` endpoint.

        // CRITICAL: FETCH2 backend `/users/{id}` requires an ID.
        // We will add `/auth/me` to the backend to return the current user profile without needing the ID.
        const profileRes = await this.$api.get('/auth/me')
        this.userData = {
          ...this.userData,
          ...profileRes.data
        }

        // resave user credentials in local storage with the merged profile data (safe, non-sensitive)
        localStorage.setItem('user', JSON.stringify(this.userData))

        await this.getUserPermissions()
      } catch (error) {
        throw error
      }
    },
    async patchLogout (reauthenticate = false) {
      try {
        // Ping backend to delete the HttpOnly cookie
        await this.$api.post('/auth/sso/logout/')

        this.resetUserStore()

        if (reauthenticate) {
          // if the reauthenticate flag is passed (usually occurs when user gets timed-out via 401)
          // we want to auto reauthenticate the user and preserve the users route by sending the user to the sso login with a preserve_route query param
          window.location.replace(`${process.env.VITE_INV_SERVCE_API}${inventoryServiceApi.authSsoLogin}?preserve_route=${this.router.currentRoute._value.fullPath}`)
        }
      } catch (error) {
        throw error
      }
    },
    async getUserPermissions () {
      try {
        const res = await this.$api.get(`${inventoryServiceApi.users}${this.userData.user_id}/permissions`)
        this.userData.permissions = res.data.permissions
        // update user credentials in local storage
        localStorage.setItem('user', JSON.stringify(this.userData))
      } catch (error) {
        if (error.response?.status == '404') {
          this.userData.permissions = []
          // update user credentials in local storage
          localStorage.setItem('user', JSON.stringify(this.userData))
        } else {
          throw error
        }
      }
    },
    async updateUserProfile (id, payload) {
      try {
        const res = await this.$api.patch(`${inventoryServiceApi.users}${id}/`, payload)
        this.userData = {
          ...this.userData,
          ...res.data
        }
        localStorage.setItem('user', JSON.stringify(this.userData))
        return res.data
      } catch (error) {
        throw error
      }
    }
  }
})