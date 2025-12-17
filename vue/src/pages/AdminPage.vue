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

    <AdminListManagerDisplay
      v-if="route.name.includes('admin-manage')"
      :list-type="route.name.split('admin-manage-').pop()"
    />

    <AdminLocationManagerDisplay
      v-if="route.name.includes('admin-location-manage')"
      :location-type="route.name.split('-').pop()"
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
import AdminLocationManagerDisplay from '@/components/Admin/AdminLocationManagerDisplay.vue'

const route = useRoute()

// Logic
const handlePageOffset = inject('handle-page-offset')
</script>