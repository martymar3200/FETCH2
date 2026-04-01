
<template>
  <div class="shipping-execute">
    <!-- Header using shared component -->
    <JobPageHeader
      title="Shipping Job"
      :job-id="shippingJob.id"
      :status="shippingJob.status"
      :status-color="getStatusColor(shippingJob.status)"
      :menu-options="menuOptions"
    >
      <template #actions>
        <BaseButton
          v-if="!isCompleted"
          no-caps
          unelevated
          color="accent"
          label="Complete Job"

          :disable="!canComplete"
          @click="confirmComplete"
        />
      </template>
    </JobPageHeader>

    <!-- Scanning Section -->
    <div
      v-if="!isCompleted"
      class="row q-col-gutter-md q-mb-lg"
    >
      <!-- Bin Card -->
      <div class="col-12 col-md-6">
        <q-card
          flat
          bordered
          class="full-height"
        >
          <q-card-section>
            <div class="text-h5 q-mb-md">
              Bin Barcode
            </div>

            <div class="form-group q-mb-md">
              <TextInput
                ref="binInputRef"
                v-model="binBarcode"
                :disable="isBinLocked"
                placeholder="Scan Bin"
                @keydown.enter="handleBinScan"
              >
                <template #append>
                  <q-icon name="qr_code_scanner" />
                </template>
              </TextInput>
              <div class="flex justify-end q-mt-xs">
                <q-checkbox
                  :model-value="isBinLocked"
                  @update:model-value="toggleLock"
                  label="Lock Bin"
                  dense
                  size="sm"
                />
                <BaseButton
                  v-if="isBinLocked"
                  flat
                  dense
                  size="sm"
                  color="primary"
                  label="Unlock/Change"
                  class="q-ml-sm"
                  @click="unlockBin"
                />
              </div>
            </div>

            <!-- Current Bin Info -->
            <div
              v-if="currentBin"
              class="q-pa-md bg-grey-2 rounded-borders"
            >
              <div class="text-subtitle1 text-weight-bold">
                Current Bin: {{ currentBin.barcode }}
              </div>
              <div class="text-caption">
                ID: {{ currentBin.id }} | Items: {{ currentBin.items?.length || 0 }}
              </div>
              <div
                v-if="currentBin.delivery_location"
                class="text-body2 text-primary text-weight-bold q-mt-xs"
              >
                Dest: {{ currentBin.delivery_location.name }}
              </div>
            </div>
          </q-card-section>
        </q-card>
      </div>

      <!-- Item Card -->
      <div class="col-12 col-md-6">
        <q-card
          flat
          bordered
          class="full-height"
        >
          <q-card-section>
            <div class="text-h5 q-mb-md">
              Item Barcode
            </div>

            <div class="form-group">
              <TextInput
                ref="itemInputRef"
                v-model="itemBarcode"
                placeholder="Scan Item (finds bin)"
                @keydown.enter="handleItemScan"
              >
                <template #append>
                  <q-icon name="qr_code_scanner" />
                </template>
              </TextInput>
            </div>

            <!-- Messages Area inside Item Card for focus -->
            <div
              v-if="lastMessage"
              class="q-mt-lg text-h6 text-center q-pa-sm rounded-borders"
              :class="lastMessageType"
              style="background-color: #f5f5f5;"
            >
              {{ lastMessage }}
            </div>
          </q-card-section>
        </q-card>
      </div>
    </div>

    <!-- Job Content Card -->
    <q-card
      flat
      bordered
    >
      <q-card-section>
        <div class="text-h6 q-mb-sm">
          Job Content
        </div>

        <div
          v-if="!shippingJob.bins || shippingJob.bins.length === 0"
          class="text-grey-7 italic"
        >
          No bins scanned yet.
        </div>

        <q-list
          bordered
          separator
          v-else
        >
          <q-expansion-item
            v-for="bin in shippingJob.bins"
            :key="bin.id"
            group="bins"
            :default-opened="currentBin?.id === bin.id"
            header-class="bg-grey-1"
          >
            <template #header>
              <q-item-section>
                <q-item-label class="text-weight-bold">
                  {{ bin.barcode }}
                </q-item-label>
                <q-item-label caption>
                  {{ bin.items?.length || 0 }} Items
                  <span v-if="bin.delivery_location"> -> {{ bin.delivery_location.name }}</span>
                </q-item-label>
              </q-item-section>
              <q-item-section side>
                <q-badge :color="bin.status === 'Closed' ? 'grey' : 'green'">
                  {{ bin.status }}
                </q-badge>
              </q-item-section>
            </template>

            <q-card>
              <q-card-section class="q-pa-xs">
                <q-list dense>
                  <q-item
                    v-for="item in bin.items"
                    :key="item.id"
                  >
                    <q-item-section>
                      {{ item.barcode?.value }}
                    </q-item-section>
                    <q-item-section side>
                      <BaseButton
                        v-if="!isCompleted"
                        flat
                        round
                        dense
                        icon="delete"
                        color="negative"
                        size="sm"
                        @click="handleRemoveItem(bin.id, item.id)"
                      />
                    </q-item-section>
                  </q-item>
                  <q-item v-if="!bin.items?.length">
                    <q-item-section class="text-italic text-grey">
                      Empty Bin
                    </q-item-section>
                  </q-item>
                </q-list>
              </q-card-section>
            </q-card>
          </q-expansion-item>
        </q-list>
      </q-card-section>
    </q-card>

    <!-- Audit Trail Modal -->
    <AuditTrail
      v-if="showAuditTrailModal"
      :job-type="'shipping_jobs'"
      :job-id="Number(jobId)"
      @hide="showAuditTrailModal = false"
    />
  </div>
</template>

<script setup>
import BaseButton from '@/components/Base/BaseButton.vue'
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useShippingStore } from '@/stores/shipping-store'
import TextInput from '@/components/TextInput.vue'
import JobPageHeader from '@/components/Job/JobPageHeader.vue'
import AuditTrail from '@/components/AuditTrail.vue'
import { Notify, useQuasar } from 'quasar'

const $q = useQuasar()

const route = useRoute()
const router = useRouter()
const store = useShippingStore()
const { shippingJob, currentBin } = storeToRefs(store)
const { getShippingJob, scanBin, scanItemIntoBin, removeItemFromBin, completeShippingJob, checkItemForShipping, deleteShippingJob } = store

const binBarcode = ref('')
const itemBarcode = ref('')
const pendingItem = ref(null)
const isBinLocked = ref(false)
const lastMessage = ref('')
const lastMessageType = ref('') // 'text-positive' or 'text-negative'
const showAuditTrailModal = ref(false)

const binInputRef = ref(null)
const itemInputRef = ref(null)

const jobId = route.params.jobId

const loadJob = async () => {
  await getShippingJob(jobId)
}

onUnmounted(() => {
  store.currentBin = null
  store.shippingJob = {
    id: null,
    status: '',
    bins: []
  } // Reset job state
})

const handleBinScan = async () => {
  if (!binBarcode.value) {
    return
  }

  try {
    const bin = await scanBin(jobId, binBarcode.value)
    store.currentBin = bin // Ensure store is updated (it should be by action, but explicit helps)

    // Default to locking
    let shouldLock = true

    playSound('success')
    playSound('success')
    lastMessage.value = `Bin ${binBarcode.value} Selected`
    lastMessageType.value = 'text-green-10'

    binBarcode.value = ''

    // Checks for Pending Item (Item First Workflow)
    if (pendingItem.value) {
      // Validation: Does bin location match pending item location?
      // Bin might not have location yet (if new), or has one.
      // Item check returned delivery_location_id.

      let locationMatch = false
      if (!bin.delivery_location_id) {
        // Bin is new/unassigned? Shipping logic usually assigns on first item.
        // If we rely on scanItemIntoBin to validate/assign, we can just try.
        locationMatch = true
      } else if (bin.delivery_location_id === pendingItem.value.delivery_location_id) {
        locationMatch = true
      } else {
        // Mismatch
        playSound('error')
        lastMessage.value = `Location Mismatch! Bin is for ${bin.delivery_location?.name}, Item is for ${pendingItem.value.delivery_location?.name}`
        lastMessageType.value = 'text-red-10'
        Notify.create({
          type: 'negative',
          message: lastMessage.value
        })
        // Keep pending item? or clear? Let's keep it so they can scan correct bin.
        // But we locked the WRONG bin.
        // User needs to unlock or scan correct bin.
        // We should probably NOT lock if mismatch, but scanBin already happened.
        return
      }

      if (locationMatch) {
        // Auto-add the item
        try {
          await scanItemIntoBin(jobId, bin.id, pendingItem.value.barcode)
          Notify.create({
            type: 'positive',
            message: `Item ${pendingItem.value.barcode} Auto-added!`
          })
          pendingItem.value = null // Clear pending
          itemBarcode.value = '' // Clear item input if it still had it

          shouldLock = false
        } catch (err) {
          lastMessage.value = `Failed to auto-add item: ${err.message}`
          lastMessageType.value = 'text-red-10'
          Notify.create({
            type: 'negative',
            message: lastMessage.value
          })
        }
      }
    }

    if (shouldLock) {
      isBinLocked.value = true
    } else {
      isBinLocked.value = false
      store.currentBin = null
      lastMessage.value = 'Item Added. Ready for next.'
      lastMessageType.value = 'text-green-10'
    }

    nextTick(() => {
      // Focus item input if no pending item or if auto-add finished
      if (!pendingItem.value) {
        itemInputRef.value?.focus()
      }
    })

  } catch (e) {
    playSound('error')
    lastMessage.value = e.response?.data?.detail || 'Bin Scan Failed'
    lastMessageType.value = 'text-red-10'
    binBarcode.value = ''
  }
}

const handleItemScan = async () => {
  if (!itemBarcode.value) {
    return
  }

  // Scenario: Item First (No Bin Selected)
  if (!currentBin.value) {
    try {
      const checkResult = await checkItemForShipping(jobId, itemBarcode.value)

      // Store as pending
      pendingItem.value = {
        ...checkResult,
        barcode: itemBarcode.value
      }

      // Find if we have an open bin for this location
      const existingBin = shippingJob.value.bins.find(b =>
        b.delivery_location_id === checkResult.delivery_location_id &&
            b.status !== 'Closed'
      )

      const locName = checkResult.delivery_location?.name || 'Unknown Location'

      if (existingBin) {
        lastMessage.value = `Item is for ${locName}. Scan Bin ${existingBin.barcode}`
        lastMessageType.value = 'text-blue-10'
        Notify.create({
          type: 'info',
          message: `Scan Bin ${existingBin.barcode}`,
          timeout: 5000
        })
      } else {
        lastMessage.value = `Item is for ${locName}. Scan a NEW Bin.`
        lastMessageType.value = 'text-deep-orange-10'
        Notify.create({
          type: 'warning',
          message: `Scan a NEW Bin for ${locName}`,
          timeout: 5000
        })
      }

      // Move focus to Bin Input
      nextTick(() => {
        binInputRef.value?.focus()
      })

    } catch (e) {
      playSound('error')
      lastMessage.value = e.response?.data?.detail || 'Item Lookup Failed'
      lastMessageType.value = 'text-red-10'
      itemBarcode.value = ''
    }
    return
  }

  // Scenario: Bin First (Standard)
  try {
    await scanItemIntoBin(jobId, currentBin.value.id, itemBarcode.value)
    playSound('success')
    lastMessage.value = `Item ${itemBarcode.value} added`
    lastMessageType.value = 'text-green-10'
    itemBarcode.value = ''
    pendingItem.value = null // Clear pending if manual scan successful
  } catch (e) {
    playSound('error')
    lastMessage.value = e.response?.data?.detail || 'Item Scan Failed'
    lastMessageType.value = 'text-red-10'
    itemBarcode.value = ''
  }
}

const handleRemoveItem = async (binId, itemId) => {
  try {
    await removeItemFromBin(jobId, binId, itemId)
    Notify.create({
      type: 'positive',
      message: 'Item Removed'
    })
  } catch (e) {
    Notify.create({
      type: 'negative',
      message: 'Failed to remove item'
    })
  }
}

const unlockBin = () => {
  isBinLocked.value = false
  store.currentBin = null
  nextTick(() => {
    binInputRef.value?.focus()
  })
}

const toggleLock = (val) => {
  isBinLocked.value = val
  if (!val) {
    store.currentBin = null
    nextTick(() => {
      binInputRef.value?.focus()
    })
  }
}

const confirmCancelJob = () => {
  $q.dialog({
    title: 'Cancel Job',
    message: 'Are you sure you want to cancel this job? All items will be reverted to Retrieved status and bins will be cleared.',
    cancel: true,
    persistent: true,
    ok: {
      label: 'Yes, Cancel',
      color: 'negative',
      flat: true
    }
  }).onOk(async () => {
    try {
      await deleteShippingJob(jobId)
      Notify.create({
        type: 'positive',
        message: 'Job Cancelled'
      })
      router.push({ name: 'shipping' })
    } catch (e) {
      Notify.create({
        type: 'negative',
        message: 'Failed to cancel job'
      })
    }
  })
}

const canComplete = computed(() => {
  return shippingJob.value?.bins?.some(b => b.items?.length > 0)
})

const isCompleted = computed(() => {
  return shippingJob.value?.status === 'Completed'
})

const menuOptions = computed(() => {
  const options = []

  if (!isCompleted.value) {
    options.push({
      label: 'Cancel Job',
      class: 'text-negative',
      action: confirmCancelJob
    })
  }

  options.push({
    label: 'View Manifest',
    action: () => {
      router.push({
        name: 'shipping-manifest',
        params: { jobId }
      })
    }
  })

  options.push({
    label: 'View History',
    action: () => {
      showAuditTrailModal.value = true
    }
  })

  return options
})

const confirmComplete = async () => {
  try {
    await completeShippingJob(jobId)
    Notify.create({
      type: 'positive',
      message: 'Job Completed!'
    })
    router.push({ name: 'shipping' })
  } catch (e) {
    Notify.create({
      type: 'negative',
      message: 'Failed to complete job'
    })
  }
}

const playSound = () => {
  // Implementing sound feedback
  // Usually via Audio object
  // Ignoring for now to keep it simple, but good for UX
}

const getStatusColor = (status) => {
  switch (status) {
    case 'Created': return 'blue-grey'
    case 'Assigned': return 'info'
    case 'Running': return 'primary'
    case 'Completed': return 'positive'
    default: return 'grey'
  }
}

onMounted(() => {
  loadJob()
  // If we have auto assign on start, status should be Running
})

</script>

<style scoped>
</style>
