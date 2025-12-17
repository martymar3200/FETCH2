<template>
  <q-btn
    no-caps
    flat
    dense
    class="text-body1 authentication"
    color="white"
    label="Login"
    aria-label="Login"
    :loading="isStageOrProd ? appActionIsLoadingData : null"
    @click="isStageOrProd ? ssoLogin() : null"
  >
    <q-menu
      v-if="!isStageOrProd"
      :offset="[12, 9]"
      :class="$style['authentication-menu']"
    >
      <q-list>
        <q-item role="menuitem">
          <q-item-section>
            <div class="form-group">
              <label class="form-group-label">
                User Email
              </label>
              <TextInput
                v-model="loginForm.user"
                placeholder="Enter User Email"
                @keyup.enter="isLoginValid ? internalLogin() : null"
              />
            </div>
          </q-item-section>
        </q-item>
        <q-item role="menuitem">
          <q-btn
            no-caps
            unelevated
            class="text-body1"
            color="accent"
            label="Login"
            aria-label="Internal Login"
            :disabled="!isLoginValid"
            :loading="appActionIsLoadingData"
            @click="internalLogin"
          />
          <q-btn
            no-caps
            flat
            class="q-ml-auto text-body2"
            color="accent"
            label="SSO Login"
            aria-label="SSO Login"
            @click="ssoLogin"
          />
        </q-item>
      </q-list>
    </q-menu>
  </q-btn>
</template>

<script setup>
import inventoryServiceApi from '@/http/InventoryService.js'
import { jwtDecode } from 'jwt-decode'
import { ref, inject, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useGlobalStore } from '@/stores/global-store'
import { useUserStore } from '@/stores/user-store'
import TextInput from '@/components/TextInput.vue'

const router = useRouter()
const route = useRoute()

// Store Data
const { appActionIsLoadingData } = storeToRefs(useGlobalStore())
const { patchLogin } = useUserStore()

// Local Data
const isStageOrProd = computed(() => {
  return process.env.VITE_ENV == 'production' || process.env.VITE_ENV == 'stage'
})
const isLoginValid = computed(() => {
  return loginForm.value.user == '' ? false : true
})
const loginForm = ref({
  user: '',
  password: ''
})

// Logic
const handleAlert = inject('handle-alert')

onMounted(async () => {
  // when a user is using sso login they will get redirected back to the app in a logged out state with a token in the route
  // there might also be a preserve_route query, this occurs when user is timeout via a 401 and we preserve the users location to come back to on reauthentication
  if (route.query.token && route.query.preserve_route) {
    // decode the token and pass and store that info in localstorage
    appActionIsLoadingData.value = true
    const payload = {
      token: route.query.token,
      ...jwtDecode(route.query.token)
    }
    await patchLogin(payload, 'Sso')

    // send the user to the preserved route
    router.push(route.query.preserve_route)

    appActionIsLoadingData.value = false
  } else if (route.query.token) {
    // decode the token and pass and store that info in localstorage
    appActionIsLoadingData.value = true
    const payload = {
      token: route.query.token,
      ...jwtDecode(route.query.token)
    }
    await patchLogin(payload, 'Sso')

    // clear token from the route since we are now logged in
    router.replace(route.path)

    appActionIsLoadingData.value = false
  }
})

const ssoLogin = () => {
  // Replace current url with SSO login url (this is where the sso service will handle login from and redirect the user back to the pwa)
  window.location.replace(`${process.env.VITE_INV_SERVCE_API}${inventoryServiceApi.authSsoLogin}`)
  return
}
const internalLogin = async () => {
  try {
    appActionIsLoadingData.value = true
    const payload = {
      email: loginForm.value.user
    }
    await patchLogin(payload, 'Internal')
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
  }
}
</script>

<style lang="scss" module>
.authentication-menu {
  @media (max-width: $breakpoint-sm-min) {
    width: 100%;
  }
}
</style>