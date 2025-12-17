<template>
  <div class="alert row items-center">
    <!-- regular alert notifications -->
    <q-banner
      v-if="!mainProps.persistent"
      inline-actions
      dense
      rounded
      class="alert-banner"
      :class="renderAlertType"
    >
      <p
        id="alertText"
        class="text-body1"
        v-html="mainProps.alertText"
      />
      <template #action>
        <q-btn
          icon="close"
          flat
          round
          dense
          aria-label="dismissAlert"
          @click="emit('reset')"
        />
      </template>
    </q-banner>

    <!-- hard alert notifications (user is required to acknowledge always is an error) -->
    <q-dialog
      v-else
      v-model="showAlertModal"
      persistent
      @hide="emit('reset')"
      aria-label="alertModal"
    >
      <q-card
        class="alert-modal"
      >
        <q-card-section class="column items-center text-negative">
          <q-icon
            :name="'error'"
            size="150px"
          />

          <p
            id="alertText"
            class="text-body1"
            v-html="mainProps.alertText"
          />
        </q-card-section>

        <q-card-section
          class="row items-center"
        >
          <q-btn
            outline
            no-caps
            label="Cancel"
            color="negative"
            class="text-body1 full-width"
            aria-label="dismissAlert"
            @click="showAlertModal = false"
          />
        </q-card-section>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ref, computed, onMounted, nextTick, inject } from 'vue'

const router = useRouter()

// Props
const mainProps = defineProps({
  alertType: {
    type: String,
    default: 'error',
    required: true
  },
  alertText: {
    type: null,
    default: 'some alert text',
    required: true
  },
  persistent: {
    type: Boolean,
    default: false
  },
  autoClose: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['reset'])

// Local Data
const showAlertModal = ref(false)
const renderAlertType = computed(() => {
  // defined style classes based on the passed in alert type
  if (mainProps.alertType == 'success') {
    return [
      'text-positive',
      'bg-color-green-light'
    ]
  } else {
    return [
      'text-negative',
      'bg-color-pink'
    ]
  }
})

// Logic
const audioAlert = inject('audio-alert')

onMounted(async () => {
  if (mainProps.persistent) {
    showAlertModal.value = true
    audioAlert()
  }

  // check if text contains a local link and convert them to router events
  await nextTick()
  checkForRouteLinks()
})

const checkForRouteLinks = () => {
  // if prop text contains a anchor tag with an href that matches the hostname we can assume that anchor tag is meant to be a route link
  const textLinks = document.querySelector('#alertText').getElementsByTagName('a')

  Array.from(textLinks).forEach(link => {
    if (link.hostname == window.location.hostname) {
      link.onclick = (event => {
        event.preventDefault()
        router.push(link.pathname + link.search)
        emit('reset')
      })
    }
  })
}
</script>

<style lang="scss" scoped>
.alert {
  &-banner {
    position: relative;
    top: 1rem;
    left: 50%;
    transform: translateX(-50%);
    width: 96%;
    z-index: 2000;
    box-shadow: rgba(0, 0, 0, 0.1) 0px 4px 6px -1px, rgba(0, 0, 0, 0.06) 0px 2px 4px -1px;
    transition: all .3s ease-in-out;
  }

  &-modal {
    min-width: 250px;
    max-width: 500px;
    z-index: 2000;

    @media (max-width: $breakpoint-sm-min) {
      width: 90vw;
    }
  }
}
</style>