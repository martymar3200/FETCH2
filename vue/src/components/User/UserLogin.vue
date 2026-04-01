<template>
  <BaseButton
    no-caps
    flat
    dense
    class="text-body1 authentication"
    color="primary"
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
          <BaseButton
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
          <BaseButton
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
  </BaseButton>
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
import inventoryServiceApi from '@/http/InventoryService.js'
import { ref, computed, onMounted } from 'vue'
import { Notify } from 'quasar'
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


onMounted(async () => {
  // when a user is using sso login they will get redirected back to the app in a logged out state.
  // The backend will have set an HttpOnly secure cookie containing the authentication JWT.
  if (route.query.preserve_route) {
    appActionIsLoadingData.value = true
    try {
      // 1. Trigger the store to fetch the profile using the cookie
      await patchLogin({}, 'Sso')
      // 2. send the user to the preserved route
      router.push(route.query.preserve_route)
    } catch (e) {
      console.error('SSO Cookie Authentication failed', e)
    } finally {
      appActionIsLoadingData.value = false
    }
  } else {
    // If there's no specific route to preserve, check if we just logged in via SSO
    // and arrived at the root page. Let the global navigation guards or app mount
    // handle the profile fetch if needed. We no longer parse route tokens here.
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

    // Refresh to allow the app to boot with the new cookie
    window.location.reload()
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Login failed'
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