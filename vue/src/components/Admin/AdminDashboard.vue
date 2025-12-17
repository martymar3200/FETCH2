<template>
  <div class="admin-dashboard">
    <div class="row q-mb-md">
      <div class="col-auto">
        <h1 class="text-h4 text-bold">
          Admin Dashboard
        </h1>
      </div>
    </div>

    <div class="row">
      <div class="col-12">
        <q-list>
          <template
            v-for="link in adminLinkList"
            :key="link.title"
          >
            <EssentialLink
              v-if="link.link"
              v-bind="link"
              icon-right="chevron_right"
              icon-right-size="28px"
              class="text-h6 text-bold q-px-xs-none q-px-sm-sm"
            />
            <q-expansion-item
              v-else
              class="admin-dashboard-expansion"
              header-class="text-h6 text-bold q-px-xs-none q-px-sm-sm"
              :label="link.title"
            >
              <EssentialLink
                v-for="sublink in link.sublinks"
                :key="sublink.title"
                :title="sublink.title"
                icon-right="chevron_right"
                icon-right-size="28px"
                class="text-h6 text-bold q-px-sm-lg q-pr-xs-sm q-pr-sm-lg"
                @click="handleRouting(sublink)"
              />
            </q-expansion-item>
          </template>
        </q-list>
      </div>
    </div>
  </div>

  <!-- routing modal whenever user selects any nested building field ex: modules, aisles ect-->
  <AdminLocationManagerRouting
    v-if="showLocationManageRouteModal !== null"
    :location-title="showLocationManageRouteModal"
    @hide="showLocationManageRouteModal = null; resetBuildingStore();"
  />

  <AdminLocationManagerBulkUpload
    v-if="showBulkUploadLocationModal"
    @hide="showBulkUploadLocationModal = false"
  />
</template>

<script setup>
import { ref, computed, onBeforeMount } from 'vue'
import { useRouter } from 'vue-router'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'
import { useBuildingStore } from '@/stores/building-store'
import EssentialLink from '@/components/EssentialLink.vue'
import AdminLocationManagerRouting from '@/components/Admin/AdminLocationManagerRouting.vue'
import AdminLocationManagerBulkUpload from '@/components/Admin/AdminLocationManagerBulkUpload.vue'

const router = useRouter()

// Composables
const { checkUserPermission } = usePermissionHandler()

// Store Data
const { resetBuildingStore } = useBuildingStore()

// Local Data
const adminLinkList = computed(() => {
  let linkList = [
    {
      title: 'Groups & Permissions',
      link: '/admin/groups/',
      hidden: !checkUserPermission('can_manage_groups_and_permissions')
    },
    {
      title: 'List Configurations',
      sublinks: [
        {
          title: 'Add/Edit/Remove Owners',
          hidden: !checkUserPermission('can_manage_owners')
        },
        {
          title: 'Add/Edit/Remove Media Type',
          hidden: !checkUserPermission('can_manage_media_type')
        },
        {
          title: 'Add/Edit/Remove Size Class',
          hidden: !checkUserPermission('can_manage_size_class')
        },
        {
          title: 'Add/Edit/Remove Shelf Type',
          hidden: !checkUserPermission('can_manage_shelf_type')
        }
      ],
      hidden: !(checkUserPermission('can_manage_size_class') || checkUserPermission('can_manage_owners') || checkUserPermission('can_manage_media_type') || checkUserPermission('can_manage_shelf_type'))
    },
    {
      title: 'Location Manager',
      sublinks: [
        {
          title: 'Buildings'
        },
        {
          title: 'Modules'
        },
        {
          title: 'Aisles'
        },
        {
          title: 'Ladders'
        },
        {
          title: 'Shelves'
        },
        {
          title: 'Bulk Upload Ladders/Shelves'
        }
      ],
      hidden: !checkUserPermission('can_manage_locations')
    }
  ]

  // filters out all links without sublinks if they are hidden, also filters links with sublinks if they hard hidden
  return linkList.filter(l => {
    if (!l.hidden && !l.sublinks) {
      return l
    } else if (!l.hidden && l.sublinks) {
      l.sublinks = l.sublinks.filter(sl => !sl.hidden)
      return l
    } else {
      return
    }
  })
})
const showLocationManageRouteModal = ref(null)
const showBulkUploadLocationModal = ref(false)

// Logic
onBeforeMount(() => {
  resetBuildingStore()
})

const handleRouting = (link) => {
  switch (link.title) {
    case 'Buildings':
      router.push({ name: 'admin-location-manage-buildings' })
      break
    case 'Bulk Upload Ladders/Shelves':
      showBulkUploadLocationModal.value = true
      break
    case 'Add/Edit/Remove Owners':
      router.push({ name: 'admin-manage-owner' })
      break
    case 'Add/Edit/Remove Media Type':
      router.push({ name: 'admin-manage-media-type' })
      break
    case 'Add/Edit/Remove Size Class':
      router.push({ name: 'admin-manage-size-class' })
      break
    case 'Add/Edit/Remove Shelf Type':
      router.push({ name: 'admin-manage-shelf-type' })
      break
    default:
      showLocationManageRouteModal.value = link.title
      break
  }
}
</script>

<style lang="scss" scoped>
.admin-dashboard {
  &-expansion {
    :deep(.q-icon) {
      font-size: 28px;
      color: $color-black;
    }
  }
}
</style>
