<template>
  <div class="search-bar">
    <q-btn
      dense
      no-caps
      unelevated
      icon-right="arrow_drop_down"
      :label="searchType"
      aria-label="searchBarMenu"
      aria-haspopup="menu"
      :aria-expanded="searchMenuState"
      class="search-bar-menu text-body2 text-secondary"
    >
      <q-menu
        @show="searchMenuState = true"
        @hide="searchMenuState = false"
        aria-label="searchMenuList"
      >
        <q-list>
          <q-item
            v-for="obj in searchTypes"
            :key="obj.name"
            clickable
            v-close-popup
            @click="searchType = obj.name"
            role="menuitem"
          >
            <q-item-section>
              <q-item-label>
                <span>
                  {{ obj.name }}
                </span>
              </q-item-label>
            </q-item-section>
          </q-item>
        </q-list>
      </q-menu>
    </q-btn>

    <div class="row full-width">
      <q-input
        class="search-bar-input"
        dense
        borderless
        v-model="searchText"
        :placeholder="renderSearchPlaceholder"
        @keyup.enter="executeExactSearch()"
        aria-label="SearchBar"
        type="search"
      >
        <template #append>
          <q-spinner
            v-if="searchIsLoadingData"
            color="secondary"
            size="24px"
          />
          <q-icon
            v-else-if="currentScreenSize == 'xs' && !searchIsLoadingData && searchText === ''"
            name="search"
            @click="$event.target.closest('div.q-field__control').querySelector('input').focus()"
          />
          <q-icon
            v-else-if="!searchIsLoadingData && searchText !== ''"
            name="clear"
            role="img"
            aria-label="clearSearch"
            class="cursor-pointer"
            @click="searchText = ''"
          />
        </template>
      </q-input>

      <!-- exact search results menu -->
      <div class="col-12">
        <q-menu
          fit
          v-model="showExactSearch"
          :class="$style['search-input-menu']"
        >
          <q-list
            class="search-results-list"
          >
            <q-item
              clickable
              v-close-popup
              @click="exactSearchResponseInfo !== null ? handlingSearchResultRouting() : null"
              role="menuitem"
            >
              <!-- ====================================================== -->
              <!-- ============ START: LOCATION DISPLAY CODE ============ -->
              <!-- ====================================================== -->
              <q-item-section v-if="exactSearchResponseInfo">
                <!-- For Item searches, show Barcode, Status, and Location -->
                <template v-if="searchType === 'Item'">
                  <q-item-label>{{ exactSearchResponseInfo.barcode.value }}</q-item-label>
                  <q-item-label>
                    Status: {{ exactSearchResponseInfo.status }}
                  </q-item-label>
                  <q-item-label>
                    <!-- This line checks if the item has a .tray property. If so, it passes the tray to the helper. If not, it passes the item itself. -->
                    Location: {{ getItemLocation(exactSearchResponseInfo.tray ? exactSearchResponseInfo.tray : exactSearchResponseInfo) || 'N/A' }}
                  </q-item-label>
                </template>
                <!-- For other search types, keep the original display -->
                <template v-else>
                  {{ searchResults[0] }}
                </template>
              </q-item-section>

              <!-- Show a "not found" message if the search returned no results -->
              <q-item-section v-else>
                <q-item-label class="text-grey">
                  No results found.
                </q-item-label>
              </q-item-section>
              <!-- ====================================================== -->
              <!-- ============= END: LOCATION DISPLAY CODE ============= -->
              <!-- ====================================================== -->
            </q-item>
          </q-list>
        </q-menu>
      </div>
    </div>

    <template v-if="currentScreenSize !== 'xs'">
      <q-btn
        dense
        no-caps
        flat
        color="secondary"
        icon="search"
        class="search-bar-action btn-no-wrap text-body2"
        @click="executeExactSearch()"
        aria-label="exactSearchButton"
      />
      <q-btn
        dense
        no-caps
        flat
        color="secondary"
        label="Advanced Search"
        class="search-bar-advanced btn-no-wrap text-body2"
        :disabled="!searchType"
        @click="showAdvancedSearchModal = true"
        aria-label="advancedSearchButton"
      />
    </template>
    <q-btn
      v-else
      dense
      no-caps
      flat
      color="secondary"
      icon="tune"
      class="search-bar-advanced btn-no-wrap text-body2"
      :disabled="!searchType"
      @click="showAdvancedSearchModal = true"
      aria-label="advancedSearchButton"
    />
  </div>

  <!-- Generate Advance Search Modal -->
  <SearchAdvancedModal
    v-if="showAdvancedSearchModal"
    :search-type="searchType"
    :search-bar-input="searchText"
    @hide="showAdvancedSearchModal = false"
  />

  <!-- Item Overlay (quick view for item exact searchs)-->
  <ItemDataOverlay
    v-if="showItemQuickView"
    :item-data="itemDetails"
    @close="showItemQuickView = false"
    @update="router.push({
      name: 'record-management-items',
      params: {
        barcode: searchText
      }
    })"
  />
</template>

<script setup>
import { onMounted, ref, watch, inject, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import { useSearchStore } from '@/stores/search-store'
import { useRecordManagementStore } from '@/stores/record-management-store'
import { useAccessionStore } from '@/stores/accession-store'
import { useVerificationStore } from '@/stores/verification-store'
import { useShelvingStore } from '@/stores/shelving-store'
import { useRequestStore } from '@/stores/request-store'
import { usePicklistStore } from '@/stores/picklist-store'
import { useRefileStore } from '@/stores/refile-store'
import { useWithdrawalStore } from '@/stores/withdrawal-store'
import { storeToRefs } from 'pinia'
import SearchAdvancedModal from '@/components/Search/SearchAdvancedModal.vue'
import ItemDataOverlay from '@/components/RecordManagement/ItemDataOverlay.vue'

const route = useRoute()
const router = useRouter()

// Composables
const { currentScreenSize } = useCurrentScreenSize()
const { checkUserPermission } = usePermissionHandler()

// Store Data
const { getExactSearchResult } = useSearchStore()
const { searchResults } = storeToRefs(useSearchStore())
const {
  itemDetails,
  trayDetails,
  shelfDetails
} = storeToRefs(useRecordManagementStore())
const { accessionJob } = storeToRefs(useAccessionStore())
const { verificationJob } = storeToRefs(useVerificationStore())
const { shelvingJob } = storeToRefs(useShelvingStore())
const { requestJob } = storeToRefs(useRequestStore())
const { picklistJob } = storeToRefs(usePicklistStore())
const { refileJob } = storeToRefs(useRefileStore())
const { withdrawJob } = storeToRefs(useWithdrawalStore())

// Local Data
const searchIsLoadingData = ref(false)
const searchMenuState = ref(false)
const renderSearchPlaceholder = computed(() => {
  let placeholderText = 'Search'
  if (searchType.value == 'Item' || searchType.value == 'Tray' || searchType.value == 'Shelf') {
    placeholderText = `Search ${searchType.value} Barcode`
  } else {
    placeholderText = 'Search Job Number'
  }
  return placeholderText
})
const searchText = ref('')
const searchType = ref('')
const searchTypes = computed(() => {
  let searchList = [
    {
      name: 'Item',
      hidden: !checkUserPermission('can_access_item_detail')
    },
    {
      name: 'Tray',
      hidden: !checkUserPermission('can_access_tray_detail')
    },
    {
      name: 'Shelf',
      hidden: !checkUserPermission('can_access_shelf_detail')
    },
    {
      name: 'Accession',
      hidden: !checkUserPermission('can_access_accession')
    },
    {
      name: 'Verification',
      hidden: !checkUserPermission('can_access_verification')
    },
    {
      name: 'Shelving',
      hidden: !checkUserPermission('can_access_shelving')
    },
    {
      name: 'Request',
      hidden: !checkUserPermission('can_access_request')
    },
    {
      name: 'Picklist',
      hidden: !checkUserPermission('can_access_picklist')
    },
    {
      name: 'Refile',
      hidden: !checkUserPermission('can_access_refile')
    },
    {
      name: 'Withdraw',
      hidden: !checkUserPermission('can_access_withdraw')
    }
  ]
  return searchList.filter(i => !i.hidden)
})
const showExactSearch = ref(false)
const showAdvancedSearchModal = ref(false)
const exactSearchResponseInfo = ref(null)
const showItemQuickView = ref(false)

// Logic
const handleAlert = inject('handle-alert')
// ======================================================
// ============ START: LOCATION DISPLAY CODE ============
// ======================================================
const getItemLocation = inject('get-item-location')
// ======================================================
// ============= END: LOCATION DISPLAY CODE =============
// ======================================================

onMounted(() => {
  setSearchType()
})
watch(route, () => {
  setSearchType()
})

const setSearchType = () => {
  // set the search type based on matching route if search type for that route exists
  const routeMatchingSearchType = searchTypes.value.find(typ => route.name.includes(typ.name.toLowerCase()) )?.name
  if (routeMatchingSearchType) {
    searchType.value = routeMatchingSearchType
  }
}

const executeExactSearch = async () => {
  try {
    exactSearchResponseInfo.value = null
    searchIsLoadingData.value = true
    const res = await getExactSearchResult(searchText.value, searchType.value)
    if (res) {
      exactSearchResponseInfo.value = res.data
    }
    showExactSearch.value = true
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    searchIsLoadingData.value = false
  }
}
const handlingSearchResultRouting = () => {
  // load the exact search result route depending on search type and assign info to matching job store if needed
  switch (searchType.value) {
    case 'Item':
      itemDetails.value = exactSearchResponseInfo.value
      showItemQuickView.value = true
      break
    case 'Tray':
      trayDetails.value = exactSearchResponseInfo.value
      router.push({
        name: 'record-management-tray',
        params: {
          barcode: searchText.value
        }
      })
      break
    case 'Shelf':
      shelfDetails.value = exactSearchResponseInfo.value
      router.push({
        name: 'record-management-shelf',
        params: {
          barcode: searchText.value
        }
      })
      break
    case 'Accession':
      accessionJob.value = exactSearchResponseInfo.value
      router.push({
        name: searchType.value.toLowerCase(),
        params: {
          jobId: searchText.value
        }
      })
      break
    case 'Verification':
      verificationJob.value = exactSearchResponseInfo.value
      router.push({
        name: searchType.value.toLowerCase(),
        params: {
          jobId: searchText.value
        }
      })
      break
    case 'Shelving':
      shelvingJob.value = exactSearchResponseInfo.value
      // Route based on job origin type
      if (exactSearchResponseInfo.value.origin === 'Direct') {
        // Direct to Shelf jobs use the modernized ShelvingDirectExecute component
        router.push({
          name: 'shelving-dts',
          params: {
            jobId: searchText.value
          }
        })
      } else if (exactSearchResponseInfo.value.origin === 'List') {
        // Shelve by List jobs use their dedicated component
        router.push({
          name: 'ShelveByListExecute',
          params: {
            id: searchText.value
          }
        })
      } else if (exactSearchResponseInfo.value.origin === 'Move') {
        // Handle Move jobs
        const moveType = exactSearchResponseInfo.value.mode === 'MoveTrayItem' ? 'tray-item' : 'tray-non-tray'
        router.push({
          name: 'shelving-move',
          params: {
            type: moveType,
            jobId: searchText.value
          }
        })
      } else {
        // Verification-origin jobs are deprecated - redirect to dashboard
        router.push({ name: 'shelving' })
      }
      break
    case 'Request':
      requestJob.value = exactSearchResponseInfo.value
      router.push({
        name: 'request-details',
        params: {
          jobId: searchText.value
        }
      })
      break
    case 'Picklist':
      picklistJob.value = exactSearchResponseInfo.value
      router.push({
        name: searchType.value.toLowerCase(),
        params: {
          jobId: searchText.value
        }
      })
      break
    case 'Refile':
      refileJob.value = exactSearchResponseInfo.value
      router.push({
        name: searchType.value.toLowerCase(),
        params: {
          jobId: searchText.value
        }
      })
      break
    case 'Withdraw':
      withdrawJob.value = exactSearchResponseInfo.value
      router.push({
        name: 'withdrawal',
        params: {
          jobId: searchText.value
        }
      })
      break
    default:
      break
  }
}
</script>

<style lang="scss" scoped>
.search-bar {
  position: relative;
  display: flex;
  flex-flow: row nowrap;
  width: 100%;
  height: 40px;
  background-color: $color-white;
  border-radius: 3px;
  overflow: hidden;

  @media (max-width: $breakpoint-sm-min) {
    height: 32px;
  }

  &-menu {
    min-width: 110px;
    min-height: 100%;
    border-right: 2px solid $secondary;
    border-top-right-radius: 0;
    border-bottom-right-radius: 0;
    :deep(.q-btn__content) {
      flex-wrap: nowrap;
    }

    @media (max-width: $breakpoint-sm-min) {
      min-width: initial;
    }
  }

  &-input {
    width: 100%;
    height: 100%;
    padding: 0 10px;

    :deep(input) {
      color: $secondary;
    }

    :deep(i) {
      color: $secondary;
    }

    @media (max-width: $breakpoint-sm-min) {
      :deep(.q-field__control) {
        height: 100%;
      }

      :deep(.q-field__marginal) {
        height: 100%;
      }
    }
  }

  &-action {
    border-right: 2px solid $secondary;
    border-radius: 0;
  }

  &-advanced {
    padding-left: 8px;
    padding-right: 8px;
    border-top-left-radius: 0;
    border-bottom-left-radius: 0;
  }
}
</style>

<style lang="scss" module>
.search-input-menu {
  // forces max width to not expand past the parent width
  max-width: 10px !important;
  @media (max-width: $breakpoint-sm) {
    width: 100%;
    max-width: 100% !important;
    top: 50px !important;
    left: 0 !important;
  }
}
</style>