<template>
  <BaseButton
    ref="userAvatarBtnRef"
    no-caps
    flat
    dense
    round
    icon="person"
    color="primary"
    class="user-avatar"
    aria-label="User profile menu"
    aria-haspopup="menu"
    :aria-expanded="userMenuState"
  >
    <q-menu
      class="user-menu"
      :offset="[11, 9]"
      @show="userMenuState = true"
      @hide="onUserMenuHide"
      aria-label="userMenuList"
    >
      <q-list
        style="min-width: 200px"
        role="none"
      >
        <q-item
          tag="label"
          v-ripple
          class="full-width cursor-pointer"
          role="menuitemcheckbox"
          :aria-checked="barcodeScanAllowed"
          aria-label="Toggle barcode scanning"
          tabindex="0"
          @keydown.space.prevent="barcodeScanAllowed = !barcodeScanAllowed"
          @keydown.enter.prevent="barcodeScanAllowed = !barcodeScanAllowed"
        >
          <q-item-section>
            <q-item-label class="text-body1 text-nowrap">
              Toggle Barcode Scan
            </q-item-label>
          </q-item-section>
          <q-item-section side>
            <q-toggle
              name="barcode_scan_active"
              v-model="barcodeScanAllowed"
              tabindex="-1"
            />
          </q-item-section>
        </q-item>
        <q-item
          dense
          class="items-center q-pb-sm"
          role="menuitem"
        >
          <div class="col-8">
            <p class="text-body2 text-color-gray-dark">
              barcode input delay (seconds)
            </p>
          </div>
          <div class="col-4">
            <TextInput
              dense
              type="number"
              v-model="barcodeInputDelay"
              :disabled="barcodeScanAllowed"
              aria-label="barcodeInputDelay"
            />
          </div>
        </q-item>
        <q-space class="divider" />
        <q-item role="menuitem">
          <q-item-section>
            <h1 class="text-h6">
              {{ userData.first_name }} {{ userData.last_name }}
            </h1>
            <p class="text-body2 text-color-gray-dark">
              {{ userData.email }}
            </p>
          </q-item-section>
        </q-item>
        <q-space class="divider" />
        <q-item
          v-for="(opt, i) in userOptions"
          :key="i"
          clickable
          v-close-popup
          @click="handleOptions(opt.text)"
          role="menuitem"
        >
          <q-item-section>
            <q-item-label class="flex items-center text-body1">
              <q-icon
                v-if="opt.icon"
                color="secondary"
                :name="opt.icon"
                class="q-mr-sm"
                size="20px"
              />
              {{ opt.text }}
            </q-item-label>
          </q-item-section>
        </q-item>
      </q-list>
    </q-menu>
  </BaseButton>
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
import { ref } from 'vue'
import { notify } from '@/utils/notify'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user-store'
import { useBarcodeStore } from '@/stores/barcode-store'
import { storeToRefs } from 'pinia'
import TextInput from '@/components/TextInput.vue'

const router = useRouter()

// Store Data
const { userData } = storeToRefs(useUserStore())
const { patchLogout } = useUserStore()
const { barcodeScanAllowed, barcodeInputDelay } = storeToRefs(useBarcodeStore())

// Local Data
const userAvatarBtnRef = ref(null)
const userMenuState = ref(false)
const userOptions = ref([
  {
    text: 'Settings',
    icon: 'settings'
  },
  {
    text: 'Logout',
    icon: 'logout'
  }
])

const onUserMenuHide = () => {
  userMenuState.value = false
  if (userAvatarBtnRef.value) {
    const el = userAvatarBtnRef.value.$el || userAvatarBtnRef.value
    if (el && typeof el.focus === 'function') {
      el.focus()
    }
  }
}

// Logic


const handleOptions = (option) => {
  if (option == 'Logout') {
    logoutUser()
  } else if (option == 'Settings') {
    router.push({ name: 'user-settings' })
  }
}
const logoutUser = async () => {
  try {
    await patchLogout()

    //reload the route to trigger any route gaurds if the user is on an auth based page
    router.go()
  } catch (error) {
    notify({
      type: 'negative',
      message: error.response?.data?.detail || error.message || 'Logout failed'
    })
  }
}
</script>

<style lang="scss" scoped>
.user {
  &-avatar {
    &:hover {
      cursor: pointer;
    }

    @media (max-width: 599px) {
      width: 32px;
      height: 32px;
      font-size: 10px;

      :deep(.q-icon) {
        font-size: 20px;
      }
    }
  }
}
</style>