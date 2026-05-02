import { boot } from 'quasar/wrappers'
import axios from 'axios'

import { useUserStore } from '@/stores/user-store'
import { useGlobalStore } from '@/stores/global-store'

const api = axios.create({
  baseURL: process.env.VITE_INV_SERVCE_API,
  headers: {
    Accept: ['application/json'],
    'Access-Control-Allow-Origin': '*'
  },
  paramsSerializer: (queryParams) => {
    // this will process param arrays as multiple entries in get request queryParams params and also removes null query params as well
    // ex: owner_id: [1,2] => owner_id=1&owner_id=2
    for (const key of Object.keys(queryParams)) {
      if (queryParams[key] === null) {
        delete queryParams[key]
      }
    }
    return Object.entries(queryParams).map(([
      key,
      value
    ]) => Array.isArray(value) ? `${key}=${value.join('&' + key + '=')}` : `${key}=${value}`).join('&')
  }
})

export default boot(({ app }) => {
  // for use inside Vue files (Options API) through this.$axios and this.$api

  app.config.globalProperties.$axios = axios
  // ^ ^ ^ this will allow you to use this.$axios (for Vue Options API form)
  //       so you won't necessarily have to import axios in each vue file

  app.config.globalProperties.$api = api
  // ^ ^ ^ this will allow you to use this.$api (for Vue Options API form)
  //       so you can easily perform requests against your app's API


  // Set withCredentials to true to ensure cookies are sent with cross-origin requests
  api.defaults.withCredentials = true

  // axios interceptor (reduced since browser handles cookies automatically)
  api.interceptors.request.use((config) => {
    // We no longer manually map a token from sessionStorage to Authorization header
    return config
  })

  // axios response interceptor to handle specific error responses/codes
  const userStore = useUserStore()
  api.interceptors.response.use((response) => {
    return response
  }, (error) => {
    const globalStore = useGlobalStore()

    if (!error.response) {
      error.response = {
        data: {
          detail: error.message
        }
      }
    } else if (error.response.status == 401 && userStore.userData.user_id) {
      // if we get a 401 error then user needs to be logged out and reauthenticated
      // Suppress logouts during offline sync to prevent interrupting the queue presentation
      if (!globalStore.appPendingSync) {
        userStore.patchLogout(true)
      }
    }
    return Promise.reject(error)
  })
})

// exposes our api reference to areas outside of the vue files such as pinia
export { api }
