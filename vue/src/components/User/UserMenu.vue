<template>
  <q-btn
    no-caps
    flat
    dense
    round
    icon="person"
    color="white"
    class="user-avatar"
    aria-label="UserMenu"
    aria-haspopup="menu"
    :aria-expanded="userMenuState"
  >
    <q-menu
      class="user-menu"
      :offset="[11, 9]"
      @show="userMenuState = true"
      @hide="userMenuState = false"
      aria-label="userMenuList"
    >
      <q-list style="min-width: 200px">
        <q-item
          class="q-pa-none"
          role="menuitem"
        >
          <q-item
            tag="label"
            v-ripple
            class="full-width"
            role=""
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
                aria-label="barcodeToggle"
              />
            </q-item-section>
          </q-item>
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
  </q-btn>
</template>

<script setup>
import { ref, inject } from 'vue'
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

// Logic
const handleAlert = inject('handle-alert')

const handleOptions = (option) => {
  if (option == 'Logout') {
    logoutUser()
  }
}
const logoutUser = async () => {
  try {
    await patchLogout()

    //reload the route to trigger any route gaurds if the user is on an auth based page
    router.go()
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
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
  }
}
</style>