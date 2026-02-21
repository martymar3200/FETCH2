<template>
  <q-page padding>
    <div class="row q-mb-lg">
      <div class="col-12">
        <h1 class="text-h4 text-bold">
          User Settings
        </h1>
      </div>
    </div>

    <div class="row">
      <div class="col-12 col-md-8 col-lg-6">
        <q-card
          flat
          bordered
        >
          <q-card-section>
            <div class="text-h6">
              Personal Information
            </div>
          </q-card-section>

          <q-separator />

          <q-card-section>
            <div class="row q-col-gutter-y-md">
              <div class="col-12 col-sm-6">
                <div class="text-caption text-grey-7">
                  First Name
                </div>
                <div class="text-body1 text-weight-medium">
                  {{ userData.first_name }}
                </div>
              </div>

              <div class="col-12 col-sm-6">
                <div class="text-caption text-grey-7">
                  Last Name
                </div>
                <div class="text-body1 text-weight-medium">
                  {{ userData.last_name }}
                </div>
              </div>

              <div class="col-12">
                <div class="text-caption text-grey-7">
                  Email Address
                </div>
                <div class="text-body1 text-weight-medium">
                  {{ userData.email }}
                </div>
              </div>
            </div>
          </q-card-section>
        </q-card>
      </div>
    </div>
  </q-page>
</template>

<script setup>
import { useUserStore } from '@/stores/user-store'
import { storeToRefs } from 'pinia'

const userStore = useUserStore()
const { userData } = storeToRefs(userStore)
</script>

<style lang="scss" scoped>
</style>
