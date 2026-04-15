<template>
  <q-page
    :style-fn="handlePageOffset"
    :padding="route.name == 'admin-home'"
    class="admin-page column no-wrap"
  >
    <LoadingOverlay />

    <AdminDashboard v-if="route.name == 'admin-home'" />

    <AdminGroups v-if="route.name == 'admin-groups' && !route.params.groupId" />
    <AdminGroupDetails v-if="route.name == 'admin-groups' && route.params.groupId" />

    <AdminILSConfiguration
      v-if="route.name == 'admin-manage-ils'"
    />

    <AdminIntegrationIssues
      v-if="route.name == 'admin-integration-issues'"
    />

    <AdminShelfPositionDirection
      v-if="route.name == 'admin-manage-shelf-position-direction'"
    />

    <AdminChildOwnerShelving
      v-if="route.name == 'admin-manage-child-owner-shelving'"
    />

    <AdminUserManagement
      v-if="route.name == 'admin-users'"
    />

    <AdminShippingSettings
      v-if="route.name == 'admin-manage-shipping'"
    />

    <AdminListManagerDisplay
      v-if="route.name.includes('admin-manage') && !['admin-manage-shelf-position-direction', 'admin-manage-child-owner-shelving', 'admin-manage-shipping', 'admin-manage-ils'].includes(route.name)"
      :list-type="route.name.split('admin-manage-').pop()"
    />

    <AdminLocationExplorer
      v-if="route.name === 'admin-location-explorer'"
    />
  </q-page>
</template>

<script setup>
import { inject } from 'vue'
import { useRoute } from 'vue-router'
import LoadingOverlay from '@/components/LoadingOverlay.vue'
import AdminDashboard from '@/components/Admin/AdminDashboard.vue'
import AdminGroups from '@/components/Admin/AdminGroups.vue'
import AdminGroupDetails from '@/components/Admin/AdminGroupDetails.vue'
import AdminListManagerDisplay from '@/components/Admin/AdminListManagerDisplay.vue'
import AdminLocationExplorer from '@/components/Admin/AdminLocationExplorer.vue'
import AdminILSConfiguration from '@/components/Admin/AdminILSConfiguration.vue'
import AdminIntegrationIssues from '@/components/Admin/AdminIntegrationIssues.vue'
import AdminShelfPositionDirection from '@/components/Admin/AdminShelfPositionDirection.vue'
import AdminChildOwnerShelving from '@/components/Admin/AdminChildOwnerShelving.vue'
import AdminUserManagement from '@/components/Admin/AdminUserManagement.vue'
import AdminShippingSettings from '@/components/Admin/AdminShippingSettings.vue'

const route = useRoute()

// Logic
const handlePageOffset = inject('handle-page-offset')
</script>