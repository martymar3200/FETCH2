import { store } from 'quasar/wrappers'
import { api } from 'boot/axios'
import { createPinia } from 'pinia'

export default store(() => {
  const pinia = createPinia()

  // You can add Pinia plugins here
  // pinia.use(SomePiniaPlugin)

  // allows us to call this.$api in store files similar to our globally defined $api for vue files
  pinia.use(({ store }) => {
    store.$api = api
    store.apiPageSizeDefault = 50
  })

  return pinia
})
