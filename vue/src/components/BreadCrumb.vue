<template>
  <div class="breadcrumb row items-center">
    <div
      class="col-auto"
      v-for="(breadCrumb, i) in breadcrumbList"
      :key="i"
    >
      <div class="breadcrumb-items">
        <EssentialLink
          :title="breadCrumb.text"
          :icon="currentScreenSize !== 'xs' ? breadCrumb.icon : null"
          :icon-size="'25px'"
          :icon-padding="'0px 4px 0px 0px'"
          @click="router.push(breadCrumb.to)"
          :disabled="!breadCrumb.to"
          :dense="currentScreenSize == 'xs'"
        />

        <q-icon
          v-if="i !== (breadcrumbList.length - 1)"
          name="arrow_right"
          color="primary"
          size="25px"
        />
      </div>
    </div>

    <q-space class="divider" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import { storeToRefs } from 'pinia'
import { useGroupStore } from '@/stores/group-store'
import EssentialLink from '@/components/EssentialLink.vue'

const route = useRoute()
const router = useRouter()

// Composables
const { currentScreenSize } = useCurrentScreenSize()

// Store Data
const { groupDetails } = storeToRefs(useGroupStore())

// Local Data
const breadcrumbList = computed(() => {
  let breadCrumbs = [
    {
      text: 'Home',
      to: '/',
      icon: 'mdi-home'
    }
  ]

  // handle the breadcrumb generation for specific routes and their params
  // this list of routes should match all the routes in the routes.js file
  switch (route.name) {
    case 'accession':
      if (!route.params.jobId) {
        breadCrumbs = [
          ...breadCrumbs,
          { text: 'Accession' }
        ]
      } else {
        breadCrumbs = [
          ...breadCrumbs,
          {
            text: 'Accession',
            to: '/accession'
          },
          { text: `${route.params.jobId}` }
        ]
      }
      break
    case 'accession-container':
      breadCrumbs = [
        ...breadCrumbs,
        {
          text: 'Accession',
          to: '/accession'
        },
        {
          text: route.params.jobId,
          to: `/accession/${route.params.jobId}`
        },
        { text: route.params.containerId }
      ]
      break
    case 'admin-home':
      breadCrumbs = [
        ...breadCrumbs,
        { text: 'Admin' }
      ]
      break
    case 'admin-groups':
      if (!route.params.groupId) {
        breadCrumbs = [
          ...breadCrumbs,
          {
            text: 'Admin',
            to: '/admin'
          },
          { text: 'Groups & Permissions' }
        ]
      } else {
        breadCrumbs = [
          ...breadCrumbs,
          {
            text: 'Admin',
            to: '/admin'
          },
          {
            text: 'Groups & Permissions',
            to: '/admin/groups/'
          },
          { text: `${groupDetails.value.name}` }
        ]
      }
      break
    case 'admin-manage-media-type':
      breadCrumbs = [
        ...breadCrumbs,
        {
          text: 'Admin',
          to: '/admin'
        },
        { text: 'Manage Media Types' }
      ]
      break
    case 'admin-manage-owner':
      breadCrumbs = [
        ...breadCrumbs,
        {
          text: 'Admin',
          to: '/admin'
        },
        { text: 'Manage Owners' }
      ]
      break
    case 'admin-manage-size-class':
      breadCrumbs = [
        ...breadCrumbs,
        {
          text: 'Admin',
          to: '/admin'
        },
        { text: 'Manage Size Class' }
      ]
      break
    case 'admin-manage-shelf-type':
      breadCrumbs = [
        ...breadCrumbs,
        {
          text: 'Admin',
          to: '/admin'
        },
        { text: 'Manage Shelf Type' }
      ]
      break
    case 'admin-location-manage-buildings':
      breadCrumbs = [
        ...breadCrumbs,
        {
          text: 'Admin',
          to: '/admin'
        },
        { text: 'Manage Buildings' }
      ]
      break
    case 'admin-location-manage-modules':
      breadCrumbs = [
        ...breadCrumbs,
        {
          text: 'Admin',
          to: '/admin'
        },
        { text: 'Manage Modules' }
      ]
      break
    case 'admin-location-manage-aisles':
      breadCrumbs = [
        ...breadCrumbs,
        {
          text: 'Admin',
          to: '/admin'
        },
        { text: 'Manage Aisles' }
      ]
      break
    case 'admin-location-manage-ladders':
      breadCrumbs = [
        ...breadCrumbs,
        {
          text: 'Admin',
          to: '/admin'
        },
        { text: 'Manage Ladders' }
      ]
      break
    case 'admin-location-manage-shelves':
      breadCrumbs = [
        ...breadCrumbs,
        {
          text: 'Admin',
          to: '/admin'
        },
        { text: 'Manage Shelves' }
      ]
      break
    case 'picklist':
      if (!route.params.jobId) {
        breadCrumbs = [
          ...breadCrumbs,
          { text: 'Pick List' }
        ]
      } else {
        breadCrumbs = [
          ...breadCrumbs,
          {
            text: 'Pick List',
            to: '/picklist'
          },
          { text: `${route.params.jobId}` }
        ]
      }
      break
    case 'record-management-items':
      breadCrumbs = [
        ...breadCrumbs,
        { text: `Record Management - Tray/Non-Tray Item: ${route.params.barcode}` }
      ]
      break
    case 'record-management-shelf':
      breadCrumbs = [
        ...breadCrumbs,
        { text: `Record Management - Shelf: ${route.params.barcode}` }
      ]
      break
    case 'record-management-tray':
      breadCrumbs = [
        ...breadCrumbs,
        { text: `Record Management - Tray: ${route.params.barcode}` }
      ]
      break
    case 'refile':
      if (!route.params.jobId) {
        breadCrumbs = [
          ...breadCrumbs,
          { text: 'Refile' }
        ]
      } else {
        breadCrumbs = [
          ...breadCrumbs,
          {
            text: 'Refile',
            to: '/refile'
          },
          { text: `${route.params.jobId}` }
        ]
      }
      break
    case 'reports':
      breadCrumbs = [
        ...breadCrumbs,
        { text: 'Reports' }
      ]
      break
    case 'request':
      breadCrumbs = [
        ...breadCrumbs,
        { text: 'Request' }
      ]
      break
    case 'request-details':
      breadCrumbs = [
        ...breadCrumbs,
        {
          text: 'Request',
          to: '/request'
        },
        { text: `Request Item: ${route.params.jobId}` }
      ]
      break
    case 'request-batch':
      breadCrumbs = [
        ...breadCrumbs,
        {
          text: 'Request',
          to: '/request'
        },
        { text: `Request Batch: ${route.params.jobId}` }
      ]
      break
    case 'search-results':
      breadCrumbs = [
        ...breadCrumbs,
        { text: `Advanced Search Results: ${route.params.searchType}` }
      ]
      break
    case 'shelving':
      if (!route.params.jobId) {
        breadCrumbs = [
          ...breadCrumbs,
          { text: 'Shelving' }
        ]
      } else {
        breadCrumbs = [
          ...breadCrumbs,
          {
            text: 'Shelving',
            to: '/shelving'
          },
          { text: `${route.params.jobId}` }
        ]
      }
      break
    case 'shelving-dts':
      breadCrumbs = [
        ...breadCrumbs,
        {
          text: 'Shelving',
          to: '/shelving'
        },
        { text: `${route.params.jobId}` }
      ]
      break
    case 'shelving-move':
      breadCrumbs = [
        ...breadCrumbs,
        {
          text: 'Shelving',
          to: '/shelving'
        },
        { text: `Move ${route.params.type == 'tray-non-tray' ? 'Tray/Non-Tray' : 'Tray Item'}` }
      ]
      break
    case 'verification':
      if (!route.params.jobId) {
        breadCrumbs = [
          ...breadCrumbs,
          { text: 'Verification' }
        ]
      } else {
        breadCrumbs = [
          ...breadCrumbs,
          {
            text: 'Verification',
            to: '/verification'
          },
          { text: `${route.params.jobId}` }
        ]
      }
      break
    case 'verification-container':
      breadCrumbs = [
        ...breadCrumbs,
        {
          text: 'Verification',
          to: '/verification'
        },
        {
          text: route.params.jobId,
          to: `/verification/${route.params.jobId}`
        },
        { text: route.params.containerId }
      ]
      break
    case 'withdrawal':
      if (!route.params.jobId) {
        breadCrumbs = [
          ...breadCrumbs,
          { text: 'Withdrawal' }
        ]
      } else {
        breadCrumbs = [
          ...breadCrumbs,
          {
            text: 'Withdrawal',
            to: '/withdrawal'
          },
          { text: `${route.params.jobId}` }
        ]
      }
      break
    default:
      break
  }

  return breadCrumbs
})
</script>

<style lang="scss" scoped>
.breadcrumb {
  position: sticky;
  top: 50px; // this offsets the main nav
  background-color: $color-white;
  z-index: 1500;

  &-items {
    display: flex;
    align-items: center;

    :deep(.essential-link.q-item--dense) {
      padding: 2px 3px;
    }
  }
}
</style>
