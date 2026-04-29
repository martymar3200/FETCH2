<template>
  <q-page
    :style-fn="handlePageOffset"
    padding
  >
    <div class="row">
      <div class="col-12">
        <h1 class="text-h4 text-primary q-mb-md">
          List of Owner Tiers
        </h1>
        <ul class="owner-list">
          <div
            v-if="loadingData"
            class="overlay"
          >
            <q-spinner-bars
              color="primary"
              size="2rem"
              class="overlay-loading"
            />
          </div>

          <li v-if="ownerTierOptions && ownerTierOptions.length == 0 && !loadingData">
            No Owner Tiers found...
          </li>
          <li
            v-for="(data, i) in ownerTierOptions"
            :key="i"
            :class="data.storedOffline ? 'text-negative' : ''"
          >
            {{ data.storedOffline ? `${data.name} (stored offline)` : data.name }}
          </li>
        </ul>
      </div>

      <div class="col-12 q-mt-md">
        <BaseButton
          no-caps
          unelevated
          class="text-body1 q-mr-sm"
          color="primary"
          label="Create New Owner Tier"
          @click="showOwnerTierCreation = !showOwnerTierCreation"
        />

        <BaseButton
          no-caps
          unelevated
          class="text-body1"
          color="primary"
          label="Save Owner Tier List For Offline Use"
          @click="saveOwnerTierList"
        />
      </div>
    </div>

    <q-space class="divider q-my-xs-lg q-my-sm-xl" />

    <div class="row">
      <div class="col-12">
        <h1 class="text-h4 text-primary q-mb-md">
          Pagination Example
        </h1>

        <div class="row no-wrap items-center q-mt-md">
          <div class="col-4">
            <SelectInput
              v-model="shelfInput"
              :options="shelves"
              option-type="shelves"
              option-value="id"
              option-label="id"
              :placeholder="'Select Shelve'"
              aria-label="shelfSelect"
            />
          </div>
        </div>

        <div class="row no-wrap items-center q-mt-md">
          <div class="col-4">
            <SelectInput
              v-model="ownerInput"
              :options="owners"
              option-type="owners"
              option-value="id"
              option-label="name"
              :placeholder="'Select Owner'"
              aria-label="ownerSelect"
            />
          </div>
        </div>

        <div class="row no-wrap items-center q-mt-lg">
          <div class="col-grow q-mb-xs-md q-mb-sm-none">
            <EssentialTable
              :table-columns="shelfTableColumns"
              :table-data="shelves"
              :enable-table-reorder="false"
              :hide-table-rearrange="true"
              :enable-pagination="true"
              :pagination-total="optionsTotal"
              :pagination-loading="tableLoading"
              @update-pagination="loadShelves($event)"
            >
              <template #heading-row>
                <div
                  class="col-sm-5 col-md-12 col-lg-auto"
                  :class="'self-center'"
                >
                  <h1 class="text-h4 text-bold q-mb-lg">
                    Shelves List
                  </h1>
                </div>
              </template>
            </EssentialTable>
          </div>
        </div>
      </div>

      <q-space class="divider q-my-xs-lg q-my-sm-xl" />

      <div class="row">
        <div class="col-12">
          <h1 class="text-h4 text-primary q-mb-md">
            Alert Examples
          </h1>

          <div class="row no-wrap items-center q-mt-md">
            <BaseButton
              no-caps
              outline
              color="negative"
              label="Show Generic Alert"
              class="text-body1 q-mr-sm"
              @click="generateTestAlert(1)"
            />

            <BaseButton
              unelevated
              no-caps
              color="negative"
              label="Show Persistent Alert"
              class="text-body1"
              @click="generateTestAlert(2)"
            />
          </div>
        </div>
      </div>

      <q-space class="divider q-my-xs-lg q-my-sm-xl" />

      <div class="row">
        <div class="col-12">
          <h1 class="text-h4 text-primary q-mb-md">
            File System Access Api Examples
          </h1>
        </div>

        <div class="col-12 q-mb-sm">
          <textarea
            name="text-contents"
            id="text-contents"
            cols="30"
            rows="4"
            placeholder="Select a file to read it contents here or add some text and save a new text file to your system..."
            v-model="fileContent"
          />
        </div>

        <div class="col-12">
          <BaseButton
            no-caps
            unelevated
            color="primary"
            label="Select & Read Text File"
            class="text-body1 q-mr-sm"
            @click="selectTextFile()"
          />

          <BaseButton
            no-caps
            unelevated
            color="primary"
            label="Save To Selected Text File"
            class="text-body1 q-mr-sm"
            :disabled="fileReference == null"
            @click="saveChangesToText"
          />

          <BaseButton
            no-caps
            unelevated
            color="primary"
            label="Save New Text File"
            class="text-body1 q-mr-sm"
            :disabled="fileContent == null || fileContent == ''"
            @click="saveTextFileToDevice"
          />

          <BaseButton
            unelevated
            no-caps
            color="negative"
            label="Reset Content"
            class="text-body1"
            :disabled="fileContent == null || fileContent == ''"
            @click="fileContent = null; fileReference = null;"
          />
        </div>
      </div>

      <!-- owner tier create modal -->
      <PopupModal
        v-if="showOwnerTierCreation"
        title="Create A New Owner Tier"
        @reset="reset"
        aria-label="newOwnerTierModal"
      >
        <template #main-content>
          <q-card-section
            class="column no-wrap items-center"
          >
            <TextInput
              v-model="newOwnerTier"
              :placeholder="'Enter Owner Tier Name'"
            />
          </q-card-section>
        </template>

        <template #footer-content="{ hideModal }">
          <q-card-section class="row no-wrap justify-between items-center">
            <BaseButton
              no-caps
              unelevated
              color="accent"
              label="Confirm"
              class="text-body1 full-width"
              @click="createNewOwnerTier(); hideModal();"
            />

            <q-space class="q-mx-xs" />

            <BaseButton
              outline
              no-caps
              label="Cancel"
              class="text-body1 full-width"
              @click="hideModal"
            />
          </q-card-section>
        </template>
      </PopupModal>
    </div>
  </q-page>
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
import { storeToRefs } from 'pinia'
import { useOptionStore } from '@/stores/option-store'
import { useGlobalStore } from '@/stores/global-store'
import { ref, toRaw, onMounted, watch, inject } from 'vue'
import { notify } from '@/utils/notify'
import { useIndexDbHandler } from '@/composables/useIndexDbHandler.js'
import { useFileSystemAccessHandler } from '@/composables/useFileSystemAccessHandler.js'
import PopupModal from '@/components/PopupModal.vue'
import TextInput from '@/components/TextInput.vue'
import SelectInput from '@/components/SelectInput.vue'
import EssentialTable from '@/components/EssentialTable.vue'

// Composables
const { indexDb, getDataInIndexDb, addDataToIndexDb } = useIndexDbHandler()
const { fileReference, fileContent, selectTextFile, saveAsTextFile, updateTextFile } = useFileSystemAccessHandler()

// Store Data
const { getOwnerTierList, postOwnerTier, getOptions } = useOptionStore()
const { ownerTierOptions, shelves, owners, optionsTotal } = storeToRefs(useOptionStore())
const { appIsOffline } = storeToRefs(useGlobalStore())

// Local Data
const loadingData = ref(true)
const tableLoading = ref(false)
const showOwnerTierCreation = ref(false)
const newOwnerTier = ref('')
const shelfInput = ref(null)
const ownerInput = ref(null)
const shelfTableColumns = ref([
  {
    name: 'id',
    field: 'id',
    label: 'Shelf Number',
    align: 'left',
    sortable: true
  },
  {
    name: 'location',
    field: 'location',
    label: 'Shelf Location',
    align: 'left',
    sortable: true
  }
])

// Logic
const handlePageOffset = inject('handle-page-offset')

onMounted(async () => {
  await getOwnerTierList()
  await loadShelves()
  loadingData.value = false

  // when user comes back online we listen for the stored owner api calls to sync and update the ownerTiers
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.addEventListener('message', event => {
      if (event.data.url && event.data.url.includes('/owners/tiers')) {
        updateOwnerTierList(event.data.response)
      }
    })
  }
})

watch(indexDb, async () => {
  // watch for indexDb to get mounted in the compasable then get the data we need
  await getDataInIndexDb('ownerTiers')
})

watch(appIsOffline, async () => {
  if (appIsOffline.value && ownerTierOptions.value.length == 0) {
    // get saved indexDb ownerTierOptions if were offline and page was reloaded/refreshed
    const res = await getDataInIndexDb('ownerTiers')
    ownerTierOptions.value = res.data
  }
})

const reset = () => {
  showOwnerTierCreation.value = false
  newOwnerTier.value = ''
}

const createNewOwnerTier = async () => {
  try {
    const payload = {
      name: newOwnerTier.value,
      level: ownerTierOptions.value[ownerTierOptions.value.length - 1].level + 1
    }
    await postOwnerTier(payload)

    if (!window.navigator.onLine) {
      ownerTierOptions.value = [
        ...ownerTierOptions.value,
        {
          ...payload,
          storedOffline: true
        }
      ]
    }
  } catch (error) {
    console.log(error)
  } finally {
    reset()
  }
}
const updateOwnerTierList = (requestdata) => {
  ownerTierOptions.value = [
    ...ownerTierOptions.value,
    requestdata
  ].filter(tier => !tier.storedOffline)
}
const saveOwnerTierList = async () => {
  try {
    await addDataToIndexDb('ownerTiers', toRaw(ownerTierOptions.value))

    notify({
      type: 'positive',
      message: 'Owner Tiers saved for offline usage'
    })
  } catch (err) {
    notify({
      type: 'negative',
      message: err.message || 'Error saving Owner Tiers'
    })
  }
}

const loadShelves = async (qParams) => {
  try {
    tableLoading.value = true
    await getOptions('shelves', { ...qParams })
  } catch (err) {
    notify({
      type: 'negative',
      message: err.message || 'Error loading shelves'
    })
  } finally {
    tableLoading.value = false
  }
}


const generateTestAlert = (val) => {
  if (val == 1) {
    notify({
      type: 'negative',
      message: 'This is a user generated error message'
    })
  } else {
    notify({
      type: 'negative',
      message: 'This is a user generated error message with audio',
      timeout: 0,
      actions: [
        {
          label: 'Dismiss',
          color: 'white',
          handler: () => { /* ... */ }
        }
      ]
    })
  }
}

const saveTextFileToDevice = async () => {
  await saveAsTextFile(fileContent.value)

  await saveAsTextFile(fileContent.value)

  notify({
    type: 'positive',
    message: 'A new text file has been saved to the device'
  })
}
const saveChangesToText = async () => {
  await updateTextFile(fileContent.value)

  await updateTextFile(fileContent.value)

  notify({
    type: 'positive',
    message: 'The selected text file has been updated on the device'
  })
}
</script>

<style lang="scss" scoped>
.owner-list {
  position: relative;
  display: inline-block;
  width: max-content;
  min-width: 300px;
  min-height: 60px;
  list-style: none;
  background-color: $color-gray;
  border-radius: 4px;
  padding: 0.5rem;
}

.test-modal {
  width: 500px;
}
</style>
