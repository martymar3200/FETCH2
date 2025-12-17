import { boot } from 'quasar/wrappers'
import axios from 'axios'

import { useUserStore } from '@/stores/user-store'

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


  // axios interceptor to handle token/security authorization being added to our requests
  api.interceptors.request.use((config) => {
    // check if we have a user in localstorage and if that user has an auth based token in session storage
    const userAuth = JSON.parse(localStorage.getItem('user'))
    const userToken = JSON.parse(sessionStorage.getItem('token'))
    if (userAuth && userToken) {
    // if there is an access token we attach that to our requests
      config.headers.Authorization = 'Bearer ' + userToken
    }
    return config
  })

  // axios response interceptor to handle specific error responses/codes
  const userStore = useUserStore()
  api.interceptors.response.use((response) => {
    return response
  }, (error) => {
    if (!error.response) {
      error.response = {
        data: {
          detail: error.message
        }
      }
    } else if (error.response.status == 401 && userStore.userData.user_id) {
      // if we get a 401 error then user needs to be logged out and reauthenticated
      userStore.patchLogout(true)
    }
    return Promise.reject(error)
  })
})

// exposes our api reference to areas outside of the vue files such as pinia
export { api }
