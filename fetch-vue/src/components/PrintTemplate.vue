<template>
  <div class="column">
    <!-- Print Doc -->
    <div
      id="print-document"
      class="print"
      style="display: none"
    >
      <slot name="print-html" />
    </div>
  </div>
</template>
<script setup>
import { inject } from 'vue'

// Logic
const htmlToPaper = inject('htmlToPaper')
const print = () => {
  // get all stylesheets from HTML
  let stylesHtml = ''
  for (const node of [...document.querySelectorAll('link[rel="stylesheet"], style')]) {
    stylesHtml += node.outerHTML
  }

  htmlToPaper('print-document', {
    // inject styles from application
    css: stylesHtml
  })
}
defineExpose({ print })
</script>

<style lang="scss" scoped>
@page {
	size: Letter;
	margin: 0.8cm;
	margin-bottom: 0.6cm;
	margin-top: 0.6cm;
}
</style>