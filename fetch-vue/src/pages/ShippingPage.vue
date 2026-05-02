
<template>
  <q-page class="shipping-page q-pa-md">
    <LoadingOverlay />

    <ShippingExecute v-if="route.params.jobId" />
    <ShippingDashboard v-else />
  </q-page>
</template>

<script setup>
import { useRoute } from 'vue-router'
import ShippingDashboard from '@/components/Shipping/ShippingDashboard.vue'
import ShippingExecute from '@/components/Shipping/ShippingExecute.vue'
import LoadingOverlay from '@/components/LoadingOverlay.vue'

const route = useRoute()
</script>
