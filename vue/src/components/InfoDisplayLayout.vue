<template>
  <div class="info-display">
    <div class="row justify-between q-pt-xs-md q-pt-md-xl q-mx-xs-sm q-mx-sm-md">
      <div class="col-xs-12 col-md-12 col-lg-3">
        <div class="info-display-details q-mb-xs-md q-mb-sm-md q-mb-md-none q-mr-sm-none q-mr-lg-lg">
          <slot name="number-box-content" />
        </div>
      </div>

      <slot name="details-content" />
    </div>

    <q-space class="divider q-my-xs-lg q-my-md-xl" />

    <div class="row q-mb-xs-xl q-mb-sm-none">
      <div class="col-grow q-mb-xs-md q-mb-sm-none">
        <slot name="table-content" />
      </div>
    </div>
  </div>
</template>

<script setup>
// This component serves as the reusable layout for the detail ui for shelving, request, picklist ect
</script>

<style lang="scss" scoped>
.info-display {
  position: relative;

  :deep(.info-display-number-box) {
    background-color: $secondary;
    color: $color-white;
    text-align: center;
    border-radius: 3px;
    width: 100%;
    padding: 16px;
  }

  :deep(.info-display-details) {
    position: relative;
    display: flex;
    height: 100%;
    flex-direction: column;
    justify-content: flex-start;
  }

  :deep(.info-display-details-label) {
    font-weight: 700;
  }

  :deep(.info-display-details-label-2) {
    margin-top: 2.25rem;
    font-weight: 700;

    @media (max-width: $breakpoint-sm-max) {
      margin-top: 0;
    }
  }

  :deep(.info-display-details-action) {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    height: 100%;
  }
}
</style>